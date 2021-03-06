package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotBlank;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;

public class WikipediaLinker {
    private final static Logger log = LogManager.getLogger();

    private final UUID id;

    @NotBlank
    private final String starterPageTitle;
    @NotBlank
    private final String targetPageTitle;
    @JsonProperty("soughtForLinks")
    private final Deque<LinkElements> soughtForLinks;

    public WikipediaLinker(@JsonProperty("starterPageTitle") String starterPageTitle,
                           @JsonProperty("targetPageTitle") String targetPageTitle,
                           @JsonProperty("id") UUID id) {
        this.starterPageTitle = starterPageTitle;
        this.targetPageTitle = targetPageTitle;
        this.id = id;
        soughtForLinks = linkPages();
    }

    public WikipediaLinker(WikipediaLinker wikipediaLinker, UUID id) {
        if (wikipediaLinker.getId() == null) {
            this.id = id;
        } else {
            this.id = wikipediaLinker.getId();
        }
        this.starterPageTitle = wikipediaLinker.starterPageTitle;
        this.targetPageTitle = wikipediaLinker.getTargetPageTitle();
        this.soughtForLinks = wikipediaLinker.soughtForLinks;
    }

    public UUID getId() {
        return id;
    }

    public String getStarterPageTitle() {
        return starterPageTitle;
    }

    public String getTargetPageTitle() {
        return targetPageTitle;
    }

    public Deque<LinkElements> linkPages() {
        long startTime = System.currentTimeMillis();
        final int MAX_DEPTH = 5;
        Deque<LinkElements> soughtForLinks = concurrentBFS(MAX_DEPTH, starterPageTitle, targetPageTitle);
        log.info("Search completion time: " + (System.currentTimeMillis() - startTime) + " ms");
        return soughtForLinks;
    }

    private static BufferedWriter getWriter() {
        final long MAX_FILE_SIZE = 268435456;
        try {
            int lastFileIndex = numberOfFilesInAFolder();
            String newFileName;
            if (lastFileIndex > 0) {
                newFileName = createFileName(lastFileIndex);
                Path realFilePath = Paths.get(newFileName);
                FileChannel fileChannel = FileChannel.open(realFilePath);
                long realFileSize = fileChannel.size();
                log.debug("File " + newFileName + " size is " + realFileSize + " bytes");
                if (realFileSize > MAX_FILE_SIZE) {
                    ++lastFileIndex;
                    newFileName = createFileName(lastFileIndex);
                }
            } else {
                newFileName = createFileName(1);
            }
            return Files.newBufferedWriter(Path.of(newFileName), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createDirectory() {
        File directory = new File(".//searchResults//");
        if (directory.mkdir()) {
            log.debug("Directory missing, creating...");
        }
    }

    private static int numberOfFilesInAFolder() {
        File directory = new File(".//searchResults//");
        var pathnames = directory.list();
        return pathnames != null ? pathnames.length : 0;
    }

    private static Cache<SearchResult, SearchResult> loadCache(int lastFileIndex) {
        log.debug("Filling cache");
        Cache<SearchResult, SearchResult> cachedResults = Caffeine.newBuilder()
                .maximumSize(50000)
                .build();
        try {
            for (int i = 1; i <= lastFileIndex; i++) {
                Path path = Path.of(createFileName(i));
                Scanner in = new Scanner(path, StandardCharsets.UTF_8);

                boolean readPageTitle = true;
                SearchResult currentResult = null;
                while (in.hasNext()) {
                    String currentLine = in.nextLine();
                    if (currentLine.equals("-NEW PAGE-")) {
                        if (currentResult != null) {
                            cachedResults.put(currentResult, currentResult);
                        }
                        readPageTitle = true;
                    } else if (readPageTitle) {
                        var tempHref = in.hasNext() ? in.nextLine() : null;
                        currentResult = new SearchResult(tempHref, currentLine);
                        readPageTitle = false;
                    } else {
                        var tempHref = in.hasNext() ? in.nextLine() : null;
                        currentResult.addChild(new SearchResult(tempHref, currentLine));
                    }
                }
                if (currentResult != null)
                    cachedResults.put(currentResult, currentResult);
            }
        } catch (IOException e) {
            log.error("Cache is ruined");
            return null;
        }
        return cachedResults;
    }

    private static String createFileName(int number) {
        return ".//searchResults//" + number + ".txt";
    }

    private static Deque<LinkElements> concurrentBFS(int MAX_DEPTH, String starterPageTitle, String targetPageTitle) {
        createDirectory();
        var cachedResults = loadCache(numberOfFilesInAFolder());
        Deque<SearchResult> stack = new ArrayDeque<>();

        var scheduledFileUpdater = Executors.newSingleThreadScheduledExecutor();
        Runnable fileUpdater = () -> updateFileContents(stack);
        scheduledFileUpdater.scheduleAtFixedRate(fileUpdater, 5, 10, TimeUnit.MINUTES);

        Deque<LinkElements> soughtForLinks = new ArrayDeque<>();

        boolean isFound = false;
        int level = 0;
        SearchResult coreNode = new SearchResult("https://en.wikipedia.org/wiki/" + starterPageTitle, starterPageTitle);
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        Set<String> majorListForDuplicates = new HashSet<>();

        while (level <= MAX_DEPTH && !isFound) {
            log.debug("Forming lvl " + level + " tasks");
            Stack<Callable<SearchResult>> tasks = new Stack<>();
            for (SearchResult node : coreNode) {
                if (node.isLeaf() && node.getLevel() == level) {
                    if (majorListForDuplicates.add(node.getTitle())) {
                        Callable<SearchResult> task = () -> Fetcher.fetchWikiPage(node, targetPageTitle, stack, cachedResults);
                        tasks.push(task);
                    }
                }
            }
            isFound = invokeTasks(tasks, soughtForLinks, executorService, targetPageTitle);
            ++level;
        }
        executorService.shutdown();
        scheduledFileUpdater.shutdown();
        updateFileContents(stack);

        return soughtForLinks;
    }

    private static boolean invokeTasks(Stack<Callable<SearchResult>> tasks, Deque<LinkElements> soughtForLinks,
                                      ExecutorService executorService, String targetPageTitle) {
        int batchCounter = 1;
        int batchSize = 50;
        while (!tasks.empty()) {
            var wantedNode = partialInvocation(tasks, executorService, batchSize, batchCounter);
            if (wantedNode != null) {
                for (var child : wantedNode.getChildren()) {
                    if (child.getTitle().equals(targetPageTitle)) {
                        soughtForLinks.addLast(new LinkElements(child.getHref(), child.getTitle()));
                        break;
                    }
                }
                while (wantedNode != null) {
                    soughtForLinks.addFirst(new LinkElements(wantedNode.getHref(), wantedNode.getTitle()));
                    wantedNode = wantedNode.parent;
                }
                log.debug("Found in this batch, aborting");
                return true;
            }
            ++batchCounter;
        }
        return false;
    }

    private static SearchResult partialInvocation(Stack<Callable<SearchResult>> tasks, ExecutorService executorService, int batchSize, int batchCounter) {
        try {
            if (batchCounter % 3 == 0) {
                Thread.sleep(500);
            }
            Stack<Callable<SearchResult>> subTasks = new Stack<>();
            for (int i = 0; i < batchSize && !tasks.empty(); i++) {
                subTasks.push(tasks.pop());
            }
            log.debug("Batch #" + batchCounter + " is being queried...");
            List<Future<SearchResult>> concurFetchResults = executorService.invokeAll(subTasks);
            for (Future<SearchResult> concurFetchResult : concurFetchResults) {
                var futureResult =  concurFetchResult.get();
                if (futureResult != null) {
                    if (futureResult.isRightOne()) {
                        return futureResult;
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void updateFileContents(Deque<SearchResult> stack) {
        var bufferedWriter = getWriter();
        if (bufferedWriter == null) {
            log.debug("Empty file writer, something went wrong");
            return;
        }
        log.debug("Updating file contents, " + stack.size() + " new elements to add");
        try {
            while (stack.peek() != null) {
                var parent = stack.pop();

                bufferedWriter.append("-NEW PAGE-");
                bufferedWriter.newLine();
                bufferedWriter.append(parent.getTitle());
                bufferedWriter.newLine();
                bufferedWriter.append(parent.getHref());
                bufferedWriter.newLine();
                for (var child : parent.getChildren()) {
                    bufferedWriter.append(child.getTitle());
                    bufferedWriter.newLine();
                    bufferedWriter.append(child.getHref());
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
                e.printStackTrace();
                log.debug("Failed updating file contents");
        }
        log.debug("File update successful");
    }
}
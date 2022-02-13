package com.example.demo.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class Fetcher {

    public static SearchResult fetchWikiPage(SearchResult queryLink, String targetPageTitle,
                                             BufferedWriter bufferedWriter, Map<SearchResult, SearchResult> cachedResults) {
        //fills unqueried page 'queryLink' with children
        checkIfInCache(queryLink, cachedResults);

        if (queryLink.getChildren().isEmpty()) {
            Elements links = attemptQuery(fixHref(queryLink.getHref()));
            if (links == null) {
                return null;
            }
            for (Element link : links) {
                String tempHref = fixHref(link.attr("href"));
                String tempTitle = link.attr("title");
                if (checkLink(link) && !isDuplicate(queryLink, tempTitle)) {
                    SearchResult temp = new SearchResult("https://en.wikipedia.org" + tempHref, tempTitle);
                    temp.setParent(queryLink);
                    queryLink.addChild(temp);
                    if (tempTitle.equals(targetPageTitle)) {
                        queryLink.setRightOne();
                        System.out.println("FOUND on the internet, on " + queryLink.getTitle());
                    }
                }
            }
            saveToFile(queryLink, bufferedWriter);
        } else {
            for (var link : queryLink.getChildren()) {
                if (link.getTitle().equals(targetPageTitle)) {
                    queryLink.setRightOne();
                    System.out.println("FOUND in the file, on " + queryLink.getHref());
                }
            }
        }
        return queryLink;
    }

    public static Elements attemptQuery(String request) {
        try {
            System.out.println("Accessing page: " + request);
            Document doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(50000).get();
            return doc.select("a[href]");
        } catch (IOException e) {
            System.out.println("Faulty query: " + request);
            e.printStackTrace();
            return null;
        }
    }

    private static String fixHref(String href) {
        if (href.contains("\"")) {
            StringBuilder fixedHref = new StringBuilder();
            for (int i = 0; i < href.length(); i++) {
                if (href.charAt(i) == '\"') {
                    fixedHref.append("%22");
                } else {
                    fixedHref.append(href.charAt(i));
                }
            }
            return new String(fixedHref);
        } else {
            return href;
        }
    }

    private static boolean checkLink(Element link) {
        return link.attr("href").startsWith("/wiki/") &&
                !link.attr("href").contains("ISBN") &&
                !link.attr("href").contains("ISBI") &&
                !link.attr("href").contains("ISNI") &&
                !link.attr("href").contains("(identifier)") &&
                !link.attr("href").contains("Special:") &&
                !link.attr("href").contains("Wikipedia:") &&
                !link.attr("href").contains("File:") &&
                !link.attr("title").contains("Visit the") &&
                !link.attr("title").contains("Talk:") &&
                !link.attr("title").contains("Template talk:") &&
                !link.attr("title").contains("Template") &&
                !link.attr("title").contains("(disambiguation)") &&
                !link.attr("title").contains("the content page") &&
                !link.attr("title").equals("");
        //!link.attr("title").equals("Category:");
    }

    private static boolean isDuplicate(SearchResult result, String target) {
        for (var link : result.getChildren()) {
            if (link.getTitle().equals(target)) {
                return true;
            }
        }
        return false;
    }

     private static synchronized void saveToFile(SearchResult parent, BufferedWriter bufferedWriter) {
        try {
            //separating previous page from old pages
            bufferedWriter.append("-NEW PAGE-");
            bufferedWriter.newLine();
            //first two lines are for parent, next lines are for children
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkIfInCache(SearchResult queryLink, Map<SearchResult, SearchResult> cachedResults) {
        var cacheResult = cachedResults.get(queryLink); //non synchronized check of cache
        if (cacheResult != null) {
            for (var cacheChild : cacheResult) {
                cacheChild.setParent(queryLink);
                queryLink.addChild(cacheChild);
            }
        }
    }

    private static String fixTitle(String title) {
        //System.out.println("Title to fix: " + title);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < title.length(); ++i) {
            if (title.charAt(i) == '/') {
                builder.append("_u47");
            } else if (title.charAt(i) == '<') {
                builder.append("_u60");
            } else if (title.charAt(i) == '>') {
                builder.append("_u62");
            } else if (title.charAt(i) == ':') {
                builder.append("_u58");
            } else if (title.charAt(i) == '\"') {
                builder.append("_u34");
            } else if (title.charAt(i) == '\\') {
                builder.append("_u92");
            } else if (title.charAt(i) == '|') {
                builder.append("_u124");
            } else if (title.charAt(i) == '?') {
                builder.append("_u63");
            } else if (title.charAt(i) == '*') {
                builder.append("_u42");
            } else {
                builder.append(title.charAt(i));
            }
        }
        return builder.toString();
    }
}
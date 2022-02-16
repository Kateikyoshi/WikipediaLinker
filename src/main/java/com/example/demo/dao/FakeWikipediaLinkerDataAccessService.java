package com.example.demo.dao;

import com.example.demo.model.WikipediaLinker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("fakeWikipediaDao")
public class FakeWikipediaLinkerDataAccessService implements WikipediaLinkerDao {

    private static List<WikipediaLinker> DB = new ArrayList<>();
    private final static Logger log = LogManager.getLogger();

    @Override
    public int insertWikipediaLinkerTask(WikipediaLinker wikipediaLinker, UUID id) {
        log.debug("ID: " + id);
        DB.add(new WikipediaLinker(wikipediaLinker, id));
        return 1;
    }

    @Override
    public List<WikipediaLinker> selectAllWikipediaLinkerResults() {
        return DB;
    }

    @Override
    public Optional<WikipediaLinker> selectWikipediaLinkerResultById(UUID id) {
        return DB.stream()
                .filter(wikipediaLinker -> wikipediaLinker.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<WikipediaLinker> selectWikipediaLinkerResultByRequest(String start, String target) {
        return DB.stream()
                .filter(thisWikipediaLinker -> thisWikipediaLinker.getStarterPageTitle().equals(start)
                        && thisWikipediaLinker.getTargetPageTitle().equals(target))
                .findFirst();
    }
}

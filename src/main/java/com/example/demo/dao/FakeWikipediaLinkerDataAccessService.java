package com.example.demo.dao;

import com.example.demo.model.WikipediaLinker;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("fakeWikipediaDao")
public class FakeWikipediaLinkerDataAccessService implements WikipediaLinkerDao {

    private static List<WikipediaLinker> DB = new ArrayList<>();

    @Override
    public int insertWikipediaLinkerTask(WikipediaLinker wikipediaLinker, UUID id) {
        System.out.println("DAO WikipediaLinker starter: " + wikipediaLinker.getStarterPageTitle());
        System.out.println("DAO WikipediaLinker target: " + wikipediaLinker.getTargetPageTitle());
        System.out.println("DAO WikipediaLinker ID: " + wikipediaLinker.getId());
        /*
        DB.add(new WikipediaLinker(wikipediaLinker.getStarterPageTitle(),
                wikipediaLinker.getTargetPageTitle(),
                id));
         */
        DB.add(new WikipediaLinker(wikipediaLinker));
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

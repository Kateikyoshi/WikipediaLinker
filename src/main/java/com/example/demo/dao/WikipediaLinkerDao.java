package com.example.demo.dao;
import com.example.demo.model.WikipediaLinker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WikipediaLinkerDao {

    int insertWikipediaLinkerTask(WikipediaLinker wikipediaLinker, UUID id);

    default int insertWikipediaLinkerTask(WikipediaLinker wikipediaLinker) {
        System.out.println("Inserting DAO interface default");
        UUID id = wikipediaLinker.getId();
        if (id == null) {
            id = UUID.randomUUID();
        }
        System.out.println("Generated id is... " + id);
        return insertWikipediaLinkerTask(wikipediaLinker, id);
    }

    List<WikipediaLinker> selectAllWikipediaLinkerResults();

    Optional<WikipediaLinker> selectWikipediaLinkerResultById(UUID id);

    Optional<WikipediaLinker> selectWikipediaLinkerResultByRequest(String start, String target);
}

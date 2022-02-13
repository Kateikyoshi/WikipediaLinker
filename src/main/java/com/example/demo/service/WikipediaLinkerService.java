package com.example.demo.service;

import com.example.demo.dao.WikipediaLinkerDao;
import com.example.demo.model.WikipediaLinker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WikipediaLinkerService {

    private final WikipediaLinkerDao wikipediaLinkerDao;

    @Autowired
    public WikipediaLinkerService(@Qualifier("fakeWikipediaDao") WikipediaLinkerDao wikipediaLinkerDao) {
        this.wikipediaLinkerDao = wikipediaLinkerDao;
    }

    public int addWikipediaLinkerTask(WikipediaLinker wikipediaLinker) {
        System.out.println("Service woke up, asking DAO to proceed");
        return wikipediaLinkerDao.insertWikipediaLinkerTask(wikipediaLinker);
    }

    public List<WikipediaLinker> getAllWikipediaLinkerResults() {
        return wikipediaLinkerDao.selectAllWikipediaLinkerResults();
    }

    public Optional<WikipediaLinker> getWikipediaLinkerResultById(UUID id) {
        return wikipediaLinkerDao.selectWikipediaLinkerResultById(id);
    }

    public Optional<WikipediaLinker> getWikipediaLinkerResultByRequest(String start, String target) {
        return wikipediaLinkerDao.selectWikipediaLinkerResultByRequest(start, target);
    }
}

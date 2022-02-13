package com.example.demo.api;

import com.example.demo.model.WikipediaLinker;
import com.example.demo.service.WikipediaLinkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("api/v1/wikipediaLinker")
@RestController
public class WikipediaLinkerController {

    private final WikipediaLinkerService wikipediaLinkerService;

    @Autowired
    public WikipediaLinkerController(WikipediaLinkerService wikipediaLinkerService) {
        this.wikipediaLinkerService = wikipediaLinkerService;
    }

    @PostMapping
    public void addWikipediaLinker(@Valid @NonNull @RequestBody WikipediaLinker wikipediaLinker) {
        System.out.println("Posting through Controller and asking Service to proceed");
        System.out.println("Also your current id is... " + wikipediaLinker.getId());
        wikipediaLinkerService.addWikipediaLinkerTask(wikipediaLinker);
    }

    @GetMapping
    public List<WikipediaLinker> getAllWikipediaLinkerResults() {
        return wikipediaLinkerService.getAllWikipediaLinkerResults();
    }

    @GetMapping(path = "{id}")
    public WikipediaLinker getWikipediaLinkerResultById(@PathVariable("id") UUID id) {
        return wikipediaLinkerService.getWikipediaLinkerResultById(id)
                .orElse(null);
    }
    @GetMapping(path = "{start}/{target}")
    public WikipediaLinker getWikipediaLinkerResultByRequest(@PathVariable Map<String, String> pathVarsMap) {
        String start = pathVarsMap.get("start");
        String target = pathVarsMap.get("target");
        return wikipediaLinkerService.getWikipediaLinkerResultByRequest(start, target)
                .orElse(null);
    }

}

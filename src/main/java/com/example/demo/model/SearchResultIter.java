package com.example.demo.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SearchResultIter implements Iterator<SearchResult> {

    private final Iterator<SearchResult> iterator;

    public SearchResultIter(SearchResult treeNode) {
        Set<SearchResult> flatSet = new HashSet<>();
        flatten(treeNode, flatSet);
        iterator = flatSet.iterator();
    }

    public void flatten(SearchResult treeNode, Set<SearchResult> flatSet) {
        flatSet.add(treeNode);
        var children = treeNode.getChildren();
        if (!children.isEmpty()) {
            for (var child : children) {
                flatSet.add(child);
                flatten(child, flatSet);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public SearchResult next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
package com.example.demo.model;

import java.util.*;

public class SearchResult implements Comparable<SearchResult>, Iterable<SearchResult> {
    private final LinkElements linkElements;
    public Set<SearchResult> children;
    private boolean isRightOne;
    public SearchResult parent;

    public SearchResult(String href, String title) {
        isRightOne = false;
        linkElements = new LinkElements(href, title);
        children = new java.util.concurrent.CopyOnWriteArraySet<>();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void addChild(SearchResult child) {
        this.children.add(child);
    }

    public synchronized void setParent(SearchResult parent) {
        this.parent = parent;
    }

    public Set<SearchResult> getChildren() {
        return children;
    }

    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    public String getHref() {
        return linkElements.getHref();
    }
    public String getTitle() {
        return linkElements.getTitle();
    }

    public void setRightOne() {
        this.isRightOne = true;
    }

    public boolean isRightOne() {
        return isRightOne;
    }

    @Override
    public String toString() {
        return this.getHref() + " " + this.getTitle();
    }

    @Override
    public int compareTo(SearchResult o) {
        if (this.getTitle().equals(o.getTitle())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public Iterator<SearchResult> iterator() {
        return new SearchResultIter(this);
    }

    @Override
    public int hashCode() {
        return getTitle().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        var otherReal = (SearchResult) other;
        return this.getTitle().equals(otherReal.getTitle());
    }
}
package com.example.demo.model;

public class LinkElements {
    private String href;
    private String title;

    LinkElements(String href, String title) {
        this.href = href;
        this.title = title;
    }

    public String getHref() {
        return href;
    }
    public String getTitle() {
        return title;
    }

    public void setHref(String href) {
        this.href = href;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "[" + title + " " + href + "]";
    }
}
package com.acme.model.ytrawcomment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopLevelComment {
    private String kind;
    private String etag;
    private String id;
    @JsonProperty("snippet")
    private InnerSnippet innerSnippet;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InnerSnippet getInnerSnippet() {
        return innerSnippet;
    }

    public void setInnerSnippet(InnerSnippet innerSnippet) {
        this.innerSnippet = innerSnippet;
    }

    @Override
    public String toString() {
        return "TopLevelComment{" +
                "kind='" + kind + '\'' +
                ", etag='" + etag + '\'' +
                ", id='" + id + '\'' +
                ", innerSnippet=" + innerSnippet +
                '}';
    }
}

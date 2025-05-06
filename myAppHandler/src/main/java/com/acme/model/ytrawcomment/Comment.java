package com.acme.model.ytrawcomment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment {
    private String kind;
    private String etag;
    private String id;
    @JsonProperty("snippet")
    private OuterSnippet outerSnippet;

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

    public OuterSnippet getOuterSnippet() {
        return outerSnippet;
    }

    public void setOuterSnippet(OuterSnippet outerSnippet) {
        this.outerSnippet = outerSnippet;
    }
}

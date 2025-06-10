package common.model.analysisrequest;

import java.util.List;

public class SentimentAnalysisChunkRequest extends SentimentAnalysisRequest {
    private String analysisChunkId;
    private int pageNumber;
    private int pageSize;
    List<CommentToAnalyze> comments;

    public SentimentAnalysisChunkRequest(String videoId, int commentCount, String analysisObject, String videoTitle) {
        super(videoId, commentCount, analysisObject, videoTitle);
    }

    // Constructor for chunked analysis with comments
    public SentimentAnalysisChunkRequest(String analysisObject, String videoTitle, String moreInfo, List<CommentToAnalyze> comments, int pageSize, int pageNumber) {
        super(analysisObject, videoTitle, moreInfo);
        this.comments = comments;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }


    public String getAnalysisChunkId() {
        return analysisChunkId;
    }

    public void setAnalysisChunkId(String analysisChunkId) {
        this.analysisChunkId = analysisChunkId;
    }

    public List<CommentToAnalyze> getComments() {
        return comments;
    }

    public void setComments(List<CommentToAnalyze> comments) {
        this.comments = comments;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}

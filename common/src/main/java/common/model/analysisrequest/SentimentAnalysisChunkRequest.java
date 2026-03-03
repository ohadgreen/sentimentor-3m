package common.model.analysisrequest;

import java.util.List;
import java.util.UUID;

public class SentimentAnalysisChunkRequest extends SentimentAnalysisRequest {
    private String videoTitle;
    private UUID analysisChunkId;
    private int pageNumber;
    private int pageSize;
    List<CommentToAnalyze> comments;

    public SentimentAnalysisChunkRequest() {
        super();
    }

    public SentimentAnalysisChunkRequest(UUID analysisId, String videoId, String videoTitle, String analysisObject, String moreInfo, int totalCommentsToAnalyze,
                                         UUID analysisChunkId, List<CommentToAnalyze> comments, int pageSize, int pageNumber) {
        super(analysisId, videoId, analysisObject, moreInfo, totalCommentsToAnalyze);
        this.videoTitle = videoTitle;
        this.analysisChunkId = analysisChunkId;
        this.comments = comments;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public SentimentAnalysisChunkRequest(String analysisObject, String videoTitle, String moreInfo, List<CommentToAnalyze> comments, int pageSize, int pageNumber) {
        super(analysisObject, moreInfo);
        this.videoTitle = videoTitle;
        this.comments = comments;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public UUID getAnalysisChunkId() {
        return analysisChunkId;
    }

    public void setAnalysisChunkId(UUID analysisChunkId) {
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

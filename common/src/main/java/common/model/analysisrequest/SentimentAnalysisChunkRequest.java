package common.model.analysisrequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SentimentAnalysisChunkRequest extends SentimentAnalysisRequest {
    private UUID analysisChunkId;
    private Set<UUID> processingChunkIds;
    private int pageNumber;
    private int pageSize;
    List<CommentToAnalyze> comments;

    public SentimentAnalysisChunkRequest(UUID analysisId, String videoId, String videoTitle, String analysisObject, String moreInfo, int totalCommentsCount,
                                         UUID analysisChunkId, List<CommentToAnalyze> comments, int pageSize, int pageNumber) {
        super(analysisId, videoId, videoTitle, analysisObject, moreInfo, totalCommentsCount);
        this.analysisChunkId = analysisChunkId;
        this.comments = comments;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public SentimentAnalysisChunkRequest(String analysisObject, String videoTitle, String moreInfo, List<CommentToAnalyze> comments, int pageSize, int pageNumber) {
        super(analysisObject, videoTitle, moreInfo);
        this.comments = comments;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public UUID getAnalysisChunkId() {
        return analysisChunkId;
    }

    public void setAnalysisChunkId(UUID analysisChunkId) {
        this.analysisChunkId = analysisChunkId;
    }

    public Set<UUID> getProcessingChunkIds() {
        return processingChunkIds;
    }

    public void setProcessingChunkIds(Set<UUID> processingChunkIds) {
        this.processingChunkIds = processingChunkIds;
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

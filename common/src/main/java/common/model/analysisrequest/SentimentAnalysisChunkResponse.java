package common.model.analysisrequest;


import java.util.List;
import java.util.UUID;

public class SentimentAnalysisChunkResponse {
    private UUID analysisId;
    private UUID analysisChunkId;
    private List<CommentToAnalyze> commentSentiments;

    public SentimentAnalysisChunkResponse(UUID sentimentAnalysisId, UUID analysisChunkId, List<CommentToAnalyze> commentSentiments) {
        this.analysisId = sentimentAnalysisId;
        this.analysisChunkId = analysisChunkId;
        this.commentSentiments = commentSentiments;
    }

    public UUID getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(UUID analysisId) {
        this.analysisId = analysisId;
    }

    public UUID getAnalysisChunkId() {
        return analysisChunkId;
    }
    public void setAnalysisChunkId(UUID analysisChunkId) {
        this.analysisChunkId = analysisChunkId;
    }
    public List<CommentToAnalyze> getCommentSentiments() {
        return commentSentiments;
    }

    public void setCommentSentiments(List<CommentToAnalyze> commentSentiments) {
        this.commentSentiments = commentSentiments;
    }
}

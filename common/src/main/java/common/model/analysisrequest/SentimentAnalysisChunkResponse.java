package common.model.analysisrequest;


import java.util.List;
import java.util.UUID;

public class SentimentAnalysisChunkResponse {
    private UUID sentimentAnalysisId;
    private UUID analysisChunkId;
    private List<CommentSentiment> commentSentiments;

    public SentimentAnalysisChunkResponse(UUID sentimentAnalysisId, UUID analysisChunkId, List<CommentSentiment> commentSentiments) {
        this.sentimentAnalysisId = sentimentAnalysisId;
        this.analysisChunkId = analysisChunkId;
        this.commentSentiments = commentSentiments;
    }

    public UUID getSentimentAnalysisId() {
        return sentimentAnalysisId;
    }

    public void setSentimentAnalysisId(UUID sentimentAnalysisId) {
        this.sentimentAnalysisId = sentimentAnalysisId;
    }

    public UUID getAnalysisChunkId() {
        return analysisChunkId;
    }
    public void setAnalysisChunkId(UUID analysisChunkId) {
        this.analysisChunkId = analysisChunkId;
    }
    public List<CommentSentiment> getCommentSentiments() {
        return commentSentiments;
    }

    public void setCommentSentiments(List<CommentSentiment> commentSentiments) {
        this.commentSentiments = commentSentiments;
    }
}

package common.model.analysisrequest;


import java.util.List;
import java.util.UUID;

public class SentimentAnalysisResponse {
    private UUID sentimentAnalysisId;
    private List<CommentSentiment> commentSentiments;

    public SentimentAnalysisResponse(UUID sentimentAnalysisId, List<CommentSentiment> commentSentiments) {
        this.sentimentAnalysisId = sentimentAnalysisId;
        this.commentSentiments = commentSentiments;
    }

    public UUID getSentimentAnalysisId() {
        return sentimentAnalysisId;
    }

    public void setSentimentAnalysisId(UUID sentimentAnalysisId) {
        this.sentimentAnalysisId = sentimentAnalysisId;
    }

    public List<CommentSentiment> getCommentSentiments() {
        return commentSentiments;
    }

    public void setCommentSentiments(List<CommentSentiment> commentSentiments) {
        this.commentSentiments = commentSentiments;
    }
}

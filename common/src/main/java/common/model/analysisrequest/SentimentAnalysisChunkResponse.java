package common.model.analysisrequest;


import java.util.List;
import java.util.UUID;

public class SentimentAnalysisChunkResponse {
    private UUID analysisId;
    private UUID analysisChunkId;
    private List<CommentToAnalyze> commentSentiments;
    private int inputTokens;
    private int outputTokens;

    public SentimentAnalysisChunkResponse() {}

    public SentimentAnalysisChunkResponse(UUID sentimentAnalysisId, UUID analysisChunkId, List<CommentToAnalyze> commentSentiments, int inputTokens, int outputTokens) {
        this.analysisId = sentimentAnalysisId;
        this.analysisChunkId = analysisChunkId;
        this.commentSentiments = commentSentiments;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
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

    public int getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }
}

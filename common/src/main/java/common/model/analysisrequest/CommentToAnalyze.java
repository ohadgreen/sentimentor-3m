package common.model.analysisrequest;

public class CommentToAnalyze {
    private String commentId;
    private String commentText;

    public CommentToAnalyze(String commentId, String commentText) {
        this.commentId = commentId;
        this.commentText = commentText;
    }
    public String getCommentId() {
        return commentId;
    }
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    @Override
    public String toString() {
        return "CommentToAnalyze{" +
                "commentId='" + commentId + '\'' +
                ", commentText='" + commentText + '\'' +
                '}';
    }

    public String toAnalyzeString() {
        return "commentId: '" + commentId + '\'' +", text: '" + commentText + '\'';
    }
}

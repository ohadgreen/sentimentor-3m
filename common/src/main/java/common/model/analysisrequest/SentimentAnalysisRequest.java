package common.model.analysisrequest;

import java.util.List;

public class SentimentAnalysisRequest {
    private String videoId;
    private int commentCount;
    private String analysisObject;
    private String videoTitle;
    private String moreInfo;
    List<CommentToAnalyze> comments;

    public SentimentAnalysisRequest(String videoId, int commentCount, String analysisObject, String videoTitle) {
        this.videoId = videoId;
        this.commentCount = commentCount;
        this.analysisObject = analysisObject;
        this.videoTitle = videoTitle;
    }

    public SentimentAnalysisRequest(String analysisObject, String videoTitle, String moreInfo, List<CommentToAnalyze> comments) {
        this.analysisObject = analysisObject;
        this.videoTitle = videoTitle;
        this.moreInfo = moreInfo;
        this.comments = comments;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getAnalysisObject() {
        return analysisObject;
    }

    public void setAnalysisObject(String analysisObject) {
        this.analysisObject = analysisObject;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public List<CommentToAnalyze> getComments() {
        return comments;
    }

    public void setComments(List<CommentToAnalyze> comments) {
        this.comments = comments;
    }

}

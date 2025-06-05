package common.model.analysisrequest;

import java.util.List;
import java.util.UUID;

public class SentimentAnalysisRequest {
    private UUID analysisId;
    private String videoId;
    private String analysisObject;
    private int commentCount;
    private String videoTitle;
    private String moreInfo;


    public SentimentAnalysisRequest(String videoId, int commentCount, String analysisObject, String videoTitle) {
        this.videoId = videoId;
        this.commentCount = commentCount;
        this.analysisObject = analysisObject;
        this.videoTitle = videoTitle;
    }

    public SentimentAnalysisRequest(String analysisObject, String videoTitle, String moreInfo) {
        this.analysisObject = analysisObject;
        this.videoTitle = videoTitle;
        this.moreInfo = moreInfo;
    }

    public UUID getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(UUID analysisId) {
        this.analysisId = analysisId;
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

}

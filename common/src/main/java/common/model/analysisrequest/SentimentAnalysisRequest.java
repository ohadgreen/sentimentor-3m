package common.model.analysisrequest;

import java.io.Serializable;
import java.util.UUID;

public class SentimentAnalysisRequest implements Serializable {
    private UUID analysisId;
    private String videoId;
    private String analysisObject;
    private String moreInfo;
    private int totalCommentsToAnalyze;
    private int totalCommentsForVideo;

    public SentimentAnalysisRequest() {
    }

    public SentimentAnalysisRequest(UUID analysisId, String videoId, String analysisObject, String moreInfo, int totalCommentsToAnalyze) {
        this.analysisId = analysisId;
        this.videoId = videoId;
        this.analysisObject = analysisObject;
        this.moreInfo = moreInfo;
        this.totalCommentsToAnalyze = totalCommentsToAnalyze;
    }

    public SentimentAnalysisRequest(String analysisObject, String moreInfo) {
        this.analysisObject = analysisObject;
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

    public int getTotalCommentsToAnalyze() {
        return totalCommentsToAnalyze;
    }

    public void setTotalCommentsToAnalyze(int totalCommentsToAnalyze) {
        this.totalCommentsToAnalyze = totalCommentsToAnalyze;
    }

    public String getAnalysisObject() {
        return analysisObject;
    }

    public void setAnalysisObject(String analysisObject) {
        this.analysisObject = analysisObject;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

}

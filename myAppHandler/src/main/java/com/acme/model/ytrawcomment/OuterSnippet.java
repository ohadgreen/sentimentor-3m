package com.acme.model.ytrawcomment;

public class OuterSnippet {
    private String channelId;
    private String videoId;
    private boolean canReply;
    private Integer totalReplyCount;
    private boolean isPublic;
    private TopLevelComment topLevelComment;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
    }

    public Integer getTotalReplyCount() {
        return totalReplyCount;
    }

    public void setTotalReplyCount(Integer totalReplyCount) {
        this.totalReplyCount = totalReplyCount;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public TopLevelComment getTopLevelComment() {
        return topLevelComment;
    }

    public void setTopLevelComment(TopLevelComment topLevelComment) {
        this.topLevelComment = topLevelComment;
    }

    @Override
    public String toString() {
        return "OuterSnippet{" +
                "channelId='" + channelId + '\'' +
                ", videoId='" + videoId + '\'' +
                ", canReply=" + canReply +
                ", totalReplyCount=" + totalReplyCount +
                ", isPublic=" + isPublic +
                ", topLevelComment=" + topLevelComment +
                '}';
    }
}

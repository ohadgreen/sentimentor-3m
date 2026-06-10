import React, { useState } from "react";
import VideoSearch from "./VideoSearch";
import VideoDisplayWrapper from "./videoStats/VideoDisplayWrapper";
import LatestAnalysis from "./LatestAnalysis";
import TrendAnalysisForm from "./TrendAnalysisForm";
import TrendAnalysisResult from "./TrendAnalysisResult";
import LatestTrendAnalysis from "./LatestTrendAnalysis";
import "./TubeSurfMain.css";

const TubeSurfMain = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [committedSearchTerm, setCommittedSearchTerm] = useState("");
  const [searchVersion, setSearchVersion] = useState(0);
  const [videoIdToAnalyze, setVideoIdToAnalyze] = useState("");
  const [selectedVideoFromSearch, setSelectedVideoFromSearch] = useState(null);
  const [showSearchList, setShowSearchList] = useState(false);
  const [showVideoDetails, setShowVideoDetails] = useState(false);
  const [trendJobId, setTrendJobId] = useState(null);
  const [trendParams, setTrendParams] = useState(null);

  const videoSetter = (event, searchItem) => {
    event.stopPropagation();
    const id = searchItem?.id?.videoId ?? searchItem;
    setVideoIdToAnalyze(id);
    setSelectedVideoFromSearch(typeof searchItem === "object" && searchItem?.id?.videoId ? searchItem : null);
    setShowVideoDetails(true);
    setShowSearchList(false);
  };

  const onLatestCardClick = (videoId) => {
    setVideoIdToAnalyze(videoId);
    setSelectedVideoFromSearch(null);
    setShowVideoDetails(true);
    setShowSearchList(false);
  };

  const onJobStarted = (jobId, params) => {
    setTrendJobId(jobId);
    setTrendParams(params);
    setShowVideoDetails(false);
    setShowSearchList(false);
  };

  const onBackFromTrend = () => {
    setTrendJobId(null);
    setTrendParams(null);
  };

  const onBackFromVideo = () => {
    setShowVideoDetails(false);
    setShowSearchList(false);
  };

  if (showVideoDetails) {
    return (
      <div className="search--main">
        <div className="search--back-bar">
          <button className="search--back-btn" onClick={onBackFromVideo}>
            ← Back
          </button>
        </div>
        <VideoDisplayWrapper videoId={videoIdToAnalyze} selectedVideoFromSearch={selectedVideoFromSearch} />
      </div>
    );
  }

  if (trendJobId) {
    return (
      <div className="search--main">
        <TrendAnalysisResult
          jobId={trendJobId}
          initialParams={trendParams}
          onBack={onBackFromTrend}
        />
      </div>
    );
  }

  return (
    <div className="search--main">
      <div className="home-columns">
        <div className="home-col-left">
          <h2 className="section-title">Ad-hoc Analysis</h2>
          <div className="search--input-row">
            <input
              type="text"
              required={false}
              placeholder="e.g. us elections, tech news..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && searchTerm !== "") {
                  setCommittedSearchTerm(searchTerm);
                  setSearchVersion((v) => v + 1);
                  setShowSearchList(true);
                }
              }}
            />
            <button
              onClick={() => { setCommittedSearchTerm(searchTerm); setSearchVersion((v) => v + 1); setShowSearchList(true); }}
              disabled={searchTerm === ""}
            >
              Search
            </button>
          </div>

          <div className="home-col-left__content">
            {showSearchList ? (
              <VideoSearch key={searchVersion} onClick={videoSetter} searchTerm={committedSearchTerm} />
            ) : (
              <LatestAnalysis onCardClick={onLatestCardClick} />
            )}
          </div>
        </div>

        <div className="home-col-right">
          <h2 className="section-title">Trend Analysis</h2>
          <div className="trend-form-card">
            <p className="trend-form-card__desc">
              Analyse sentiment over time for a specific topic and target object.
            </p>
            <TrendAnalysisForm onJobStarted={onJobStarted} />
          </div>
          <LatestTrendAnalysis onCardClick={(jobId, params) => { setTrendJobId(jobId); setTrendParams(params); }} />
        </div>
      </div>
    </div>
  );
};

export default TubeSurfMain;

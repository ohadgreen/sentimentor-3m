import React, { useState, useEffect, useRef } from "react";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";
import "./TrendAnalysisResult.css";
import { apiFetch } from "../utils/api";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const API_BASE = "/api/trend";
const POLL_INTERVAL_MS = 10000;

const STATUS_LABELS = {
  PENDING: "Pending",
  RUNNING: "Running",
  COMPLETED: "Completed",
  FAILED: "Failed",
};

const formatCount = (val) => {
  const num = typeof val === "string" ? parseInt(val, 10) : val;
  if (!num || Number.isNaN(num)) return "—";
  return num.toLocaleString();
};

const TrendAnalysisResult = ({ jobId, initialParams, onBack }) => {
  const [job, setJob] = useState(null);
  const [error, setError] = useState(null);
  const intervalRef = useRef(null);

  const fetchJob = async () => {
    try {
      const res = await apiFetch(`${API_BASE}/${jobId}`);
      if (!res.ok) throw new Error("Failed to fetch job status");
      const data = await res.json();
      setJob(data);
      if (data.jobStatus === "COMPLETED" || data.jobStatus === "FAILED") {
        clearInterval(intervalRef.current);
      }
    } catch (err) {
      setError(err.message);
      clearInterval(intervalRef.current);
    }
  };

  useEffect(() => {
    fetchJob();
    intervalRef.current = setInterval(fetchJob, POLL_INTERVAL_MS);
    return () => clearInterval(intervalRef.current);
  }, [jobId]); // eslint-disable-line

  const isTerminal = job && (job.jobStatus === "COMPLETED" || job.jobStatus === "FAILED");
  const progressPct =
    job && job.totalDays > 0
      ? Math.round((job.daysCompleted / job.totalDays) * 100)
      : 0;

  const chartData = (() => {
    if (!job || !job.dailyResults || job.dailyResults.length === 0) return null;
    const labels = job.dailyResults.map((d) => d.date);
    return {
      labels,
      datasets: [
        {
          label: "Positive",
          data: job.dailyResults.map((d) => d.positiveComments),
          borderColor: "#4caf50",
          backgroundColor: "rgba(76, 175, 80, 0.12)",
          tension: 0.3,
          pointRadius: 5,
          pointHoverRadius: 7,
        },
        {
          label: "Negative",
          data: job.dailyResults.map((d) => d.negativeComments),
          borderColor: "#f44336",
          backgroundColor: "rgba(244, 67, 54, 0.12)",
          tension: 0.3,
          pointRadius: 5,
          pointHoverRadius: 7,
        },
        {
          label: "Neutral",
          data: job.dailyResults.map((d) => d.neutralComments),
          borderColor: "#9e9e9e",
          backgroundColor: "rgba(158, 158, 158, 0.1)",
          tension: 0.3,
          pointRadius: 5,
          pointHoverRadius: 7,
        },
      ],
    };
  })();

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "top",
        labels: { font: { size: 13 } },
      },
      title: {
        display: true,
        text: job
          ? `Sentiment trend for "${job.sentimentObject}" — "${job.searchQuery}"`
          : "",
        font: { size: 15, weight: "600" },
        color: "#333",
      },
      tooltip: {
        callbacks: {
          afterLabel: (ctx) => {
            const day = job.dailyResults[ctx.dataIndex];
            return `Total analyzed: ${day.totalCommentsAnalyzed}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1 },
        title: { display: true, text: "Comments", font: { size: 12 } },
      },
      x: {
        title: { display: true, text: "Date", font: { size: 12 } },
      },
    },
  };

  const params = job || initialParams;

  return (
    <div className="trend-result">
      <div className="trend-result__header">
        <button className="trend-result__back" onClick={onBack}>
          ← Back
        </button>
        <div className="trend-result__meta">
          <span className="trend-result__meta-item">
            <span className="trend-result__meta-label">Query:</span>{" "}
            <strong>{params?.searchQuery}</strong>
          </span>
          <span className="trend-result__meta-sep">·</span>
          <span className="trend-result__meta-item">
            <span className="trend-result__meta-label">Object:</span>{" "}
            <strong>{params?.sentimentObject}</strong>
          </span>
          {job && (
            <>
              <span className="trend-result__meta-sep">·</span>
              <span className="trend-result__meta-item">
                <span className="trend-result__meta-label">Days:</span>{" "}
                <strong>{job.daysBack}</strong>
              </span>
            </>
          )}
        </div>
        {job && (
          <span className={`trend-result__badge trend-result__badge--${job.jobStatus.toLowerCase()}`}>
            {STATUS_LABELS[job.jobStatus] ?? job.jobStatus}
          </span>
        )}
      </div>

      {error && (
        <div className="trend-result__error">{error}</div>
      )}

      {!error && job && !isTerminal && (
        <div className="trend-result__progress-wrap">
          <div className="trend-result__progress-bar">
            <div
              className="trend-result__progress-fill"
              style={{ width: `${progressPct}%` }}
            />
          </div>
          <span className="trend-result__progress-text">
            {job.daysCompleted} / {job.totalDays} days processed — polling every 10s…
          </span>
        </div>
      )}

      {!error && !job && (
        <div className="trend-result__loading">Starting job…</div>
      )}

      {job?.jobStatus === "FAILED" && (
        <div className="trend-result__error">
          Job failed{job.errorMessage ? `: ${job.errorMessage}` : "."}
        </div>
      )}

      {chartData && (
        <div className={`trend-result__body${job?.jobStatus === "COMPLETED" && job.dailyResults?.length > 0 ? " trend-result__body--with-table" : ""}`}>
          <div className="trend-result__chart-wrap">
            <Line data={chartData} options={chartOptions} />
          </div>

          {job?.jobStatus === "COMPLETED" && job.dailyResults?.length > 0 && (
            <div className="trend-result__table-wrap">
              <table className="trend-result__table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Total</th>
                    <th>Positive %</th>
                    <th>Negative %</th>
                  </tr>
                </thead>
                <tbody>
                  {job.dailyResults.map((d) => {
                    const total = d.totalCommentsAnalyzed || 0;
                    const posPct = total > 0 ? Math.round((d.positiveComments / total) * 100) : 0;
                    const negPct = total > 0 ? Math.round((d.negativeComments / total) * 100) : 0;
                    const [, month, day] = d.date.split("-");
                    const mmdd = `${month}/${day}`;
                    return (
                      <tr key={d.date}>
                        <td>{mmdd}</td>
                        <td>{total}</td>
                        <td className="trend-result__td--positive">{posPct}%</td>
                        <td className="trend-result__td--negative">{negPct}%</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {job?.dailyResults?.some((d) => d.firstVideo) && (
        <div className="trend-result__video-section">
          <span className="trend-result__video-section-label">Videos by day</span>
          <div className="trend-result__video-scroll">
            {job.dailyResults
              .filter((d) => d.firstVideo)
              .map((d) => {
                const v = d.firstVideo;
                const [, month, day] = d.date.split("-");
                return (
                  <div key={d.date} className="trend-result__video-card">
                    <div className="trend-result__video-date">{`${month}/${day}`}</div>
                    <img
                      src={v.thumbnailUrl}
                      alt=""
                      className="trend-result__video-thumb"
                    />
                    <div className="trend-result__video-info">
                      <div className="trend-result__video-title">{v.title}</div>
                      <div className="trend-result__video-stats">
                        <span>{formatCount(v.viewCount)} views</span>
                        <span>{formatCount(v.likeCount)} likes</span>
                        <span>{formatCount(v.commentCount)} comments</span>
                      </div>
                    </div>
                  </div>
                );
              })}
          </div>
        </div>
      )}
    </div>
  );
};

export default TrendAnalysisResult;

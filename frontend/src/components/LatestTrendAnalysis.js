import React, { useState, useEffect } from "react";
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from "chart.js";
import { Pie, Line } from "react-chartjs-2";
import "./LatestTrendAnalysis.css";
import { apiFetch } from "../utils/api";

ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend
);

const API_URL = "http://localhost:8081/api/trend/latest";

const PIE_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom",
      labels: { font: { size: 10 }, boxWidth: 10, padding: 6 },
    },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.label}: ${ctx.raw}`,
      },
    },
  },
};

const buildLineOptions = (item) => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "top",
      labels: { font: { size: 10 }, boxWidth: 10, padding: 6 },
    },
    title: {
      display: true,
      text: "Sentiment by day",
      font: { size: 11, weight: "500" },
      color: "#666",
      padding: { bottom: 4 },
    },
    tooltip: {
      callbacks: {
        afterLabel: (ctx) => {
          const day = item.dailyResults[ctx.dataIndex];
          return `Total: ${day.totalCommentsAnalyzed}`;
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { font: { size: 10 }, stepSize: 1 },
      title: { display: false },
    },
    x: {
      ticks: { font: { size: 9 }, maxRotation: 30, autoSkip: true, maxTicksLimit: 6 },
    },
  },
});

const TrendCard = ({ item, onClick }) => {
  const pieData = {
    labels: ["Positive", "Negative", "Neutral"],
    datasets: [
      {
        data: [item.totalPositive, item.totalNegative, item.totalNeutral],
        backgroundColor: [
          "rgba(76, 175, 80, 0.85)",
          "rgba(244, 67, 54, 0.85)",
          "rgba(158, 158, 158, 0.65)",
        ],
        borderColor: ["#4caf50", "#f44336", "#9e9e9e"],
        borderWidth: 1,
      },
    ],
  };

  const lineData = {
    labels: item.dailyResults.map((d) => d.date),
    datasets: [
      {
        label: "Positive",
        data: item.dailyResults.map((d) => d.positiveComments),
        borderColor: "#4caf50",
        backgroundColor: "rgba(76, 175, 80, 0.1)",
        tension: 0.35,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
      {
        label: "Negative",
        data: item.dailyResults.map((d) => d.negativeComments),
        borderColor: "#f44336",
        backgroundColor: "rgba(244, 67, 54, 0.08)",
        tension: 0.35,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
      {
        label: "Neutral",
        data: item.dailyResults.map((d) => d.neutralComments),
        borderColor: "#9e9e9e",
        backgroundColor: "rgba(158, 158, 158, 0.06)",
        tension: 0.35,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
    ],
  };

  return (
    <div className="lta__card lta__card--clickable" onClick={onClick}>
      <div className="lta__card-header">
        <div className="lta__card-title-row">
          <span className="lta__card-query" title={item.searchQuery}>
            {item.searchQuery}
          </span>
          <span className="lta__card-arrow">›</span>
          <span className="lta__card-object" title={item.sentimentObject}>
            {item.sentimentObject}
          </span>
        </div>
        <div className="lta__card-stats">
          <span className="lta__stat">
            <span className="lta__stat-label">Comments</span>
            <span className="lta__stat-value">{item.totalAnalyzed.toLocaleString()}</span>
          </span>
          <span className="lta__stat">
            <span className="lta__stat-label">Days</span>
            <span className="lta__stat-value">{item.daysBack}</span>
          </span>
          <span className="lta__stat lta__stat--neg">
            <span className="lta__stat-label">Neg%</span>
            <span className="lta__stat-value">{item.negativePercent}%</span>
          </span>
          <span className="lta__stat lta__stat--pos">
            <span className="lta__stat-label">Pos%</span>
            <span className="lta__stat-value">{item.positivePercent}%</span>
          </span>
        </div>
      </div>

      <div className="lta__card-charts">
        <div className="lta__card-pie">
          <Pie data={pieData} options={PIE_OPTIONS} />
        </div>
        <div className="lta__card-line">
          <Line data={lineData} options={buildLineOptions(item)} />
        </div>
      </div>
    </div>
  );
};

const LatestTrendAnalysis = ({ onCardClick }) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchLatest = async () => {
      try {
        setLoading(true);
        setError(null);
        const res = await apiFetch(API_URL);
        if (!res.ok) throw new Error("Failed to fetch latest trend analyses");
        const data = await res.json();
        setItems(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message);
        setItems([]);
      } finally {
        setLoading(false);
      }
    };
    fetchLatest();
  }, []);

  return (
    <div className="lta">
      <div className="lta__header">
        <span className="lta__title">Latest Trend Analyses</span>
      </div>

      {loading && <div className="lta__status">Loading…</div>}
      {error && <div className="lta__status lta__status--error">{error}</div>}
      {!loading && !error && items.length === 0 && (
        <div className="lta__status">No trend analyses found.</div>
      )}

      {items.length > 0 && (
        <div className="lta__scroll">
          {items.map((item) => (
            <TrendCard
              key={item.jobId}
              item={item}
              onClick={() => onCardClick && onCardClick(item.jobId, { searchQuery: item.searchQuery, sentimentObject: item.sentimentObject })}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default LatestTrendAnalysis;

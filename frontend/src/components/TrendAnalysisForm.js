import React, { useState } from "react";
import "./TrendAnalysisForm.css";
import { apiFetch } from "../utils/api";

const API_URL = "/api/trend/start";

const DEFAULT_FORM = {
  searchQuery: "",
  sentimentObject: "",
  daysBack: 7,
  commentsPerVideo: 100,
  videosPerDay: 1,
};

const TrendAnalysisForm = ({ onJobStarted }) => {
  const [form, setForm] = useState(DEFAULT_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "number" ? Number(value) : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const res = await apiFetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error("Failed to start trend analysis");
      const data = await res.json();
      onJobStarted(data.jobId, form);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const isValid = form.searchQuery.trim() !== "" && form.sentimentObject.trim() !== "";

  return (
    <form className="trend-form" onSubmit={handleSubmit}>
      <div className="trend-form__field">
        <label className="trend-form__label" htmlFor="tf-searchQuery">
          Search Query
        </label>
        <input
          id="tf-searchQuery"
          className="trend-form__input"
          type="text"
          name="searchQuery"
          placeholder="e.g. la lakers"
          value={form.searchQuery}
          onChange={handleChange}
        />
      </div>

      <div className="trend-form__field">
        <label className="trend-form__label" htmlFor="tf-sentimentObject">
          Sentiment Object
        </label>
        <input
          id="tf-sentimentObject"
          className="trend-form__input"
          type="text"
          name="sentimentObject"
          placeholder="e.g. lebron james"
          value={form.sentimentObject}
          onChange={handleChange}
        />
      </div>

      <div className="trend-form__row">
        <div className="trend-form__field">
          <label className="trend-form__label" htmlFor="tf-daysBack">
            Days Back
          </label>
          <input
            id="tf-daysBack"
            className="trend-form__input trend-form__input--number"
            type="number"
            name="daysBack"
            min={1}
            max={30}
            value={form.daysBack}
            onChange={handleChange}
          />
        </div>

        <div className="trend-form__field">
          <label className="trend-form__label" htmlFor="tf-commentsPerVideo">
            Comments / Video
          </label>
          <input
            id="tf-commentsPerVideo"
            className="trend-form__input trend-form__input--number"
            type="number"
            name="commentsPerVideo"
            min={10}
            max={500}
            step={10}
            value={form.commentsPerVideo}
            onChange={handleChange}
          />
        </div>

        <div className="trend-form__field">
          <label className="trend-form__label" htmlFor="tf-videosPerDay">
            Videos / Day
          </label>
          <input
            id="tf-videosPerDay"
            className="trend-form__input trend-form__input--number"
            type="number"
            name="videosPerDay"
            min={1}
            max={10}
            value={form.videosPerDay}
            onChange={handleChange}
          />
        </div>
      </div>

      {error && <div className="trend-form__error">{error}</div>}

      <button
        className="trend-form__submit"
        type="submit"
        disabled={!isValid || submitting}
      >
        {submitting ? "Starting…" : "Analyze Trend"}
      </button>
    </form>
  );
};

export default TrendAnalysisForm;

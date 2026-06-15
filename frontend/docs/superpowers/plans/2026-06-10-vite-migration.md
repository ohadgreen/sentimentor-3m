# Vite Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Create React App (`react-scripts`) with Vite, add a dev-server proxy to eliminate hardcoded backend URLs, and clean up CRA-specific env variable access and boilerplate.

**Architecture:** In-place replacement — no files are moved within `src/`, no TypeScript, no test framework. The only structural change is moving `public/index.html` to the project root (required by Vite). All hardcoded `http://localhost:8081` URLs become relative `/api/...` paths, proxied to the backend by Vite's dev server.

**Tech Stack:** Vite 6, `@vitejs/plugin-react`, React 18, existing dependencies unchanged.

---

## File Map

| File | Action |
|---|---|
| `package.json` | Remove `react-scripts`, CRA test libs, `web-vitals`; add `vite` + `@vitejs/plugin-react` as devDeps; update scripts; remove `browserslist` |
| `vite.config.js` | Create — Vite config with React plugin and `/api` proxy |
| `index.html` | Create at project root from `public/index.html` — strip `%PUBLIC_URL%`, add module script tag |
| `public/index.html` | Delete |
| `.env` | Rename `REACT_APP_*` keys to `VITE_*` |
| `src/index.js` | Change `process.env.REACT_APP_GOOGLE_CLIENT_ID` → `import.meta.env.VITE_GOOGLE_CLIENT_ID`; remove `reportWebVitals` import and call |
| `src/components/VideoSearch.js` | Strip `process.env.PUBLIC_URL` (3×); strip `http://localhost:8081` from search URL |
| `src/components/videoStats/VideoPlayer.js` | Strip `process.env.PUBLIC_URL` (3×) |
| `src/components/videoStats/CommentListItem.js` | Strip `process.env.PUBLIC_URL` (2×) |
| `src/context/AuthContext.js` | Strip `http://localhost:8081` from auth URL |
| `src/components/LatestAnalysis.js` | Strip `http://localhost:8081` from API_URL |
| `src/components/LatestTrendAnalysis.js` | Strip `http://localhost:8081` from API_URL |
| `src/components/TrendAnalysisForm.js` | Strip `http://localhost:8081` from API_URL |
| `src/components/TrendAnalysisResult.js` | Strip `http://localhost:8081` from API_BASE |
| `src/components/videoStats/SentimentAnalysis.js` | Strip `http://localhost:8081` from 2 URLs |
| `src/components/videoStats/VideoCommentsAnalysis.js` | Strip `http://localhost:8081` from 2 URL constants |
| `src/reportWebVitals.js` | Delete |
| `src/setupTests.js` | Delete |
| `src/logo.svg` | Delete |

---

## Task 1: Replace react-scripts with Vite

**Files:**
- Modify: `package.json`

- [ ] **Step 1: Uninstall CRA packages**

Run from `frontend/`:
```bash
npm uninstall react-scripts web-vitals @testing-library/jest-dom @testing-library/react @testing-library/user-event
```
Expected: packages removed from `node_modules` and `package.json`.

- [ ] **Step 2: Install Vite**

```bash
npm install --save-dev vite @vitejs/plugin-react
```
Expected: both packages appear under `devDependencies` in `package.json`.

- [ ] **Step 3: Update scripts and remove `browserslist` in `package.json`**

Replace the `scripts` block and remove `browserslist`. The relevant sections of `package.json` should become:

```json
"scripts": {
  "dev":     "vite",
  "build":   "vite build",
  "preview": "vite preview"
},
```

Delete the entire `browserslist` key and its value from `package.json`.

- [ ] **Step 4: Commit**

```bash
git add package.json package-lock.json
git commit -m "chore: replace react-scripts with vite"
```

---

## Task 2: Create `vite.config.js`

**Files:**
- Create: `vite.config.js` (project root, next to `package.json`)

- [ ] **Step 1: Create the file**

Create `vite.config.js` at the project root with this exact content:

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      }
    }
  }
})
```

The proxy entry means every `fetch('/api/...')` call in dev is forwarded to `http://localhost:8081/api/...`. The backend never needs to be referenced by URL in source code again.

- [ ] **Step 2: Commit**

```bash
git add vite.config.js
git commit -m "chore: add vite config with api proxy"
```

---

## Task 3: Move and rewrite `index.html`

**Files:**
- Create: `index.html` (project root)
- Delete: `public/index.html`

Vite expects the HTML entry point at the project root, not inside `public/`. Static assets (`.png`, `.webp`, etc.) remain in `public/` — Vite serves that folder unchanged.

- [ ] **Step 1: Create `index.html` at the project root**

Create `index.html` at the project root with this content (all `%PUBLIC_URL%/` stripped, CRA comments removed, module script tag added):

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <link rel="icon" href="/video-img.jpeg" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="theme-color" content="#000000" />
    <meta name="description" content="YouTube comment sentiment analysis" />
    <link rel="apple-touch-icon" href="/video-img.jpeg" />
    <link rel="manifest" href="/manifest.json" />
    <title>Tube Surf</title>
  </head>
  <body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>
    <script type="module" src="/src/index.js"></script>
  </body>
</html>
```

- [ ] **Step 2: Delete `public/index.html`**

```bash
rm public/index.html
```

Or delete via the IDE. Verify only `index.html` at the root remains.

- [ ] **Step 3: Commit**

```bash
git add index.html
git rm public/index.html
git commit -m "chore: move index.html to project root for vite"
```

---

## Task 4: Update env variables

**Files:**
- Modify: `.env`
- Modify: `src/index.js`

Vite requires custom env vars to be prefixed `VITE_` (not `REACT_APP_`). They are accessed via `import.meta.env.VITE_*` instead of `process.env.REACT_APP_*`.

- [ ] **Step 1: Update `.env`**

Replace the contents of `.env` with:

```
VITE_API_KEY=my_api_key_here
VITE_GOOGLE_CLIENT_ID=93152652781-7t1h9d39pi41gqi93coucpin8o6nookh.apps.googleusercontent.com
```

(Key names changed: `REACT_APP_API_KEY` → `VITE_API_KEY`, `REACT_APP_GOOGLE_CLIENT_ID` → `VITE_GOOGLE_CLIENT_ID`)

- [ ] **Step 2: Update `src/index.js`**

Replace the full content of `src/index.js` with:

```js
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './components/App';
import { GoogleOAuthProvider } from '@react-oauth/google';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <App />
    </GoogleOAuthProvider>
  </React.StrictMode>
);
```

Changes: `process.env.REACT_APP_GOOGLE_CLIENT_ID` → `import.meta.env.VITE_GOOGLE_CLIENT_ID`; `reportWebVitals` import and call removed.

- [ ] **Step 3: Commit**

```bash
git add .env src/index.js
git commit -m "chore: migrate env vars from REACT_APP_ to VITE_ prefix"
```

---

## Task 5: Strip `process.env.PUBLIC_URL` from asset paths

**Files:**
- Modify: `src/components/VideoSearch.js`
- Modify: `src/components/videoStats/VideoPlayer.js`
- Modify: `src/components/videoStats/CommentListItem.js`

Vite serves `public/` assets directly from `/`. Replace `` `${process.env.PUBLIC_URL}/filename` `` with `"/filename"` (plain string).

- [ ] **Step 1: Update `src/components/VideoSearch.js` (3 occurrences, lines 142, 146, 150)**

Find and replace all three occurrences:

```js
// Before
<img src={`${process.env.PUBLIC_URL}/view.png`} width={15} height={15} alt="views" />
// After
<img src="/view.png" width={15} height={15} alt="views" />

// Before
<img src={`${process.env.PUBLIC_URL}/like.png`} width={15} height={15} alt="likes" />
// After
<img src="/like.png" width={15} height={15} alt="likes" />

// Before
<img src={`${process.env.PUBLIC_URL}/comment.png`} width={15} height={15} alt="comments" />
// After
<img src="/comment.png" width={15} height={15} alt="comments" />
```

- [ ] **Step 2: Update `src/components/videoStats/VideoPlayer.js` (3 occurrences, lines 69, 78, 87)**

```js
// Before
src={`${process.env.PUBLIC_URL}/view.png`}
// After
src="/view.png"

// Before
src={`${process.env.PUBLIC_URL}/like.png`}
// After
src="/like.png"

// Before
src={`${process.env.PUBLIC_URL}/comment.png`}
// After
src="/comment.png"
```

- [ ] **Step 3: Update `src/components/videoStats/CommentListItem.js` (2 occurrences, lines 44, 73)**

```js
// Before (line 44, inside onError handler)
e.target.src = `${process.env.PUBLIC_URL}/person-fallback.webp`;
// After
e.target.src = "/person-fallback.webp";

// Before (line 73)
src={`${process.env.PUBLIC_URL || ""}/like.png`}
// After
src="/like.png"
```

- [ ] **Step 4: Commit**

```bash
git add src/components/VideoSearch.js src/components/videoStats/VideoPlayer.js src/components/videoStats/CommentListItem.js
git commit -m "chore: replace process.env.PUBLIC_URL with static asset paths"
```

---

## Task 6: Strip hardcoded backend URLs

**Files:**
- Modify: `src/components/LatestAnalysis.js`
- Modify: `src/components/LatestTrendAnalysis.js`
- Modify: `src/components/TrendAnalysisForm.js`
- Modify: `src/components/TrendAnalysisResult.js`
- Modify: `src/components/VideoSearch.js`
- Modify: `src/components/videoStats/SentimentAnalysis.js`
- Modify: `src/components/videoStats/VideoCommentsAnalysis.js`
- Modify: `src/context/AuthContext.js`

With the Vite proxy in place (Task 2), `fetch('/api/...')` in dev is forwarded to `http://localhost:8081/api/...` automatically. Strip the origin from every URL constant.

- [ ] **Step 1: `src/components/LatestAnalysis.js` (line 5)**

```js
// Before
const API_URL = "http://localhost:8081/api/sentiment/latest";
// After
const API_URL = "/api/sentiment/latest";
```

- [ ] **Step 2: `src/components/LatestTrendAnalysis.js` (line 26)**

```js
// Before
const API_URL = "http://localhost:8081/api/trend/latest";
// After
const API_URL = "/api/trend/latest";
```

- [ ] **Step 3: `src/components/TrendAnalysisForm.js` (line 5)**

```js
// Before
const API_URL = "http://localhost:8081/api/trend/start";
// After
const API_URL = "/api/trend/start";
```

- [ ] **Step 4: `src/components/TrendAnalysisResult.js` (line 26)**

```js
// Before
const API_BASE = "http://localhost:8081/api/trend";
// After
const API_BASE = "/api/trend";
```

- [ ] **Step 5: `src/components/VideoSearch.js` (line 23)**

```js
// Before
const searchBaseUrl = "http://localhost:8081/api/videos/search";
// After
const searchBaseUrl = "/api/videos/search";
```

- [ ] **Step 6: `src/components/videoStats/SentimentAnalysis.js` (lines 138, 204)**

```js
// Before (line 138, inside fetchSentimentSummary)
`http://localhost:8081/api/sentiment/sentimentOngoingAnalysis/${vidId}/${aId}`,
// After
`/api/sentiment/sentimentOngoingAnalysis/${vidId}/${aId}`,

// Before (line 204, inside handleAnalyze)
const response = await apiFetch("http://localhost:8081/api/sentiment/analyzeRequest", {
// After
const response = await apiFetch("/api/sentiment/analyzeRequest", {
```

- [ ] **Step 7: `src/components/videoStats/VideoCommentsAnalysis.js` (lines 34–35)**

```js
// Before
const commentsListReqUrl = "http://localhost:8081/api/sentiment/getRawVideoComments";
const commentsPageReqUrl = "http://localhost:8081/api/comments/page";
// After
const commentsListReqUrl = "/api/sentiment/getRawVideoComments";
const commentsPageReqUrl = "/api/comments/page";
```

- [ ] **Step 8: `src/context/AuthContext.js` (line 24)**

```js
// Before
const res = await fetch("http://localhost:8081/api/auth/google", {
// After
const res = await fetch("/api/auth/google", {
```

- [ ] **Step 9: Commit**

```bash
git add src/components/LatestAnalysis.js src/components/LatestTrendAnalysis.js src/components/TrendAnalysisForm.js src/components/TrendAnalysisResult.js src/components/VideoSearch.js src/components/videoStats/SentimentAnalysis.js src/components/videoStats/VideoCommentsAnalysis.js src/context/AuthContext.js
git commit -m "chore: replace hardcoded backend URLs with relative /api paths"
```

---

## Task 7: Delete CRA boilerplate

**Files:**
- Delete: `src/reportWebVitals.js`
- Delete: `src/setupTests.js`
- Delete: `src/logo.svg`

`reportWebVitals.js` is a CRA-only performance measurement helper. Its import and call were already removed from `src/index.js` in Task 4. `setupTests.js` is CRA's Jest setup shim — no tests exist in this project. `logo.svg` is the CRA default placeholder and is not imported anywhere.

- [ ] **Step 1: Delete the three files**

```bash
git rm src/reportWebVitals.js src/setupTests.js src/logo.svg
```

- [ ] **Step 2: Commit**

```bash
git commit -m "chore: remove CRA boilerplate files"
```

---

## Task 8: Smoke test

No automated tests exist. Verify the app runs correctly by hand.

- [ ] **Step 1: Start the dev server**

```bash
npm run dev
```

Expected output:
```
  VITE v6.x.x  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

If you see an error like `Cannot find module 'react-scripts'` the uninstall in Task 1 didn't complete — re-run the uninstall command.

- [ ] **Step 2: Open the app and verify login page loads**

Open `http://localhost:3000` in a browser. Expected: the TubeSurf login page appears with the Google sign-in button. No console errors about missing env vars or failed asset loads.

- [ ] **Step 3: Verify assets load**

Open the browser DevTools Network tab. Reload the page. Confirm `view.png`, `like.png`, `comment.png`, `person-fallback.webp` all return HTTP 200. If any return 404, the `PUBLIC_URL` strip in Task 5 introduced a path error.

- [ ] **Step 4: Verify API proxy**

Sign in with Google. Once logged in, the home page should load latest analyses. Open DevTools Network tab — API calls should show as `/api/sentiment/latest` etc. (not `http://localhost:8081/...`). They should return 200 if the backend is running, or show a proxy connection error if the backend is stopped — both confirm the proxy is active.

- [ ] **Step 5: Verify production build**

```bash
npm run build
```

Expected: a `dist/` folder is created with no errors. Warnings about bundle size are OK.

- [ ] **Step 6: Commit if any fixes were needed**

If Step 1–5 uncovered any typos or missed replacements, fix and commit before closing the task:

```bash
git add <changed files>
git commit -m "fix: correct vite migration issues found during smoke test"
```

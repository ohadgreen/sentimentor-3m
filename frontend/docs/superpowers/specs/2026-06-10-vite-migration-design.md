# Vite Migration Design

**Date:** 2026-06-10
**Branch:** vite-migration
**Scope:** Migrate frontend from Create React App (`react-scripts 5.0.1`) to Vite. Includes proxy setup to replace hardcoded backend URLs and env variable audit.

---

## Motivation

- CRA is no longer maintained (EOL)
- Slow dev server cold start and HMR compared to Vite
- Access to modern Vite ecosystem and plugin model
- General toolchain modernisation

---

## Approach

In-place replacement: remove `react-scripts`, install Vite and its React plugin, update configs and source files in the existing repo. No structural reorganisation of `src/`.

---

## Section 1: Dependencies & Scripts

### Remove
- `react-scripts` (CRA bundler + dev server)

### Add
- `vite` — build tool and dev server
- `@vitejs/plugin-react` — React JSX transform and Fast Refresh for Vite

### `package.json` scripts
```json
"scripts": {
  "dev":     "vite",
  "build":   "vite build",
  "preview": "vite preview"
}
```

- `npm start` → `npm run dev`
- Remove the `browserslist` field (CRA-specific; Vite has its own target config)

---

## Section 2: Project Structure

### New file: `vite.config.js` (project root)
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

The proxy forwards all `/api/*` requests to the backend in development, enabling relative API paths throughout the codebase.

### Move `public/index.html` → `index.html` (project root)

Vite requires the HTML entry point at the project root. Move the file (not copy) — delete `public/index.html` after creating `index.html` at the root. The `public/` folder itself remains — Vite still serves its contents as static assets.

**Changes to `index.html` after moving:**
1. Replace all `%PUBLIC_URL%/` prefixes with `/` (e.g. `href="%PUBLIC_URL%/favicon.ico"` → `href="/video-img.jpeg"`)
2. Remove CRA boilerplate comments
3. Add module entry script at end of `<body>`:
   ```html
   <script type="module" src="/src/index.js"></script>
   ```

---

## Section 3: Source Code Changes

### 3a — Environment variables

**`.env`:**
| Old key | New key |
|---|---|
| `REACT_APP_GOOGLE_CLIENT_ID` | `VITE_GOOGLE_CLIENT_ID` |
| `REACT_APP_API_KEY` | `VITE_API_KEY` (unused, kept for future use) |

**`src/index.js` line 11:**
```js
// Before
<GoogleOAuthProvider clientId={process.env.REACT_APP_GOOGLE_CLIENT_ID}>
// After
<GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
```

### 3b — `process.env.PUBLIC_URL` asset paths

Strip `${process.env.PUBLIC_URL}` prefix — replace with plain string literal paths.

Affected files (8 occurrences):

| File | Example change |
|---|---|
| `src/components/VideoSearch.js` (3×) | `` `${process.env.PUBLIC_URL}/view.png` `` → `"/view.png"` |
| `src/components/videoStats/VideoPlayer.js` (3×) | same pattern |
| `src/components/videoStats/CommentListItem.js` (2×) | same pattern |

### 3c — Hardcoded backend URLs

Strip `http://localhost:8081` from all API URLs, leaving only the `/api/...` path. The Vite dev proxy handles forwarding to `localhost:8081` at the config level.

| File | Occurrences | Example change |
|---|---|---|
| `src/context/AuthContext.js` | 1 | `"http://localhost:8081/api/auth/google"` → `"/api/auth/google"` |
| `src/components/LatestAnalysis.js` | 1 | `"http://localhost:8081/api/sentiment/latest"` → `"/api/sentiment/latest"` |
| `src/components/LatestTrendAnalysis.js` | 1 | `"http://localhost:8081/api/trend/latest"` → `"/api/trend/latest"` |
| `src/components/TrendAnalysisForm.js` | 1 | `"http://localhost:8081/api/trend/start"` → `"/api/trend/start"` |
| `src/components/TrendAnalysisResult.js` | 1 | `"http://localhost:8081/api/trend"` → `"/api/trend"` |
| `src/components/VideoSearch.js` | 1 | `"http://localhost:8081/api/videos/search"` → `"/api/videos/search"` |
| `src/components/videoStats/SentimentAnalysis.js` | 2 | Strip `http://localhost:8081` prefix |
| `src/components/videoStats/VideoCommentsAnalysis.js` | 2 | Strip `http://localhost:8081` prefix |

**Total: 10 occurrences across 7 files.**

---

## Section 4: Cleanup

Delete CRA-specific files with no role in Vite:

| File | Reason |
|---|---|
| `src/reportWebVitals.js` | CRA-only performance measurement utility |
| `src/setupTests.js` | CRA Jest setup; no tests in project |
| `src/logo.svg` | CRA placeholder; not referenced anywhere |

Also remove from `src/index.js`:
- `import reportWebVitals from './reportWebVitals'`
- `reportWebVitals()` call at the bottom

---

## File Change Summary

| File | Action |
|---|---|
| `package.json` | Update deps and scripts, remove `browserslist` |
| `vite.config.js` | Create |
| `public/index.html` → `index.html` | Move to root, update content |
| `.env` | Rename keys |
| `src/index.js` | Update env var access, remove reportWebVitals |
| `src/components/VideoSearch.js` | Strip `PUBLIC_URL`, strip localhost URL |
| `src/components/videoStats/VideoPlayer.js` | Strip `PUBLIC_URL` |
| `src/components/videoStats/CommentListItem.js` | Strip `PUBLIC_URL` |
| `src/context/AuthContext.js` | Strip localhost URL |
| `src/components/LatestAnalysis.js` | Strip localhost URL |
| `src/components/LatestTrendAnalysis.js` | Strip localhost URL |
| `src/components/TrendAnalysisForm.js` | Strip localhost URL |
| `src/components/TrendAnalysisResult.js` | Strip localhost URL |
| `src/components/videoStats/SentimentAnalysis.js` | Strip localhost URLs |
| `src/components/videoStats/VideoCommentsAnalysis.js` | Strip localhost URLs |
| `src/reportWebVitals.js` | Delete |
| `src/setupTests.js` | Delete |
| `src/logo.svg` | Delete |

---

## Out of Scope

- TypeScript migration
- Renaming `.js` files to `.jsx` (`@vitejs/plugin-react` handles JSX in `.js` files)
- Vitest or any test framework setup (no existing tests)
- Production deployment config changes

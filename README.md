# AttentionTwin 5.0 — Latest build

[Android APK](release-v5/AttentionTwin-5.0.apk) · [Setup and release notes](release-v5/README.md) · [Verification](release-v5/VERIFICATION.md) · [Offline UI demo](release-v5/offline-demo.html)

Version 5 replaces conversation snoozing with an encrypted distinct-message inbox, local emergency and promotion rules, grouped Gemini review and an Important inbox. It fixes keyboard resizing, retains permission setup consent, and restores quota blocking after service restart. Upgrade over the previous build. Physical phone and live Gemini behaviour still need verification; see the record above. Older release descriptions below are historical and may describe superseded notification behaviour.

# AttentionTwin 4.0 — Daily goals, pause screens & all-app Smart

Latest build: [AttentionTwin-4.0.apk](release-v4/AttentionTwin-4.0.apk), [setup and changes](release-v4/README.md), [verification](release-v4/VERIFICATION.md), [offline UI preview](release-v4/offline-demo.html).

Version 4 adds visible Home scrolling goals, separate Instagram/YouTube limits, an optional full-screen pause at the daily goal, all-app Smart/Hold controls, local greeting filtering, newest-message parsing, app dark theme, a launcher widget with an independent theme button, and guided planned breaks. Android/JVM tests include framework and listener-pipeline simulations; physical phone and live user-key Gemini behaviour remain unverified.

## Version 3 history

Current build: [AttentionTwin-3.0.apk](release-v3/AttentionTwin-3.0.apk), [setup and release notes](release-v3/README.md), [verification](release-v3/VERIFICATION.md), [offline UI demonstration](release-v3/offline-demo.html).

Version 3 adds optional automatic Instagram/YouTube reminders through an explicitly enabled Accessibility service, daily time reminders, foreground diagnostics, one-minute breathing, configurable notification scope, and focus-first/balanced Gemini filtering. Casual chats are distinguished from time-sensitive requests. Updated Smart conversations are reclassified; real held alerts, history and tests have separate views. The existing encrypted phone key is retained on normal upgrade. Physical device and live Gemini verification remain pending; see the verification record.

## Version 2 history

The current app uses Home, Focus, Alerts and Reflect. Bedtime and scrolling reminders open from Home. The entry point no longer imports the old Lab screens or global styles. See [version 2 release notes](docs/V2_RELEASE.md) and [current APK/output](release-v2/README.md).

This version adds an optional Gemini API connection using a key entered privately on the Android phone. No key ships with the app. Important apps and local safety exceptions bypass classification; unavailable or uncertain Gemini responses allow the alert. No training is needed.

Build and regression details are in release-v2/VERIFICATION.md. The following documentation describes the earlier iterations.

# AttentionTwin Wellness update — 16 November 2026

A new Android debug APK has been built and signature-verified in this workspace. See [deliverables/START_HERE.txt](deliverables/START_HERE.txt) for installation, the offline demonstration and assignment materials. Wellness adds a study-to-break planner, bedtime rituals and reminders, voluntary scrolling check-ins, and a local reflection journal. The Android build, 16 existing decision tests, 3 wellness model tests, and browser core-flow checks pass. Physical Android testing and participant research remain pending. [Report](docs/wellness/REPORT.md).

The following is the original repository documentation; its historical build-status notes predate this update.

# AttentionTwin — Personal Attention Digital Twin

> **Phone app status (2026-10-26):** The app has an on-device notification listener, local SQLite history, focus timer/schedule, break reminders, per-app roles, safety exceptions, and labeled sample activity. The latest source pass makes app roles default to Allow and checks focus/role before reusing any snooze marker. `npm run build` and Capacitor sync pass. The Android test/APK build could not run in this environment because Java is not installed or available; the changed APK has not been produced or installed. Real Redmi/HyperOS behavior is unverified. Android may show an alert briefly before snoozing unless DND access is enabled, and Android controls when snoozed alerts return. Local rules and sample data are estimates, not validated productivity claims. See [`docs/DEVICE_TEST_CHECKLIST.md`](docs/DEVICE_TEST_CHECKLIST.md), [`docs/DATA_AND_ML.md`](docs/DATA_AND_ML.md), and [`docs/DEMO_GUIDE.md`](docs/DEMO_GUIDE.md).

> **CASCADE 2026 — IIIT Nagpur Hackathon Project**  
> **Phase 1: Project Foundation & Architecture Shell**  
> **Phase 2: Database Persistence & Event APIs**  
> **Phase 3: Synthetic Dataset Generation & Feature Engineering**  
> **Phase 4: Baseline Machine Learning Prediction Engine**  
> **Phase 5: Action-Aware Counterfactual Comparison Engine**  
> **Phase 6: Live Dashboard, History Panel & Model Status UI**  
> **Phase 7: Native Android APK (Capacitor)**

AttentionTwin is a personal Attention Digital Twin designed to model how an individual responds to notifications and estimate the outcomes of **allowing**, **suppressing**, or **deferring** a notification.

The defining requirement is **counterfactual comparison**: evaluating predicted outcomes under different actions for the **same** notification and user context. The core prediction engine runs locally on scikit-learn without mandatory external cloud APIs or paid dependencies.

---

## Architecture Overview

```
d:\digital_twin\
├── data\
│   └── synthetic\
│       ├── raw_synthetic_events.json # 8,000 raw synthetic event records
│       ├── feature_dataset.csv       # Model-ready features table with binary & multiclass targets
│       ├── metadata.json             # Seed, generator version, provenance, and summary stats
│       └── README.md                 # Data dictionary, assumptions, cold-start handling
├── backend\
│   ├── artifacts\
│   │   ├── baseline_model.joblib     # Serialized RandomForestClassifier pipeline (Phase 4)
│   │   ├── model_metadata.json       # Version, metrics, evaluation basis, and disclaimers
│   │   ├── action_aware_model.joblib # Serialized Action-Aware RandomForest pipeline (Phase 5)
│   │   └── action_aware_metadata.json# Action-aware metrics, per-action breakdowns, disclaimers
│   ├── requirements.txt              # FastAPI, Uvicorn, Pydantic, SQLAlchemy, scikit-learn
│   ├── app\
│   │   ├── main.py                   # FastAPI entrypoint, lifespan db init, router setup
│   │   ├── core\config.py            # Settings, DATABASE_URL, CORS configuration
│   │   ├── db\                       # Base, SessionLocal, get_db dependency
│   │   ├── models\event.py           # Event, Decision, Outcome, Prediction, Feedback models
│   │   ├── schemas\                  # Pydantic schemas (event.py, ml.py, counterfactual.py)
│   │   ├── ml\
│   │   │   ├── feature_schema.py     # Logical feature groups, cold-start defaults, sources
│   │   │   ├── synthetic_generator.py# Deterministic multi-factor synthetic data generator
│   │   │   ├── feature_pipeline.py   # Chronological feature extractor & leakage validator
│   │   │   ├── split_utils.py        # Chronological and user-held-out evaluation splitters
│   │   │   ├── trainer.py            # BaselineModelTrainer (Phase 4)
│   │   │   ├── predictor.py          # PredictionService for inference & limitation disclosure
│   │   │   ├── action_aware_trainer.py# ActionAwareModelTrainer with per-action breakdowns (Phase 5)
│   │   │   └── counterfactual_engine.py# CounterfactualEngine for invariant context comparison
│   │   └── api\routes\
│   │       ├── health.py             # GET /api/health implementation
│   │       ├── events.py             # Event CRUD, decisions, outcomes, history
│   │       ├── ml.py                 # GET /api/models/status, POST /api/models/predict
│   │       └── counterfactuals.py    # POST /api/counterfactuals/compare
│   └── tests\
│       ├── test_health.py            # Phase 1 health check tests
│       ├── test_events.py            # Phase 2 event persistence & cascade tests
│       ├── test_synthetic_data.py    # Phase 3 feature pipeline & cold-start tests
│       ├── test_ml_pipeline.py       # Phase 4 ML training, evaluation, & inference tests
│       └── test_counterfactuals.py   # Phase 5 counterfactual engine & DB safety tests
└── frontend\
    ├── index.html                    # Vite HTML shell with typography
    ├── package.json                  # React 18, Vite, TypeScript, Recharts, Lucide
    ├── tsconfig.json                 # Strict TypeScript configuration
    └── src\                          # Application shell and UI components
```

---

## Action-Aware Counterfactual Engine (Phase 5)

### Core Invariant
For any incoming notification, all pre-decision attributes (**source app, category, text length, hour of day, focus mode, active focus block, trailing alert load, and historical responsiveness**) remain **strictly identical**. Only the candidate intervention action (`allow`, `defer`, `suppress`) is varied to estimate alternative outcomes under identical circumstances.

### Feature Schema (18 Features)
- **Categorical (5):** `source_app`, `app_category`, `notification_category`, `focus_mode`, `proposed_action` (`allow`, `defer`, `suppress`).
- **Numeric (13):** `text_length`, `hour_of_day`, `day_of_week`, `is_weekend`, `is_focus_session`, `recent_notification_count_15m`, `minutes_since_last_notification`, `notification_load_1h`, `app_historical_open_rate`, `app_historical_ignore_rate`, `user_historical_open_rate`, `recent_allow_rate_1h`, `recent_suppress_rate_1h`.

### Evaluation Metrics (Action-Aware Model)

Chronological Split (80% train / 20% test per user):
- **Overall Accuracy:** 71.44% (vs Dummy Baseline: 64.69%)
- **Overall ROC-AUC:** 0.7698

#### Per-Action Breakdown on Test Set
- **ALLOW Action:** Prompt open rate: 43.31% | Accuracy: 66.88% | F1: 0.5407
- **DEFER Action:** Prompt open rate: 17.84% | Accuracy: 79.88% | F1: 0.2885
- **SUPPRESS Action:** Prompt open rate: 8.38% | Accuracy: 89.04% | F1: 0.1111

---

## API Endpoints Reference

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Service health status and metadata | 200 OK |
| `POST` | `/api/events` | Create a new notification event with context | 201 Created |
| `GET` | `/api/events` | List events with pagination and filters | 200 OK |
| `GET` | `/api/events/{id}` | Retrieve specific notification event | 200 OK / 404 |
| `POST` | `/api/events/{id}/decisions` | Record action taken (`allow`, `suppress`, `defer`) | 201 Created / 404 |
| `POST` | `/api/events/{id}/outcomes` | Record factual observed outcome and interaction metrics | 201 Created / 404 |
| `POST` | `/api/events/{id}/predictions`| Store counterfactual prediction for candidate action | 201 Created / 404 |
| `POST` | `/api/events/{id}/feedback` | Record user rating (1–5) and comments | 201 Created / 400 / 404 |
| `GET` | `/api/events/{id}/history` | Retrieve complete event audit trail | 200 OK / 404 |
| `GET` | `/api/models/status` | Baseline model operational status and metadata | 200 OK |
| `POST` | `/api/models/predict` | Predict probability of quick open (unconditioned) | 200 OK / 422 / 503 |
| `POST` | `/api/counterfactuals/compare` | Compare simulated outcomes across `allow`, `defer`, `suppress` | 200 OK / 422 / 503 |

---

## Quickstart Guide (Windows PowerShell)

### 1. Run Backend Automated Test Suite (39 Tests)

```powershell
$env:PYTHONPATH="backend"
.venv\Scripts\python -m pytest backend\tests -v
```

Latest verified result:
```
============================== 39 passed, 1 warning ==============================
```

### 2. Retrain Models (Optional)

```powershell
$env:PYTHONPATH="backend"
# Retrain Phase 4 baseline model
.venv\Scripts\python -c "from app.ml.trainer import BaselineModelTrainer; BaselineModelTrainer().train_and_save()"

# Retrain Phase 5 action-aware model
.venv\Scripts\python -c "from app.ml.action_aware_trainer import ActionAwareModelTrainer; ActionAwareModelTrainer().train_and_save()"
```

### 3. Start Backend Server

```powershell
.venv\Scripts\python -m uvicorn app.main:app --app-dir backend --host 127.0.0.1 --port 8000 --reload
```
- Interactive Swagger UI: `http://127.0.0.1:8000/docs`

### 4. Start Frontend Server

```powershell
cd d:\digital_twin\frontend
npm run dev
```
- Frontend UI: `http://localhost:5173`
- Production Build Check: `npm run build`

---

## Android Native Application (Phase 7)

The Capacitor Android source (`com.attentiontwin.app`) includes the after-post listener and Android snooze actions. Select **Block in focus** for apps you choose; app roles begin on **Allow**. The listener checks the live focus state and role before acting. The visible Pause control disables blocking and restores DND owned by AttentionTwin. The release control restores saved previews and sends a digest for held items; Android does not provide an API for pulling a snoozed notification forward.

No laptop backend is required for phone-side rules or history. DND is optional and needs Notification Policy Access. The listener acts after Android posts a notification, so an alert can flash briefly without DND. HyperOS may stop background work unless its Autostart and battery settings allow it. The latest source has not been built into a verified APK yet; see the device checklist before relying on it.

### Two-minute judge demo

1. Open the app, keep every suggested app on Allow, and show that setup does not silently block alerts.
2. Choose WhatsApp (or a harmless test app) and set **Block in focus**. Start a 25-minute session.
3. Send a test alert and show its role, decision, and reason in Live/Phone check-up. Explain that this listener acts after Android posts the alert.
4. Use **Pause AttentionTwin** and show that focus handling is off; use **Release everything now** to see the honest Android snooze limitation.
5. Open Insights and load the clearly labeled synthetic sample. Describe every number as a local estimate, not a personal or validated ML result.

### 1. Pre-built APK Artifact
- **APK Path:** `frontend/android/app/build/outputs/apk/debug/app-debug.apk`
- **Package ID:** `com.attentiontwin.app`
- **Application Name:** `AttentionTwin`
- **Size:** ~4.3 MB

### 2. Run Backend for Mobile Device Access
Optional: to connect Android to the development backend over your local network:
```powershell
.venv\Scripts\python -m uvicorn app.main:app --app-dir backend --host 0.0.0.0 --port 8000 --reload
```
Find your laptop's local IP address (`ipconfig`, e.g. `192.168.1.15`).

### 3. Connect Android App to Backend
1. Open the **AttentionTwin** app on your phone or emulator.
2. Tap the **⚙ (Gear)** icon in the top header.
3. Enter your backend URL:
   - Physical device on same Wi-Fi: `http://<YOUR_LAPTOP_IP>:8000` (e.g., `http://192.168.1.15:8000`)
   - Android Emulator: `http://10.0.2.2:8000` (preset button provided)
   - Desktop / Web Browser: `http://localhost:8000` (preset button provided)
4. Tap **Save & Reconnect**. The app will verify connectivity with `/api/health`.

### 4. Install APK on Android Device
Using ADB:
```powershell
D:\Android\sdk\platform-tools\adb.exe install -r frontend\android\app\build\outputs\apk\debug\app-debug.apk
```
Or copy `app-debug.apk` to your phone via USB or Google Drive and install directly (allow installation from unknown sources when prompted).

### 5. Rebuilding the APK
```powershell
cd d:\digital_twin\frontend
npm run build
npx cap sync android
$env:JAVA_HOME = "D:\jdk-21.0.4+7"
$env:ANDROID_HOME = "D:\Android\sdk"
cd android
.\gradlew.bat assembleDebug
```

---

## Current API scenario comparison (synthetic and rule-based)

The `/api/counterfactuals/compare` endpoint compares one submitted scenario under `allow`, `defer`, and `suppress`. It is an exploratory simulation; the action-conditioned opening classifier is trained on synthetic data and does not identify causal effects for unchosen actions.

### What the API currently returns

- **Quick-open probability:** classifier prediction from synthetic training labels. This is not notification importance.
- **Browsing time:** hand-authored point value by app category, multiplied by quick-open probability for ALLOW. It is not a learned duration distribution.
- **Resumption time:** hand-authored point value by focus mode, multiplied by quick-open probability for ALLOW. It is not a personalized or measured outcome.
- **Immediate disruption:** sum of those two heuristic terms for ALLOW; the API assigns zero immediate cost to DEFER and SUPPRESS by assumption.
- **Recommendation:** a fixed rule-based policy. The displayed DEFER interval (`+25m`) is illustrative and is not connected to Android delivery. Separately, the native UI lets a user configure daily break times and manually request system snoozing until the next one.

These figures must not be presented as empirical or causal results. See [`docs/COUNTERFACTUAL_DESIGN.md`](docs/COUNTERFACTUAL_DESIGN.md) and [`docs/DATA_AND_ML.md`](docs/DATA_AND_ML.md).



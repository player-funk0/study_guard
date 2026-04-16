# StudyGuard 📚

> **Obrynex Studio** — by Moaaz  
> Dark, cinematic productivity app for serious students.

---

## Features

| Module | Description |
|---|---|
| ⏱ **Study Tracker** | Start/stop sessions, daily progress bar, streak counter, Room DB |
| 🧠 **Smart Summarizer** | Instant TF-IDF (offline) + Hugging Face AI upgrade with Compare mode |
| 📵 **Digital Wellbeing** | Real screen time via `UsageStatsManager`, addiction score 0–100, top apps |
| 🔔 **Notifications** | WorkManager daily reminder + screen limit warning, fully local |

---

## Setup

### 1. Clone / open in Android Studio
```
File → Open → select /StudyGuard folder
```

### 2. Get a free Hugging Face token
1. Go to https://huggingface.co → Sign up (free, no card)
2. Settings → Access Tokens → New token (read-only)
3. Copy the token

### 3. Add your token
Open `app/build.gradle` and replace:
```groovy
buildConfigField "String", "HF_TOKEN", "\"YOUR_HF_TOKEN_HERE\""
```
with:
```groovy
buildConfigField "String", "HF_TOKEN", "\"hf_xxxxxxxxxxxxxxxxxxxx\""
```

### 4. Grant Usage Access (Digital Wellbeing)
On your phone: **Settings → Apps → Special app access → Usage access → StudyGuard → Allow**

Or tap "Grant Permission" inside the Wellbeing tab — it opens the right screen automatically.

### 5. Build & Run
```
Build → Run 'app'   (minSdk 26 = Android 8.0+)
```

---

## Architecture

```
com.obrynex.studyguard/
├── data/
│   ├── db/          ← Room (StudySession entity + DAO)
│   └── prefs/       ← DataStore (goal, limit, reminder time)
├── summarizer/      ← TfIdfSummarizer + HuggingFaceService + ViewModel + Screen
├── tracker/         ← TrackerViewModel (timer, Room) + TrackerScreen
├── wellbeing/       ← WellbeingViewModel (UsageStats) + WellbeingScreen
├── notifications/   ← NotificationHelper + Workers + ReminderScheduler
├── navigation/      ← NavGraph (bottom nav, 3 tabs)
├── ui/theme/        ← Dark color palette + MaterialTheme
├── MainActivity.kt
└── StudyGuardApp.kt
```

---

## Summarizer Flow

```
[Paste text] → [Summarize ⚡]
    └─► TfIdfSummarizer (instant, offline)
            └─► Card appears immediately

[Smart Summary button]
    └─► HuggingFaceService (Kotlin coroutine)
            ├─► Loading spinner: "Generating smarter summary…"
            ├─► Success → [Instant | AI | Compare] toggle appears
            └─► Error   → Fallback card + Retry
```

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **DB:** Room
- **Prefs:** DataStore
- **Background:** WorkManager
- **Screen time:** UsageStatsManager (on-device, no API)
- **AI (optional):** Hugging Face free inference API (`facebook/bart-large-cnn`)
- **Min SDK:** 26 (Android 8.0)

---

## Obrynex Studio

Built under **Obrynex** — a solo game & app development studio.  
itch.io: https://obrynex.itch.io

**Group:** 8
**Module:** OPSC6312  
**Course:** Information Technology   
**Demo Video:** [https://youtu.be/3pmLt_MpE-4?si=zcrNW_YKXeAs9hvI]  
**Repository:** https://github.com/st10202512/MzansiReady  
**Release APK:** https://github.com/st10202512/MzansiReady/releases

# MzansiReady

A South African daily information and preparedness Android application built with Kotlin, connecting to a custom ASP.NET Core REST API and the Open-Meteo weather service.

---

## 📖 Overview

MzansiReady brings together weather information, community alerts, and daily preparedness data into one simple platform for South African users. The app supports multiple languages, works offline with cached data, and stores user settings both locally and on a remote server.

**Target users:** Students, workers, commuters, and households who need quick access to useful information about their area.

---

## ✨ Features

### User Authentication
- Registration with full name, email, and password
- Login with JWT-based session management
- BCrypt password hashing on the backend
- Secure JWT storage in EncryptedSharedPreferences
- Input validation with user-friendly error messages

### Weather Information
- Live weather data from the Open-Meteo REST API
- Current temperature, condition, min/max temperatures, and rain probability
- WMO weather codes translated to readable descriptions and emojis
- Pull-to-refresh for updates

### Offline Support
- Room database caches weather per location
- Cached data displays when the device is offline
- Automatic sync when connectivity returns

### Multi-Language Support
- English (default)
- isiZulu
- isiXhosa
- Afrikaans
- User-selectable from Settings, persisted locally and on the backend

### Settings
- Language selection (English / isiZulu / IsiXhosa / Afrikaans)
- Theme selection (Light / Dark)
- Notification preferences
- Persisted locally and synced with the backend
- Upload pp

### Security
- HTTPS-ready API communication
- Encrypted local storage for JWT
- Input validation on all forms
- Password strength requirements (8+ chars, 1 digit, 1 uppercase)

---

## 🛠️ Tech Stack

### Android App

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Architecture | MVVM (Model-View-ViewModel) |
| UI | XML Layouts, Material 3, ViewBinding |
| Networking | Retrofit 2, OkHttp, Gson |
| Local Storage | Room Database |
| Secure Storage | EncryptedSharedPreferences |
| Async | Kotlin Coroutines, StateFlow |
| Testing | JUnit 4 |

### Backend API

| Component | Technology |
|-----------|-----------|
| Framework | ASP.NET Core 8 |
| ORM | Entity Framework Core |
| Database | PostgreSQL (hosted on Supabase) |
| Authentication | JWT Bearer tokens |
| Hashing | BCrypt.Net-Next |
| Documentation | Swagger (Swashbuckle) |

### CI/CD

- **GitHub Actions** — automated build and unit tests on every push
[![Android CI](https://github.com/st10202512/MzansiReady/actions/workflows/build.yml/badge.svg)](https://github.com/st10202512/MzansiReady/actions/workflows/build.yml)

---

## 🏗️ Architecture

The app follows a layered MVVM architecture:
┌────────────────────────────────────┐
│ UI Layer │
│ Activities with ViewBinding │
├────────────────────────────────────┤
│ ViewModel Layer │
│ StateFlow for reactive state │
├────────────────────────────────────┤
│ Repository Layer │
│ Coordinates API + local cache │
├────────────────────────────────────┤
│ Data Layer │
│ Retrofit (network) + Room (cache)│
└────────────────────────────────────┘

text

### Design Considerations

- **Offline-first**: Local cache is checked first; network requests happen when available
- **Encrypted sessions**: JWT tokens stored using Android Keystore-backed EncryptedSharedPreferences
- **Coroutines**: All I/O operations run on background dispatchers to keep the UI responsive
- **Reactive UI**: StateFlow drives UI updates — no manual refresh needed
- **Type-safe layouts**: ViewBinding eliminates `findViewById` and null-pointer risks
- **Testability**: Repository pattern makes unit testing straightforward
- **Localisation**: All user-facing strings use Android string resources for multi-language support

---
## 🎨 App Icon & Store Assets

### App Icon
![App Icon](screenshots/play-store/app_icon_512.png)

### Feature Graphic
![Feature Graphic](screenshots/play-store/feature_graphic.png)

### Play Store Screenshots
See the [screenshots/play-store](./screenshots/play-store) folder.

## 🎬 Demo Video

[▶️ Watch the final PoE demonstration on YouTube](https://youtu.be/3pmLt_MpE-4?si=zcrNW_YKXeAs9hvI)

The video demonstrates:
- User registration and login
- Password hashing (verified in Supabase)
- Google Sign-In (SSO)
- Live weather from Open-Meteo
- Multi-language switching (English ↔ isiZulu ↔ isiXhosa ↔ Afrikaans)
- Theme switching (Light ↔ Dark)
- Offline mode with cached weather + sync
- Community Feed with Firestore
- Profile picture upload
- Push notifications (FCM)
- Backend REST API (Swagger)
- Database contents in Supabase

## 📸 Screenshots

### Login Screen
![Login](screenshots/01-login.png)

### Register Screen
![Register](screenshots/03-register.png)

### Home Screen with Live Weather
![Home](screenshots/02-home-weather.png)

### Settings (English + Light Theme)
![Settings English Light](screenshots/04-settings-english-light.png)

### Home Screen (isiZulu + Dark Theme)
![Home isiZulu Dark](screenshots/05-home-isizulu-dark.png)

### Settings (isiZulu + Dark Theme)
![Settings isiZulu Dark](screenshots/06-settings-isizulu-dark.png)

### Supabase Alerts Table
![Supabase Alerts](screenshots/07-supabase-alerts.png)

### Supabase Users Table (Hashed Password)
![Supabase Users](screenshots/08-supabase-users.png)

### Swagger API Endpoints
![Swagger](screenshots/09-swagger.png)

---
## 📚 Full Documentation

Complete documentation for each phase is available in the [`screenshots/`](screenshots/) folder:

| Phase | Documentation | Screenshots |
|-------|---------------|-------------|
| **Phase 1** | [README](phase-1/README.md) | [Screenshots](phase-1/screenshots/) |
| **Phase 2** | [README](phase-2/README.md) | [Screenshots](phase-2/screenshots/) |
| **Phase 3** | [README](phase-3/README.md) | [Screenshots](phase-3/screenshots/) |


## 🚀 Release Notes — v1.0 (Part 3)

### What's New Since the Prototype

This is the final release of MzansiReady, updated from the Part 2 prototype.

#### New Features Added
- **Google Sign-In (SSO)**: One-tap authentication using Google accounts
- **Push Notifications (FCM)**: Real-time alerts via Firebase Cloud Messaging
- **Offline Sync**: Room database + WorkManager syncs data when connectivity returns
- **Community Feed (Firestore NoSQL)**: Users can post messages visible to all
- **Profile Picture Upload (Firestore)**: User profile pictures stored as Base64 in Firestore
- **Multi-language Support**: English and isiZulu with full translation
- **App Icon & Splash Screen**: Custom branding for Play Store
- **Play Store Preparation**: Signed AAB and Play Console assets ready

#### Bug Fixes & Improvements
- Fixed redirect URI handling for Google Sign-In on Huawei devices
- Improved error handling across all forms
- Added input validation to all user inputs
- Added logging throughout the codebase
- Improved UI consistency with Material 3

#### Known Limitations
- Play Console account pending (requires $25 fee) — signed AAB ready for upload
- Firebase Storage requires Blaze plan — profile pictures stored in Firestore instead

### Version History
- **v1.0** (Part 3 Final) — All features implemented, signed AAB ready
- **v0.9** (Part 2 Prototype) — Basic app with weather, settings, multi-language

## 🤖 AI Tools Disclosure

In accordance with the assignment policy on AI tool use, the following AI tools were used during this project:

- **Claude (Anthropic)** — Used for code review, debugging assistance (specifically resolving Google Sign-In configuration errors, ASP.NET Core API validation issues, and multi-language resource setup), and structuring this README. All AI-suggested code was reviewed, tested, and understood before inclusion.
- **Android Studio's built-in AI assistance** — For autocomplete and import suggestions only.

No AI tool was used to generate the app's core logic, architecture decisions, or documentation without active engagement and verification. Every feature was tested on a real Android device before inclusion in the demo video.

## 🚀 How to Run

### Prerequisites

- Android Studio (Koala or later)
- JDK 17
- Emulator or physical Android device (API 24+)
- The backend API running (see [MzansiReady-API](https://github.com/st10202512/MzansiReady-API))

### Steps

1. **Clone the repository**

   ```bash
   git clone https://github.com/st10202512/MzansiReady.git
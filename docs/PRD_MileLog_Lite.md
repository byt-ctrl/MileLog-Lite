# Product Requirements Document (PRD)

## MileLog Lite - Android Application


## 1. Project Overview

### 1.1 Purpose
MileLog Lite is a native Android application that helps a car owner log fuel fill-ups, automatically calculate mileage and cost efficiency, and view that data as simple visual charts. It replaces a manual notebook or spreadsheet with a small, focused, offline app.

### 1.2 Problem Statement
Most vehicle owners don't track fuel consumption consistently. Without a running log, it's hard to know actual mileage, cost per kilometer, or whether spending is trending up.

### 1.3 Goal
Build a lightweight, offline-first Android app that:
- Logs fuel fill-ups (date, liters, cost, odometer)
- Automatically calculates mileage (km/l) and cost per km
- Shows a dashboard with running totals and averages
- Visualizes mileage and spending trends with charts

### 1.4 Scope
This is a **mini academic project** scoped to 3–4 weeks. It intentionally covers a single vehicle only, with one core analytical feature (charts) added on top of basic CRUD. It is designed to be fully finishable and demo-ready, rather than broad.

### 1.5 Explicitly Out of Scope

> More features have been done in original full application , This is completely diff , in terms of UI wise

---

## 2. Target Audience

| Persona | Description |
|---|---|
| **Individual Car Owner** | A single-vehicle user who wants a simple way to track fuel spend and mileage. |
| **Course Evaluator** | Needs to see clean CRUD, calculated logic, local persistence, and a visual/data feature in a small, well-executed app. |

---

## 3. User Stories

### Core Logging
1. *As a user*, I want to add a fuel fill-up entry (date, liters, cost, odometer reading) so I can maintain a fuel history.
2. *As a user*, I want to edit or delete a past entry so I can correct mistakes.
3. *As a user*, I want to view a list of all my past fuel entries so I can review my history.

### Calculations & Dashboard
4. *As a user*, I want to see my average mileage (km/l) automatically calculated so I don't have to do manual math.
5. *As a user*, I want to see my cost per kilometer so I understand my true driving cost.
6. *As a user*, I want a dashboard showing total spend, total distance, and average mileage at a glance.

### Visualization
7. *As a user*, I want a line chart of my mileage over time so I can spot efficiency trends.
8. *As a user*, I want a bar chart of my monthly fuel spending so I can see cost trends.

### Data & Reliability
9. *As a user*, I want my data to be available even without internet, since I may fill fuel anywhere.
10. *As a user*, I want the app to prevent obviously invalid entries (e.g., a lower odometer reading than before).

---

## 4. Functional Requirements

### 4.1 Fuel Entry Logging
- FR1: User can add a fuel entry with: date, odometer reading, fuel quantity (liters), total cost.
- FR2: User can view, edit, and delete existing fuel entries.
- FR3: The app validates that a new odometer reading is greater than the previous one.
- FR4: The app validates that fuel quantity and cost are positive numbers.
- FR5: Required fields must be filled before an entry can be saved.

### 4.2 Mileage & Cost Calculation
- FR6: The app calculates mileage (km/l) between consecutive fill-ups: `distance / fuel_quantity`.
- FR7: The app calculates cost per km: `total_cost / total_distance`.
- FR8: The app maintains a running average mileage across all entries.
- FR9: The app maintains running totals: total distance, total fuel consumed, total cost.

### 4.3 Dashboard
- FR10: The dashboard displays: latest odometer reading, total fuel spend, average mileage, cost per km.
- FR11: The "add entry" action is reachable within 2 taps from the dashboard.

### 4.4 Charts
- FR12: The app displays a line chart of mileage (km/l) per fill-up over time.
- FR13: The app displays a bar chart of total fuel spend grouped by month.
- FR14: Charts update automatically when entries are added, edited, or deleted.

### 4.5 Data Persistence
- FR15: All data is stored locally (Room database) and is available fully offline.
- FR16: No data is lost on app crash or force-close; writes are atomic.

---

## 5. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | Dashboard and chart screens load within 2 seconds for up to ~500 log entries. |
| **Usability** | UI follows Material Design 3; add-entry action reachable within 2 taps. |
| **Reliability** | No data loss on crash/force-close; database writes are atomic. |
| **Offline Availability** | All features work without internet access. |
| **Security & Privacy** | No data leaves the device; no network permissions required. |
| **Maintainability** | Code follows MVVM architecture for testability and separation of concerns. |
| **Compatibility** | Minimum SDK: Android 8.0 (API 26); Target SDK: latest stable Android version. |
| **Accessibility** | Standard font scaling and content descriptions on core interactive elements. |

---

## 6. Recommended Tech Stack

| Layer | Choice | Why |
|---|---|---|
| **Language** | Kotlin | Standard for modern native Android; required by course conventions. |
| **UI Toolkit** | Jetpack Compose | Faster to build a small number of screens; less boilerplate than XML for a 3–4 week timeline; pairs cleanly with ViewModel/StateFlow. |
| **Architecture** | MVVM | Clean separation, easy to unit test calculation logic independent of UI. |
| **Local Database** | Room (SQLite) | Simple relational storage for one entity (`FuelEntry`); minimal setup. |
| **Charts** | MPAndroidChart | Well-documented, widely used in course projects, supports line + bar charts out of the box. |
| **Testing** | JUnit (calculation logic), basic Compose UI test for add-entry flow | Keeps testing scope proportional to project size. |

**Explicitly not needed for this scope:** DataStore, WorkManager, CameraX/FileProvider, Hilt, Google Maps SDK, FusedLocationProviderClient, Storage Access Framework. These can be mentioned in documentation as "future work" but should not be implemented.

---

## 7. Feature Roadmap (Single Phase - Mini Scope)

Because this is a small project, there is one build phase rather than a multi-phase roadmap:

- [ ] Add/edit/delete fuel entry (date, odometer, liters, cost)
- [ ] Fuel entry list view (history)
- [ ] Input validation (required fields, positive values, odometer monotonicity)
- [ ] Automatic mileage (km/l) calculation
- [ ] Automatic cost-per-km calculation
- [ ] Dashboard with running totals and averages
- [ ] Mileage trend line chart
- [ ] Monthly spend bar chart
- [ ] Local persistence with Room (fully offline)
- [ ] Basic unit tests for calculation logic

**Optional, only if time remains after the above is complete and tested:**
- [ ] Dark mode / theming
- [ ] Simple CSV export (no import, no backup)
- [ ] Basic maintenance reminder (single odometer-based reminder, no full reminder system)

---

## 8. Success Criteria

The mini project is considered successful if it demonstrates:
1. A fully functional offline CRUD system using Room for fuel entries.
2. Accurate, automated mileage and cost-per-km calculations.
3. A working dashboard with running totals.
4. At least two chart types (line + bar) reflecting real logged data.
5. Clean MVVM architecture with separated UI, ViewModel, and data layers.
6. A simple, polished UI following Material Design principles.

---

*End of Document*

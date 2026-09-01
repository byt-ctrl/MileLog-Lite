# Product Requirements Document (PRD)

## MileLog Lite - Android Application


## 1. Project Overview

### 1.1 Purpose
MileLog Lite is a native Android application that helps a car owner log fuel fill-ups, automatically calculate mileage and cost efficiency, categorize entries by fuel type, and view that data as simple visual charts. It replaces a manual notebook or spreadsheet with a small, focused, offline app.

### 1.2 Problem Statement
Most vehicle owners don't track fuel consumption consistently. Without a running log, it's hard to know actual mileage, cost per kilometer, or whether spending is trending up. Multi-fuel vehicles (Petrol/Diesel/CNG) lack a way to compare efficiency across fuel types.

### 1.3 Goal
Build a lightweight, offline-first Android app that:
- Logs fuel fill-ups (date, liters, cost, odometer, fuel category)
- Automatically calculates mileage (km/l) and cost per km
- Shows a dashboard with running totals and averages
- Allows categorizing entries by fuel type (Petrol, Diesel, CNG)
- Filters history and charts by fuel category
- Visualizes mileage and spending trends with charts (combined and per-category)

### 1.4 Scope
This is a **mini academic project** scoped to 5–6 weeks. It intentionally covers a single vehicle only, with fuel category selection and visual analytics (charts) added on top of basic CRUD. It is designed to be fully finishable and demo-ready, rather than broad.

### 1.5 Explicitly Out of Scope

- Multi-vehicle profiles
- Cloud synchronization or account login
- Camera, maps, or location services
- DataStore, WorkManager, Hilt dependency injection
- CSV import (export only)
- Push notifications or reminders beyond single odometer reminder
- Network permissions

> More features have been done in original full application, this is completely different in terms of UI and scope.

---

## 2. Target Audience

| Persona | Description |
|---|---|
| **Individual Car Owner** | A single-vehicle user who wants a simple way to track fuel spend, mileage, and compare efficiency across fuel types. |
| **Course Evaluator** | Needs to see clean CRUD, calculated logic, category filtering, local persistence, and a visual/data feature in a small, well-executed app. |

---

## 3. User Stories

### Core Logging
1. *As a user*, I want to add a fuel fill-up entry (date, liters, cost, odometer reading, fuel category) so I can maintain a fuel history.
2. *As a user*, I want to edit or delete a past entry so I can correct mistakes.
3. *As a user*, I want to view a list of all my past fuel entries so I can review my history.

### Fuel Category
4. *As a user*, I want to categorize each fill-up by fuel type (Petrol, Diesel, CNG) so I can track efficiency per fuel type.
5. *As a user*, I want to filter my history and charts by fuel category so I can compare performance across fuel types.

### Calculations & Dashboard
6. *As a user*, I want to see my average mileage (km/l) automatically calculated so I don't have to do manual math.
7. *As a user*, I want to see my cost per kilometer so I understand my true driving cost.
8. *As a user*, I want a dashboard showing total spend, total distance, average mileage, and latest fuel category at a glance.

### Visualization
9. *As a user*, I want a line chart of my mileage over time so I can spot efficiency trends.
10. *As a user*, I want a bar chart of my monthly fuel spending so I can see cost trends.
11. *As a user*, I want to see per-category trend overlays on charts so I can compare fuel type performance.

### Data & Reliability
12. *As a user*, I want my data to be available even without internet, since I may fill fuel anywhere.
13. *As a user*, I want the app to prevent obviously invalid entries (e.g., a lower odometer reading than before).
14. *As a user*, I want to export my fuel history to a CSV file (including fuel category) for record-keeping.
15. *As a user*, I want to undo a deletion if I accidentally remove an entry.

---

## 4. Functional Requirements

### 4.1 Fuel Entry Logging
- FR1: User can add a fuel entry with: date, odometer reading, fuel quantity (liters), total cost, fuel category (Petrol/Diesel/CNG).
- FR2: User can view, edit, and delete existing fuel entries.
- FR3: The app validates that a new odometer reading is greater than the previous one.
- FR4: The app validates that fuel quantity and cost are positive numbers.
- FR5: Required fields must be filled before an entry can be saved.
- FR6: Fuel category defaults to Petrol if not specified.

### 4.2 Fuel Category Management
- FR7: User can select a fuel category (Petrol, Diesel, CNG) from a dropdown when adding or editing an entry.
- FR8: User can filter the history list by fuel category using filter chips (All / Petrol / Diesel / CNG).
- FR9: Dashboard displays the latest entry's fuel category as a subtitle on the odometer card.
- FR10: Charts support per-category overlay lines (dashed) on the mileage trend chart.
- FR11: Charts support grouped multi-series bars (per category) on the monthly spend chart.

### 4.3 Mileage & Cost Calculation
- FR12: The app calculates mileage (km/l) between consecutive fill-ups: `distance / fuel_quantity`.
- FR13: The app calculates cost per km: `total_cost / total_distance`.
- FR14: The app maintains a running average mileage across all entries.
- FR15: The app maintains running totals: total distance, total fuel consumed, total cost.
- FR16: Per-category mileage averages and monthly spend are computed independently.

### 4.4 Dashboard
- FR17: The dashboard displays: latest odometer reading (with fuel category subtitle), total fuel spend, average mileage, cost per km.
- FR18: The "add entry" action is reachable within 2 taps from the dashboard.

### 4.5 Charts
- FR19: The app displays a line chart of mileage (km/l) per fill-up over time with per-category overlay support.
- FR20: The app displays a bar chart of total fuel spend grouped by month with per-category grouping support.
- FR21: Charts update automatically when entries are added, edited, or deleted.

### 4.6 History
- FR22: History displays entries in reverse-chronological order (most recent first).
- FR23: Each history card shows date, odometer, liters, cost, and computed mileage badge.
- FR24: Tapping a card opens the entry in edit mode.
- FR25: Delete requires confirmation dialog; deleted entries can be undone via snackbar.

### 4.7 Data Persistence
- FR26: All data is stored locally (Room database) and is available fully offline.
- FR27: No data is lost on app crash or force-close; writes are atomic.
- FR28: Database includes indices on `date`, `odometer`, composite `(date, odometer)`, and `fuelCategory` for query performance.

### 4.8 CSV Export
- FR29: User can export the full fuel history to a CSV file via the system document picker (SAF).
- FR30: CSV includes columns: `id, date, odometer, liters, cost, fuelCategory`.
- FR31: Export is one-way (no import or backup/restore).

---

## 5. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | Dashboard and chart screens load within 2 seconds for up to ~500 log entries. DAO benchmarks: 5000 entries bulk insert, getAll <500ms, getLatest 20x <100ms. |
| **Usability** | UI follows Material Design 3 with Kinetic Logic brand palette; add-entry action reachable within 2 taps; category filter chips for quick filtering. |
| **Reliability** | No data loss on crash/force-close; database writes are atomic; destructive migration for schema changes. |
| **Offline Availability** | All features work without internet access. |
| **Security & Privacy** | No data leaves the device; no network permissions required. |
| **Maintainability** | Code follows MVVM + Repository architecture with pure-Kotlin domain layer for testability and separation of concerns. |
| **Compatibility** | Minimum SDK: Android 8.0 (API 26); Target SDK: API 36; Compile SDK: API 37. |
| **Accessibility** | Font scaling tested up to 200% without clipping; content descriptions on core interactive elements; 48dp minimum touch targets. |
| **Testing** | 80 tests across 11 files: 42 unit tests (validator, calculator, CSV exporter, category) + 38 instrumented tests (DAO CRUD, category filtering, benchmarks, full regression). |

---

## 6. Tech Stack

| Layer | Choice | Version | Why |
|---|---|---|---|
| **Language** | Kotlin | 2.2.10 | Standard for modern native Android; required by course conventions. |
| **UI Toolkit** | Jetpack Compose + Material 3 | BOM 2026.02.01 | Declarative UI, less boilerplate, pairs cleanly with ViewModel/StateFlow. |
| **Icons** | Material Icons Extended | (BOM-managed) | Full icon set for Dashboard, History, Charts, Settings navigation. |
| **Architecture** | MVVM + Repository | - | Clean separation, unidirectional data flow, easy to unit test. |
| **Local Database** | Room (SQLite) | 2.8.4 | Indexed storage for one entity with category field; minimal setup. |
| **Navigation** | Navigation Compose | 2.9.8 | Type-safe routes for 5 screens. |
| **Charts** | MPAndroidChart | v3.1.0 | Mature line + bar charts with View interop via `AndroidView`. |
| **Testing** | JUnit 4, Turbine, Room in-memory | 4.13.2 | Keeps testing proportional to project size. |
| **Build** | AGP 9.2.1, KSP 2.3.11 | Gradle 9.4.1 | Room annotation processing via KSP. |

**Explicitly not needed for this scope:** DataStore, WorkManager, CameraX/FileProvider, Hilt, Google Maps SDK, FusedLocationProviderClient.

---

## 7. Feature Roadmap

### Completed Sprints

#### Sprint 1: Foundation, Data Layer & Fuel CRUD
- [x] Initialize Android project with Kotlin and Jetpack Compose
- [x] Add dependencies: Room, Compose Navigation, Lifecycle ViewModel, JUnit
- [x] Set up MVVM package structure (`data`, `domain`, `ui`)
- [x] Create `FuelEntry` Room entity with indices
- [x] Create `FuelEntryDao` with CRUD operations
- [x] Create Room Database and repository layer
- [x] Build Add/Edit Fuel Entry screen with form UI
- [x] Build Delete Fuel Entry with confirmation dialog
- [x] Build Fuel History screen (reverse-chronological)

#### Sprint 2: Calculations, Dashboard & Validation
- [x] Implement mileage calculation (per-fillup, average, running totals)
- [x] Implement cost-per-km calculation
- [x] Add input validation (required fields, positive values, odometer monotonicity)
- [x] Build Dashboard screen with 2x2 metric cards
- [x] Wire dashboard to live data (auto-update on CRUD)

#### Sprint 3: Charts & Visualization
- [x] Add MPAndroidChart dependency
- [x] Build mileage trend line chart (chronological)
- [x] Build monthly fuel spend bar chart (grouped by month)
- [x] Handle empty/single-entry states gracefully

#### Sprint 4: Polish, Testing & Release Readiness
- [x] Add empty states, loading/error states
- [x] Review spacing, typography, Material 3 consistency
- [x] Add content descriptions and font scaling tests
- [x] Write project documentation (setup, features, architecture, screenshots)
- [x] Implement CSV export via system document picker (stretch goal)

#### Sprint 5: Fuel Category Selection
- [x] Create `FuelCategory` enum (Petrol, Diesel, CNG)
- [x] Add `fuelCategory` field to `FuelEntry` entity
- [x] Update DAO queries for category filtering
- [x] Build fuel category dropdown in Add/Edit form
- [x] Add category filter chips on History screen
- [x] Update Dashboard to show latest category
- [x] Update Charts with per-category overlays
- [x] Update CSV export to include category column
- [x] Unit tests for category enum and filtering logic
- [x] Instrumented tests for DAO queries with categories

### Planned Sprints

#### Sprint 6: Settings, Design System Migration & Bottom Navigation
- [ ] Settings screen (Theme toggle, Distance Units, Export Logs, Clear Data, About)
- [ ] Kinetic Logic design system migration (Inter font, 8px spacing, tonal elevation)
- [ ] 5-tab bottom navigation bar (Dashboard, History, Add, Reports, Settings)

---

## 8. Success Criteria

The project is considered successful if it demonstrates:
1. A fully functional offline CRUD system using Room for fuel entries with category support.
2. Accurate, automated mileage and cost-per-km calculations with per-category analytics.
3. A working dashboard with running totals and latest fuel category.
4. At least two chart types (line + bar) with per-category overlay support.
5. Category filtering across history and charts.
6. CSV export including fuel category column.
7. Clean MVVM + Repository architecture with separated UI, ViewModel, Domain, and data layers.
8. 80+ tests covering validator, calculator, CSV exporter, category logic, DAO operations, and full regression.
9. A polished UI following Material Design 3 with Kinetic Logic brand palette.

---

*End of Document*

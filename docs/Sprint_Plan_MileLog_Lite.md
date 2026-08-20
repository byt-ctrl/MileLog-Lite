# MileLog Lite - Sprint Execution Plan & Checklist

This document provides an interactive execution checklist for the mini-scope MileLog Android application. It mirrors the structure of the full MileLog sprint plan but is reduced to a **single-vehicle fuel logger with calculations and charts**, sized for a 3–4 week build.

---

### Quick Sprint Navigation
> - [Sprint 1: Foundation, Data Layer & Fuel CRUD](#sprint-1-foundation-data-layer--fuel-crud)
> - [Sprint 2: Calculations, Dashboard & Validation](#sprint-2-calculations-dashboard--validation)
> - [Sprint 3: Charts & Visualization](#sprint-3-charts--visualization)
> - [Sprint 4: Polish, Testing & Release Readiness](#sprint-4-polish-testing--release-readiness)

---

## Sprint 1: Foundation, Data Layer & Fuel CRUD

**Timeline:** Week 1

**Primary Goal:** Set up the project skeleton and get fuel entries persisting locally with full CRUD.

### 1.1 Key Deliverables
- [x] Initialize Android project with Kotlin and Jetpack Compose.
- [ ] Add dependencies: Room, Compose Navigation, Lifecycle ViewModel, JUnit.
- [ ] Set up MVVM package structure (`data`, `domain`, `ui`).
- [x] Define base Material Design 3 theme (colors, typography).
- [ ] Create `FuelEntry` Room entity (date, odometer, liters, cost, id).
- [ ] Create `FuelEntryDao` with insert, update, delete, and get-all queries.
- [ ] Create Room `Database` class and repository layer (`FuelEntryRepository`).
- [ ] Build Add Fuel Entry screen (form UI).
- [ ] Build Edit Fuel Entry flow (reuse Add screen or dedicated screen).
- [ ] Build Delete Fuel Entry (with confirmation dialog).
- [ ] Build Fuel History screen listing all entries, most recent first.

### 1.2 Sprint 1 Milestone
- [ ] User can add, edit, delete, and view a list of fuel entries. Data persists after app restart, fully offline.

### 1.3 Sprint 1 Testing
- [ ] DAO tests for insert, update, delete, and fetch-all operations.
- [ ] Manual test: add several entries, force-close app, reopen, confirm data persists.

---

## Sprint 2: Calculations, Dashboard & Validation

**Timeline:** Week 2

**Primary Goal:** Add the calculation engine, input validation, and the dashboard summary screen.

### 2.1 Key Deliverables
- [ ] Implement mileage calculation: `(current_odometer - previous_odometer) / fuel_quantity`.
- [ ] Implement cost-per-km calculation: `total_cost / total_distance`.
- [ ] Implement running average mileage across all entries.
- [ ] Implement running totals: total distance, total fuel, total cost.
- [ ] Add validation: required fields, positive fuel quantity, positive cost.
- [ ] Add validation: new odometer reading must be greater than the previous one.
- [ ] Show inline error messages on the Add/Edit form for invalid input.
- [ ] Build Dashboard screen UI:
    - [ ] Latest odometer reading
    - [ ] Total fuel spend
    - [ ] Average mileage (km/l)
    - [ ] Cost per km
- [ ] Add a quick-add fuel entry button on the dashboard (reachable within 2 taps).
- [ ] Wire dashboard values to live data (auto-update when entries change).

### 2.2 Sprint 2 Milestone
- [ ] Dashboard shows accurate, auto-updating totals and averages. Invalid entries (bad odometer, negative values, missing fields) are blocked with clear error messages.

### 2.3 Sprint 2 Testing
- [ ] Unit tests for mileage calculation.
- [ ] Unit tests for cost-per-km calculation.
- [ ] Unit tests for running average mileage and running totals.
- [ ] Unit tests for odometer validation (rejects non-increasing values).
- [ ] Manual test: add/edit/delete entries and confirm dashboard updates correctly each time.

---

## Sprint 3: Charts & Visualization

**Timeline:** Week 3

**Primary Goal:** Add the visual analytics layer - the one standout feature beyond basic CRUD.

### 3.1 Key Deliverables
- [ ] Add MPAndroidChart dependency.
- [ ] Build mileage trend line chart (km/l per fill-up, chronological).
- [ ] Build monthly fuel spend bar chart (grouped by calendar month).
- [ ] Add a Charts/Insights screen (or section on the dashboard) hosting both charts.
- [ ] Handle empty state (no chart shown / friendly message when there are fewer than 2 entries).
- [ ] Ensure charts refresh automatically when entries are added, edited, or deleted.

### 3.2 Sprint 3 Milestone
- [ ] Charts screen accurately reflects logged data and updates live as entries change. Empty and single-entry states are handled gracefully (no crashes).

### 3.3 Sprint 3 Testing
- [ ] Unit tests for the data-grouping logic that feeds the monthly spend chart (grouping by month, summing cost).
- [ ] Manual visual check: add entries across 3+ months and confirm chart bars/line points match expected values.
- [ ] Manual test: fresh install (zero entries) does not crash the Charts screen.

---

## Sprint 4: Polish, Testing & Release Readiness

**Timeline:** Week 4

**Primary Goal:** Stabilize, polish, and prepare the app for submission/demo.

### 4.1 Key Deliverables
- [ ] Add empty states (no entries yet) to History and Dashboard screens.
- [ ] Add loading/error states where relevant.
- [ ] Review spacing, typography, and Material 3 consistency across all screens.
- [ ] Add content descriptions to key interactive elements (buttons, icons).
- [ ] Confirm font scaling doesn't break layouts on any screen.
- [ ] Add a database index on the date/odometer field if entry counts are large in testing.
- [ ] Write short project documentation:
    - [ ] Setup instructions
    - [ ] Feature list
    - [ ] Architecture overview (MVVM diagram or description)
    - [ ] Screenshots of core screens
    - [ ] Known limitations (e.g., single vehicle only, no export)
- [ ] **Only if the above is done with time remaining**, consider one optional stretch item (see PRD §7): dark mode, simple CSV export, or a single basic reminder. Do not start a stretch item if core polish/testing is incomplete.

### 4.2 Sprint 4 Milestone
- [ ] MileLog Lite is stable, demo-ready, documented, and satisfies all success criteria in the PRD (§8).

### 4.3 Sprint 4 Testing
- [ ] Full regression pass: add entry → edit entry → delete entry → dashboard reflects change → charts reflect change.
- [ ] Test with a larger seeded dataset (~100–500 entries) to confirm dashboard/chart load stays under 2 seconds.
- [ ] Manual accessibility check: font scaling and screen-reader labels on core screens.
- [ ] Final manual test on emulator or physical device, fresh install through full flow.

---

## Deferred / Not in Mini Scope

> More features have been done in original full application , This is completely diff , in terms of UI wise

*End of Document*

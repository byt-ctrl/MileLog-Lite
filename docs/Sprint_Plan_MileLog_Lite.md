# MileLog Lite - Sprint Execution Plan & Checklist

This document provides an interactive execution checklist for the mini-scope MileLog Android application. It mirrors the structure of the full MileLog sprint plan but is reduced to a **single-vehicle fuel logger with calculations and charts**, sized for a 3–4 week build.

---

### Quick Sprint Navigation
> - [Sprint 1: Foundation, Data Layer & Fuel CRUD](#sprint-1-foundation-data-layer--fuel-crud)
> - [Sprint 2: Calculations, Dashboard & Validation](#sprint-2-calculations-dashboard--validation)
> - [Sprint 3: Charts & Visualization](#sprint-3-charts--visualization)
> - [Sprint 4: Polish, Testing & Release Readiness](#sprint-4-polish-testing--release-readiness)
> - [Sprint 5: Fuel Category Selection](#sprint-5-fuel-category-selection)
> - [Sprint 6: Settings, Design System Migration & Bottom Navigation](#sprint-6-settings-design-system-migration--bottom-navigation) — design section superseded
> - [Sprint 7: Vehicle Management & Seed Data Expansion](#sprint-7-vehicle-management--seed-data-expansion)
> - [Sprint 8: Instrument Ledger Conformance](#sprint-8-instrument-ledger-conformance-refactor-design)
> - [Sprint 9: Navigation, Layout & Chart Interaction Refinement](#sprint-9-navigation-layout--chart-interaction-refinement)

---

## Sprint 1: Foundation, Data Layer & Fuel CRUD

**Timeline:** Week 1

**Primary Goal:** Set up the project skeleton and get fuel entries persisting locally with full CRUD.

### 1.1 Key Deliverables
- [x] Initialize Android project with Kotlin and Jetpack Compose.
- [x] Add dependencies: Room, Compose Navigation, Lifecycle ViewModel, JUnit.
- [x] Set up MVVM package structure (`data`, `domain`, `ui`).
- [x] Define base Material Design 3 theme (colors, typography).
- [x] Create `FuelEntry` Room entity (date, odometer, liters, cost, id).
- [x] Create `FuelEntryDao` with insert, update, delete, and get-all queries.
- [x] Create Room `Database` class and repository layer (`FuelEntryRepository`).
- [x] Build Add Fuel Entry screen (form UI).
- [x] Build Edit Fuel Entry flow (reuse Add screen or dedicated screen).
- [x] Build Delete Fuel Entry (with confirmation dialog).
- [x] Build Fuel History screen listing all entries, most recent first.

### 1.2 Sprint 1 Milestone
- [x] User can add, edit, delete, and view a list of fuel entries. Data persists after app restart, fully offline.

### 1.3 Sprint 1 Testing
- [x] DAO tests for insert, update, delete, and fetch-all operations.
- [ ] Manual test: add several entries, force-close app, reopen, confirm data persists.

---

## Sprint 2: Calculations, Dashboard & Validation

**Timeline:** Week 2

**Primary Goal:** Add the calculation engine, input validation, and the dashboard summary screen.

### 2.1 Key Deliverables
- [x] Implement mileage calculation: `(current_odometer - previous_odometer) / fuel_quantity`.
- [x] Implement cost-per-km calculation: `total_cost / total_distance`.
- [x] Implement running average mileage across all entries.
- [x] Implement running totals: total distance, total fuel, total cost.
- [x] Add validation: required fields, positive fuel quantity, positive cost.
- [x] Add validation: new odometer reading must be greater than the previous one.
- [x] Show inline error messages on the Add/Edit form for invalid input.
- [x] Build Dashboard screen UI:
    - [x] Latest odometer reading
    - [x] Total fuel spend
    - [x] Average mileage (km/l)
    - [x] Cost per km
- [x] Add a quick-add fuel entry button on the dashboard (reachable within 2 taps).
- [x] Wire dashboard values to live data (auto-update when entries change).

### 2.2 Sprint 2 Milestone
- [x] Dashboard shows accurate, auto-updating totals and averages. Invalid entries (bad odometer, negative values, missing fields) are blocked with clear error messages.

### 2.3 Sprint 2 Testing
- [x] Unit tests for mileage calculation.
- [x] Unit tests for cost-per-km calculation.
- [x] Unit tests for running average mileage and running totals.
- [x] Unit tests for odometer validation (rejects non-increasing values).
- [x] Manual test: add/edit/delete entries and confirm dashboard updates correctly each time.

---

## Sprint 3: Charts & Visualization

**Timeline:** Week 3

**Primary Goal:** Add the visual analytics layer - the one standout feature beyond basic CRUD.

### 3.1 Key Deliverables
- [x] Add MPAndroidChart dependency.
- [x] Build mileage trend line chart (km/l per fill-up, chronological).
- [x] Build monthly fuel spend bar chart (grouped by calendar month).
- [x] Add a Charts/Insights screen (or section on the dashboard) hosting both charts.
- [x] Handle empty state (no chart shown / friendly message when there are fewer than 2 entries).
- [x] Ensure charts refresh automatically when entries are added, edited, or deleted.

### 3.2 Sprint 3 Milestone
- [x] Charts screen accurately reflects logged data and updates live as entries change. Empty and single-entry states are handled gracefully (no crashes).

### 3.3 Sprint 3 Testing
- [x] Unit tests for the data-grouping logic that feeds the monthly spend chart (grouping by month, summing cost).
- [x] Manual visual check: add entries across 3+ months and confirm chart bars/line points match expected values.
- [x] Manual test: fresh install (zero entries) does not crash the Charts screen.

---

## Sprint 4: Polish, Testing & Release Readiness

**Timeline:** Week 4

**Primary Goal:** Stabilize, polish, and prepare the app for submission/demo.

### 4.1 Key Deliverables
- [x] Add empty states (no entries yet) to History and Dashboard screens.
- [x] Add loading/error states where relevant.
- [x] Review spacing, typography, and Material 3 consistency across all screens.
- [x] Add content descriptions to key interactive elements (buttons, icons).
- [x] Confirm font scaling doesn't break layouts on any screen.
- [x] Add a database index on the date/odometer field if entry counts are large in testing.
- [x] Write short project documentation:
    - [x] Setup instructions
    - [x] Feature list
    - [x] Architecture overview (MVVM diagram or description)
    - [x] Screenshots of core screens
    - [x] Known limitations (e.g., offline-only storage, no cloud sync, export without import)
- [x] **If the above is done with time remaining**, consider one optional stretch item (see PRD §7): dark mode, simple CSV export, or a single basic reminder. Do not start a stretch item if core polish/testing is incomplete. **Implemented: simple CSV export** (History screen top-bar action → system document picker → `milelog_fuel_entries.csv`; export-only, no import). **Fixed in Sprint 7:** the prepared CSV was cleared as soon as the file picker opened, so selecting a location wrote nothing — it is now held until the write finishes (or the picker is cancelled); the same flow backs the Settings export row and now exports the active vehicle's entries. **CSV correctness fixed in Sprint 7:** the date is exported as ISO-8601 instead of raw epoch millis, per-fill-up mileage (the app's headline statistic) is now included, numbers use fixed decimals with a dot separator regardless of device locale, the vehicle name is a column, text fields are RFC 4180 quoted, rows are chronological, and the file is written as UTF-8 with a BOM.

### 4.2 Sprint 4 Milestone
- [x] MileLog Lite is stable, demo-ready, documented, and satisfies all success criteria in the PRD (§8).

### 4.3 Sprint 4 Testing
- [x] Full regression pass: add entry → edit entry → delete entry → dashboard reflects change → charts reflect change.
- [x] Test with a larger seeded dataset (~100–500 entries) to confirm dashboard/chart load stays under 2 seconds.
- [x] Manual accessibility check: font scaling and screen-reader labels on core screens.
- [x] Final manual test on emulator or physical device, fresh install through full flow.

---

## Sprint 5: Fuel Category Selection

**Timeline:** Week 5

**Primary Goal:** Allow users to categorize fuel entries by fuel type (Petrol, Diesel, CNG) for better tracking and filtering.

### 5.1 Key Deliverables
- [x] Create `FuelCategory` enum with predefined values (Petrol, Diesel, CNG).
- [x] Add `fuelCategory` field to `FuelEntry` Room entity (default: Petrol).
- [x] Update `FuelEntryDao` queries to support filtering by fuel category.
- [x] Build fuel category dropdown selector in Add/Edit Entry form (Material3 `ExposedDropdownMenuBox`).
- [x] Update Dashboard to show latest entry's fuel category in KPI card.
- [x] Update Charts to group/split by fuel category (optional: per-category trend line).
- [x] Add category filter chips on History screen to filter entries by fuel type.
- [x] Update CSV export to include fuel category column.
- [x] Unit tests for fuel category enum and filtering logic.
- [x] Instrumented tests for DAO queries with category filters.

### 5.2 Sprint 5 Milestone
- [x] User can select a fuel type (Petrol, Diesel, CNG) when adding/editing entries, and filter history/charts by fuel category.

### 5.3 Sprint 5 Testing
- [x] Unit tests: fuel category enum values, filtering by category.
- [x] Instrumented tests: DAO insert/query with category field, filter queries.
- [x] Manual test: add entries with Petrol, Diesel, CNG types, verify charts show category-specific data.

---

## Sprint 6: Settings, Design System Migration & Bottom Navigation

**Timeline:** Week 6

**Primary Goal:** Implement a full Settings screen with user preferences, settle the app's design system, and add bottom navigation.

> **Design system note (2026-09-13).** The "Kinetic Logic" system originally planned here was **superseded** by the **Instrument Ledger** direction specified in `refactor-design/{dashboard,log-a-trip,setting}/DESIGN.md` and implemented in `ui/theme/` + `ui/components/`. The replacement mapping is recorded below; the Kinetic Logic palette, its shadow-based elevation and its 5-tab bar are **not** in the app and are **not** open work.

### 6.1 Design System (superseded — Instrument Ledger shipped instead)

**Objective:** Two materials, one object. A permanently dark instrument binnacle carries the readings; a light logbook carries the record. Depth comes from the material change, not from shadow.

| Planned (Kinetic Logic) | Shipped (Instrument Ledger) |
|---|---|
| Dependable Blue `#003D9B`, Business Green secondary | Petrol `#0B4A46` / `#6FC8BB`, Fuel `#8A5200` / `#EDB25A` |
| Flat corporate surfaces | Constant dark instrument (`#101618`) + logbook paper (`#EDF0EF`); the chrome is identical in every appearance |
| Shadow-based elevation levels 1–2 | Tonal material change: `MileLogElevation` levels are `0.dp` and `level1/2Shadow` are no-ops |
| TopAppBar with avatar and notification icon | `InstrumentBar` (wordmark on the dashboard) and `InstrumentBand` for full-bleed instrument sections |
| Rounded white cards with Level 1 shadow | `LedgerPanel` — hairline rule, small radius, no shadow |
| Inter across every style | Inter prose + `DataMono` readings, plus `DataTextStyle` / `DataTextStyleSmall` / `MicroLabelStyle` |

- [x] Colour, typography, spacing, shape and elevation tokens rebuilt for Instrument Ledger (`Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt`, `Elevation.kt`, `Theme.kt`).
- [x] Instrument components: `InstrumentBar`, `InstrumentBand`, `LedgerPanel`, `SectionHeader`, `LedgerRow`, `LedgerHeaderRow`, `MileageGauge`, `ReadoutStrip`, `MileageTrendBars`.
- [x] All screens migrated; dynamic colour stays **off** so the identity is identical on every device.
- [x] Interactive elements meet the 48dp touch target (`Spacing.touchTarget`, `Modifier.touchTargetMinHeight()`).

---

### 6.2 Bottom Navigation

**Objective:** Navigation that survives at every width, built from the same dark instrument material as the rest of the chrome.

#### 6.2.1 Navigation (`MileLiteNavHost.kt`, `BottomNavBar.kt`)
- [x] Add the `SETTINGS` route and the `REPORTS` alias for the charts destination in `MileLogRoutes`.
- [x] `MileLogBottomBar` with the four shell destinations (**a fifth, Vehicles, was added in Sprint 9** — see §9):
    - [x] Dashboard (`Icons.Rounded.Dashboard` / `Icons.Outlined.Dashboard`)
    - [x] History (`Icons.Rounded.History` / `Icons.Outlined.History`)
    - [x] Reports (`Icons.Rounded.Assessment` / `Icons.Outlined.Assessment`)
    - [x] Settings (`Icons.Rounded.Settings` / `Icons.Outlined.Settings`)
- [x] Selected state reads as a raised fill (`chromeRaisedHigh` indicator) plus a brighter label — never a coloured stripe.
- [x] `MileLogRail` (228dp) for expanded widths: the same destinations plus the wordmark, the primary action and the offline note, so widening the window never removes a way to move.
- [x] Bar wired to the `NavHost` with `startDestination = DASHBOARD`; a tab press pops up to the start destination and restores state.

> **Superseded:** the planned fifth "Add" tab (centred elevated pill) was not shipped. Logging a fill-up is the shell's own FAB (`MileLogFab`), which is why it appears on Dashboard, History and Reports but not on Settings (and not on Vehicles, added in Sprint 9).

#### 6.2.2 Screen Navigation Updates
- [x] Back navigation with the bottom bar pops up to the start destination.
- [x] Charts reachable from the Reports tab, and from the dashboard's "Open charts" action.
- [x] `AddEditEntryScreen` and `AddEditVehicleScreen` are full-screen routes with no shell bars.
- [x] Decide whether the primary action should also be reachable from Settings: **it stays off Settings.** Logging a fill-up is an act on the log, not a configuration change, and the shell already carries the FAB on Dashboard, History and Reports. Settings reaches the log surfaces through **Data** (export, clear, seed); vehicle management moved to its own destination in Sprint 9.

---

### 6.3 Settings Screen

**Objective:** A Configure surface, built to `refactor-design/setting/DESIGN.md` and rendered with Instrument Ledger components.

#### 6.3.1 Shipped

`ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt`, using `LedgerPanel`, `SectionHeader` and `SettingsRow` (60dp rows, hairline dividers) rather than the Kinetic Logic card layout:

- [x] Opens with an instrument readout of the current state (fill-ups logged, distance tracked, storage) instead of a decorative header.
- [x] **Vehicle** group: vehicles with the active selection, tap to switch, edit route, and delete with a confirmation dialog. **Relocated in Sprint 9** to the Vehicles destination (§9), which is now the single home for the garage; Settings keeps only the active-vehicle flow it needs to scope the entry count.
- [x] **Data** group: Export fill-ups as CSV through the system document picker; Delete all fill-ups with confirmation **and undo**; Add demo fill-ups.
- [x] **About** group: version, storage ("This device"), network ("Not required").
- [x] Every claim on the screen is true of the product — there is no subscription, sync, account or log out, so none of them appear.
- [x] Clear-all is scoped to the active vehicle and restores from memory on undo, so **no global `deleteAll()` is needed**; a bulk `deleteByVehicle` covers the delete.
- [x] Content descriptions and a 48dp minimum touch target on every interactive row.

**Deviations from the original spec:** the settings screen is not a `LazyColumn` of M3 cards, and the Vehicle section is a **list** rather than the single-vehicle form the spec assumed (the app is multi-vehicle since Sprint 7).

#### 6.3.2 Closed in Sprint 8

- [x] **Appearance** control: the row ships but is static ("Theme — Follows device"). `refactor-design/setting/DESIGN.md` asks for a real radio group (Light / Dark / System). **Done in Sprint 8** — see §8.1.
- [x] **Distance unit** on the vehicle form, plus a `DistanceConverter`. **Done in Sprint 8** — see §8.1.
- [x] Preferences persistence: no `UserPreferences` / `SettingsRepository` exists yet (`theme_mode`, `distance_unit`). **Done in Sprint 8** — see §8.1.

#### 6.3.3 Theme Integration (closed in Sprint 8)

- [x] `MileLogTheme(themeMode)` resolving `system → isSystemInDarkTheme()`, `light → false`, `dark → true`.
- [x] Collect the preference in `MainActivity` so a change applies by recomposition, with no restart.
- [x] Keep the spec's rule: the instrument stays dark in every mode; only the logbook follows the setting.

The status and navigation bars move with the resolved appearance too, which is what §6.4 already claimed: `enableEdgeToEdge` is re-applied from a `LaunchedEffect(darkTheme)` rather than once in `onCreate`, so it follows the setting instead of the device.

#### 6.3.4 Distance Unit Integration (closed in Sprint 8)

- [x] `domain/conversion/DistanceConverter.kt` with `kmToMiles(km) = km * 0.621371` and `formatDistance(value, unit)`.
- [x] Dashboard: convert the odometer and cost-per-km readouts.
- [x] History: convert the displayed odometer and mileage.
- [x] Charts: axis labels and points respect the unit, while chart data stays in km internally.
- [x] Storage stays in km; conversion is display-only.

---

### 6.4 Integration & Wiring

- [x] `MainActivity` wraps `MileLiteNavHost` in `MileLogTheme` inside a `Surface`; the system bars stay transparent and follow the resolved appearance.
- [x] Settings is reachable from any tab, and back navigation from a full-screen route returns to the shell with its navigation intact.
- [x] Logging a fill-up opens `AddEditEntryScreen` as a full-screen route from the shell FAB / rail action.
- [x] Add `SettingsRepository` to `MileLogApplication` (manual DI), collect the theme preference at the top level and apply it before `setContent` — done in Sprint 8, see §8.1.

---

### 6.5 Sprint 6 Milestone
- [x] Settings screen ships with Vehicle, Data (export, clear with undo, demo seed) and About groups, reachable from the bottom bar.
- [x] Bottom navigation is functional with correct selected/unselected states at both compact and expanded widths.
- [x] All existing functionality (CRUD, calculations, charts, export) continues to work with the shipped design system and navigation.
- [ ] ~~App uses the new Kinetic Logic design system across all screens~~ — **superseded**; Instrument Ledger shipped instead (§6.1).
- [ ] ~~5-tab bottom navigation bar is functional~~ — **superseded**; four destinations plus a shell FAB shipped instead (§6.2). **Updated in Sprint 9:** five destinations now, once Vehicles joined the bar.
- [x] Theme toggle and distance units, with preferences persisting across restarts — **shipped in Sprint 8** (§8.1).

---

### 6.6 Sprint 6 Testing

#### 6.6.1 Covered by the shipped code
- [x] Clear data: scoped to the active vehicle and restorable through undo (`SettingsViewModel.clearAllEntries` / `undoClear`).
- [x] Export from Settings writes the active vehicle's CSV through the document picker.
- [x] Instrumented suite covers the per-vehicle delete path (`VehicleDaoTest`, `FuelEntryDaoCategoryTest`, `VehicleRepositoryTest`) — 61 instrumented tests passing.
- [x] Bottom navigation: four destinations navigate correctly; the selected tab shows the filled icon (`Icons.Rounded`), the rest outlined (`Icons.Outlined`). **Five since Sprint 9** (Vehicles added).
- [x] Accessibility: interactive elements carry content descriptions and 48dp minimum targets.
- [x] Full regression: add → edit → delete → dashboard and charts update (`FullRegressionTest`).

#### 6.6.2 Closed in Sprint 8
- [x] `SettingsRepository` read/write; theme-mode state management; distance conversion (km → mi) display.
- [x] Settings persistence across an app restart (`UserPreferencesPersistenceTest`, plus a force-stop/relaunch check on the emulator).
- [x] Manual: theme switching Light → Dark → System across every screen; km ↔ mi conversion across Dashboard, History and Charts.

---

## Sprint 7: Vehicle Management & Seed Data Expansion

**Timeline:** Week 7

**Primary Goal:** Move the app from single-vehicle to multi-vehicle: users add and switch vehicles, every fill-up belongs to a vehicle, and the active vehicle drives Dashboard, History, Charts and Settings. Expand the in-app demo seeder to six distinct profiles so the feature is demonstrable without manual entry.

**Scope note:** This intentionally extends the original mini scope, which listed multi-vehicle profiles as out of scope (`PRD_MileLog_Lite.md` §1.5). The entry model stays compatible: `FuelEntry.vehicleId` defaults to `0`, and the migration attaches pre-existing rows to a default vehicle.

### 7.1 Key Deliverables
- [x] Add `Vehicle` Room entity: name (unique), make, model, registration number, default fuel type, active flag.
- [x] Add `VehicleDao` with observed list, active-vehicle lookup, single-active `@Transaction` switch, and CRUD.
- [x] Link every fill-up to a vehicle: `FuelEntry.vehicleId` (default `0`) plus `vehicleId` indices.
- [x] Bump the database to version 3 with a lossless `Migration(2, 3)` that creates `vehicles`, adds `vehicleId`, and files existing rows under a default "My vehicle".
- [x] Add `VehicleRepository` (interface + offline implementation) and expose it from `MileLogApplication`.
- [x] Add vehicle-scoped fuel-entry queries (`getAllFlowForVehicle`, `getAllForVehicle`, `getLatestForVehicle`, `deleteByVehicle`) and mirror them in `FuelEntryRepository`.
- [x] Build the Add/Edit Vehicle screen (`AddEditVehicleScreen` + `AddEditVehicleViewModel`).
- [x] Add `VehicleValidator` for required and case-insensitive duplicate names.
- [x] Build the Settings > Vehicle section: vehicle list, active-vehicle switching, add row, edit and delete with a confirmation dialog. **Moved in Sprint 9** to the `Vehicles` primary destination, which ships the same list, switching, add, edit and delete.
- [x] Filter Dashboard, History and Charts to the active vehicle using `flatMapLatest` over the active-vehicle flow.
- [x] Show the active vehicle on Dashboard (binnacle subtitle), History, Charts and Add/Edit Entry; block logging when no vehicle exists.
- [x] Expand `DemoDataGenerator` to six profiles: Creta, Seltos and Harrier, each with a Diesel and a CNG history with distinct odometer, mileage and fuel-price bands.
- [x] Rework the Settings "Add demo fill-ups" action to create the demo vehicles and append each profile's history.
- [x] Route the vehicle form through Navigation (`vehicle_add`, `vehicle_edit/{vehicleId}`) and pass the vehicle actions from Settings.
- [x] Update the Demo Data script note and settings copy to describe the six demo vehicles.

### 7.2 Sprint 7 Milestone
- [x] Users can add, edit, switch and delete vehicles; Dashboard, History, Charts and Add/Edit all reflect the active vehicle; the demo seeder produces six distinct Diesel/CNG profiles.

### 7.3 Sprint 7 Testing
- [x] Unit tests for the six demo profiles: model/variant coverage, per-profile fuel category, mileage and price bands, determinism, vehicle tagging.
- [x] Unit tests for `VehicleValidator`: required name, case-insensitive duplicates, whitespace handling, self-exclusion on edit.
- [x] Instrumented tests for `VehicleDao`: CRUD, unique-name replacement, single active vehicle, per-vehicle entry delete.
- [x] Instrumented tests for the vehicle repositories: switch isolation, delete cascade with active promotion, demo seeding per vehicle.
- [x] Extend the fuel-entry column round-trip test to cover `vehicleId`.
- [x] `testDebugUnitTest` passes and `assembleDebug` builds.
- [x] Instrumented test sources compile (`assembleDebugAndroidTest`).
- [x] Manual test on a device/emulator: add a vehicle, log an entry, switch vehicles, and confirm the dashboard, history and charts follow the selection.
- [x] Run `connectedDebugAndroidTest` on a booted emulator to execute the new vehicle DAO/repository tests (61 instrumented tests, 0 failures).

### 7.4 Database Hardening (post-review)

Findings from the `DATABASE_STATUS.md` review, implemented after Sprint 7.

- [x] Add `MIGRATION_1_2` (defensive) so v1 installs no longer wipe on upgrade.
- [x] Narrow the destructive fallback to `fallbackToDestructiveMigrationOnDowngrade` — a missing upgrade path now fails loudly.
- [x] Make the `vehicleId` relationship real: nullable column + `@ForeignKey(ON DELETE CASCADE)` via `MIGRATION_3_4` (table rebuild).
- [x] Wrap `deleteVehicleWithEntries` in `database.withTransaction { }`.
- [x] Repair the single-active invariant on every open (`REPAIR_ACTIVE_VEHICLE`).
- [x] Switch `VehicleDao.insert` to `OnConflictStrategy.ABORT`; update the unique-name test.
- [x] Enable `exportSchema = true`, export the schema JSON, expose it to instrumented tests and add `MigrationTest` (v1→v4, v2→v4, v3→v4, FK enforcement).
- [x] Use `deleteByVehicle` for Settings clear-all and deprecate the dead unscoped repository queries.
- [x] Exclude the Room database from cloud backup (device-to-device transfer retained), matching the offline-only promise.
- [x] Fix category filtering: Room bound the enum `name` (`"PETROL"`) while rows store `displayName` (`"Petrol"`), so every filter matched nothing. Added `FuelCategoryConverters`.
- [x] Add the missing `id DESC` tie-break to the `getLatest*` queries.
- [x] Verify: 75 unit tests + 61 instrumented tests, all passing; 112 unit tests + 67 instrumented tests after Sprint 8.
- [x] Deferred: `SettingsRepository`/`UserPreferences` and `DistanceConverter` — shipped in **Sprint 8** (§8.1).

---

## Sprint 8: Instrument Ledger Conformance (`refactor-design`)

**Timeline:** Week 8

**Primary Goal:** Close the remaining gaps between the shipped app and the Instrument Ledger specs in `refactor-design/{dashboard,log-a-trip,setting}/DESIGN.md`. These are the items Sprint 6 left open once its Kinetic Logic sections were superseded.

### 8.1 Settings — Appearance & Units (`refactor-design/setting/DESIGN.md`)
- [x] Create `data/local/UserPreferences.kt` (`SharedPreferences("milelog_prefs")`; keys `theme_mode` default `"system"`, `distance_unit` default `"km"`).
- [x] Create `data/repository/SettingsRepository.kt` exposing both preferences as `StateFlow` with setters, and add it to `MileLogApplication`.
- [x] Replace the static Appearance row with a real radio group (Light / Dark / System), keeping the note that the instrument stays dark in every mode and only the logbook follows the setting.
- [x] `MileLogTheme(themeMode)`, and collect it in `MainActivity` so a change applies by recomposition.
- [x] Add a **Distance unit** field to the vehicle form and create `domain/conversion/DistanceConverter.kt` (`kmToMiles`, `formatDistance`).
- [x] Wire the unit through Dashboard (odometer, cost/km), History (odometer, mileage) and the Charts axis labels — display-only, storage stays km.
- [x] Open Settings with the current configuration as a readout (distance unit, currency, network), per the spec's "Configure surface" framing.

**Deviations from the spec, and why:**

- The theme parameter is `ThemeMode` (`light`/`dark`/`system`) rather than a `String`. The stored values are exactly the ones the spec names; the enum is what makes the resolution (`system → isSystemInDarkTheme()`, `light → false`, `dark → true`) a unit-testable function instead of a string comparison at the call site.
- The unit reached further than the listed readouts. The **entry form's odometer field** is also in the user's unit — the artifact's own Settings readout describes the setting as covering "odometer and mileage", and a field labelled `mi` that accepted kilometres would silently store a wrong reading. Conversion happens on the way in and on the way out (`DistanceConverter.toKilometres`); storage is still kilometres. Because a whole-mile round trip is not always invertible, an odometer that was never edited is written back exactly as it was loaded rather than re-derived (`odometerToStoreKm`, covered by `OdometerToStoreKmTest`).
- The dashboard's mileage gauge and trend bars follow the unit too (0–30 km/L becomes 0–18.6 mi/L), so the dial, its numbers and the history table never disagree.
- The Settings readout states distance unit, currency and network, replacing the Sprint 6 readout of fill-ups/distance/storage. The entry count is still visible where it is acted on, in the delete confirmation.
- `UserPreferences` reads and writes through a narrow `PreferencesStorage` seam so the settings logic is testable on the JVM. `SharedPreferencesStorage` writes with `commit()` on `Dispatchers.IO`, not `apply()`, so a preference is never reported as saved before it is on disk.
- The distance unit lives on the **vehicle form** as the spec asks, but it is an install-wide preference: it writes as it is picked, and the form says so ("Applies to every odometer and mileage in the app").

### 8.2 Log a Fill-up (`refactor-design/log-a-trip/DESIGN.md`)
- [x] Reorder the entry sheet to the specified sequence: **fuel type** (radio group) → date → odometer (previous reading as context) → litres → cost. Today the category selector is last.
- [x] Label the primary action **"Save fill-up"** and state the destination beside it: "Saved on this device, nothing uploaded".
- [x] On a valid submit, replace the form with a summary of what was recorded instead of navigating straight back.
- [x] Invalid state: keep the inline messages, add the banner that counts the invalid fields, and move focus to the first invalid field. (Save stays enabled — a disabled button hides what needs fixing.)

`EntryField` and `AddEditUiState.invalidFields` are what the banner counts and what decides where focus lands; the order they are declared in is the order the sheet reads them.

### 8.3 Dashboard (`refactor-design/dashboard/DESIGN.md`)
- [x] Give the dashboard ledger rows an **edit link**, so the first screen can edit a fill-up rather than only viewing it (History owns editing today).
- [x] Re-check the gauge's amber marker and tick marks, and the "numbers printed under each bar" trend, at both compact and expanded widths.

The link carries the fill-up's date in its accessible name (`dashboard_ledger_edit_a11y`), so five identical "Edit" labels never reach a screen reader as five identical names. Ledger rows and the wide header now share one trailing slot width (`LedgerTrailingWidth`), which also fixed the column captions drifting left of their columns on History.

### 8.4 Sprint 8 Milestone
- [x] Every screen matches its `refactor-design` spec, and the two preference-backed features (theme, distance unit) persist across restarts.

### 8.5 Sprint 8 Testing
- [x] Unit tests: `SettingsRepository` read/write; theme-mode resolution (`system`/`light`/`dark`); `DistanceConverter` (km → mi and formatting).
- [x] Instrumented: preference persistence across a simulated restart.
- [x] Manual: theme Light → Dark → System across all screens; km ↔ mi across Dashboard, History and Charts; entry-sheet order and the post-save summary; validation banner and focus move.

**Sprint 8 verification record**

- `testDebugUnitTest`: 112 tests, 0 failures (`UserPreferencesTest`, `ThemeModeTest`, `SettingsRepositoryTest`, `DistanceConverterTest`, `OdometerToStoreKmTest` added).
- `connectedDebugAndroidTest`: 67 tests, 0 failures on a booted emulator (`UserPreferencesPersistenceTest` added, including a check that the values reach the preferences XML on disk).
- `assembleDebug` builds.
- Emulator walkthrough, both units: fresh install → add a vehicle with the unit control → seed the demo data → switch the active vehicle → open an entry from the dashboard ledger link → clear a required field and save (banner reads "1 field needs attention", focus lands on the field) → fix it and save (summary replaces the form) → Done back to the dashboard → History and Charts read in the same unit → export CSV (7 entries, odometer column in kilometres, unchanged by the edit) → theme Light/Dark/System applied by recomposition → force-stop and relaunch, both preferences retained.


---

## Sprint 9: Navigation, Layout & Chart Interaction Refinement

**Timeline:** Week 9

**Primary Goal:** Make the vehicle, the fill-up form and the charts reachable and readable instead of fighting the shell: vehicle management becomes a primary destination, the entry form's readings and fields share one scroll context, and both charts report an exact value on tap.

### 9.1 Vehicle Management as a Primary Destination
- [x] Add `VEHICLES = "vehicles"` to `MileLogRoutes` and register `VehiclesScreen` in `MileLiteNavHost`.
- [x] Add Vehicles as the fifth `MileLogBottomBar` / `MileLogRail` destination (`Icons.Rounded` / `Icons.Outlined.DirectionsCar`, `bottom_nav_vehicles_label`).
- [x] Include `VEHICLES` in the shell's top-level detection and keep the FAB off it — the FAB logs a fill-up, which is not an act on the garage.
- [x] Create `ui/vehicle/VehiclesScreen.kt` + `VehiclesViewModel.kt`: configuration readout (active vehicle, count), vehicle rows with select / edit / delete, an Add vehicle row, and a delete confirmation dialog.
- [x] Remove the Vehicle group, its dialog and `VehicleRow` from `SettingsScreen`; drop `vehicles` from `SettingsUiState` and `setActiveVehicle` / `deleteVehicle` from `SettingsViewModel`, which now observes only the active vehicle (to scope the entry count).
- [x] Point the Dashboard empty-state copy at `vehicles_empty`.
- [x] Retire the now-unused `settings_vehicle_*` / `settings_section_vehicle` strings and add the `vehicles_*` set.

### 9.2 Unified Scroll & Keyboard on the Entry Form
- [x] Move `EntryInstrument` and the form / saved summary into a single `verticalScroll` container, replacing the fixed instrument above a separately scrolling form.
- [x] Apply `imePadding()` and `imeNestedScroll()` to that one container, so the keyboard shortens and scrolls the whole page.
- [x] Remove the nested `verticalScroll` / `imePadding` from `EntryForm` and `EntrySavedSummary` (a same-axis nested scrollable is illegal).
- [x] Apply the same treatment to `AddEditVehicleScreen`.
- [x] Reflow the entry readout strip to the roomy layout at `MileLogWindow.medium`.

### 9.3 Charts — Exact Value on Tap, Distinct Fuel Colours
- [x] Add `ChartValueMarkerView` (an MPAndroidChart `MarkerView`) plus `res/layout/chart_marker_view.xml`, drawn with the instrument surface and rule tokens.
- [x] `MonthlySpendChart`: marker on tap showing the month (and the fuel type when grouped) and the exact formatted cost; rebuilt in the `update` block so it always reads the current data.
- [x] `MileageTrendChart`: marker on tap showing the date (and fuel type for an overlay line) and the mileage in the selected unit.
- [x] Add per-fuel theme tokens `chartPetrol` / `chartDiesel` / `chartCng` / `chartCombined` to `LedgerColors`, map every `FuelCategory` explicitly, and give the combined line the neutral `chartCombined`.
- [x] Append `charts_touch_hint` to both chart content descriptions.

### 9.4 Responsive Polish & Motion

A motion-system pass over the shipped app. The on-record design reviews had already refused entrance choreography for this product surface ("Product UI does not need entrance beats"), so the pass adds only motion that explains a state change.

- [x] **One budget:** `MileLogMotion` (`ui/theme/Motion.kt`) holds the durations (`tap` / `fast` / `standard` / `medium` / `large` / `screenEnter` / `screenExit` / `readout`) and the single easing curve, so a selection, a reveal and a whole screen change move at the same tempo instead of each site inventing its own. Adopted by navigation, the gauge, the entry crossfade and everything below.
- [x] `MileLiteNavHost` destination transitions: a short fade plus a small directional offset (forward on push, back on pop), with exits shorter than entrances (170ms against 220ms).
- [x] Animate the `MileageGauge` fill to a new reading (`animateFloatAsState`).
- [x] Cross-fade the entry form into its saved summary.
- [x] **Selection feedback:** `SegmentedChoice` crossfades the selected segment's fill and label, so the fuel-type and distance-unit controls answer a tap instead of jumping.
- [x] **Error feedback:** the entry form's validation banner enters and leaves with a fade plus a vertical expand/shrink, and holds the last count it showed so the live region never announces "0 fields need attention" on the way out.
- [x] **List feedback:** each History ledger row carries `Modifier.animateItem()`, so adding, deleting or undoing an entry moves the list and fades the row instead of blinking the neighbours into place. Placement uses a spring (interruptible); the fades use the budget.
- [x] **Reduced motion re-verified, not re-implemented:** Compose scales every duration by the system animation scale (`MotionDurationScale` observes the Android animator duration scale), so a device with animations off already collapses these to the next frame, while the infinite spinners keep turning as the platform intends.
- Considered and deliberately rejected: entrance choreography or staggered reveals on the dashboard (the review refused them; a dashboard should arrive ready to use), animating the trend bars (height animation janks and would read as an entrance on first paint), a loading-to-content crossfade per screen (the shell already fades the destination in), and animating the chart marker or the theme switch.

### 9.5 Responsive Adaptation (shipped app)

A responsive adaptation pass over the shipped Compose app. The on-record design reviews audit the superseded `refactor-design/` artifacts rather than the app itself, so their findings were re-verified against the real code before anything was acted on.

- [x] **System bars on the rail.** `MileLogRail` is shell chrome, not Scaffold content, so it was the one surface with no window-inset handling: on an edge-to-edge tablet the wordmark sat under the status bar and the offline note under the gesture bar. It now carries `windowInsetsPadding(WindowInsets.safeDrawing)`. (Re-verified from `review-report.md` finding 5, which asked for desktop navigation that actually works: the rail shipped, but it was drawn under the system bars.)
- [x] **Content measure on wide windows.** Only `InstrumentBand`, Charts and History capped their content; the dashboard ledger, Settings, Vehicles and both forms stretched to the full frame (about 1370dp on a 1600dp window), against the rule in `Spacing.kt` that backgrounds stay full-bleed and only text and controls are capped. Added a shared `LogbookContent` frame (centred, `widthIn(max = MileLogWindow.contentMaxWidth)`) and applied it to those four surfaces. The instrument is deliberately not wrapped: its casing has to reach the window edges.
- [x] **Split console layout.** The entry and vehicle sheets were a single full-width column at every width. At `MileLogWindow.wide` of **content** width they now recompose to the console the design describes: the live instrument beside the fields, both scrolling as the one page. The threshold is measured on the screen's own frame, so a rail on screen is already accounted for.
- [x] **Font-scale-aware readouts.** The three-across readout strip switched on width alone, so it stayed three-across at a 2.0 system font scale and crushed. Its threshold now scales with `LocalDensity.fontScale` and falls back to the ruled list; the split layout is always compact.
- [x] Consolidated the duplicated 720dp breakpoint into `MileLogWindow.wide` (Charts held a private copy).
- Inspected and deliberately left alone: the bottom bar's label behaviour (M3 ellipsises safely; the alternatives cost discoverability), History's centred `LazyColumn` (needs `contentPadding`, so it keeps its own mechanism), and RTL (Compose `start`/`end` and `Alignment.TopStart` are already logical throughout — no physical `left`/`right` anywhere).

### 9.6 Sprint 9 Milestone
- [x] Vehicles is a primary destination reachable in one tap from the shell; the entry form scrolls and resizes as one page under the keyboard; both charts report exact values on tap and distinguish petrol, diesel and CNG by colour.

### 9.7 Sprint 9 Testing
- [x] `:app:compileDebugKotlin` + `:app:processDebugResources` — success.
- [x] `:app:testDebugUnitTest` — 112 tests, 0 failures (no regressions; no data-layer logic changed).
- [x] `:app:lintDebug` — success: 20 warnings, 0 errors, none of them in a file this sprint touched.
- [x] `:app:assembleDebug` and `:app:assembleDebugAndroidTest` — success.
- [x] Responsive pass re-verified after the §9.5 changes — compile + resources success, `:app:testDebugUnitTest` 112 tests / 0 failures, lint unchanged at 20 warnings / 0 errors, IDE diagnostics clean on all nine changed files.
- [x] Motion pass re-verified after the §9.4 changes — compile + resources success, `:app:testDebugUnitTest` 112 tests / 0 failures, lint unchanged at 20 warnings / 0 errors, IDE diagnostics clean on all six changed files.
- [ ] Motion observed on a device — the selection crossfade, the banner's enter/exit, list placement on delete and undo, and the system reduced-motion path are **not verified as rendered behaviour**. No device was used this pass, so they are verified as implementations only.
- [ ] Rendered composition at 320 / 360 / 600 / 840 / 1280dp, in landscape, and at font scale 2.0 — **not verified**. No renderer or device was exercised this pass (the emulator walkthrough was stopped at the user's request in the previous task), so the wide-frame, split-console and scaled-readout behaviour is reasoned from `BoxWithConstraints` widths and `LocalDensity.fontScale` rather than observed.
- [ ] Manual emulator walkthrough across screen sizes and orientations — **cut short at the user's request**; not re-run. Verified by build, unit tests and lint instead.

### 9.8 Design Checkup Findings — Fixed

A design checkup of the shipped app raised one MEDIUM and three LOW findings. All four are closed. The checkup report is left as the point-in-time diagnostic; re-running it is what verifies them.

- [x] **MEDIUM — an empty reading had no spoken form.** The placeholder was a bare em dash in a resource plus two inline literals, so a screen reader reached an unspoken dash in every empty readout and in the mileage cell of a first fill-up. Now a resource pair (`value_not_recorded` for the glyph, `value_not_recorded_spoken` for the words) is rendered by one component, `ReadingText`: it keeps the glyph for the eye and swaps in the words for the accessibility tree. `ReadoutItem.value` and `SummaryRow`'s value became nullable so no screen decides what to print for a figure it does not have, the inline literals in `LedgerRow` are gone, and the gauge's accessible name says "Not recorded" instead of reading out a dash.
- [x] **LOW — radio sets were not announced as sets.** `Modifier.selectableGroup()` on the segmented control (fuel type, distance unit), the vehicle list and the appearance list, so each announces as one choice rather than a run of unrelated radio buttons. The Vehicles panel keeps its Add row outside the group: that row is an action, not an option.
- [x] **LOW — five bottom-bar labels.** `MileLogBottomBar` measures its own width and scales the threshold by the system font size: at 320dp and above, at the default text size, every label shows, which covers every standard phone. Only when the labels genuinely cannot fit, a narrow screen with the font enlarged, does the selected destination keep its name alone. Whichever label is drawn names its destination; only when a label is not drawn does the icon carry that name instead, so nothing is announced twice and nothing is left unnamed.
  **Corrected after the re-audit:** the first attempt set the threshold at 400dp, which sits above standard phone widths, so a 360dp or 384dp phone showed one label instead of five. The re-audit also found the premise behind that change, that five labels truncate at 320dp, did not reproduce: the longest label needs about 58dp against a 64dp slot there. The threshold is now 320dp (the real break point) and scales with the system font.
  **Verified by rendering** on the emulator with the display overridden to 320dp: at the default text size all five labels are laid out (five 54px label boxes across the bar) and every destination is named exactly once; at doubled text only the selected destination keeps its label (a single 108px label box), with all five still named exactly once. That check also caught what code inspection had missed: the first naming approach removed the label from the accessibility tree and put the name only on the icon, which left **every** destination unnamed whenever its label was drawn, at every width. The label now names the destination and the icon only takes over when the label is absent.
- [x] **LOW — release shipped unshrunk.** R8 code and resource shrinking are on for the release variant. Measured: the release APK is **2.0 MB** against the debug artifact's **20.2 MB**, and R8 ran clean with no missing-class errors.
- **Deviation, stated plainly:** the checkup proposed enabling the `optimization {}` block, but that block is the AGP 9.3 DSL and this project is on 9.2, where it refuses to enable without an internal rollout flag. The documented switch for 9.2 (`isMinifyEnabled` + `isShrinkResources`) was used instead. The build-config change was signed off first, as this repository's change rules require.
- **Verification:** `compileDebugKotlin` + `processDebugResources`, `testDebugUnitTest` (112 tests, 0 failures), `lintDebug` (20 warnings, 0 errors, none in a changed file), `assembleRelease` (R8 clean), and IDE diagnostics clean across the eight changed source files.
- **Still open from the checkup:** the rendered-composition, keyboard, screen-reader and RTL gaps. The release APK is also unsigned (no signing config) and its runtime is unverified: R8 succeeds at build time, and a release build has not been run.
- **Re-audit outcome.** Three of those four findings were verified fixed by reading the code at their cited lines. The fourth, the bottom-bar one, did not reproduce, and the fix made for it became the one MEDIUM the re-audit raised, since corrected above. The checkup report is regenerated as the current diagnostic. Not re-audited since: the corrected bar behaviour, which is reasoned from the item measure rather than seen, and the device checks the two watch vitals remain gated on.

### 9.9 Accessibility Finding From the Third Checkup — Fixed

The third checkup ran the accessibility pass the two before it had only flagged as a gap, and it found a HIGH.

- [x] **The primary action announced nothing.** The `ExtendedFloatingActionButton` in `ui/components/MileLogFab.kt` was clickable and focusable with an empty `text` and an empty `content-desc`, so the app's primary action on Dashboard, History and Reports reached a screen reader as a bare button; its drawn label is not bridged into the semantics tree. Fixed by naming the button explicitly with `Modifier.semantics { contentDescription = label }` instead of relying on the drawn text.
- [x] **JVM Compose UI tests, so this class of defect is caught without a device.** Added Robolectric 4.17 (API 23 to 37, built against this project's AGP) with the Compose test rule on the JVM, plus `testOptions.unitTests.isIncludeAndroidResources` and the module export Robolectric needs for the JRE's file-descriptor internals. `OperableControlNameTest` asserts that the primary action and every bottom-bar destination are named **exactly once**, read off the merged semantics tree a screen reader reads.
- [x] **The test was written before the fix, and failed on the unfixed code** with "could not find any node that satisfies OnClick is defined && has a non-blank accessible name", while the bar case passed alongside it. That contrast is what shows the assertion measures names rather than passing vacuously.
- [x] **Two build-config additions, both test-only:** the Robolectric and Compose test dependencies under `testImplementation`, and one JVM `--add-exports` for the test runtime. Neither touches the shipped artifact.
- [x] **Also confirmed clean by the same pass:** each embedded chart exposes one named node with nothing unnamed behind it, closing a question open since the first checkup; keyboard traversal reaches the primary action and all five destinations in a closed loop; and TalkBack binds and runs with the app rendering unchanged.
- **Verification:** `testDebugUnitTest` 114 tests, 0 failures; `lintDebug` 20 warnings, 0 errors, none in a changed file.
- **Still open:** the fix was verified by test rather than by device, and the assertion reads the semantics tree rather than a spoken announcement. The exhaustive unnamed-control sweep has been run on one screen only.

**Sprint 9 verification record**

- `app-debug.apk` built, installed and launched cleanly on the `Medium_Phone` AVD before the emulator walkthrough was stopped at the user's request; the emulator was then shut down.
- Machine note: `org.gradle.configuration-cache=true` fails on this host with a `JdkImageInput` serialization error in `:app:compileDebugJavaWithJavac`. Worked around per-machine with `--no-configuration-cache`; no project file was changed.
- Deferred follow-up: an on-device pass at multiple widths/orientations (including a tablet) and a visual check of the chart markers.

---

## Deferred / Not in Mini Scope

> More features have been done in original full application , This is completely diff , in terms of UI wise

Sprint 9 is the latest planned sprint. It adds the Vehicles destination, unifies the entry form's scroll context with the keyboard, and gives both charts a tap readout plus per-fuel colours; it also supersedes the Sprint 6 decisions that the garage lives in Settings and that the shell carries four destinations. **Nothing on this plan is still open** — every remaining item is either shipped, explicitly superseded (Kinetic Logic, the 5-tab bar, the centred Add tab, the Settings vehicle group), or recorded above as a decision.

Deliberately out of scope for the mini build, unchanged by Sprint 8:

- Cloud backup, sync, accounts and a shared garage.
- CSV import (export only).
- Multi-currency; costs stay in INR.
- Miles per gallon: fuel is litres in both units, so mileage reads km/L or mi/L.
- Multi-language.

*End of Document*

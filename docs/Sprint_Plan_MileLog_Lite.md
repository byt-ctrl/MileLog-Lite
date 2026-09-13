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
- [x] `MileLogBottomBar` with the four shell destinations:
    - [x] Dashboard (`Icons.Rounded.Dashboard` / `Icons.Outlined.Dashboard`)
    - [x] History (`Icons.Rounded.History` / `Icons.Outlined.History`)
    - [x] Reports (`Icons.Rounded.Assessment` / `Icons.Outlined.Assessment`)
    - [x] Settings (`Icons.Rounded.Settings` / `Icons.Outlined.Settings`)
- [x] Selected state reads as a raised fill (`chromeRaisedHigh` indicator) plus a brighter label — never a coloured stripe.
- [x] `MileLogRail` (228dp) for expanded widths: the same destinations plus the wordmark, the primary action and the offline note, so widening the window never removes a way to move.
- [x] Bar wired to the `NavHost` with `startDestination = DASHBOARD`; a tab press pops up to the start destination and restores state.

> **Superseded:** the planned fifth "Add" tab (centred elevated pill) was not shipped. Logging a fill-up is the shell's own FAB (`MileLogFab`), which is why it appears on Dashboard, History and Reports but not on Settings.

#### 6.2.2 Screen Navigation Updates
- [x] Back navigation with the bottom bar pops up to the start destination.
- [x] Charts reachable from the Reports tab, and from the dashboard's "Open charts" action.
- [x] `AddEditEntryScreen` and `AddEditVehicleScreen` are full-screen routes with no shell bars.
- [ ] Decide whether the primary action should also be reachable from Settings (the FAB is hidden there today).

---

### 6.3 Settings Screen

**Objective:** A Configure surface, built to `refactor-design/setting/DESIGN.md` and rendered with Instrument Ledger components.

#### 6.3.1 Shipped

`ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt`, using `LedgerPanel`, `SectionHeader` and `SettingsRow` (60dp rows, hairline dividers) rather than the Kinetic Logic card layout:

- [x] Opens with an instrument readout of the current state (fill-ups logged, distance tracked, storage) instead of a decorative header.
- [x] **Vehicle** group: vehicles with the active selection, tap to switch, edit route, and delete with a confirmation dialog.
- [x] **Data** group: Export fill-ups as CSV through the system document picker; Delete all fill-ups with confirmation **and undo**; Add demo fill-ups.
- [x] **About** group: version, storage ("This device"), network ("Not required").
- [x] Every claim on the screen is true of the product — there is no subscription, sync, account or log out, so none of them appear.
- [x] Clear-all is scoped to the active vehicle and restores from memory on undo, so **no global `deleteAll()` is needed**; a bulk `deleteByVehicle` covers the delete.
- [x] Content descriptions and a 48dp minimum touch target on every interactive row.

**Deviations from the original spec:** the settings screen is not a `LazyColumn` of M3 cards, and the Vehicle section is a **list** rather than the single-vehicle form the spec assumed (the app is multi-vehicle since Sprint 7).

#### 6.3.2 Still open — moved to Sprint 8

- [ ] **Appearance** control: the row ships but is static ("Theme — Follows device"). `refactor-design/setting/DESIGN.md` asks for a real radio group (Light / Dark / System).
- [ ] **Distance unit** on the vehicle form, plus a `DistanceConverter`.
- [ ] Preferences persistence: no `UserPreferences` / `SettingsRepository` exists yet (`theme_mode`, `distance_unit`).

#### 6.3.3 Theme Integration (open)

- [ ] `MileLogTheme(themeMode: String)` resolving `system → isSystemInDarkTheme()`, `light → false`, `dark → true`.
- [ ] Collect the preference in `MainActivity` so a change applies by recomposition, with no restart.
- [ ] Keep the spec's rule: the instrument stays dark in every mode; only the logbook follows the setting.

#### 6.3.4 Distance Unit Integration (open)

- [ ] `domain/conversion/DistanceConverter.kt` with `kmToMiles(km) = km * 0.621371` and `formatDistance(value, unit)`.
- [ ] Dashboard: convert the odometer and cost-per-km readouts.
- [ ] History: convert the displayed odometer and mileage.
- [ ] Charts: axis labels and points respect the unit, while chart data stays in km internally.
- [ ] Storage stays in km; conversion is display-only.

---

### 6.4 Integration & Wiring

- [x] `MainActivity` wraps `MileLiteNavHost` in `MileLogTheme` inside a `Surface`; the system bars stay transparent and follow the resolved appearance.
- [x] Settings is reachable from any tab, and back navigation from a full-screen route returns to the shell with its navigation intact.
- [x] Logging a fill-up opens `AddEditEntryScreen` as a full-screen route from the shell FAB / rail action.
- [ ] Add `SettingsRepository` to `MileLogApplication` (manual DI), collect the theme preference at the top level and apply it before `setContent` — blocked on the Sprint 8 preferences work.

---

### 6.5 Sprint 6 Milestone
- [x] Settings screen ships with Vehicle, Data (export, clear with undo, demo seed) and About groups, reachable from the bottom bar.
- [x] Bottom navigation is functional with correct selected/unselected states at both compact and expanded widths.
- [x] All existing functionality (CRUD, calculations, charts, export) continues to work with the shipped design system and navigation.
- [ ] ~~App uses the new Kinetic Logic design system across all screens~~ — **superseded**; Instrument Ledger shipped instead (§6.1).
- [ ] ~~5-tab bottom navigation bar is functional~~ — **superseded**; four destinations plus a shell FAB shipped instead (§6.2).
- [ ] Theme toggle and distance units, with preferences persisting across restarts — **moved to Sprint 8**.

---

### 6.6 Sprint 6 Testing

#### 6.6.1 Covered by the shipped code
- [x] Clear data: scoped to the active vehicle and restorable through undo (`SettingsViewModel.clearAllEntries` / `undoClear`).
- [x] Export from Settings writes the active vehicle's CSV through the document picker.
- [x] Instrumented suite covers the per-vehicle delete path (`VehicleDaoTest`, `FuelEntryDaoCategoryTest`, `VehicleRepositoryTest`) — 61 instrumented tests passing.
- [x] Bottom navigation: four destinations navigate correctly; the selected tab shows the filled icon (`Icons.Rounded`), the rest outlined (`Icons.Outlined`).
- [x] Accessibility: interactive elements carry content descriptions and 48dp minimum targets.
- [x] Full regression: add → edit → delete → dashboard and charts update (`FullRegressionTest`).

#### 6.6.2 Moved to Sprint 8
- [ ] `SettingsRepository` read/write; theme-mode state management; distance conversion (km → mi) display.
- [ ] Settings persistence across an app restart.
- [ ] Manual: theme switching Light → Dark → System across every screen; km ↔ mi conversion across Dashboard, History and Charts.

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
- [x] Build the Settings > Vehicle section: vehicle list, active-vehicle switching, add row, edit and delete with a confirmation dialog.
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
- [x] `testDebugUnitTest` passes (68 unit tests) and `assembleDebug` builds.
- [x] Instrumented test sources compile (`assembleDebugAndroidTest`).
- [ ] Manual test on a device/emulator: add a vehicle, log an entry, switch vehicles, and confirm the dashboard, history and charts follow the selection.
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
- [x] Verify: 75 unit tests + 61 instrumented tests, all passing.
- [ ] Deferred: `SettingsRepository`/`UserPreferences` and `DistanceConverter`, now tracked in **Sprint 8**.

---

## Sprint 8: Instrument Ledger Conformance (`refactor-design`)

**Timeline:** Week 8

**Primary Goal:** Close the remaining gaps between the shipped app and the Instrument Ledger specs in `refactor-design/{dashboard,log-a-trip,setting}/DESIGN.md`. These are the items Sprint 6 left open once its Kinetic Logic sections were superseded.

### 8.1 Settings — Appearance & Units (`refactor-design/setting/DESIGN.md`)
- [ ] Create `data/local/UserPreferences.kt` (`SharedPreferences("milelog_prefs")`; keys `theme_mode` default `"system"`, `distance_unit` default `"km"`).
- [ ] Create `data/repository/SettingsRepository.kt` exposing both preferences as `StateFlow` with setters, and add it to `MileLogApplication`.
- [ ] Replace the static Appearance row with a real radio group (Light / Dark / System), keeping the note that the instrument stays dark in every mode and only the logbook follows the setting.
- [ ] `MileLogTheme(themeMode: String)`, and collect it in `MainActivity` so a change applies by recomposition.
- [ ] Add a **Distance unit** field to the vehicle form and create `domain/conversion/DistanceConverter.kt` (`kmToMiles`, `formatDistance`).
- [ ] Wire the unit through Dashboard (odometer, cost/km), History (odometer, mileage) and the Charts axis labels — display-only, storage stays km.
- [ ] Open Settings with the current configuration as a readout (distance unit, currency, network), per the spec's "Configure surface" framing.

### 8.2 Log a Fill-up (`refactor-design/log-a-trip/DESIGN.md`)
- [ ] Reorder the entry sheet to the specified sequence: **fuel type** (radio group) → date → odometer (previous reading as context) → litres → cost. Today the category selector is last.
- [ ] Label the primary action **"Save fill-up"** and state the destination beside it: "Saved on this device, nothing uploaded".
- [ ] On a valid submit, replace the form with a summary of what was recorded instead of navigating straight back.
- [ ] Invalid state: keep the inline messages, add the banner that counts the invalid fields, and move focus to the first invalid field. (Save stays enabled — a disabled button hides what needs fixing.)

### 8.3 Dashboard (`refactor-design/dashboard/DESIGN.md`)
- [ ] Give the dashboard ledger rows an **edit link**, so the first screen can edit a fill-up rather than only viewing it (History owns editing today).
- [ ] Re-check the gauge's amber marker and tick marks, and the "numbers printed under each bar" trend, at both compact and expanded widths.

### 8.4 Sprint 8 Milestone
- [ ] Every screen matches its `refactor-design` spec, and the two preference-backed features (theme, distance unit) persist across restarts.

### 8.5 Sprint 8 Testing
- [ ] Unit tests: `SettingsRepository` read/write; theme-mode resolution (`system`/`light`/`dark`); `DistanceConverter` (km → mi and formatting).
- [ ] Instrumented: preference persistence across a simulated restart.
- [ ] Manual: theme Light → Dark → System across all screens; km ↔ mi across Dashboard, History and Charts; entry-sheet order and the post-save summary; validation banner and focus move.

---

## Deferred / Not in Mini Scope

> More features have been done in original full application , This is completely diff , in terms of UI wise

The only open items carried by this plan are the **Sprint 8** conformance gaps (Appearance control, distance units and their persistence, the entry-sheet order and post-save summary, and the dashboard ledger edit link). Everything else is either shipped or explicitly superseded.

*End of Document*

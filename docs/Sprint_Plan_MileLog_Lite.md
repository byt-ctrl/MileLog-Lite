# MileLog Lite - Sprint Execution Plan & Checklist

This document provides an interactive execution checklist for the mini-scope MileLog Android application. It mirrors the structure of the full MileLog sprint plan but is reduced to a **single-vehicle fuel logger with calculations and charts**, sized for a 3–4 week build.

---

### Quick Sprint Navigation
> - [Sprint 1: Foundation, Data Layer & Fuel CRUD](#sprint-1-foundation-data-layer--fuel-crud)
> - [Sprint 2: Calculations, Dashboard & Validation](#sprint-2-calculations-dashboard--validation)
> - [Sprint 3: Charts & Visualization](#sprint-3-charts--visualization)
> - [Sprint 4: Polish, Testing & Release Readiness](#sprint-4-polish-testing--release-readiness)
> - [Sprint 5: Fuel Category Selection](#sprint-5-fuel-category-selection)
> - [Sprint 6: Settings, Design System Migration & Bottom Navigation](#sprint-6-settings-design-system-migration--bottom-navigation)

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
    - [x] Known limitations (e.g., single vehicle only, no export)
- [x] **If the above is done with time remaining**, consider one optional stretch item (see PRD §7): dark mode, simple CSV export, or a single basic reminder. Do not start a stretch item if core polish/testing is incomplete. **Implemented: simple CSV export** (History screen top-bar action → system document picker → `milelog_fuel_entries.csv`; export-only, no import).

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

**Primary Goal:** Implement a full Settings screen with user preferences, migrate the entire app to the new "Kinetic Logic" design system, and add a 5-tab bottom navigation bar.

### 6.1 Design System Migration (Kinetic Logic)

**Objective:** Replace the current "Road asphalt + fuel amber + dashboard teal" theme with the new "Kinetic Logic" corporate/modern design system across all screens.

#### 6.1.1 Color Palette Overhaul (`Color.kt`)
- [x] Replace current Primary (`#1B4D4A` Teal) → `#003D9B` (Dependable Blue)
- [x] Replace Secondary (`#E8A838` Amber) → `#006C47` (Business/Success Green)
- [x] Add new accent tokens: `#FF8B00` (Personal Orange), `#36B37E` (Business Green), `#DE350B` (Active Status red)
- [ ] Map all M3 color roles to new Kinetic Logic tokens:
    - Light scheme: `surface=#F9F9FF`, `surfaceBg=#F4F5F7`, `surfaceContainerLowest=#FFFFFF`, `surfaceContainerLow=#F0F3FF`, `surfaceContainer=#E7EEFF`, `surfaceContainerHigh=#DEE8FF`, `surfaceContainerHighest=#D6E3FE`, `onSurface=#0E1C2F`, `onSurfaceVariant=#434654`, `outline=#737685`, `outlineVariant=#C3C6D6`
    - Dark scheme: derive inverse counterparts (`inverseSurface=#243145`, `inverseOnSurface=#EBF1FF`, `inversePrimary=#B2C5FF`)
- [ ] Ensure both Light and Dark schemes use the new palette

#### 6.1.2 Typography Overhaul (`Type.kt`)
- [ ] Add Inter font family (download `Inter-Regular.ttf`, `Inter-Medium.ttf`, `Inter-SemiBold.ttf`, `Inter-Bold.ttf` into `res/font/`)
- [ ] Replace system font with Inter across all text styles
- [ ] Add specialized `displayLarge` style (48sp, Bold, -0.02em letterSpacing, `fontFeatureSettings = "tnum"`) for dashboard KPIs — tabular figures ensure vertical number alignment
- [ ] Add `displayMedium` variant (36sp, Bold, -0.02em) for smaller-screen KPIs
- [ ] Update `labelSmall` with 0.05em letter spacing for metadata labels

#### 6.1.3 Spacing & Shape System
- [ ] Create `Spacing.kt` object with 8px base scale: `xs=4`, `sm=8`, `md=12`, `lg=16`, `xl=24`, `xxl=32`, `touchTarget=48`
- [ ] Create `Shape.kt` with rounded shape tokens matching design: `sm=4` (0.25rem), `md=8` (0.5rem), `lg=12` (0.75rem), `xl=16` (1rem), `xxl=24` (1.5rem), `full=9999`
- [ ] Update `Theme.kt` to use new shapes: 8px default for cards/buttons/inputs, 16px for chips/tags

#### 6.1.4 Elevation System
- [ ] Implement tonal layer approach:
    - Level 0 (Base): `surfaceBg=#F4F5F7` for application background
    - Level 1 (Cards): White surfaces (`surfaceContainerLowest`) with `shadow(elevation=1.dp, shape=RoundedCornerShape(8.dp), color=Color.Black.copy(alpha=0.10), blurRadius=4.dp)`
    - Level 2 (Active Elements): FAB and Bottom Nav with `shadow(elevation=4.dp, shape=..., color=Color.Black.copy(alpha=0.15), blurRadius=12.dp)`
    - Overlays: Bottom sheets/modals use `20%` backdrop dim
- [ ] Update card composables to use new shadow/elevation tokens

#### 6.1.5 Component Updates
- [ ] Update TopAppBar: profile avatar (circular, surface-variant background), app title "MileLog Lite" (headlineMedium, primary color), optional notification icon
- [ ] Update all existing screens (Dashboard, History, AddEdit, Charts) to use new color tokens, typography, and spacing
- [ ] Update FAB styling: Primary Blue (`#003D9B`), pill-shaped, Level 2 shadow
- [ ] Update input field styling: 1px border (`outline` at 20% opacity), thickens + Primary Blue on focus, persistent labels above field
- [ ] Update card styling: white surface (`surfaceContainerLowest`), Level 1 shadow, 8px radius
- [ ] Update chip styling: 16px radius for category tags (Petrol/Diesel/CNG), active Business Green / Personal Orange backgrounds
- [ ] Ensure all interactive elements meet 48dp minimum touch target

---

### 6.2 Bottom Navigation Bar

**Objective:** Add a fixed 5-tab bottom navigation bar and restructure navigation flow.

#### 6.2.1 Navigation Restructure (`MileLiteNavHost.kt`)
- [ ] Add new routes: `SETTINGS`, `REPORTS` (alias for Charts)
- [ ] Update `MileLogRoutes` object with new route constants
- [ ] Create `BottomNavBar.kt` composable with 5 items:
    - Dashboard (icon: `Icons.Rounded.Dashboard` selected / `Icons.Outlined.Dashboard` unselected, label: "Dashboard")
    - History (icon: `Icons.Rounded.History` / `Icons.Outlined.History`, label: "History")
    - Add (icon: `Icons.Rounded.AddCircle`, label: "Add") — centered, elevated pill: `w=48dp`, `h=48dp`, `bg=primary`, `color=onPrimary`, `shadow(elevation=8.dp)`, offset `y=-24dp`
    - Reports (icon: `Icons.Rounded.Assessment` / `Icons.Outlined.Assessment`, label: "Reports")
    - Settings (icon: `Icons.Rounded.Settings` / `Icons.Outlined.Settings`, label: "Settings")
- [ ] Active tab styling: use `NavigationBarItemDefaults.colors()` with `selectedIconColor = onPrimaryContainer`, `selectedTextColor = onPrimaryContainer`, `indicatorColor = primaryContainer`
- [ ] Inactive tab styling: use `NavigationBarItemDefaults.colors()` with `unselectedIconColor = onSurfaceVariant`, `unselectedTextColor = onSurfaceVariant`
- [ ] Wire bottom nav to `NavHost` with `startDestination = DASHBOARD`
- [ ] Bottom nav bar: `h=64dp`, `bg=surface`, `shadow(elevation=4.dp, blurRadius=12.dp, alpha=0.15)`, `roundedTopStart=16.dp`, `roundedTopEnd=16.dp`

#### 6.2.2 Screen Navigation Updates
- [ ] Remove standalone FABs from Dashboard and History screens (replaced by bottom nav "Add" tab)
- [ ] Ensure back navigation works correctly with bottom nav (pop up to start destination)
- [ ] Charts screen accessible via "Reports" tab instead of card navigation

---

### 6.3 Settings Screen

**Objective:** Build a comprehensive Settings screen with all in-scope preference sections.

#### 6.3.1 Settings Data Layer
- [ ] Add `@Query("DELETE FROM fuel_entries")` method `deleteAll()` to `FuelEntryDao.kt`
- [ ] Create `data/local/UserPreferences.kt`:
    - Use `Context.getSharedPreferences("milelog_prefs", Context.MODE_PRIVATE)`
    - Preference keys: `theme_mode` (String), `distance_unit` (String)
    - Default values: `theme_mode = "system"`, `distance_unit = "km"`
- [ ] Create `data/repository/SettingsRepository.kt`:
    - Interface + implementation wrapping `SharedPreferences`
    - Methods: `getThemeMode(): StateFlow<String>`, `setThemeMode(mode: String)`, `getDistanceUnit(): StateFlow<String>`, `setDistanceUnit(unit: String)`, `clearAllEntries()` (calls `dao.deleteAll()`)
    - Expose as `StateFlow` for reactive UI updates

#### 6.3.2 Settings UI (`ui/settings/`)
- [ ] Create `SettingsScreen.kt` with `LazyColumn` and grouped sections:
    - **Preferences Group** (header: `labelSmall`, uppercase, `onSurfaceVariant`):
        - Theme row: icon `Palette`, label "Theme", current value display ("Light"/"Dark"/"System"), chevron right → 3-option `AlertDialog`
        - Distance Units row: icon `Straighten`, label "Distance Units", current value ("km"/"mi"), chevron right → 2-option `AlertDialog`
    - **Data Group**:
        - Export Logs row: icon `IosShare`, label "Export Logs", chevron right → triggers `FuelEntryCsvExporter` via system document picker (reuse existing `HistoryViewModel` export logic)
        - Clear All Data row: icon `DeleteForever`, label "Clear All Data", `color=error` → confirmation `AlertDialog` with title "Clear all fuel entries?", body "This will permanently delete all your fuel entries. This action cannot be undone.", confirm button text "Clear All" (error color), cancel button
    - **About Group**:
        - App Version row: icon `Info`, label "Version", value from `BuildConfig.VERSION_NAME`
        - Credits row: icon `Code`, label "Built with Jetpack Compose", no action
    - Card styling: `surfaceContainerLowest`, `RoundedCornerShape(8.dp)`, Level 1 shadow, `padding=16.dp`
    - Row styling: `padding=16.dp`, `48.dp` min touch target, `Divider` between rows with `surfaceVariant` color
- [ ] Create `SettingsViewModel.kt`:
    - Inject `SettingsRepository` + `FuelEntryRepository`
    - Observe preferences as `StateFlow`
    - Methods: `setThemeMode(mode)`, `setDistanceUnit(unit)`, `clearAllEntries(onComplete: () -> Unit)`
    - Clear data: confirm state flow, executes `settingsRepository.clearAllEntries()`, navigates or resets UI on completion

#### 6.3.3 Theme Integration
- [ ] Update `Theme.kt`: `MileLogTheme` accepts `themeMode: String` parameter ("light"/"dark"/"system")
- [ ] Implement theme resolution:
    - "system" → `isSystemInDarkTheme()` (follows device setting)
    - "light" → always `darkTheme = false`
    - "dark" → always `darkTheme = true`
- [ ] Theme changes apply immediately via recomposition (no app restart needed)
- [ ] In `MainActivity.kt`: collect `themeMode` from `SettingsViewModel`, pass to `MileLogTheme` composable

#### 6.3.4 Distance Unit Integration
- [ ] Create `domain/conversion/DistanceConverter.kt` with pure functions:
    - `kmToMiles(km: Double): Double` → `km * 0.621371`
    - `formatDistance(value: Double, unit: String): String` → rounds to 1 decimal, appends unit ("km" or "mi")
- [ ] Update `DashboardScreen.kt`: read `distanceUnit` preference, convert odometer/cost-per-km display values
- [ ] Update `HistoryScreen.kt`: convert displayed mileage and odometer values based on preference
- [ ] Update Charts: Y-axis labels and data points respect the unit preference (convert values for display, keep chart data in km internally)
- [ ] Underlying data stays in km; conversion is display-only

---

### 6.4 Integration & Wiring

#### 6.4.1 Application Class Update
- [ ] Add `SettingsRepository` to `MileLogApplication` (manual DI)
- [ ] Pass settings state to `MainActivity` composable

#### 6.4.2 MainActivity Update
- [ ] Collect theme preference at top level
- [ ] Apply theme before `setContent`
- [ ] Wrap `MileLiteNavHost` with bottom nav scaffold

#### 6.4.3 Navigation Flow
- [ ] Bottom nav persists across all screens
- [ ] Settings screen accessible from any tab via bottom nav
- [ ] "Add" tab opens AddEditEntryScreen as full-screen overlay or standard route

---

### 6.5 Sprint 6 Milestone
- [ ] App uses the new Kinetic Logic design system across all screens
- [ ] 5-tab bottom navigation bar is functional with correct active states
- [ ] Settings screen allows users to toggle theme, switch distance units, export logs, clear data, and view app info
- [ ] All preferences persist across app restarts
- [ ] All existing functionality (CRUD, calculations, charts) continues to work correctly with the new design system and navigation

---

### 6.6 Sprint 6 Testing

#### 6.6.1 Unit Tests
- [ ] SettingsRepository: read/write preferences correctly
- [ ] Theme mode preference state management
- [ ] Distance unit conversion logic (km → mi display)
- [ ] Clear data operation (verify all entries deleted)

#### 6.6.2 Integration Tests
- [ ] DAO deleteAll operation (used by Clear Data)
- [ ] Settings persistence across app restart (write preference, kill app, verify on reopen)

#### 6.6.3 Manual Testing
- [ ] Theme switching: Light → Dark → System, verify all screens update
- [ ] Distance units: Switch km ↔ mi, verify Dashboard KPIs and History entries convert
- [ ] Bottom nav: All 5 tabs navigate correctly, active tab shows filled icon (Icons.Rounded), inactive tabs show outlined icon (Icons.Outlined)
- [ ] Add tab: Opens entry form from any screen
- [ ] Settings: Export Logs works from Settings screen
- [ ] Settings: Clear Data shows confirmation, clears all entries, dashboard resets to empty state
- [ ] Font scaling: Settings screen handles large font sizes without clipping
- [ ] Accessibility: All interactive elements have content descriptions
- [ ] Full regression: add → edit → delete → dashboard updates → charts update with new design

---

## Deferred / Not in Mini Scope

> More features have been done in original full application , This is completely diff , in terms of UI wise

*End of Document*

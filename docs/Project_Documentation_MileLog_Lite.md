# MileLog Lite - Technical Project Documentation

MileLog Lite is a lightweight, offline-first Android application designed for streamlined fuel logging, automatic mileage calculations, and visual cost analytics. This document provides a complete technical guide for developers, reviewers, and maintainers.

---

## 1. Setup and Installation Guide

### 1.1 Development Prerequisites
Ensure your development environment meets the following specifications:
- **Operating System:** Windows 10/11, macOS (12+), or Linux (Ubuntu 20.04+)
- **Integrated Development Environment:** Android Studio Koala (2024.1.1+) or newer
- **Java Development Kit:** JDK 17 (recommended: Android Studio bundled JBR 17 or Eclipse Temurin JDK 17)
- **Android SDK Targets:**
  - Minimum SDK: API 26 (Android 8.0 Oreo)
  - Target SDK: API 36
  - Compile SDK: API 35 / 37
  - Android Build Tools and Platform-Tools (`adb`)

### 1.2 Environment Variables
Configure your system environment variables before building from the command line:

```powershell
# Point JAVA_HOME to your JDK 17 or Android Studio JBR installation
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Point ANDROID_HOME to your Android SDK installation directory
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
```

### 1.3 Project Setup & Repository Cloning
```bash
git clone https://github.com/ombpawar/MileLog-Lite.git
cd MileLog-Lite/MileLog-Lite
```

### 1.4 Command-Line Execution Reference

| Action | Command | Expected Output |
|---|---|---|
| **Unit Tests** | `.\gradlew.bat testDebugUnitTest` | Executes domain calculations & validator test suites |
| **Benchmark Tests** | `.\gradlew.bat connectedDebugAndroidTest` | Runs on-device database queries and UI test suites |
| **Debug Build** | `.\gradlew.bat assembleDebug` | Produces `app-debug.apk` in `app/build/outputs/apk/debug/` |
| **Install & Launch** | `adb install -r app/build/outputs/apk/debug/app-debug.apk`<br>`adb shell am start -n "com.example.myapplication/.MainActivity"` | Installs and opens the application on device |

---

## 2. Feature Specification

### 2.1 Core Capabilities
- **Fast Fill-up Logging:** Complete a new log in fewer than three interactions from the home screen using the dedicated button or Floating Action Button (FAB).
- **Automated Calculations:**
  - **Per-Fillup Fuel Economy:** Calculated as `(Current Odometer - Previous Odometer) / Fuel Volume` (km/L).
  - **Running Average Mileage:** Evaluated across all valid fuel intervals (`Total Distance / Total Fuel excluding first fill`).
  - **Cost per Kilometer:** Operating cost ratio calculated as `Total Cost / Total Distance`.
  - **Summary Metrics:** Total expenditure, total liters consumed, and latest recorded odometer reading.
- **Visual Trend Analytics:**
  - **Mileage Trend Line Chart:** Chronological plot showing fuel economy progression over fill-up dates.
  - **Monthly Spend Bar Chart:** Grouped bar chart depicting total fuel costs (INR) categorized by calendar month.
  - **Empty / Single-Entry States:** Contextual fallback views when fewer than two records are available for charting.
- **Full History Management:**
  - Reverse-chronological feed of all recorded fill-ups.
  - Tap-to-edit interaction for modifying existing entries.
  - Guarded deletion flow requiring explicit confirmation before record removal.
- **Input Validation Rules:**
  - Rejects odometer values that are equal to or lower than the previous recorded reading.
  - Rejects zero or negative values for fuel volume and total cost.
  - Displays inline contextual error notices below each form field.
- **Offline Persistence:**
  - Backed by Room SQLite with database indices on `date`, `odometer`, and composite `(date, odometer)` for sub-millisecond query performance.
- **Accessibility & Font Scaling:**
  - Fully dynamic layout capable of scaling up to 200% system font size without truncation, overlap, or scroll clipping.

---

## 3. Core Screen Overview

### 3.1 Dashboard Screen (`DashboardScreen.kt`)
- **Purpose:** Central landing screen displaying primary vehicle metrics and one-tap access to all workflows.
- **UI Structure:**
  - *Metric Cards:* 2x2 grid containing Latest Odometer (km), Total Spend (INR), Average Mileage (km/L), and Cost per km (INR/km).
  - *Primary Action:* "Add Fuel Entry" button for rapid logging.
  - *Navigation Cards:* Two cards routing to "Fuel History" and "Charts & Insights".
  - *Empty State:* Displayed when zero entries exist, prompting the user to add their initial record.
- **Visual Reference:**
  > `[Screenshot Placeholder: Dashboard Screen]`  
  > *Caption: Dashboard showing summary metric cards, quick-add button, and navigation tiles.*

---

### 3.2 Add / Edit Fuel Entry Screen (`AddEditEntryScreen.kt`)
- **Purpose:** Input form for logging a new fill-up or updating an existing entry.
- **UI Structure:**
  - *Date Selector:* Field opening a Material 3 date picker dialog (defaults to today's date).
  - *Odometer Field:* Numeric input showing the previous reading as helper text.
  - *Fuel Quantity Field:* Decimal input formatted in liters (L).
  - *Total Cost Field:* Decimal input formatted in Indian Rupees (INR).
  - *Primary Button:* "Save Entry" / "Update Entry" with keyboard-aware padding (`imePadding`).
- **Visual Reference:**
  > `[Screenshot Placeholder: Add/Edit Entry Screen]`  
  > *Caption: Entry form with date picker, contextual helper text, and validation states.*

---

### 3.3 Fuel History Screen (`HistoryScreen.kt`)
- **Purpose:** Chronological log of all recorded fill-ups.
- **UI Structure:**
  - *Entry Cards:* Displays date, odometer reading, fuel volume (L), and total cost (INR).
  - *Edit Action:* Tapping anywhere on a card opens the entry in edit mode.
  - *Delete Action:* Dedicated delete button opening a modal confirmation dialog.
  - *Floating Action Button:* Fixed bottom-right button to quickly add a new entry.
- **Visual Reference:**
  > `[Screenshot Placeholder: Fuel History Screen]`  
  > *Caption: Scrollable list of fuel history cards with delete dialog.*

---

### 3.4 Charts & Insights Screen (`ChartsScreen.kt`)
- **Purpose:** Visual analytics suite providing graphical representation of fuel economy and spend patterns.
- **UI Structure:**
  - *Mileage Trend Card:* Line chart plotting km/L efficiency per fill-up using smooth curves.
  - *Monthly Spend Card:* Bar chart plotting total expenditure grouped by calendar month.
  - *Fallback View:* Friendly notification displayed when fewer than two entries are available.
- **Visual Reference:**
  > `[Screenshot Placeholder: Charts & Insights Screen]`  
  > *Caption: Visual charts showing mileage progression and monthly expenditure.*

---

## 4. Technical Architecture Summary

| Layer | Primary Components | Responsibility |
|---|---|---|
| **Presentation (UI)** | Jetpack Compose, Material Design 3 | Declarative screen layouts, theme tokens, and dynamic font scaling |
| **State Management** | ViewModels, StateFlow, Coroutines | Exposing immutable UI states and processing user actions |
| **Domain Logic** | `MileageCalculator`, `FuelEntryValidator` | Pure Kotlin business logic and validation rules |
| **Data & Persistence** | Room, SQLite, `FuelEntryRepository` | Local indexed database queries and transactional data operations |
| **Visualization** | MPAndroidChart via Compose AndroidView | Native rendering of line and bar charts |

---

## 5. Known Limitations

1. **Single-Vehicle Support:** The app tracks one vehicle profile per installation. Multi-vehicle management is omitted from the lite scope.
2. **Local-Only Storage:** All data is stored locally in SQLite. Cloud synchronization and multi-device account logins are not included.
3. **No File Export / Import:** CSV or PDF report export features are not implemented in the current version.
4. **Full-Tank Assumption:** Calculations assume each recorded fill-up fills the tank completely. Partial fill-up tracking is deferred to future releases.
5. **Fixed Currency Formatting:** Currency amounts are formatted in Indian Rupees (INR) by default without multi-currency switching options.

---

## Related Documentation

- [Product Requirements Document (PRD)](PRD_MileLog_Lite.md)
- [Sprint Execution Plan](Sprint_Plan_MileLog_Lite.md)

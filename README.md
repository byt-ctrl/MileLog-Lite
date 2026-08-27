# MileLog Lite

MileLog Lite is a clean, offline-first Android application designed for quick fuel logging and real-time vehicle mileage analytics. Built with modern Android technologies (Kotlin, Jetpack Compose, Material Design 3, Room SQLite, and MPAndroidChart), it enables vehicle owners to log fill-ups in seconds and track fuel efficiency trends without requiring an internet connection.

---

## 1. Setup Instructions

### Prerequisites
- **Android Studio:** Koala (2024.1.1+) or newer
- **JDK:** Java 17 or higher (or the bundled Android Studio Java Runtime)
- **Android SDK:**
  - Minimum SDK: API 26 (Android 8.0 Oreo)
  - Target SDK: API 36
  - Compile SDK: API 35 / 37

### Environment Configuration
Ensure your environment variables are configured in your shell (PowerShell on Windows, or Bash/Zsh on macOS/Linux):

```powershell
# Set JAVA_HOME to your JDK 17 or Android Studio JBR path
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Set ANDROID_HOME to your Android SDK installation directory
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
```

### Local Build and Run Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ombpawar/MileLog-Lite.git
   cd MileLog-Lite/MileLog-Lite
   ```

2. **Run Unit Tests:**
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```

3. **Run Device / Benchmark Tests:**
   ```powershell
   .\gradlew.bat connectedDebugAndroidTest
   ```

4. **Build the Debug APK:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

5. **Install on Device or Emulator:**
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n "com.example.myapplication/.MainActivity"
   ```

---

## 2. Feature List

- **Fast One-Tap Logging:** Quick-add action directly accessible from the dashboard and Floating Action Button (FAB) to log fill-ups in under 5 seconds.
- **Automated Calculations:**
  - **Per-Fillup Mileage:** Automatically computed as `(Current Odometer - Previous Odometer) / Fuel Quantity` (km/L).
  - **Average Mileage:** Running average calculated across all recorded fill-ups.
  - **Cost per Kilometer:** Operating expense ratio (`Total Fuel Spend / Total Distance Traveled`).
  - **Fleet Totals:** Running totals for distance traveled, fuel consumed, and overall expenditure.
- **Visual Analytics:**
  - **Mileage Trend Chart:** Chronological line graph visualizing fuel efficiency over successive fill-ups.
  - **Monthly Spend Chart:** Bar graph aggregating total fuel expenses grouped by calendar month.
- **Smart Form Validation:** Real-time feedback preventing non-increasing odometer entries, negative fuel quantities, or missing cost values.
- **History Management:** Reverse-chronological log of all entries with one-tap editing and guarded deletion confirmation dialogs.
- **Offline-First Storage:** Local Room SQLite database with multi-column indices for fast query performance.
- **Dynamic Accessibility:** Fully responsive layout tested up to 200% system font scaling without text clipping or overlapping.

---

## 3. Core Screen Overview

| Screen | Description | Primary Actions |
|---|---|---|
| **Dashboard** | Displays summary metric cards (Latest Odometer, Total Spend, Avg Mileage, Cost/km) and quick-action navigation. | Add Fuel Entry, View History, Open Charts |
| **Add / Edit Entry** | Clean form interface for entering date, odometer, fuel volume (L), and total cost (INR). | Date Picker, Inline Validation, Save / Update |
| **Fuel History** | Scrollable list of past fuel records displaying date, odometer, liters, and cost. | Tap to Edit, Delete with Confirmation |
| **Charts & Insights** | Interactive visual charts showing mileage efficiency trends and monthly spend patterns. | View Line Trend, View Monthly Spend Bars |

### Visual Layout Guide

- **Dashboard Screen:**
  - *Summary Metrics:* 2x2 grid displaying Latest Odometer, Total Fuel Spend, Average Mileage, and Cost per km.
  - *Action Area:* Primary "Add Fuel Entry" button, "View History" tile, and "Charts & Insights" tile.
  - *Placeholder:* `[Screenshot: Dashboard Screen - Metric Cards and Action Tiles]`

- **Add / Edit Fuel Entry Screen:**
  - *Input Controls:* Date selector modal, numeric inputs for odometer, fuel quantity, and total cost.
  - *Validation:* Contextual helper text and inline red error states.
  - *Placeholder:* `[Screenshot: Add/Edit Entry Screen - Input Form with Field Validation]`

- **Fuel History Screen:**
  - *Record List:* Elevated cards with fill-up date, odometer reading, liters, and total cost.
  - *Management:* Tap to edit, trash icon to delete with confirmation dialog.
  - *Placeholder:* `[Screenshot: Fuel History Screen - Chronological List with Delete Dialog]`

- **Charts & Insights Screen:**
  - *Line Chart:* Mileage (km/L) per fill-up plotted chronologically.
  - *Bar Chart:* Total monthly fuel expenditure (INR) grouped by calendar month.
  - *Placeholder:* `[Screenshot: Charts & Insights Screen - Mileage and Spend Charts]`

---

## 4. Technical Specifications

| Component | Technology | Role |
|---|---|---|
| Language | Kotlin | Core application language |
| UI Framework | Jetpack Compose (Material 3) | Declarative UI and dynamic theme styling |
| State Management | StateFlow & ViewModel | Reactive unidirectional data flow |
| Local Database | Room (SQLite) | Indexed offline data persistence |
| Charting Library | MPAndroidChart | Line and bar visualization rendering |
| Unit Testing | JUnit 4 & Coroutines Test | Calculation and validator test suites |

---

## 5. Known Limitations

1. **Single Vehicle Tracking:** Designed for single-vehicle tracking per installation (multi-vehicle profiles are not included in the lite scope).
2. **Local Storage Only:** Data is stored strictly on the local device; cloud backup and multi-device synchronization are not included.
3. **No File Export:** Exporting data to CSV, Excel, or JSON formats is not currently supported.
4. **Full-Tank Assumption:** Mileage calculations assume complete fill-ups between consecutive logs.
5. **Single Currency:** Currency values are formatted in Indian Rupees (INR); dynamic multi-currency switching is not configured.

---

## Documentation Links

- [Complete Project Documentation](docs/Project_Documentation_MileLog_Lite.md)
- [Product Requirements Document (PRD)](docs/PRD_MileLog_Lite.md)
- [Sprint Execution Plan](docs/Sprint_Plan_MileLog_Lite.md)

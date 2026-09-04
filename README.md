# MileLog Lite

MileLog Lite is a simple, offline-first Android app for logging fuel fill-ups and tracking mileage and spending. Log a fill-up in seconds, see your average mileage and cost per km automatically, and view trends over time - no internet needed.

Built with Kotlin, Jetpack Compose, Material 3, Room (local database), and MPAndroidChart.

---

## What you can do

- **Log fuel entries:** Add, edit, and delete fill-ups with date, odometer reading, fuel amount (liters), total cost, and fuel type.
- **Track by fuel type:** Choose Petrol, Diesel, or CNG for each entry, and filter your history by type.
- **See totals automatically:** Dashboard shows latest odometer, total spend, average mileage (km/L), and cost per km. Values update as soon as you change an entry.
- **Catch mistakes:** The form blocks missing fields, negative values, and odometer readings that don't go up, with clear messages next to each field.
- **View trends:** Mileage trend line chart per fill-up, and monthly spend bar chart grouped by month, including per-category views.
- **Manage history:** List of all entries (newest first), tap to edit, delete with confirmation and undo.
- **Export:** Save your full history to a CSV file (`milelog_fuel_entries.csv`) via the system file picker.
- **Works offline:** All data stays on your device and persists after restart.

---

## Screens

| Screen            | What you'll see                                                                                                     | Key actions                              |
| ----------------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| Dashboard         | Summary cards (odometer, spend, mileage, cost/km). Friendly empty state when you have no entries yet.               | Add entry, view history, open charts     |
| Add / Edit Entry  | Form for date, odometer, liters, cost, and fuel type (Petrol / Diesel / CNG) with inline validation.               | Pick date, choose category, save/update  |
| Fuel History      | All entries newest-first with All / Petrol / Diesel / CNG filter chips.                                             | Tap to edit, delete (with undo), export CSV |
| Charts & Insights | Mileage trend and monthly spend charts. Friendly message when there are fewer than 2 entries.                       | View trend, view monthly spend           |

---

## Getting started

**You need:**
- Android Studio Koala (2024.1.1) or newer
- JDK 17 (bundled with Android Studio is fine)
- Android SDK: Min API 26 (Android 8.0), Target API 36

**Run it:**

1. Clone and open the project:
   ```bash
   git clone https://github.com/byt-ctrl/MileLog-Lite.git
   cd MileLog-Lite/MileLog-Lite
   ```
   Then open the `MileLog-Lite` folder in Android Studio and press Run.

2. Or build from the command line (Windows):
   ```powershell
   .\gradlew.bat assembleDebug
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

3. To run tests:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```

> Tip (Windows): set `$env:JAVA_HOME` to your JDK 17 path and `$env:ANDROID_HOME` to your SDK path if Gradle can't find Java or Android SDK.

---

## Limitations

- Tracks one vehicle per install.
- Data lives only on the device - no cloud backup or sync.
- Export to CSV only - no import.
- Mileage assumes full-tank fill-ups between logs.
- Costs are shown in Indian Rupees (INR).
- Single language, no multi-currency switching.

---

## Screenshots

<table>
  <tr>
    <td align="center"><b>Empty Dashboard</b></td>
    <td align="center"><b>Add Entry Form</b></td>
    <td align="center"><b>Dashboard with Data</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/01_empty_dashboard.png" width="250" /></td>
    <td><img src="docs/screenshots/02_add_entry_form.png" width="250" /></td>
    <td><img src="docs/screenshots/06_dashboard_after_entry3.png" width="250" /></td>
  </tr>
  <tr>
    <td align="center"><b>Fuel History</b></td>
    <td align="center"><b>Charts & Insights</b></td>
    <td align="center"><b>Delete Confirmation</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/08_history_3_entries.png" width="250" /></td>
    <td><img src="docs/screenshots/07_charts_with_3_entries.png" width="250" /></td>
    <td><img src="docs/screenshots/13_delete_confirm_dialog.png" width="250" /></td>
  </tr>
</table>

---

## More docs

- [Project Documentation](docs/Project_Documentation_MileLog_Lite.md)
- [Product Requirements (PRD)](docs/PRD_MileLog_Lite.md)
- [Sprint Execution Plan](docs/Sprint_Plan_MileLog_Lite.md)

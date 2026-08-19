# MileLog Mini

A lightweight, offline-first Android fuel logger built with Kotlin and Jetpack Compose. Track fill-ups, mileage, and fuel costs at a glance.

## Features

- Add, edit, and delete fuel entries (date, odometer, liters, cost)
- Automatic mileage (km/l) and cost-per-km calculations
- Dashboard with running totals and averages
- Mileage trend line chart and monthly spend bar chart
- Fully offline with local persistence (Room database)

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM
- **Database:** Room (SQLite)
- **Charts:** MPAndroidChart
- **Testing:** JUnit

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17
- Android SDK (API 26+)

### Build & Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and let dependencies resolve.
4. Run on an emulator or physical device.

## Project Structure

```
app/src/main/java/
├── data/       # Room entity, DAO, database, repository
├── domain/     # Calculation and validation logic
└── ui/         # Compose screens, ViewModels, theme
```

## Documentation

- [Product Requirements Document](docs/PRD_MileLog_Mini.md)
- [Sprint Execution Plan](docs/Sprint_Plan_MileLog_Mini.md)

## License

This project is created for educational purposes.

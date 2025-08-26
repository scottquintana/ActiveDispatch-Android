# Active Dispatch (Android)

**Active Dispatch** connects you to the **Metro Nashville Police Department** via their public API and displays all **active police calls** in real time.

Active incidents can be viewed in a clean **scrollable list** or plotted on a **map view**.  
Each incident is assigned a **color + icon badge**, based on its category and threat level, so you can quickly scan for what matters.  
On the map, tapping an incident reveals its **exact location and additional details**.

---

## Features
- **Jetpack Compose UI**  
  Built fully with Compose for declarative UI, smooth animations, and responsive layouts.
- **Modern Architecture**  
  MVVM with Kotlin coroutines and StateFlow for reactive data updates.
- **Location-Aware**  
  Shows distance from your current location (falls back to city center when running in emulator).
- **Dynamic Badges**  
  Category-driven colors and icons (e.g. burglary alarms, assaults, wires down).
- **Map Integration**  
  Quick access via a floating action button to toggle into map view.
- **Cross-platform vision**  
  Designed with Ktor + Kotlin serialization, so we can expand to **Kotlin Multiplatform** if needed.

---

## Tech Stack
- **Kotlin** with coroutines + Flow
- **Jetpack Compose** for UI
- **Ktor Client** for networking
- **Google Maps Compose**
- **Material 3** components
- **MVVM** architecture pattern

---

## Screens
- **Home screen**: Hero header + scrollable incident list
- **Incident cells**: Badge, title, neighborhood + distance, relative time
- **Map view**: Pins incidents on the map, with tap-to-expand details

---

## Inspiration
The iOS version of Active Dispatch was originally built with UIKit (inspired by SwiftUI projects with gradients, reactive color changes, and MVVM). The Android version takes those lessons forward with **Jetpack Compose** for a fully modern, declarative Android app.

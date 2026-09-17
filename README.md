# Resonance Launcher

**Resonance Launcher** is a modern, media-centric Android TV and Google TV home screen launcher built with **Jetpack Compose for TV (Material 3)**, **Kotlin Coroutines**, and **Ktor**. Designed for television hardware and D-pad remote interaction, it provides dynamic artwork, ambient trailer previews, streaming service discovery, flexible application management, and remote browser setup via QR code pairing.

---

## 🌟 Key Features

* **📺 Built for Android TV & Google TV**: Built using `@androidx.tv.material3` components with full D-pad remote navigation, focus management (`focusRestorer`), and smooth TV scaling animations.
* **🎬 Streaming Service Content Discovery**: Integrates with TMDb to discover popular movies and TV shows filtered by installed streaming apps (Netflix, Prime Video, Disney+, Max, Apple TV+, Hulu, YouTube, Peacock, Paramount+, Plex).
* **🎞️ Ambient Video Previews & Trailers**: Details screens feature seamless YouTube trailer previews in the background with interactive audio muting/unmuting and full-screen trailer support.
* **📱 Customizable App Tray & Grid**:
  * Pin and reorder favorite Android TV applications in a customizable home row.
  * Context menu (via long press) to move, hide, favorite, or uninstall applications.
  * Overlay grid to access all installed applications easily.
* **🙈 Hidden Apps Management**: Hide unwanted pre-installed or system applications and manage/restore them anytime in Settings.
* **🔗 Web Companion & QR Code Setup**: Built-in Ktor embedded HTTP server allows configuring API keys from a phone or web browser by scanning a QR code on the TV screen.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technologies / Libraries |
| :--- | :--- |
| **Language** | Kotlin 2.x |
| **UI Framework** | Jetpack Compose for TV (`androidx.tv.material3`, `androidx.tv.foundation`) |
| **Navigation** | Compose Navigation (`androidx.navigation.compose`) |
| **Architecture** | MVVM (UI / ViewModel / Repository / Data Source) |
| **Networking & API** | Ktor Client (`cio`, `content-negotiation`, `json`) & TMDb API v3 |
| **Embedded Server** | Ktor Server (`cio`, `core`) for remote phone/browser configuration |
| **Local Storage** | AndroidX DataStore Preferences |
| **Image Loading** | Coil 3 (`coil-compose`, `coil-network-okhttp`) & AndroidX Palette |
| **Video Playback** | Android YouTube Player (`com.pierfrancescosoffritti.androidyoutubeplayer`) |
| **QR Code Generation**| `io.github.g0dkar:qrcode-kotlin` |

---

## 📁 Project Structure

```
resonance-launcher/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── assets/
│           │   ├── setup.html          # Remote web companion setup page
│           │   └── save.html           # Setup submission confirmation page
│           ├── java/io/github/corbant/resonancelauncher/
│           │   ├── MainActivity.kt     # App entry point & dependency wiring
│           │   ├── data/
│           │   │   ├── repository/     # App, Media, and Preferences repositories
│           │   │   ├── server/         # Embedded Ktor HTTP server for QR setup
│           │   │   └── tmdb/           # TMDb API client & provider package mappings
│           │   ├── model/              # Data models (AppItem, MediaItem, MediaDetails, WatchProvider)
│           │   ├── ui/
│           │   │   ├── features/
│           │   │   │   ├── details/    # Media details & ambient trailer player
│           │   │   │   ├── home/       # Home screen, app rows, media carousels, context dialogs
│           │   │   │   └── settings/   # Settings screen, QR pairing & hidden apps modals
│           │   │   ├── navigation/     # Navigation routes & host
│           │   │   └── theme/          # TV Material 3 design theme
│           │   └── util/               # Intent helpers & QR code generator
│           └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug (2024.2+) or newer
* **Android SDK**: API Level 34+ (compileSdk 37, minSdk 24)
* **JDK**: 11 or newer
* **Device**: Android TV / Google TV physical device or Android TV Emulator

### Building & Deploying

1. **Clone the repository:**
   ```bash
   git clone https://github.com/corbant/resonance-launcher.git
   cd resonance-launcher
   ```

2. **Build the Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on Android TV via ADB:**
   ```bash
   adb connect <your-tv-ip-address>
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Set as Default Launcher:**
   * Press the **Home** button on your TV remote and select **Resonance Launcher** when prompted, or run:
     ```bash
     adb shell cmd package set-home-activity io.github.corbant.resonancelauncher/.MainActivity
     ```

---

## ⚙️ Configuration & Setup

To enable movie & TV recommendations and trailer previews:

1. Open **Launcher Settings** (gear icon in the top right corner).
2. Select **Configuration & API Keys**.
3. Scan the displayed QR Code with your smartphone or open the printed URL (`http://<TV_IP>:8080`) in a browser on your local network.
4. Input your [TMDb API Key](https://www.themoviedb.org/settings/api) and submit.
5. The TV launcher will immediately persist the setting and reload the catalog recommendations.

---

## 📄 License

This project is open source and available under the terms of the project repository license.

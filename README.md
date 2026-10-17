# HomeArcade TV

**Native Android TV companion for HomeArcade** — the ultimate retro gaming frontend for Home Assistant.

Browse your ROM library with a fluid 10-foot overscan TV UI, discover games with dynamic hero spotlights, switch player profiles, manage multi-disc games and save states, and launch games using high-performance sub-millisecond **WebRTC Native Streaming**.

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" alt="HomeArcade TV icon" />
</p>

## Features

- **100% Native Jetpack Compose for TV** — Built with Material 3 for TV (`androidx.tv:tv-material`), spring-animated D-pad focus scaling, neon glow accents, and 48dp overscan safety margins.
- **StreamingHub TV Pattern** — Dynamic Top Navigation Bar ("Tutti", "Preferiti", "Più Giocati", "Per Console"), Hero Billboard reacting in real-time to game focus with background art & metadata.
- **WebRTC Native Streaming Player** — Ultra-low latency 60 FPS gameplay powered by `home-arcade-ha` WebRTC Native Runner (`/api/roms/:id/stream-player`) with automatic WASM fallback.
- **Automatic LAN Server Discovery** — Zero-configuration radar probing your local subnet on ports 5000 and 9876 with quick manual IP/port override.
- **Multi-Profile Support** — Select or switch local user profiles with avatars, sync play times, and isolated favorites.
- **Rich Game Details & Multi-Disc** — Synopsis, developer, genre, release year, community rating, multi-disc disc picker, and visual save state browser.
- **Instant Search** — Debounced search across your whole library with D-pad navigation and console badge tagging.
- **Retro Arcade Design System** — Neon Pink (`#EC4899`), Electric Violet (`#8B5CF6`), Cyber Amber (`#F59E0B`), and Deep Void (`#080A10`).
- **High-Performance Image Caching** — 150MB disk caching via Coil 2.7.0 for instant box art and backdrop loading.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HomeArcade-TV Architecture               │
└─────────────────────────────────────────────────────────────┘

  ┌─────────────────────────────────────────────────────────┐
  │         100% Native Jetpack Compose for TV (UI)         │
  │                                                         │
  │  [SetupScreen] ── [ProfilePicker] ── [HomeScreen]       │
  │         │                                  │            │
  │   Discovery Radar             HeroBillboard + Shelves   │
  │                                            │            │
  │             ┌──────────────────────────────┼─────────┐  │
  │             ▼                              ▼         ▼  │
  │      [DetailsScreen]                 [Search]   [Settings]
  │      Multi-Disc & Saves                                 │
  └────────────────────────────┬────────────────────────────┘
                               │ Launch WebRTC Stream
                               ▼
  ┌─────────────────────────────────────────────────────────┐
  │            PlayerScreen (Hardware Accelerated)          │
  │   /api/roms/:id/stream-player (WebRTC Runner / WASM)    │
  └─────────────────────────────────────────────────────────┘
                               ▲
  ┌────────────────────────────┴────────────────────────────┐
  │                 Data Layer & Networking                 │
  │  • Ktor 3.0.1 (OkHttp Engine + Kotlinx Serialization)   │
  │  • ServerDiscoveryManager (Subnet Scanning :5000/:9876) │
  │  • RomRepository & ProfileRepository                    │
  │  • DataStore Preferences (Server IP, Profile, Engine)   │
  └─────────────────────────────────────────────────────────┘
```

### Tech Stack

| Layer | Technology |
|---|---|
| **UI Framework** | Jetpack Compose for TV (`androidx.tv:tv-material:1.0.0`, `tv-foundation:1.0.0-alpha12`) |
| **Networking** | Ktor Client 3.0.1 (`ktor-client-okhttp`, `ktor-client-content-negotiation`) |
| **Serialization**| Kotlinx Serialization JSON 1.7.3 |
| **Image Loading**| Coil 2.7.0 with custom 150MB disk cache |
| **Persistence** | Jetpack DataStore Preferences 1.1.2 |
| **Streaming** | Hardware-accelerated WebRTC Native Player (`/api/roms/:id/stream-player`) |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 35 (Android 15) |

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Ladybug or newer
- Android TV device, Google TV, or Android TV Emulator (API 26+)
- A running [HomeArcade-HA](https://github.com/JMaroz/HomeArcade-HA) add-on instance (v2.65.1+ recommended) with port 5000 or 9876 reachable on your local network.

### Build & Run

```bash
# Clone
git clone https://github.com/JMaroz/HomeArcade-TV.git
cd HomeArcade-TV

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test

# Install on connected TV/Emulator via ADB
./gradlew installDebug
```

## First-Time Setup

1. Launch **HomeArcade TV** on your Android TV.
2. The **Radar Discovery** will automatically scan your local network for active HomeArcade servers on ports 5000 and 9876.
3. Select the discovered server (or enter IP/port manually).
4. Choose your **Player Profile**.
5. Start exploring your retro library!

## Project Structure

```
app/src/main/java/com/homearcade/tv/
├── HomeArcadeTvApp.kt           # Coil 150MB disk cache & Application entry
├── MainActivity.kt              # Main TV Activity & Compose root
├── data/
│   ├── api/
│   │   ├── ApiClient.kt         # Ktor client with OkHttp engine
│   │   ├── HomeArcadeApi.kt     # Full REST endpoints (ROMs, profiles, saves)
│   │   └── ServerDiscoveryManager.kt # LAN subnet port scanner
│   ├── local/
│   │   └── AppPreferences.kt    # DataStore persistence
│   ├── model/
│   │   └── Dtos.kt              # Serializable data models
│   └── repository/
│       ├── RomRepository.kt     # In-memory cache & shelves builder
│       └── ProfileRepository.kt # Profile switching & state
├── ui/
│   ├── components/
│   │   ├── HeroBillboard.kt     # Dynamic hero with background & badges
│   │   ├── TvButton.kt          # Accessible TV remote button
│   │   ├── TvFocusModifier.kt   # 1.08x scale + neon pink glow border
│   │   ├── TvGameCard.kt        # 3:4 box art card with status chips
│   │   ├── TvLoadingSpinner.kt  # Retro radar loading indicator
│   │   └── TvShelfRow.kt        # Horizontal carousel with overscan padding
│   ├── details/                 # Game details, discs, and save states
│   ├── home/                    # Top navigation tabs and shelf rows
│   ├── navigation/              # NavRoutes and AppNavigation graph
│   ├── player/                  # Fullscreen WebRTC Stream Player & exit dialog
│   ├── profiles/                # User profile selector
│   ├── search/                  # Instant debounced search grid
│   ├── settings/                # Server info, player mode, and diagnostics
│   ├── setup/                   # LAN discovery & manual connection
│   └── theme/                   # Retro Arcade color palette & TV typography
```

## Related

- [HomeArcade-HA](https://github.com/JMaroz/HomeArcade-HA) — Server-side Home Assistant add-on providing ROM management, WebRTC native streaming, and multi-source scrapers.
- [StreamingHub TV](https://github.com/JMaroz/streaming-hub-tv) — Sister project inspiring the native Android TV UI architecture.

## License

MIT

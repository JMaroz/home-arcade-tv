# Piano di Lavoro: Aggiornamento Dipendenze e Risoluzione Errori di Build/Uso

Data: 2026-10-17

## Obiettivo
Aggiornare tutte le dipendenze del progetto all'ultima versione stabile disponibile (AGP 9.4.1, Gradle 9.7.1, Kotlin 2.4.21, Compose BOM 2026.09.00, AndroidX con compileSdk 37, Ktor 3.6.0, OkHttp 5.5.0, Coil 3.6.3, Coroutine 1.11.0, Serialization 1.11.0), risolvendo le breaking changes di API (migrazione Coil 3, built-in Kotlin con compilerOptions, regole R8 ProGuard) e garantendo compilazione e test al 100% verdi.

## Scope & Non-Goals
- **Scope**:
  - `gradle/wrapper/gradle-wrapper.properties`: aggiornamento a Gradle 9.7.1.
  - `gradle/libs.versions.toml`: aggiornamento catalogo versioni all'ultima versione stabile.
  - `build.gradle.kts` e `app/build.gradle.kts`: transizione al supporto nativo built-in Kotlin di AGP 9.4.1, impostazione `compileSdk = 37`, configurazione `compilerOptions.jvmTarget = JVM_17`, inclusione `libs.coil.network.okhttp` e mantenimento configurazione release signing.
  - `app/proguard-rules.pro`: aggiornamento regole per Coil 3 (`coil3.**`).
  - Sorgenti Kotlin (`HomeArcadeTvApp.kt`, `HeroBillboard.kt`, `TvGameCard.kt`, `DetailsScreen.kt`): migrazione API a Coil 3 (`coil3.compose.AsyncImage`, `SingletonImageLoader.Factory`, `OkHttpNetworkFetcherFactory`, Okio Path).
  - `.gitignore`: esclusione della cache `.kotlin/`.
  - Verifica empirica: esecuzione `./gradlew test`, `./gradlew assembleDebug`, `./gradlew assembleRelease`.
  - Aggiornamento snapshot `docs/HANDOFF.md`.
- **Non-Goals**:
  - Non modificare logiche di business (WebRTC streaming, discovery server LAN, navigazione D-Pad).
  - Non aggiungere dipendenze non necessarie (YAGNI).

## Task Checklist
- [x] Creazione del piano strutturato
- [x] Aggiornamento Gradle Wrapper a 9.7.1
- [x] Aggiornamento `gradle/libs.versions.toml` con le ultime versioni stabili
- [x] Adattamento `build.gradle.kts` e `app/build.gradle.kts` per AGP 9.4.1 e built-in Kotlin (`compileSdk = 37`, `compilerOptions`)
- [x] Aggiornamento regole ProGuard in `app/proguard-rules.pro` per Coil 3
- [x] Aggiornamento sorgenti per Coil 3 (`HomeArcadeTvApp.kt`, `HeroBillboard.kt`, `TvGameCard.kt`, `DetailsScreen.kt`)
- [x] Aggiunta di `.kotlin/` a `.gitignore`
- [x] Verifica empirica: `./gradlew test` (Superata, 26 task eseguiti, 100% passati)
- [x] Verifica empirica: `./gradlew assembleDebug` (Superata, APK generato)
- [x] Verifica empirica: `./gradlew assembleRelease` (Superata, 52 task eseguiti con R8 e shrinking)
- [x] Aggiornamento finale `docs/HANDOFF.md`

## Strategia di Verifica
- `./gradlew test`: esecuzione di tutti i test unitari (Completata con successo).
- `./gradlew assembleDebug`: build dell'APK debug (Completata con successo).
- `./gradlew assembleRelease`: build con shrinking e R8 dell'APK release (Completata con successo).

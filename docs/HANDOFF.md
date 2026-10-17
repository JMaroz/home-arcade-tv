# Handoff Project Snapshot: Aggiornamento Toolchain e Dipendenze

## Stato Corrente
- **Toolchain Modernizzato**: Gradle Wrapper aggiornato a 9.7.1 e Android Gradle Plugin (AGP) a 9.4.1 con supporto built-in nativo a Kotlin (rimosso plugin legacy `kotlin-android` e configurato `compilerOptions.jvmTarget = JVM_17`).
- **Target SDK & Compatibilità**: `compileSdk` elevato a 37 per soddisfare i requisiti delle ultime release AndroidX, mantenendo `minSdk = 26` e `targetSdk = 35`.
- **Librerie Allineate alle Ultime Versioni Stabili**:
  - Kotlin 2.4.21, Compose BOM 2026.09.00, Activity Compose 1.13.0, Navigation Compose 2.10.2, DataStore 1.2.1, Lifecycle 2.11.0, Core KTX 1.19.1.
  - Ktor Client 3.6.0, OkHttp 5.5.0, Kotlinx Serialization 1.11.0, Kotlinx Coroutines 1.11.0.
- **Migrazione Coil 3 Completata**: Passaggio da Coil 2.7.0 a Coil 3.6.3 (`io.coil-kt.coil3`), integrazione modularizzata `coil-network-okhttp`, migrazione `SingletonImageLoader.Factory` in `HomeArcadeTvApp.kt` e aggiornamento import `AsyncImage` e regole R8 ProGuard.
- **Verifica Empirica**:
  - `./gradlew test`: superato al 100% (26 task eseguiti).
  - `./gradlew assembleDebug`: build APK completata con successo (38 task).
  - `./gradlew assembleRelease`: build con R8 e shrinking completata con successo (52 task).

## File Rilevanti
- `gradle/wrapper/gradle-wrapper.properties`: Upgrade a Gradle 9.7.1.
- `gradle/libs.versions.toml`: Catalogo aggiornato con le ultime release stabili e coordinate Coil 3.
- `build.gradle.kts`: Rimozione plugin `kotlin-android` (gestito nativamente da AGP 9).
- `app/build.gradle.kts`: `compileSdk = 37`, blocco top-level `kotlin.compilerOptions`, dipendenza `libs.coil.network.okhttp`.
- `app/proguard-rules.pro`: Regole keep e dontwarn per `coil3.**`.
- `app/src/main/java/com/homearcade/tv/HomeArcadeTvApp.kt`: Migrazione singleton Coil 3 (`SingletonImageLoader.Factory`, `OkHttpNetworkFetcherFactory`, Okio Path).
- `app/src/main/java/com/homearcade/tv/ui/components/HeroBillboard.kt`: Import `coil3.compose.AsyncImage`.
- `app/src/main/java/com/homearcade/tv/ui/components/TvGameCard.kt`: Import `coil3.compose.AsyncImage`.
- `app/src/main/java/com/homearcade/tv/ui/details/DetailsScreen.kt`: Import `coil3.compose.AsyncImage`.
- `.gitignore`: Esclusione cache interna compiler `.kotlin/`.
- `docs/plans/2026-10-17_upgrade_dependencies.md`: Piano di lavoro dettagliato con checklist completata.

## Prossimi Passi (Next Steps)
1. Eseguire commit atomico (`chore: upgrade dependencies to latest stable versions, AGP 9.4 and Coil 3`).
2. Test applicativo e verifica rendering immagini su emulatore Android TV o dispositivo fisico.
3. Configurazione credenziali di signing release in `keystore.properties` (opzionale per build firmata).

## Comandi Utili
- **Compilazione Debug**: `./gradlew assembleDebug`
- **Compilazione Release (R8)**: `./gradlew assembleRelease`
- **Esecuzione Test Unitari**: `./gradlew test`
- **Pulizia Build Cache**: `./gradlew clean`

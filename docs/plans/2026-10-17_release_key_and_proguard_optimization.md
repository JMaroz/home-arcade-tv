# Piano di Lavoro: Configurazione Release Key e Ottimizzazione Massima ProGuard / R8

Data: 2026-10-17

## Obiettivo
Generare una chiave di firma di release (keystore), configurare il progetto Android Gradle per utilizzarla nella compilazione `release` in modo sicuro (senza esporre secret nel version control, supportando file properties locale e variabili d'ambiente), e ottimizzare le regole ProGuard / R8 al massimo livello di minificazione e shrinking garantendo al 100% il funzionamento a runtime per l'architettura nativa Jetpack Compose TV, Ktor, OkHttp, Coil e Kotlinx Serialization.

## Scope & Non-Goals
- **Scope**:
  - Creazione directory `keystore/` e generazione della chiave di release `release.jks`.
  - Configurazione `.gitignore` per impedire il commit accidentale di keystore e credenziali (`*.jks`, `*.keystore`, `keystore.properties`).
  - Creazione del file di configurazione `keystore.properties` locale e del template versionato `keystore.properties.example`.
  - Aggiornamento di `app/build.gradle.kts` con `signingConfigs { create("release") { ... } }` collegato a `buildTypes.release.signingConfig`.
  - Ottimizzazione avanzata di `app/proguard-rules.pro`:
    - Rimozione regole obsolete per classi WebView e vecchi ViewModel rimossi.
    - Regole mirate per `@Serializable` DTOs (`kotlinx.serialization`), Ktor OkHttp engine, OkHttp3, Coil image loading, Coroutines.
    - Ottimizzazioni per rimozione log di debug (`android.util.Log`) in release.
    - Conservazione attributi essenziali per stacktrace e mapping (`SourceFile`, `LineNumberTable`).
  - Abilitazione esplicita di R8 Full Mode (`android.enableR8.fullMode=true`).
  - Verifica empirica: esecuzione `./gradlew test` e `./gradlew assembleRelease`.
- **Non-Goals**:
  - Modificare la logica di business o l'interfaccia utente dell'app.
  - Committare password o keystore privati su Git.

## Task Checklist
- [x] Creazione piano `docs/plans/2026-10-17_release_key_and_proguard_optimization.md`
- [x] Aggiornamento `.gitignore` per keystore e `keystore.properties`
- [x] Generazione del keystore di release con `keytool` (`keystore/release.jks`)
- [x] Configurazione di `keystore.properties` e `keystore.properties.example`
- [x] Configurazione `signingConfigs.release` in `app/build.gradle.kts`
- [x] Ottimizzazione e pulizia regole R8 / ProGuard in `app/proguard-rules.pro` e `gradle.properties`
- [x] Verifica empirica DoD: `./gradlew test` e `./gradlew assembleRelease`
- [x] Verifica della firma e integrità dell'APK generato (`apksigner verify`)
- [x] Aggiornamento `docs/HANDOFF.md`

## Strategia di Verifica
- `./gradlew test`: 49 task eseguiti, 100% test superati con successo.
- `./gradlew assembleRelease`: compilazione release completata con successo, shrinking risorse e codice R8 full mode.
- Verifica file APK prodotto: `app/build/outputs/apk/release/app-release.apk` (dimensione ~2.5 MB).
- Verifica firma APK: `apksigner verify --verbose --print-certs` conferma firma APK Signature Scheme v2 con certificato `CN=Home Arcade TV, OU=HomeArcade, O=HomeArcade, L=Rome, ST=RM, C=IT`.

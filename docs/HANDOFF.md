# Handoff Project Snapshot: Release Key & ProGuard / R8 Optimization

## Stato Corrente
- **Chiave di Release Creata e Configurata**:
  - Keystore PKCS12 generato in `keystore/release.jks` (RSA 2048-bit, validità 10.000 giorni, alias `homearcade`, DN `CN=Home Arcade TV, OU=HomeArcade, O=HomeArcade, L=Rome, ST=RM, C=IT`).
  - File di credenziali `keystore.properties` (locale e protetto in `.gitignore`) e template versionato `keystore.properties.example`.
  - `app/build.gradle.kts` configurato con `signingConfigs.release` (lettura dinamica da file o variabili d'ambiente CI/CD, fallback sicuro su debug se non presente).
- **ProGuard / R8 al Massimo Livello**:
  - Abilitato `android.enableR8.fullMode=true` in `gradle.properties`.
  - `app/proguard-rules.pro` riscritto con ottimizzazioni aggressive (`-allowaccessmodification`, `-repackageclasses ''`, rimozione selettiva di `android.util.Log` v/d/i in release).
  - Regole specifiche e mirate per lo stack: Jetpack Compose TV, Kotlinx Serialization DTOs, Ktor Client OkHttp, OkHttp3, Coil 2, Coroutines, ViewModel.
- **Verifica Empirica & DoD**:
  - Test unitari eseguiti e superati: `./gradlew test` (49 task eseguiti, 100% passati).
  - Build release completata con successo: `./gradlew assembleRelease` (APK generato in `app/build/outputs/apk/release/app-release.apk`, dimensione ottimizzata a 2.5 MB).
  - Firma validata con `apksigner verify --verbose --print-certs` (v2 scheme verificato con successo).

## File Rilevanti
- `.gitignore`: Aggiunte esclusioni per `*.jks`, `*.keystore`, `keystore.properties` e `.kotlin/`.
- `keystore/release.jks`: Keystore di release protetto localmente.
- `keystore.properties`: Configurazione credenziali locale (gitignored).
- `keystore.properties.example`: Template documentato per CI/CD o nuovi ambienti.
- `app/build.gradle.kts`: Configurazione `signingConfigs.release` e associazione al buildType `release`.
- `app/proguard-rules.pro`: Regole di ottimizzazione, obfuscation, shrinking e keep R8.
- `gradle.properties`: Abilitazione R8 full mode.
- `docs/plans/2026-10-17_release_key_and_proguard_optimization.md`: Piano di lavoro strutturato con checklist completata.

## Prossimi Passi (Next Steps)
1. Eseguire commit atomico dei file modificati (`feat: setup release signing config and optimize R8 proguard rules`).
2. Eventuale configurazione delle GitHub Actions (`.github/workflows/ci.yml`) con i secret `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` per la firma automatica in CI.
3. Test di installazione e avvio dell'APK release su dispositivo reale o emulatore Android TV (`adb install -r app/build/outputs/apk/release/app-release.apk`).

## Comandi Utili
- **Generazione APK Release Firmato**: `./gradlew assembleRelease`
- **Verifica Firma APK con apksigner**: `$ANDROID_HOME/build-tools/35.0.0/apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk`
- **Esecuzione Test Unitari**: `./gradlew test`
- **Installazione su Android TV**: `adb install -r app/build/outputs/apk/release/app-release.apk`

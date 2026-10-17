# Piano di Lavoro: Risoluzione Server Discovery e Network Security per Release

## Obiettivo
Risolvere il problema di rilevamento e connessione all'istanza di Home Arcade (`http://192.168.1.67:5000`) nell'app Android TV in build di release, e installare/verificare l'app sulla Google TV (`192.168.1.50`).

## Analisi Root Cause
1. **Network Security Config**: In Android `network-security-config`, i tag `<domain>` supportano unicamente nomi di dominio (es. `example.com`), NON blocchi CIDR o indirizzi IP (es. `192.168.0.0/16`). Poiché `base-config cleartextTrafficPermitted="false"` era impostato, Android bloccava a monte qualsiasi richiesta HTTP in chiaro verso indirizzi IP locali (tra cui `192.168.1.67:5000`), scatenando un'eccezione `Cleartext HTTP traffic not permitted by network security policy`.
2. **Timeout di Scansione Subnet**: `ServerDiscoveryManager` utilizzava il client Ktor globale con 12s di timeout di connessione. Nel probe di una subnet /24 (254 host), i tentativi su porte chiuse/host inattivi causavano attese enormi (minuti) e apparente blocco del radar.
3. **Priorità Porte**: Home Arcade sul Raspberry Pi risponde sulla porta standard 5000 (`/api/health` restituisce `{"status":"ok"}`), mentre la porta 9876 non è aperta.
4. **Target Device**: Installare e validare l'app sulla Google TV (`192.168.1.50`).

## Scope & Non-Goals
- **In Scope**:
  - Abilitare il cleartext HTTP in `network_security_config.xml` per le comunicazioni LAN/IP.
  - Creare un probe Ktor dedicato a bassa latenza (connect timeout 1000ms) in `ServerDiscoveryManager`.
  - Prioritizzare la porta 5000 rispetto alla 9876 e ottimizzare i candidati di scansione.
  - Aggiornare i fallback manuali in `SetupViewModel`.
  - Compilare la release (`assembleRelease`) e verificare su Google TV (`192.168.1.50`).
- **Non-Goals**:
  - Riscrittura UI di setup o navigazione (YAGNI).
  - Dipendenze esterne aggiuntive.

## Task Checklist
- [ ] Creare piano di lavoro `docs/plans/2026-10-17_fix_server_discovery_and_network_security.md`.
- [ ] Modificare `network_security_config.xml` per consentire traffico cleartext in `base-config`.
- [ ] Ottimizzare `ServerDiscoveryManager.kt` con probe client a timeout rapido e priorità porta 5000.
- [ ] Verificare ed eseguire i test unitari (`./gradlew test`).
- [ ] Compilare la build release (`./gradlew assembleRelease`).
- [ ] Installare ed avviare l'APK di release sulla Google TV (`192.168.1.50`).
- [ ] Verificare empiricamente tramite screenshot e logcat la scoperta del server e la connessione.
- [ ] Aggiornare `docs/HANDOFF.md` sovrascrivendolo integralmente.

## Strategia di Verifica
1. `./gradlew test` per verificare la non-regressione dei DTO e modelli.
2. `./gradlew assembleRelease` per validare la compilazione con R8.
3. `adb -s 192.168.1.50:5555 install -r app/build/outputs/apk/release/app-release.apk`
4. `adb -s 192.168.1.50:5555 shell am start -n com.homearcade.tv/.MainActivity`
5. Verifica visiva tramite screenshot `adb exec-out screencap` del rilevamento automatico del server a `192.168.1.67:5000`.

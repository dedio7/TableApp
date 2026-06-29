# Piano di Implementazione - Citazioni Online Dinamiche

Sostituzione delle citazioni statiche con un servizio online multilingua (Italiano/Inglese).

## Obiettivo
Attualmente le citazioni sono salvate nel codice e si ripetono ogni pochi giorni. L'obiettivo è collegare l'app a un'API esterna per avere una citazione diversa ogni giorno, mantenendo il supporto sia per l'Italiano che per l'Inglese.

## Servizi Identificati
- **Multilingual Quote API**: Un servizio open-source che supporta nativamente `it` ed `en`.
- **ZenQuotes**: Ottimo per l'inglese (come backup).

## Proposed Changes

### 1. Data Layer (Repository)

#### [NEW] [InspirationRepository.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/inspiration/InspirationRepository.kt)
- Gestione chiamate `GET` verso l'API delle citazioni.
- Logica di parsing JSON per estrarre testo e autore.
- Implementazione di un timeout rapido per non rallentare l'avvio dell'app.

### 2. UI Layer (Widget)

#### [InspirationWidget.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/inspiration/InspirationWidget.kt)
- Aggiunta di un caricamento asincrono (`LaunchedEffect`).
- **Sistema di Fallback**: Se internet è assente o l'API non risponde, l'app userà istantaneamente la lista locale di citazioni (quelle attuali).
- Cache giornaliera: La citazione scaricata verrà salvata temporaneamente per la sessione corrente.

---

## Prossimi Passaggi
1. Creazione della classe `InspirationRepository`.
2. Modifica del widget per gestire lo stato "Online" vs "Offline".

---

## Piano di Verifica

### Manual Verification
1. **Verifica Online**: Avvio l'app e controllo che la citazione sia diversa da quelle predefinite.
2. **Verifica Offline**: Metto il dispositivo in modalità aereo e verifico che compaia una citazione della lista locale senza errori o blocchi.
3. **Verifica Lingua**: Cambio lingua nelle impostazioni e verifico che la citazione cambi lingua coerentemente.

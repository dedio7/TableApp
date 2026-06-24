# Piano di Implementazione - Fix GPS & Domotica (Home Assistant)

Sistemazione definitiva del tracciamento posizione e introduzione della domotica.

## 1. Fix GPS Dinamico (Meteo in viaggio)

### Problema
L'app memorizza le coordinate una volta e non si accorge se l'utente cambia città (es. da Roma a Bratislava) finché non viene forzato manualmente.

### Soluzione
- **Monitoraggio Attivo**: Aggiunta di un servizio in `MainScreenViewModel` che, se l'impostazione "Usa GPS" è attiva, controlla la posizione ogni ora (o all'avvio dello screensaver).
- **Reverse Geocoding**: Quando la posizione cambia di oltre 5km, l'app userà il sistema Android per tradurre le coordinate nel nome della città corretta (es. "Bratislava") e aggiornerà automaticamente le impostazioni.
- **Refresh Automatico**: L'aggiornamento della città nelle impostazioni scatenerà immediatamente un nuovo download del meteo.

---

## 2. Widget Domotica (Home Assistant) - Spiegazione Semplice

### Cos'è Home Assistant?
È un software (gratuito) che molti usano per gestire tutta la casa (luci, prese, tapparelle di marche diverse) da un unico posto.

### Cosa faremo in DailyPulse?
- **Pannello di Controllo**: Aggiungeremo una sezione nella dashboard con dei bottoni veloci (es. "Spegni Tutto", "Luce Comodino").
- **Configurazione**: Nelle impostazioni basterà inserire l'indirizzo del tuo server Home Assistant e una "chiave" (Token) che trovi nel tuo profilo HA.
- **Risultato**: Mentre guardi l'ora, potrai spegnere la luce della stanza con un tocco direttamente dall'orologio, senza cercare il telefono o aprire altre app.

---

## Prossimi Passaggi

### [Componente GPS]
#### [MainScreenViewModel.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/ui/main/MainScreenViewModel.kt)
- Implementazione logica `refreshLocationIfGpsEnabled`.
- Uso di `FusedLocationProviderClient` per ottenere la posizione precisa.
- Implementazione `Geocoder` per aggiornare il nome della città.

### [Componente Domotica]
#### [NEW] [HomeAssistantWidget.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/domotics/HomeAssistantWidget.kt)
- Creazione widget con icone per i dispositivi smart.
#### [NEW] [HomeAssistantRepository.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/domotics/HomeAssistantRepository.kt)
- Gestione chiamate API verso il server HA.

---

## Piano di Verifica
1. **Test GPS**: Simulerò un cambio di coordinate via software e verificherò che il nome della città cambi automaticamente in "Bratislava".
2. **Test Meteo**: Verificherò che al cambio città il meteo si aggiorni entro pochi secondi.
3. **Test Domotica**: Verificherò che i bottoni inviino correttamente il comando "Accendi/Spegni" (userò un server di test).

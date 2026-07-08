# Piano di Implementazione - Discovery 2.0: Immagini e Wiki Localizzata

Aggiornamento del sistema Discovery per mostrare le locandine originali e aprire la pagina Wikipedia nella lingua corretta.

## Proposed Changes

### 1. Data Layer (Miglioramento Repository)

#### [DiscoveryRepository.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/inspiration/DiscoveryRepository.kt)
- Estrarre l'URL dell'immagine (locandina/copertina) dal feed Apple RSS.
- Trasformare l'URL in alta risoluzione (da 100x100 a 600x600).
- Implementare la logica `getLocalizedWikiUrl(title, lang)` che genera automaticamente il link:
    - `https://it.wikipedia.org/wiki/Titolo_Film` (se l'app è in ITA)
    - `https://en.wikipedia.org/wiki/Movie_Title` (se l'app è in ENG)

### 2. UI Layer (Widget & Immagini)

#### [DiscoveryWidget.kt](file:///C:/Android Project/DailyPulse/app/src/main/java/com/dedio/dailypulse/inspiration/DiscoveryWidget.kt)
- Integrazione della libreria **Coil** per il caricamento fluido delle immagini.
- Sostituzione dell'emoji statica con la locandina reale.
- Gestione di un "placeholder" elegante mentre l'immagine scarica.
- Supporto per angoli arrotondati e ombre sulle locandine per mantenere lo stile Neon.

---

## Prossimi Passaggi

### [Componente Dati]
- Aggiornamento `MediaItem` per includere `imageUrl`.
- Refactoring `DiscoveryRepository` per catturare l'immagine dai metadati Apple.

### [Componente UI]
- Layout aggiornato con immagine a sinistra e testi a destra.
- Logica di navigazione verso Wikipedia in base alla lingua attiva.

---

## Piano di Verifica

### Manual Verification
1. **Verifica Immagine**: Verificherò che compaia la locandina reale del film o la copertina del disco invece dell'emoji.
2. **Verifica Wikipedia**: Cliccherò su un film (es. "Interstellar") e verificherò che si apra la pagina in italiano se l'app è in ITA.
3. **Verifica Qualità**: Verificherò che le immagini non siano sgranate (usando il trucco della risoluzione 600x600).

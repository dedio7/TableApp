# Istruzioni per ospitare la Privacy Policy su GitHub Pages

Per pubblicare l'app sul Google Play Store, devi fornire un URL pubblico della tua Privacy Policy. Avendo scelto di usare **GitHub Pages** (che è gratuito e sicuro), ecco i passaggi semplici per configurarlo:

---

## Opzione 1: Usare un repository esistente su GitHub
Se hai già un repository GitHub per questo progetto (o un altro repository personale):

1. Carica il file `privacy-policy/index.html` nel repository (puoi metterlo in una cartella chiamata `docs/` o direttamente nella root).
2. Su GitHub, vai alla pagina del tuo repository e clicca su **Settings** (Impostazioni) in alto a destra.
3. Nel menu laterale sinistro, sotto la sezione "Code and automation", clicca su **Pages**.
4. Sotto **Build and deployment**:
   - Imposta **Source** su `Deploy from a branch`.
   - Sotto **Branch**, seleziona il tuo branch principale (es. `main` o `master`).
   - Seleziona la cartella: `/ (root)` se hai messo il file nella root, oppure `/docs` se lo hai messo nella cartella `docs`.
5. Clicca su **Save**.
6. Attendi 1-2 minuti. GitHub ti mostrerà un messaggio in alto con il link pubblico (es. `https://tuo-username.github.io/nome-repo/`). Il link finale alla privacy policy sarà:
   `https://tuo-username.github.io/nome-repo/index.html` (o senza `index.html` se è nella root).

---

## Opzione 2: Creare un repository dedicato per la Privacy Policy (Consigliato)
Se vuoi tenere la privacy policy separata dai tuoi progetti di sviluppo:

1. Accedi a GitHub e crea un nuovo repository pubblico chiamato `dailypulse-privacy`.
2. Carica il file `index.html` (contenuto nella cartella `privacy-policy` di questo progetto) direttamente nella root del repository.
3. Vai in **Settings** -> **Pages**.
4. Imposta il branch su `main` e la cartella su `/ (root)`. Clicca su **Save**.
5. Il tuo link sarà subito pronto a un indirizzo simile a:
   `https://<tuo-username>.github.io/dailypulse-privacy/`

---

## Passaggi successivi in Google Play Console:
1. Copia il link generato da GitHub Pages.
2. Accedi alla [Google Play Console](https://play.google.com/console/).
3. Seleziona l'app **DailyPulse**.
4. Nel menu laterale, scorri fino a **Contenuto app** (App Content) -> **Norme sulla privacy** (Privacy Policy).
5. Incolla l'URL e clicca su **Salva**.

package com.dedio.dailypulse.media

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Servizio necessario per far apparire l'app nella lista "Accesso alle Notifiche" di Android.
 * Senza questo servizio dichiarato nel Manifest, Android non mostrerà l'app tra quelle
 * autorizzabili a leggere i metadati dei media (come Spotify).
 */
class SpotifyNotificationService : NotificationListenerService() {
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        // Listener connesso correttamente
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Rimosso requestRebind da qui per evitare conflitti con il sistema
        // durante la fase di scollegamento (es. update dell'app).
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Non abbiamo bisogno di processare le singole notifiche qui
        // perché usiamo il MediaSessionManager, ma il servizio deve esistere.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Idem come sopra
    }
}

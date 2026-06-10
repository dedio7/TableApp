package com.example.dailypulse.media

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Servizio necessario per far apparire l'app nella lista "Accesso alle Notifiche" di Android.
 * Senza questo servizio dichiarato nel Manifest, Android non mostrerà l'app tra quelle
 * autorizzabili a leggere i metadati dei media (come Spotify).
 */
class SpotifyNotificationService : NotificationListenerService() {
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Non abbiamo bisogno di processare le singole notifiche qui
        // perché usiamo il MediaSessionManager, ma il servizio deve esistere.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Idem come sopra
    }
}

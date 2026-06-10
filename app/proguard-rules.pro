# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\...\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Keep Compose related classes
-keep class androidx.compose.ui.platform.** { *; }

# Keep DataStore preferences
-keep class androidx.datastore.preferences.protobuf.** { *; }

# Keep Kotlin Serialization models (mostly handled by the plugin, but as a fallback)
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# Keep our Service classes that are referenced in AndroidManifest.xml
-keep class com.dedio.dailypulse.DailyPulseDreamService
-keep class com.dedio.dailypulse.media.SpotifyNotificationService

# Optional: Add any other library specific rules if you see issues in Release build.

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\...\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Library consumer rules are automatically included by Jetpack libraries.
# Avoid using broad package wildcards like '** { *; }' as they prevent proper shrinking.

# Keep Kotlin Serialization models (if used in the future) using targeted annotation selectors
-keepattributes *Annotation*, EnclosingMethod, Signature
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep our Service classes that are referenced in AndroidManifest.xml (Specific classes)
-keep class com.dedio.dailypulse.DailyPulseDreamService
-keep class com.dedio.dailypulse.media.SpotifyNotificationService

# Optional: Add any other library specific rules if you see issues in Release build.

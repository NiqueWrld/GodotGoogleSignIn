# Add project specific ProGuard rules here.
# Keep Google Sign-In classes
-keep class com.niquewrld.casino.googlesignin.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# Keep Godot plugin interface
-keep class org.godotengine.godot.plugin.** { *; }

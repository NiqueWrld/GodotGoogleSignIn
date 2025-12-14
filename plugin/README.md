# Google Sign-In Android Plugin Build Instructions

## Prerequisites

1. **Android Studio** (latest version recommended)
2. **JDK 17** or higher
3. **Godot 4.x godot-lib.aar** - Download from Godot's release page

## Build Steps

### Step 1: Get Godot Library

1. Download the Godot Editor for your version
2. Export an Android APK once to generate the library, OR
3. Download `godot-lib.X.X.X.stable.template_release.aar` from:
   https://downloads.tuxfamily.org/godotengine/

4. Copy the `godot-lib.*.aar` file to:
   ```
   android/plugins/GoogleSignIn/libs/godot-lib.aar
   ```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Open the `android/plugins/GoogleSignIn` folder as a project
3. Wait for Gradle sync to complete

### Step 3: Build the Plugin

1. Open Terminal in Android Studio
2. Run:
   ```bash
   ./gradlew assembleRelease
   ```
3. The AAR file will be at:
   ```
   build/outputs/aar/GoogleSignIn-release.aar
   ```

### Step 4: Copy to Godot Plugins Folder

1. Rename the output file to `GoogleSignIn.aar`
2. Copy to your Godot project's `android/plugins/` folder:
   ```
   android/plugins/GoogleSignIn.aar
   android/plugins/GoogleSignIn.gdap  (already created)
   ```

### Step 5: Enable Plugin in Godot

1. Open Godot Editor
2. Go to **Project → Export**
3. Select your Android export preset
4. Scroll down to **Plugins** section
5. Enable **GodotGoogleSignIn**

### Step 6: Configure Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Select your project
3. Go to **APIs & Services → Credentials**
4. Create an **Android OAuth client** with:
   - Package name: `com.niquewrld.casino`
   - SHA-1: Your keystore's SHA-1 fingerprint
5. Create a **Web OAuth client** (for Firebase auth exchange)
6. Note both Client IDs

### Step 7: Update FirebaseConfig.gd

Make sure your `FirebaseConfig.gd` has the Web Client ID:
```gdscript
const CONFIG = {
    "GOOGLE_CLIENT_ID": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com",
    # ... other config
}
```

## Usage in GDScript

The plugin is automatically used by `Firebase.gd` when:
- Running on Android
- Plugin is enabled in export settings
- Web Client ID is configured

```gdscript
# Simply call sign_in_with_google() - it will use native plugin on Android
Firebase.sign_in_with_google()

# Connect to the signal
Firebase.google_auth_completed.connect(_on_google_auth)

func _on_google_auth(success: bool, error: String):
    if success:
        print("Signed in!")
    else:
        print("Error: ", error)
```

## Troubleshooting

### "Plugin not found"
- Make sure `GoogleSignIn.aar` is in `android/plugins/`
- Make sure `GoogleSignIn.gdap` is in `android/plugins/`
- Enable the plugin in Export settings

### "Sign-in cancelled"
- Check your Web Client ID is correct
- Verify SHA-1 fingerprint matches your keystore
- Make sure the Android OAuth client has correct package name

### "Invalid credential"
- Ensure you're using the **Web Client ID** (not Android Client ID)
- The Web Client ID is used for Firebase authentication

## Notes

- The plugin uses Google's Credential Manager API (modern approach)
- Requires Android API 24+ (Android 7.0)
- Works with Google Play Services

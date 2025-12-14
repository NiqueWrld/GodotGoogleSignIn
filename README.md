# Godot Google Sign-In Plugin

A native Android plugin for Godot 4.2+ that enables Google Sign-In using the modern **Credential Manager API**.

## Features

- ✅ Native Google Sign-In on Android
- ✅ Uses modern Credential Manager API (Google's recommended approach)
- ✅ Returns ID token for Firebase Authentication
- ✅ Supports Godot 4.2+ (v2 plugin architecture)
- ✅ Auto-select previously signed-in accounts
- ✅ Account chooser support

## Requirements

- Godot 4.2 or higher
- Android export with Gradle build enabled
- Google Cloud Console project with OAuth 2.0 credentials

## Installation

### Option 1: Pre-built Plugin (Recommended)

1. Copy the `addons/GodotGoogleSignIn` folder to your project's `addons/` directory
2. In Godot, go to **Project → Project Settings → Plugins**
3. Enable **GodotGoogleSignIn**
4. Go to **Project → Install Android Build Template...**
5. In Export settings, enable **Use Gradle Build**

### Option 2: Build from Source

See [Building from Source](#building-from-source) below.

## Setup

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Go to **APIs & Services → Credentials**
4. Create an **OAuth 2.0 Client ID**:
   - For Android: Select "Android" application type
   - Package name: Your app's package name (e.g., `com.yourcompany.yourgame`)
   - SHA-1 fingerprint: Your signing key's SHA-1
5. Create a **Web Client ID** (required for Firebase):
   - Select "Web application" type
   - This ID is used to get the ID token

### 2. Get Your SHA-1 Fingerprint

```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore
keytool -list -v -keystore your-release-key.keystore -alias your-alias
```

## Usage

```gdscript
extends Node

var google_sign_in: Object = null

func _ready():
    if OS.get_name() == "Android" and Engine.has_singleton("GodotGoogleSignIn"):
        google_sign_in = Engine.get_singleton("GodotGoogleSignIn")
        google_sign_in.connect("sign_in_success", _on_sign_in_success)
        google_sign_in.connect("sign_in_failed", _on_sign_in_failed)
        google_sign_in.connect("sign_out_complete", _on_sign_out_complete)
        
        # Initialize with your Web Client ID
        google_sign_in.initialize("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com")

func sign_in():
    if google_sign_in:
        google_sign_in.signIn()  # Auto-selects if previously signed in
        # Or use: google_sign_in.signInWithGoogleButton()  # Always shows account picker

func sign_out():
    if google_sign_in:
        google_sign_in.signOut()

func _on_sign_in_success(id_token: String, email: String, display_name: String):
    print("Signed in as: ", email)
    print("Display name: ", display_name)
    # Use id_token with Firebase Auth:
    # https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp

func _on_sign_in_failed(error: String):
    print("Sign-in failed: ", error)

func _on_sign_out_complete():
    print("Signed out")
```

## API Reference

### Methods

| Method | Description |
|--------|-------------|
| `initialize(web_client_id: String)` | Initialize with your Web Client ID from Google Cloud Console |
| `isInitialized() -> bool` | Check if plugin is initialized |
| `signIn()` | Start sign-in flow (auto-selects if previously authorized) |
| `signInWithAccountChooser()` | Sign in with account picker |
| `signInWithGoogleButton()` | Sign in using Google's branded button flow |
| `signOut()` | Sign out and clear credential state |

### Signals

| Signal | Parameters | Description |
|--------|------------|-------------|
| `sign_in_success` | `id_token: String, email: String, display_name: String` | Emitted on successful sign-in |
| `sign_in_failed` | `error: String` | Emitted when sign-in fails |
| `sign_out_complete` | None | Emitted when sign-out completes |

## Firebase Authentication

To authenticate with Firebase using the ID token:

```gdscript
func _sign_in_with_firebase(google_id_token: String):
    var url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=YOUR_FIREBASE_API_KEY"
    
    var body = {
        "postBody": "id_token=%s&providerId=google.com" % google_id_token,
        "requestUri": "http://localhost",
        "returnIdpCredential": true,
        "returnSecureToken": true
    }
    
    var http = HTTPRequest.new()
    add_child(http)
    http.request(url, ["Content-Type: application/json"], HTTPClient.METHOD_POST, JSON.stringify(body))
```

## Building from Source

### Requirements

- Android SDK (API 33+)
- Java 17+
- Gradle 8.4+

### Build Steps

1. Clone this repository
2. Navigate to `plugin/`
3. Update `local.properties` with your Android SDK path:
   ```
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```
4. Build:
   ```bash
   ./gradlew assembleRelease
   ```
5. The AAR will be in `build/outputs/aar/`

## Dependencies

The plugin uses these Android libraries (automatically included via Gradle):

- `androidx.credentials:credentials:1.3.0`
- `androidx.credentials:credentials-play-services-auth:1.3.0`
- `com.google.android.libraries.identity.googleid:googleid:1.1.1`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`

## Troubleshooting

### "Plugin not available" error
- Make sure **Use Gradle Build** is enabled in Android export settings
- Install Android Build Template via **Project → Install Android Build Template**
- Ensure the plugin is enabled in Project Settings → Plugins

### Sign-in cancelled immediately
- Verify your SHA-1 fingerprint matches in Google Cloud Console
- Check that package name matches exactly
- Ensure Web Client ID is correct

### "No credentials available"
- The device needs at least one Google account signed in
- Play Services must be up to date

## License

MIT License - see [LICENSE](LICENSE)

## Credits

Built for Godot 4.5.1 using the v2 Android plugin architecture.

Uses Google's Credential Manager API for modern, secure authentication.

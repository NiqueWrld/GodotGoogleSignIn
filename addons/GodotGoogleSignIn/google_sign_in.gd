## GodotGoogleSignIn — GDScript wrapper
##
## Attach this script to an autoload node (or any Node) and call
## initialize() before using any sign-in method.
##
## Signals forwarded from the native plugin:
##   sign_in_success(id_token, email, display_name)
##   sign_in_failed(error)
##   silent_sign_in_failed(error)
##   sign_out_complete()
extends Node

signal sign_in_success(id_token: String, email: String, display_name: String)
signal sign_in_failed(error: String)
signal silent_sign_in_failed(error: String)
signal sign_out_complete()

var _plugin: Object = null

func _ready() -> void:
	if OS.get_name() == "Android" and Engine.has_singleton("GodotGoogleSignIn"):
		_plugin = Engine.get_singleton("GodotGoogleSignIn")
		_plugin.connect("sign_in_success", _on_sign_in_success)
		_plugin.connect("sign_in_failed", _on_sign_in_failed)
		_plugin.connect("silent_sign_in_failed", _on_silent_sign_in_failed)
		_plugin.connect("sign_out_complete", _on_sign_out_complete)

## Initialize with the Web Client ID from Google Cloud Console.
## Must be called before any sign-in method.
func initialize(web_client_id: String) -> void:
	if _plugin:
		_plugin.initialize(web_client_id)

func is_initialized() -> bool:
	return _plugin != null and _plugin.isInitialized()

# ---------------------------------------------------------------------------
# Interactive sign-in methods (may show account picker UI)
# ---------------------------------------------------------------------------

## Sign in, auto-selecting a previously authorized account if available,
## otherwise falling back to the full account chooser.
func sign_in() -> void:
	if _plugin:
		_plugin.signIn()

## Same as sign_in() but embeds a raw nonce in the ID token for backend
## verification (Supabase, Firebase, custom OIDC).
func sign_in_with_nonce(nonce: String) -> void:
	if _plugin:
		_plugin.signInWithNonce(nonce)

## Always show the full account chooser.
func sign_in_with_account_chooser() -> void:
	if _plugin:
		_plugin.signInWithAccountChooser()

## Account chooser with nonce support.
func sign_in_with_account_chooser_with_nonce(nonce: String) -> void:
	if _plugin:
		_plugin.signInWithAccountChooserWithNonce(nonce)

## Sign in with the branded "Sign in with Google" button flow.
func sign_in_with_google_button() -> void:
	if _plugin:
		_plugin.signInWithGoogleButton()

## Google button flow with nonce support.
func sign_in_with_google_button_with_nonce(nonce: String) -> void:
	if _plugin:
		_plugin.signInWithGoogleButtonWithNonce(nonce)

# ---------------------------------------------------------------------------
# Silent sign-in methods (no UI — fail immediately if not resolvable)
# ---------------------------------------------------------------------------

## Attempt a non-interactive sign-in using a previously authorized account.
##
## Emits sign_in_success on success.
## Emits silent_sign_in_failed (never sign_in_failed) if no account can be
## resolved without user interaction — the caller can then decide whether to
## show the full picker or leave the user signed out.
func silent_sign_in() -> void:
	if _plugin:
		_plugin.silentSignIn()

## Silent sign-in with a caller-supplied raw nonce.
## See silent_sign_in() for the silent flow and sign_in_with_nonce() for
## nonce details.
func silent_sign_in_with_nonce(nonce: String) -> void:
	if _plugin:
		_plugin.silentSignInWithNonce(nonce)

# ---------------------------------------------------------------------------
# Sign-out
# ---------------------------------------------------------------------------

func sign_out() -> void:
	if _plugin:
		_plugin.signOut()

# ---------------------------------------------------------------------------
# Internal signal forwarders
# ---------------------------------------------------------------------------

func _on_sign_in_success(id_token: String, email: String, display_name: String) -> void:
	sign_in_success.emit(id_token, email, display_name)

func _on_sign_in_failed(error: String) -> void:
	sign_in_failed.emit(error)

func _on_silent_sign_in_failed(error: String) -> void:
	silent_sign_in_failed.emit(error)

func _on_sign_out_complete() -> void:
	sign_out_complete.emit()

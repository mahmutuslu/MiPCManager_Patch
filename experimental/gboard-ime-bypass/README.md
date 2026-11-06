# Gboard Mirror IME Bypass — experimental LSPosed module

Built from AOSP `InputMethodManager.dispatchInputEvent` behavior. For **Xiaomi Mirror** events with
`deviceId=-100`, `source=SOURCE_KEYBOARD`, `scanCode=0`, and printable Unicode,
it returns `DISPATCH_NOT_HANDLED` (0) **only when Gboard is the selected IME**.

This tests whether bypassing the IME stage restores ordinary letters in the application View.
The module does **not** modify Gboard, the Mirror service, or the working V6.9 keyboard bridge.
No ADB/PowerShell helper is needed for normal operation once LSPosed is active.

## First test
1. Install debug APK.
2. Enable module in LSPosed; scope only `com.example.mirrorkeydiag` first.
3. Force-stop and relaunch MirrorKeyDiag. (Depending on LSPosed configuration, restart may be needed.)
4. Keep Gboard selected. With Xiaomi PC Manager screen mirroring, type `abc123` in MirrorKeyDiag.
5. Copy MirrorKeyDiag logs. Optionally check `adb logcat -s MiKeyImeBypass:I '*:S'`.
6. Disable LSPosed module to revert.

Expected when hook is effective: MirrorKeyDiag includes `ACTIVITY_DISPATCH`/`VIEW_DOWN`, whereas without it only `VIEW_PRE_IME` was seen.

**Limitations:** This is a diagnostic proof of concept, not a confirmed fix. It does not correctly map Turkish-Q characters:
Xiaomi currently sends punctuation codes instead of `ğ ü ş ö ç`. Does not handle input in WebViews/Compose necessarily.
The module targets only MirrorKeyDiag and Google Keep for safety.
If `DISPATCH_NOT_HANDLED` does not restore text, the problem is elsewhere, and logs will help determine that.

**Security:** No internet permissions, no accessibility service, no default keyboard switch, no passwords/clipboard access.
Do not type private text into diagnostic app because test logs include input metadata.

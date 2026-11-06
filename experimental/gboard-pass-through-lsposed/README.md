# Gboard Mirror Turkish Q v0.2 — experimental LSPosed module
This is a test, not a confirmed fix.

Built from successful v0.1 pass-through. v0.2 uses a different applicationId, so it can be installed beside v0.1.
Disable v0.1 in LSPosed before enabling v0.2. Scope only Gboard (com.google.android.inputmethod.latin). Reboot or force-stop Gboard.

DeviceId -100 keyboard-source event mappings: left bracket to ğ/Ğ, right bracket to ü/Ü, semicolon to ş/Ş, slash to ö/Ö, backslash to ç/Ç, I key to dotless ı/I, apostrophe key to dotted i/İ.
Shift and CapsLock select case using XOR; this needs verification against Xiaomi's forwarded metadata.
Converted characters are committed through Gboard's InputConnection.commitText; mapped events are consumed. Other printable keys retain v0.1 pass-through.
Enter, Backspace, Tab, Ctrl/Alt/Meta shortcuts and AltGr are left untouched.

To test: MirrorKeyDiag plus Google Keep, type abc123ğüşöçıi and uppercase with Shift and CapsLock.
Expected ordinary key: VIEW_PRE_IME, ACTIVITY_DISPATCH, VIEW_DOWN. Expected converted Turkish key: VIEW_PRE_IME and IC_COMMIT_TEXT [ğ] without VIEW_DOWN for punctuation.
If the module fails, disable v0.2 and re-enable v0.1. The module requires LSPosed, does not modify Xiaomi binaries, and does not use an ADB or PowerShell bridge.
Do not enter passwords during tests.

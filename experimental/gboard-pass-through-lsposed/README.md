# Gboard Turkish Q LSPosed v0.6 — Ctrl+A / Ctrl+C / Ctrl+V
An isolated follow-up to working v0.5 for Xiaomi PC Manager mirroring on a Turkish Q Windows keyboard.

v0.6 adds focused editor context actions via Android InputConnection.performContextMenuAction:
- Ctrl+A → android.R.id.selectAll
- Ctrl+C → android.R.id.copy
- Ctrl+V → android.R.id.paste

Only for Xiaomi synthetic deviceId=-100 keyboard source in the Gboard IME aeht.onKeyDown; only with Ctrl down and no Alt/Meta/Shift; only on repeat 0. If editor declines, module logs and leaves original behavior. It does not capture clipboard contents or need an ADB bridge.
All v0.5 Turkish characters, @, Backspace, punctuation including Shift+UNKNOWN → ? stay unchanged.

Test in Google Keep: Type deneme 123, Ctrl+A, Ctrl+C, tap a second line, Ctrl+V. Retest AltGr+Q, Backspace and ?.
LSPosed: Disable v0.5 and other old versions, install v0.6, scope to Gboard only, restart phone. Revert via LSPosed toggle to v0.5.
If shortcuts still fail, collect Ctrl+A/C/V logs in MirrorKeyDiag; Xiaomi may omit the Ctrl modifier. Do not assume this test build is proven.
Context menu actions are editor-dependent. This does not implement Windows OS global shortcuts.

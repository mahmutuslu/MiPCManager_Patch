# Gboard Mirror Turkish Q v0.3 — experimental LSPosed module
Independent APK from v0.2, designed for a Windows Turkish Q keyboard and Xiaomi PC Manager 6.0.2.19 mirroring.

v0.3 adds:
- Backspace via InputConnection.deleteSurroundingTextInCodePoints(1,0), with selected-text deletion
- Forward Delete via deleteSurroundingTextInCodePoints(0,1)
- AltGr+Q (@), AltGr+E (€), AltGr+T (₺), AltGr+3 (#), AltGr+4 ($), AltGr+7/8/9/0 ({[]}), and other standard Turkish Q third-level characters
- Shifted digit and punctuation symbols (!, apostrophe, ^, +, %, &, /, (, ), =, ?, _, :, etc.)
- Keep v0.2 mappings for ğ, ü, ş, ı, ö, ç, i including uppercase and v0.1 printable-key pass-through

Install the APK, disable both v0.1 and v0.2 in LSPosed, enable v0.3 and scope only Gboard. Restart Gboard/phone.
To revert, disable v0.3, re-enable v0.2.

Notes:
- An actual Turkish Q keyboard and the observed Xiaomi KeyEvent mappings are assumed. Punctuation on some physical keys may need further adjustments.
- Right Alt (AltGr) must survive Xiaomi mirroring as a modifier on the actual letter. The app also recognizes KEYCODE_AT or @ Unicode when Xiaomi converts it.
- This does not modify other apps, Xiaomi binaries or Gboard APK.
- Test in MirrorKeyDiag or Keep with 12@34, punctuation and Backspace. This is a test build, not a confirmed fix.

# Gboard Mirror Turkish Q v0.5 — Xiaomi question mark fix
Observed logs (MirrorKeyDiag):
* physical * = KEYCODE_NUMPAD_MULTIPLY with unicode 42: works
* physical Shift+* (?) = KEYCODE_UNKNOWN with deviceId=-100, source 0x101, meta=0x1, scanCode=0, flags=0x8, unicode=0: no text
* physical Shift+Ö = KEYCODE_SLASH, unicode 63: v0.4 correctly prints Ö via physical-position mapping

v0.5 maps *only* the observed Shift+KEYCODE_UNKNOWN from Xiaomi keyboard source and deviceId=-100 to ? via the active Gboard InputConnection. Preserves all other v0.4 mappings, Backspace, Turkish letters, @ and punctuation.
This is a cautious experimental heuristic; another unmapped Shift+unknown key on the same mirror might produce ? as well. Test in MirrorKeyDiag and Keep.
Different applicationId so install beside v0.4. Disable v0.4 LSPosed module, enable v0.5 and scope only Gboard. Reboot or restart Gboard. Revert by turning off v0.5 and re-enabling v0.4.
Not a change to Xiaomi PC Manager or Gboard installation; not a generic fix for unmapped keys.

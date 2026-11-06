# Gboard Mirror Turkish Q v0.4 — hyphen correction
v0.3 had KEYCODE_MINUS mapped to `*`, causing the physical hyphen key to write a star on the tested Windows Turkish Q keyboard.
v0.4 swaps these two punctuation mappings: `KEYCODE_MINUS` -> `-` and with Shift -> `_`; `KEYCODE_EQUALS` -> `*` and with Shift -> `?`.
Everything else is inherited unchanged from the working v0.3 (Gboard pass-through, Turkish letters, @, Backspace).
This is a hardware-layout-specific, experimental mapping. Confirm `-`, `*`, `_`, `?` on the device; Xiaomi may encode the star key differently.
App ID is different from v0.3. Before enabling v0.4, disable v0.1, v0.2 and v0.3 in LSPosed. Scope only Gboard; reboot after enabling.
To revert, disable v0.4 and re-enable the working v0.3. No original Xiaomi/Gboard files are overwritten.

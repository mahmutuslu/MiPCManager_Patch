# Experimental LSPosed Gboard Mirror Pass-through
This **is not a confirmed fix**. Built for Gboard APK inspected on 2026-11-06.
Requires rooted phone with working LSPosed. Only scope **Gboard** (com.google.android.inputmethod.latin). Avoid enabling system-wide or on Xiaomi Mirror.
The hook forces Gboard's `aeht.onKeyDown/onKeyUp` to report UNHANDLED for **printable mirrored KeyEvents with deviceId=-100** and no Ctrl/Alt/Meta. Android TextView may then handle those as XiaoAI's does.
The experiment does **not** repair Turkish Q mapping; that is a separate Windows-to-Android layout problem. No ADB, clipboard or network behavior. No special permissions.
If Gboard is updated/obfuscation changes, the target class may not exist and module intentionally does nothing.
To revert: disable module in LSPosed, force-stop Gboard / reboot.
Risk: predictive composition, editing and some apps may behave differently. Use test editor first and do not type sensitive data during troubleshooting.

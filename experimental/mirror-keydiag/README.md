# Mirror Key Diag
Read-only keyboard event debugger. No privileges or permissions. It does not replace Gboard, Xiaomi Mirror, or V6.9.
1. Open the application in Xiaomi mirroring.
2. Focus the text box and type abc123, Turkish Q characters and Backspace.
3. Copy the logs, or use adb logcat -s MirrorKeyDiag:I '*:S'.
4. Repeat with Gboard and XiaoAI.
Logs contain test text. Do not type private information.
GitHub Actions builds a debug APK available under workflow artifacts.

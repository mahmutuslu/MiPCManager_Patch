# V7 Gboard support — experimental

This is NOT a native MiScreenShare.exe fix. It is a separate experimental option that keeps Gboard as the default Android keyboard. The V6.9 IME path remains available.

## Installation
1. Install the newly compiled APK from the latest GitHub Actions artifact (app-debug.apk).
2. Select Gboard as the Android default keyboard (do not select BridgeIme).
3. Open Xiaomi Turkish Bridge and choose Gboard support: Accessibility settings.
4. Explicitly enable Xiaomi Gboard Turkce Q Kopru accessibility service.
5. On Windows use KOPRU_V6_9.ps1 with your existing app pairing token.
6. In Xiaomi mirroring open a NEW EMPTY Google Keep test note and type Turkish letters slowly.

## Security and limitations
Accessibility grants broad screen access. This prototype does not log screen events or send text to remote servers, but only enable it for testing and switch off afterwards.
Android AccessibilityNodeInfo.ACTION_SET_TEXT replaces the entire editable field text for insertion. It can fail in some apps and can unintentionally alter existing text or cursor position. Never test on passwords, personal information, messages or important documents.
This is not native Xiaomi integration. Compatibility and input ordering are not yet tested.

The new APK must be installed: the older V6.9 version lacks the Gboard accessibility feature.
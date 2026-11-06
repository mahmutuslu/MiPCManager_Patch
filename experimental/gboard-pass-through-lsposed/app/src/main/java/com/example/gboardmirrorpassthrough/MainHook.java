package com.example.gboardmirrorpassthrough;

import android.inputmethodservice.InputMethodService;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.util.SparseBooleanArray;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
 private static final String PKG = "com.google.android.inputmethod.latin";
 private static final int XIAOMI_MIRROR_DEVICE_ID = -100;
 private final SparseBooleanArray committedDown = new SparseBooleanArray();

 private static boolean isMirroredKeyboard(KeyEvent event) {
  return event != null
    && event.getDeviceId() == XIAOMI_MIRROR_DEVICE_ID
    && (event.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD;
 }

 private static boolean shouldPass(int keyCode, KeyEvent event) {
  if (!isMirroredKeyboard(event)) return false;
  int meta = event.getMetaState();
  // Leave system shortcuts and AltGr combinations alone.
  if ((meta & (KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON | KeyEvent.META_META_ON)) != 0) return false;
  if (event.getRepeatCount() != 0) return false;
  if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DEL
     || keyCode == KeyEvent.KEYCODE_FORWARD_DEL || keyCode == KeyEvent.KEYCODE_TAB) return false;
  int cp = event.getUnicodeChar();
  return cp >= 32 && Character.isValidCodePoint(cp) && !Character.isISOControl(cp);
 }

 private static String turkishText(int keyCode, KeyEvent event) {
  // Turkish Q shifted/caps letters: use XOR so Shift+CapsLock produces lowercase.
  boolean upper = event.isShiftPressed() ^ event.isCapsLockOn();
  switch (keyCode) {
   case KeyEvent.KEYCODE_LEFT_BRACKET: return upper ? "Ğ" : "ğ";
   case KeyEvent.KEYCODE_RIGHT_BRACKET: return upper ? "Ü" : "ü";
   case KeyEvent.KEYCODE_SEMICOLON: return upper ? "Ş" : "ş";
   case KeyEvent.KEYCODE_SLASH: return upper ? "Ö" : "ö";
   case KeyEvent.KEYCODE_BACKSLASH: return upper ? "Ç" : "ç";
   case KeyEvent.KEYCODE_I: return upper ? "I" : "ı";
   // Physical Turkish-Q dotted i key corresponds to the US apostrophe position.
   case KeyEvent.KEYCODE_APOSTROPHE: return upper ? "İ" : "i";
   default: return null;
  }
 }

 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
  if (!PKG.equals(lpparam.packageName)) return;

  try {
   XposedHelpers.findAndHookMethod("aeht", lpparam.classLoader, "onKeyDown",
     int.class, KeyEvent.class, new XC_MethodHook() {
      @Override protected void beforeHookedMethod(MethodHookParam p) {
       int code = (Integer) p.args[0];
       KeyEvent event = (KeyEvent) p.args[1];
       if (!shouldPass(code, event)) return;

       String text = turkishText(code, event);
       if (text != null && p.thisObject instanceof InputMethodService) {
        try {
         InputConnection ic = ((InputMethodService) p.thisObject).getCurrentInputConnection();
         if (ic != null && ic.commitText(text, 1)) {
          synchronized (committedDown) { committedDown.put(code, true); }
          p.setResult(true); // Character committed; suppress the original punctuation key.
          return;
         }
        } catch (Throwable error) {
         XposedBridge.log("GboardMirrorTR: InputConnection commit failed: " + error);
        }
       }
       // Preserve the working v0.1 pass-through for standard keys and when commit fails.
       p.setResult(false);
      }
     });

   XposedHelpers.findAndHookMethod("aeht", lpparam.classLoader, "onKeyUp",
     int.class, KeyEvent.class, new XC_MethodHook() {
      @Override protected void beforeHookedMethod(MethodHookParam p) {
       int code = (Integer) p.args[0];
       KeyEvent event = (KeyEvent) p.args[1];
       if (!isMirroredKeyboard(event)) return;
       synchronized (committedDown) {
        if (committedDown.get(code)) {
         committedDown.delete(code);
         p.setResult(true); // Pair with the key-down that committed Turkish text.
         return;
        }
       }
       if (shouldPass(code, event)) p.setResult(false);
      }
     });

   XposedBridge.log("GboardMirrorTR v0.2: aeht hooks installed for deviceId=-100");
  } catch (Throwable error) {
   XposedBridge.log("GboardMirrorTR v0.2: hook unavailable on this Gboard version: " + error);
  }
 }
}

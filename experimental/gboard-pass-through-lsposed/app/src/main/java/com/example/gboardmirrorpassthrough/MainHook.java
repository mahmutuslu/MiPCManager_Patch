package com.example.gboardmirrorpassthrough;

import android.view.KeyEvent;
import android.view.InputDevice;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
 private static final String PKG = "com.google.android.inputmethod.latin";
 private static final int XIAOMI_MIRROR_DEVICE_ID = -100;
 private static boolean shouldPass(int keyCode, KeyEvent event) {
  if (event == null || event.getDeviceId() != XIAOMI_MIRROR_DEVICE_ID) return false;
  if ((event.getSource() & InputDevice.SOURCE_KEYBOARD) != InputDevice.SOURCE_KEYBOARD) return false;
  int meta = event.getMetaState();
  if ((meta & (KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON | KeyEvent.META_META_ON)) != 0) return false;
  if (event.getRepeatCount() != 0) return false;
  if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DEL
     || keyCode == KeyEvent.KEYCODE_FORWARD_DEL || keyCode == KeyEvent.KEYCODE_TAB) return false;
  int cp = event.getUnicodeChar();
  return cp >= 32 && Character.isValidCodePoint(cp) && !Character.isISOControl(cp);
 }
 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
  if (!PKG.equals(lpparam.packageName)) return;
  final XC_MethodHook pass = new XC_MethodHook() {
   @Override protected void beforeHookedMethod(MethodHookParam p) {
    int keyCode = (Integer) p.args[0];
    KeyEvent event = (KeyEvent) p.args[1];
    if (shouldPass(keyCode,event)) {
     // When a service reports UNHANDLED, the focused TextView can process this KeyEvent.
     p.setResult(false);
    }
   }
  };
  try {
   // 'aeht' is the name observed in the analyzed Gboard build's base.apk/classes.dex.
   XposedHelpers.findAndHookMethod("aeht", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, pass);
   XposedHelpers.findAndHookMethod("aeht", lpparam.classLoader, "onKeyUp", int.class, KeyEvent.class, pass);
   XposedBridge.log("GboardMirrorPassThrough: hooks installed in aeht");
  } catch (Throwable e) {
   XposedBridge.log("GboardMirrorPassThrough: hook unavailable on this Gboard build: " + e);
  }
 }
}

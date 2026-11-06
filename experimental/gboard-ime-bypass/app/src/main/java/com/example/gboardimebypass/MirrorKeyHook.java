package com.example.gboardimebypass;
import android.app.Application;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import de.robv.android.xposed.AndroidAppHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Experimental, app-side input-stage bypass. This module never alters the IME APK,
 * injects input, or intercepts credentials. It only returns NOT_HANDLED for Xiaomi
 * synthetic text keys when the selected IME is Gboard.
 */
public final class MirrorKeyHook implements IXposedHookLoadPackage {
  private static final String TAG = "MiKeyImeBypass";
  private static final String GBOARD = "com.google.android.inputmethod.latin/";
  private static final int XIAOMI_DEVICE_ID = -100;
  @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam pkg) throws Throwable {
    final String name=pkg.packageName;
    // Constrain first experiments to previously tested editor apps only.
    if (!"com.example.mirrorkeydiag".equals(name) && !"com.google.android.keep".equals(name)) return;
    try {
      XposedBridge.hookAllMethods(InputMethodManager.class, "dispatchInputEvent", new XC_MethodHook() {
        @Override protected void beforeHookedMethod(MethodHookParam p) {
          try {
            if (p.args==null || p.args.length==0 || !(p.args[0] instanceof KeyEvent)) return;
            KeyEvent k=(KeyEvent)p.args[0];
            if(k.getDeviceId()!=XIAOMI_DEVICE_ID
               || (k.getSource() & InputDevice.SOURCE_KEYBOARD)!=InputDevice.SOURCE_KEYBOARD
               || k.getScanCode()!=0) return;
            // Only printable keys. Leave Backspace, Enter, navigation and shortcuts untouched.
            int cp=k.getUnicodeChar();
            if(cp<0x20 || cp==0x7f || k.isCtrlPressed() || k.isAltPressed() || k.isMetaPressed()) return;
            Application app=AndroidAppHelper.currentApplication();
            if(app==null) return;
            String id=Settings.Secure.getString(app.getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
            if(id==null || !id.startsWith(GBOARD)) return;
            p.setResult(0); // InputMethodManager.DISPATCH_NOT_HANDLED
            Log.i(TAG,"BYPASS "+name+" action="+k.getAction()+" key="+KeyEvent.keyCodeToString(k.getKeyCode())+
                     " id="+k.getDeviceId()+" source=0x"+Integer.toHexString(k.getSource()));
          } catch(Throwable err) {
            Log.e(TAG,"Bypass hook failed; original event left intact",err);
          }
        }
      });
      XposedBridge.log("MiKeyImeBypass HOOK_INSTALLED: "+name);
      Log.i(TAG,"HOOK_INSTALLED "+name);
    } catch (Throwable error) {
      XposedBridge.log("MiKeyImeBypass HOOK_FAILED: "+name+" "+error);
      Log.e(TAG,"HOOK_FAILED "+name,error);
    }
  }
}

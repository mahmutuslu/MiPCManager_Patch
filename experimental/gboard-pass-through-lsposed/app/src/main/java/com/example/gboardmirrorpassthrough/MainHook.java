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

/**
 * Experimental, Gboard-scoped input fix for Xiaomi Mirror virtual KeyEvents.
 * It changes no Gboard APK or system files; LSPosed can disable it at any time.
 */
public final class MainHook implements IXposedHookLoadPackage {
 private static final String PKG = "com.google.android.inputmethod.latin";
 private static final int MIRROR_DEVICE_ID = -100;
 private final SparseBooleanArray consumedDown = new SparseBooleanArray();

 private static boolean mirrored(KeyEvent e) {
  return e != null && e.getDeviceId() == MIRROR_DEVICE_ID &&
    (e.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD;
 }

 private static boolean altGr(KeyEvent e) {
  int m = e.getMetaState();
  // Android OEMs differ: right-Alt or the Windows Ctrl+Alt (AltGr) emulation.
  return (m & KeyEvent.META_ALT_RIGHT_ON) != 0 ||
     ((m & KeyEvent.META_CTRL_ON) != 0 && (m & KeyEvent.META_ALT_ON) != 0) ||
     ((m & KeyEvent.META_ALT_ON) != 0 && (m & KeyEvent.META_ALT_LEFT_ON) == 0);
 }

 private static boolean ordinaryShortcut(KeyEvent e) {
  int m = e.getMetaState();
  if ((m & KeyEvent.META_META_ON) != 0) return true;
  if ((m & (KeyEvent.META_ALT_ON | KeyEvent.META_CTRL_ON)) != 0 && !altGr(e)) return true;
  return false;
 }

 private static boolean upper(KeyEvent e) {
  return e.isShiftPressed() ^ e.isCapsLockOn();
 }

 private static String turkishLetter(int key, KeyEvent e) {
  boolean u=upper(e);
  switch (key) {
   case KeyEvent.KEYCODE_LEFT_BRACKET: return u ? "Ğ" : "ğ";
   case KeyEvent.KEYCODE_RIGHT_BRACKET: return u ? "Ü" : "ü";
   case KeyEvent.KEYCODE_SEMICOLON: return u ? "Ş" : "ş";
   // Keep mappings verified on this Xiaomi/Windows keyboard: native mirror
   // sends the physical Ö and Ç keys as KEYCODE_SLASH / KEYCODE_BACKSLASH.
   case KeyEvent.KEYCODE_SLASH: return u ? "Ö" : "ö";
   case KeyEvent.KEYCODE_BACKSLASH: return u ? "Ç" : "ç";
   case KeyEvent.KEYCODE_I: return u ? "I" : "ı";
   case KeyEvent.KEYCODE_APOSTROPHE: return u ? "İ" : "i";
   default: return null;
  }
 }

 private static String altGrSymbol(int key, KeyEvent e) {
  switch (key) {
   case KeyEvent.KEYCODE_Q: return "@";
   case KeyEvent.KEYCODE_E: return "€";
   case KeyEvent.KEYCODE_T: return "₺";
   case KeyEvent.KEYCODE_2: return "£";
   case KeyEvent.KEYCODE_3: return "#";
   case KeyEvent.KEYCODE_4: return "$";
   case KeyEvent.KEYCODE_5: return "½";
   case KeyEvent.KEYCODE_7: return "{";
   case KeyEvent.KEYCODE_8: return "[";
   case KeyEvent.KEYCODE_9: return "]";
   case KeyEvent.KEYCODE_0: return "}";
   case KeyEvent.KEYCODE_MINUS: return "\\";
   case KeyEvent.KEYCODE_EQUALS: return "|";
   case KeyEvent.KEYCODE_S: return "ß";
   case KeyEvent.KEYCODE_A: return e.isShiftPressed() ? "Æ" : "æ";
   case KeyEvent.KEYCODE_GRAVE: return "<";
   case KeyEvent.KEYCODE_I: return e.isShiftPressed() ? "İ" : "i";
   default: return null;
  }
 }

 private static String shiftedTurkishSymbol(int key, KeyEvent e) {
  boolean shifted=e.isShiftPressed();
  switch (key) {
   case KeyEvent.KEYCODE_1: return shifted ? "!" : null;
   case KeyEvent.KEYCODE_2: return shifted ? "'" : null;
   case KeyEvent.KEYCODE_3: return shifted ? "^" : null;
   case KeyEvent.KEYCODE_4: return shifted ? "+" : null;
   case KeyEvent.KEYCODE_5: return shifted ? "%" : null;
   case KeyEvent.KEYCODE_6: return shifted ? "&" : null;
   case KeyEvent.KEYCODE_7: return shifted ? "/" : null;
   case KeyEvent.KEYCODE_8: return shifted ? "(" : null;
   case KeyEvent.KEYCODE_9: return shifted ? ")" : null;
   case KeyEvent.KEYCODE_0: return shifted ? "=" : null;
   case KeyEvent.KEYCODE_MINUS: return shifted ? "_" : "-";
   case KeyEvent.KEYCODE_EQUALS: return shifted ? "?" : "*";
   case KeyEvent.KEYCODE_PERIOD: return shifted ? ":" : ".";
   case KeyEvent.KEYCODE_COMMA: return shifted ? ";" : ",";
   case KeyEvent.KEYCODE_GRAVE: return shifted ? "é" : Character.toString((char)34);
   default: return null;
  }
 }

 private static String mappedText(int key, KeyEvent e) {
  // Some Xiaomi versions encode AltGr+Q as KEYCODE_AT or a literal '@'.
  // Always respect a direct @ event, even if modifier metadata was lost.
  if (key == KeyEvent.KEYCODE_AT || (key==KeyEvent.KEYCODE_Q && e.getUnicodeChar()=='@'))
   return "@";
  if (altGr(e)) return altGrSymbol(key,e);
  if (ordinaryShortcut(e)) return null;
  String letter=turkishLetter(key,e);
  if (letter!=null) return letter;
  return shiftedTurkishSymbol(key,e);
 }

 private static InputConnection connection(Object target) {
  return target instanceof InputMethodService ?
    ((InputMethodService)target).getCurrentInputConnection() : null;
 }

 private static boolean delete(InputConnection ic, boolean forward) {
  try {
   CharSequence selected=ic.getSelectedText(0);
   if (selected!=null && selected.length()>0) return ic.commitText("",1);
   if (forward) return ic.deleteSurroundingTextInCodePoints(0,1);
   return ic.deleteSurroundingTextInCodePoints(1,0);
  } catch (Throwable t) {
   XposedBridge.log("GboardMirrorTR v0.6 delete failed: "+t);
   return false;
  }
 }

 private static boolean passPrintable(int key, KeyEvent e) {
  if (ordinaryShortcut(e) || altGr(e)) return false;
  if (e.getRepeatCount()!=0) return false;
  int cp=e.getUnicodeChar();
  return cp>=32 && Character.isValidCodePoint(cp) && !Character.isISOControl(cp);
 }

 private static boolean passEditingKey(int key) {
  switch(key) {
   case KeyEvent.KEYCODE_ENTER: case KeyEvent.KEYCODE_TAB:
   case KeyEvent.KEYCODE_DPAD_LEFT: case KeyEvent.KEYCODE_DPAD_RIGHT:
   case KeyEvent.KEYCODE_DPAD_UP: case KeyEvent.KEYCODE_DPAD_DOWN:
   case KeyEvent.KEYCODE_MOVE_HOME: case KeyEvent.KEYCODE_MOVE_END:
   case KeyEvent.KEYCODE_ESCAPE: case KeyEvent.KEYCODE_PAGE_UP:
   case KeyEvent.KEYCODE_PAGE_DOWN: return true;
   default: return false;
  }
 }

 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
  if (!PKG.equals(lp.packageName)) return;
  try {
   XposedHelpers.findAndHookMethod("aeht",lp.classLoader,"onKeyDown",int.class,KeyEvent.class,new XC_MethodHook(){
    @Override protected void beforeHookedMethod(MethodHookParam p) {
     int key=(Integer)p.args[0];
     KeyEvent e=(KeyEvent)p.args[1];
     if (!mirrored(e)) return;
     InputConnection ic=connection(p.thisObject);

     // Android's InputConnection provides native editing actions for focused views.
     // Gboard normally swallows these synthetic Xiaomi Ctrl events, so dispatch
     // only the exact Ctrl+A/C/V combinations before ordinaryShortcut() returns.
     // Do not hijack AltGr (often Ctrl+Alt), Shift variants, Win/Meta shortcuts.
     if (e.isCtrlPressed() && !e.isAltPressed() && !e.isMetaPressed()
         && !e.isShiftPressed() && e.getRepeatCount() == 0) {
      final int action;
      switch (key) {
       case KeyEvent.KEYCODE_A: action = android.R.id.selectAll; break;
       case KeyEvent.KEYCODE_C: action = android.R.id.copy; break;
       case KeyEvent.KEYCODE_V: action = android.R.id.paste; break;
       default: action = 0;
      }
      if (action != 0) {
       try {
        if (ic != null && ic.performContextMenuAction(action)) {
         synchronized (consumedDown) { consumedDown.put(key, true); }
         XposedBridge.log("GboardMirrorTR v0.6: Ctrl shortcut performed for key=" + key);
         p.setResult(true);
         return;
        }
        XposedBridge.log("GboardMirrorTR v0.6: Ctrl shortcut unsupported for key=" + key);
       } catch (Throwable error) {
        XposedBridge.log("GboardMirrorTR v0.6: Ctrl shortcut failed for key=" + key + ": " + error);
       }
      }
     }

     // Xiaomi PC Manager maps the physical Turkish-Q Shift+asterisk (?) position
     // to an UNKNOWN key with no unicode value. This exact event was observed in
     // MirrorKeyDiag: deviceId=-100 source=0x101 scanCode=0 meta=0x1 flags=0x8.
     // Do NOT map Unicode 63 universally: Shift+Ö also reports unicode=63!
     if (key == KeyEvent.KEYCODE_UNKNOWN && e.getScanCode() == 0
         && e.getUnicodeChar() == 0 && e.isShiftPressed()
         && e.getRepeatCount() == 0
         && (e.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0) {
      try {
       if (ic != null && ic.commitText("?", 1)) {
        synchronized (consumedDown) { consumedDown.put(key, true); }
        XposedBridge.log("GboardMirrorTR v0.6: mapped Xiaomi Shift+UNKNOWN to ?");
        p.setResult(true);
        return;
       }
      } catch (Throwable error) {
       XposedBridge.log("GboardMirrorTR v0.6: question mark commit failed: " + error);
      }
     }

     // Backspace and Delete previously disappeared in Gboard's IME stage.
     if ((key==KeyEvent.KEYCODE_DEL || key==KeyEvent.KEYCODE_FORWARD_DEL) &&
          !ordinaryShortcut(e) && !altGr(e)) {
      if (ic!=null && delete(ic,key==KeyEvent.KEYCODE_FORWARD_DEL)) {
       synchronized(consumedDown){ consumedDown.put(key,true); }
       p.setResult(true);
      } else p.setResult(false);
      return;
     }

     if (ordinaryShortcut(e)) return;

     // Punctuation/AltGr and verified Turkish letters through the real InputConnection.
     String txt=mappedText(key,e);
     if (txt!=null) {
      try {
       if (ic!=null && ic.commitText(txt,1)) {
        synchronized(consumedDown){consumedDown.put(key,true);}
        p.setResult(true);
        return;
       }
      } catch(Throwable t) { XposedBridge.log("GboardMirrorTR v0.6 commit failed: "+t); }
     }

     // v0.1-compatible pass-through for everything else printable.
     if (passPrintable(key,e) || passEditingKey(key)) p.setResult(false);
    }
   });

   XposedHelpers.findAndHookMethod("aeht",lp.classLoader,"onKeyUp",int.class,KeyEvent.class,new XC_MethodHook(){
    @Override protected void beforeHookedMethod(MethodHookParam p) {
     int key=(Integer)p.args[0];
     KeyEvent e=(KeyEvent)p.args[1];
     if (!mirrored(e)) return;
     synchronized(consumedDown) {
      if (consumedDown.get(key)) {
       consumedDown.delete(key);
       p.setResult(true);
       return;
      }
     }
     if (passPrintable(key,e) || passEditingKey(key) ||
         key==KeyEvent.KEYCODE_DEL || key==KeyEvent.KEYCODE_FORWARD_DEL)
      p.setResult(false);
    }
   });
   XposedBridge.log("GboardMirrorTR v0.6: Turkish Q symbols + backspace active for deviceId=-100");
  } catch(Throwable t) {
   XposedBridge.log("GboardMirrorTR v0.6: hook unavailable: "+t);
  }
 }
}

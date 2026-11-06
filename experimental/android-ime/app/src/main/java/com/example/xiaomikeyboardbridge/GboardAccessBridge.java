package com.example.xiaomikeyboardbridge;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.function.BiConsumer;

/**
 * Optional Gboard-compatible bridge. The OS input method remains Gboard.
 * Requires explicit Accessibility approval. Only acts on a valid authenticated
 * one-character command; does not collect events, keystrokes, or text history.
 */
public final class GboardAccessBridge extends AccessibilityService {
  private static volatile GboardAccessBridge active;
  private static final Handler MAIN = new Handler(Looper.getMainLooper());
  @Override protected void onServiceConnected() { super.onServiceConnected(); active=this; }
  @Override public void onDestroy() { if(active==this) active=null; super.onDestroy(); }
  @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* no logging */ }
  @Override public void onInterrupt() { }
  public static boolean enabled() { return active!=null; }

  static void insert(String value, BiConsumer<Integer,String> completion) {
    MAIN.post(() -> {
      GboardAccessBridge service=active;
      if(service==null) { completion.accept(30,"ACCESSIBILITY_NOT_ENABLED"); return; }
      AccessibilityNodeInfo root=service.getRootInActiveWindow();
      if(root==null) { completion.accept(31,"NO_ACTIVE_WINDOW"); return; }
      AccessibilityNodeInfo target=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
      if(target==null || !target.isEditable() || target.isPassword()) {
        completion.accept(32,"NO_SAFE_EDITABLE_FOCUS"); return;
      }
      CharSequence oldChars=target.getText();
      if(oldChars==null) { completion.accept(33,"TEXT_NOT_READABLE"); return; }
      String oldText=oldChars.toString();
      // Prevent rewriting unexpectedly long or private fields.
      if(oldText.length()>4096) { completion.accept(34,"TEXT_TOO_LONG"); return; }
      int start=target.getTextSelectionStart();
      int end=target.getTextSelectionEnd();
      if(start<0 || end<start || end>oldText.length()) {
        completion.accept(35,"SELECTION_UNKNOWN"); return;
      }
      String newText=oldText.substring(0,start)+value+oldText.substring(end);
      Bundle args=new Bundle();
      args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,newText);
      if(!target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)) {
        completion.accept(36,"SET_TEXT_REJECTED"); return;
      }
      // ACTION_SET_TEXT generally puts the cursor at the end. Restore its
      // intended position when insertion occurred in the middle of the text.
      int next=start+value.length();
      Bundle sel=new Bundle();
      sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT,next);
      sel.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT,next);
      target.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION,sel);
      MAIN.postDelayed(() -> {
        AccessibilityNodeInfo latest=service.getRootInActiveWindow();
        AccessibilityNodeInfo focused=latest==null ? null :
            latest.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        String now=focused==null||focused.getText()==null ? null : focused.getText().toString();
        if(newText.equals(now)) completion.accept(120,"GBOARD_TEXT_VERIFIED");
        else completion.accept(121,"TEXT_NOT_VERIFIED");
      }, 150);
    });
  }
}

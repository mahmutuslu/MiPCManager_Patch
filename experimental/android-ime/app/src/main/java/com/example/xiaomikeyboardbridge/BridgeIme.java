package com.example.xiaomikeyboardbridge;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputConnection;
import android.os.Handler;
import android.os.Looper;
import java.util.function.BiConsumer;

/**
 * Experimental IME: verify a Unicode insert by reading the target editor.
 * Never retry blindly if the editor does not support text inspection.
 */
public final class BridgeIme extends InputMethodService {
  private static volatile BridgeIme active;
  private static final Handler MAIN = new Handler(Looper.getMainLooper());

  @Override public void onCreate() { super.onCreate(); active = this; }
  @Override public void onDestroy() { if (active == this) active = null; super.onDestroy(); }

  private static String before(InputConnection ic) {
    try {
      CharSequence text = ic.getTextBeforeCursor(256, 0);
      return text == null ? null : text.toString();
    } catch (Exception ignored) { return null; }
  }

  private static boolean appended(String oldText, String newText, String value) {
    return oldText != null && newText != null &&
        newText.equals(oldText + value);
  }

  static void commit(String value, BiConsumer<Integer, String> completion) {
    MAIN.post(() -> {
      BridgeIme ime = active;
      if (ime == null) { completion.accept(22, "IME_NOT_ACTIVE"); return; }
      InputConnection ic = ime.getCurrentInputConnection();
      if (ic == null) { completion.accept(23, "NO_INPUT_CONNECTION"); return; }

      final String oldText = before(ic);
      final boolean committed;
      try {
        committed = ic.commitText(value, 1);
      } catch (Exception ex) {
        completion.accept(25, "COMMIT_EXCEPTION"); return;
      }
      // The editor may update asynchronously. Sample after a short delay.
      MAIN.postDelayed(() -> {
        String newText = before(ic);
        if (appended(oldText, newText, value)) {
          completion.accept(100, "INSERT_VERIFIED"); return;
        }
        if (oldText == null || newText == null) {
          completion.accept(committed ? 110 : 24,
              committed ? "COMMIT_ACCEPTED_NOT_VERIFIABLE" : "COMMIT_REJECTED");
          return;
        }
        if (!newText.equals(oldText)) {
          completion.accept(111, "TEXT_CHANGED_BUT_NOT_APPEND"); return;
        }
        // An editor reported success but still shows no insertion. Try
        // an actual composing-text finalization, only after verifying no change.
        try {
          boolean composing = ic.setComposingText(value, 1);
          boolean finished = composing && ic.finishComposingText();
          MAIN.postDelayed(() -> {
            String afterFallback = before(ic);
            if (appended(oldText, afterFallback, value)) {
              completion.accept(101, "COMPOSING_FALLBACK_VERIFIED");
            } else if (afterFallback != null && !afterFallback.equals(oldText)) {
              completion.accept(112, "FALLBACK_CHANGED_OTHER_TEXT");
            } else {
              completion.accept(finished ? 113 : 114,
                  finished ? "NO_VISIBLE_CHANGE_AFTER_BOTH" : "FALLBACK_REJECTED");
            }
          }, 180);
        } catch (Exception ex) {
          completion.accept(115, "FALLBACK_EXCEPTION");
        }
      }, 220);
    });
  }
}

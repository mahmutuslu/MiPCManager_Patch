package com.example.xiaomikeyboardbridge;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputConnection;
import android.os.Handler;
import android.os.Looper;
import java.util.function.BiConsumer;

public final class BridgeIme extends InputMethodService {
  private static volatile BridgeIme active;
  private static final Handler main = new Handler(Looper.getMainLooper());

  @Override public void onCreate() { super.onCreate(); active = this; }
  @Override public void onDestroy() { if(active == this) active = null; super.onDestroy(); }

  // Completion is reported only after Android's InputConnection call runs.
  static void commit(String value, BiConsumer<Integer,String> completion) {
    main.post(() -> {
      BridgeIme ime = active;
      if(ime == null) { completion.accept(22, "IME_NOT_ACTIVE"); return; }
      InputConnection ic = ime.getCurrentInputConnection();
      if(ic == null) { completion.accept(23, "NO_INPUT_CONNECTION"); return; }
      try {
        boolean ok = ic.commitText(value, 1);
        completion.accept(ok ? 100 : 24, ok ? "COMMIT_ACCEPTED" : "COMMIT_REJECTED");
      } catch(Exception ex) {
        completion.accept(25, "COMMIT_EXCEPTION");
      }
    });
  }
}

package com.example.xiaomikeyboardbridge;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputConnection;
import android.os.Handler;
import android.os.Looper;
public final class BridgeIme extends InputMethodService {
  private static volatile BridgeIme active;
  private static final Handler main = new Handler(Looper.getMainLooper());
  @Override public void onCreate() { super.onCreate(); active = this; }
  @Override public void onDestroy() { if(active == this) active = null; super.onDestroy(); }
  static void commit(String text) {
    main.post(() -> {
      BridgeIme ime = active;
      if(ime == null) return;
      InputConnection connection = ime.getCurrentInputConnection();
      if(connection != null) connection.commitText(text, 1);
    });
  }
}

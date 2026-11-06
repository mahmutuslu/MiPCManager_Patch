package com.example.xiaomikeyboardbridge;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Single-codepoint bridge. Uses integer extras to avoid shell/locale quoting. */
public final class TextReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    if (!"com.example.xiaomikeyboardbridge.SEND".equals(i.getAction())) {
      setResultCode(20); setResultData("WRONG_ACTION"); return;
    }
    String secret = c.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
        .getString("secret", null);
    if (secret == null || !secret.equals(i.getStringExtra("token"))) {
      setResultCode(21); setResultData("BAD_TOKEN"); return;
    }
    String value;
    if (i.hasExtra("cp")) {
      int cp = i.getIntExtra("cp", -1);
      if (!Character.isValidCodePoint(cp) || cp < 0x20 ||
          cp == 0x7F || (cp >= 0xD800 && cp <= 0xDFFF)) {
        setResultCode(26); setResultData("INVALID_CODEPOINT"); return;
      }
      value = new String(Character.toChars(cp));
    } else {
      value = i.getStringExtra("char");
      if (value == null || value.codePointCount(0, value.length()) != 1 ||
          !"ğĞüÜşŞıİiIöÖçÇ".contains(value)) {
        setResultCode(26); setResultData("INVALID_CHARACTER"); return;
      }
    }
    final PendingResult pending = goAsync();
    java.util.function.BiConsumer<Integer,String> result = (code, status) -> {
      try { pending.setResultCode(code); pending.setResultData(status); }
      finally { pending.finish(); }
    };
    if (GboardAccessBridge.enabled()) GboardAccessBridge.insert(value, result);
    else BridgeIme.commit(value, result);
  }
}

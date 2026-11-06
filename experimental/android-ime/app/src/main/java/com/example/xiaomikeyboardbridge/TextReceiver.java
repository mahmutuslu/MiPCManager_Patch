package com.example.xiaomikeyboardbridge;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class TextReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    if(!"com.example.xiaomikeyboardbridge.SEND".equals(i.getAction())) {
      setResultCode(20); setResultData("WRONG_ACTION"); return;
    }
    String secret = c.getSharedPreferences("MainActivity", Context.MODE_PRIVATE).getString("secret", null);
    if(secret == null || !secret.equals(i.getStringExtra("token"))) {
      setResultCode(21); setResultData("BAD_TOKEN"); return;
    }
    String value = i.getStringExtra("char");
    if(value == null || value.codePointCount(0, value.length()) != 1 ||
        !"ğĞüÜşŞıİiIöÖçÇ".contains(value)) {
      setResultCode(26); setResultData("INVALID_CHARACTER"); return;
    }
    final PendingResult pending = goAsync();
    BridgeIme.commit(value, (code, status) -> {
      try { pending.setResultCode(code); pending.setResultData(status); }
      finally { pending.finish(); }
    });
  }
}

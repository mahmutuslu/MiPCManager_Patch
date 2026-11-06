package com.example.xiaomikeyboardbridge;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public final class TextReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    if(!"com.example.xiaomikeyboardbridge.SEND".equals(i.getAction())) return;
    String token = i.getStringExtra("token");
    String secret = c.getSharedPreferences("MainActivity", Context.MODE_PRIVATE).getString("secret", null);
    if(secret == null || !secret.equals(token)) return;
    String value = i.getStringExtra("char");
    if(value != null && value.codePointCount(0, value.length()) == 1 &&
      "ğĞüÜşŞıİöÖçÇ".contains(value)) BridgeIme.commit(value);
  }
}

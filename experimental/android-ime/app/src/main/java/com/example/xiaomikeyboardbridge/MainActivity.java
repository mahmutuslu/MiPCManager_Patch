package com.example.xiaomikeyboardbridge;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import java.security.SecureRandom;
import java.util.Locale;
public final class MainActivity extends Activity {
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    if(!getPreferences(MODE_PRIVATE).contains("secret")) {
      byte[] data = new byte[16]; new SecureRandom().nextBytes(data);
      StringBuilder h = new StringBuilder();
      for(byte c : data) h.append(String.format(Locale.ROOT, "%02x", c & 255));
      getPreferences(MODE_PRIVATE).edit().putString("secret", h.toString()).apply();
    }
    LinearLayout l = new LinearLayout(this); l.setOrientation(1); l.setPadding(28,28,28,28);
    TextView t = new TextView(this);
    t.setText("Deneysel Türkçe Q giriş yöntemi. Xiaomi APK dosyalarını değiştirmez.\n\nEşleştirme anahtarı:\n" +
       getPreferences(MODE_PRIVATE).getString("secret", ""));
    t.setTextIsSelectable(true); l.addView(t);
    Button enable = new Button(this); enable.setText("1 - Klavyeyi etkinleştir");
    enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))); l.addView(enable);
    Button choose = new Button(this); choose.setText("2 - Klavyeyi seç");
    choose.setOnClickListener(v -> ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker());
    l.addView(choose);
    Button access = new Button(this); access.setText("3 - Gboard destegi: Erisilebilirlik ayarlari");
    access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
    l.addView(access);
    TextView w = new TextView(this); w.setText("Gboard modu: Gboard varsayilan klavye olarak secilir; Xiaomi Gboard Turkce Q Kopru erisilebilirlik hizmeti ayarlardan acilir. Erisilebilirlik izni ekrandaki metinlere erisebilir: test disinda kapatin. Parola alanlari atlanir. Eski IME modu da korunur.");
    l.addView(w); setContentView(l);
  }
}

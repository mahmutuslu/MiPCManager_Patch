package com.example.gboardimebypass;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public final class MainActivity extends Activity {
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    TextView text = new TextView(this);
    text.setPadding(35,50,35,30);
    text.setTextSize(17);
    text.setText("Gboard Mirror IME Bypass | Experimental v0.1\n\n"+
      "This is an LSPosed module, not an Android keyboard.\n\n"+
      "Enable this module in LSPosed. Select ONLY Mirror Key Diag (com.example.mirrorkeydiag) for the first test, then fully restart Mirror Key Diag.\n\n"+
      "Keep Gboard selected and type abc123 through Xiaomi mirroring.\n\n"+
      "Expected: MirrorKeyDiag VIEW_DOWN events; text may appear.\n\n"+
      "Logs: adb logcat -s MiKeyImeBypass:I '*:S'\n\n"+
      "Disable module in LSPosed to revert.\n\n"+
      "No permanent ADB/PowerShell bridge. No modifications to Gboard or Xiaomi Mirror.");
    setContentView(text);
  }
}

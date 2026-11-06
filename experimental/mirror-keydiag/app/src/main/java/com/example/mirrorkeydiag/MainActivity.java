package com.example.mirrorkeydiag;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.content.*;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

public class MainActivity extends Activity {
  private static final String TAG="MirrorKeyDiag";
  private final StringBuilder lines=new StringBuilder();
  private TextView output;
  private Probe editor;

  private void log(String value) {
    String line=SystemClock.uptimeMillis()+" | "+value;
    Log.i(TAG,line);
    lines.append(line).append("\n");
    if(lines.length()>64000) lines.delete(0,32000);
    if(output!=null) output.setText(lines.toString());
  }
  private String event(KeyEvent k) {
    return "action="+k.getAction()+" key="+KeyEvent.keyCodeToString(k.getKeyCode())+
      " deviceId="+k.getDeviceId()+" source=0x"+Integer.toHexString(k.getSource())+
      " scanCode="+k.getScanCode()+
      " meta=0x"+Integer.toHexString(k.getMetaState())+
      " flags=0x"+Integer.toHexString(k.getFlags())+
      " unicode="+k.getUnicodeChar()+" repeat="+k.getRepeatCount();
  }
  private String printable(CharSequence s) {
    return s==null?"null":s.toString().replace("\n","\\n").replace("\r","\\r");
  }
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    LinearLayout root=new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(14,12,14,10);
    TextView heading=new TextView(this);
    heading.setText("Mirror Key Diag | type abc123, Turkish Q, Backspace using Xiaomi mirroring");
    heading.setTextSize(16);
    root.addView(heading);
    editor=new Probe(this);
    editor.setTextSize(20);
    editor.setMinLines(3);
    editor.setHint("Type here...");
    editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    root.addView(editor,new LinearLayout.LayoutParams(-1,0,2));
    LinearLayout actions=new LinearLayout(this);
    Button copy=new Button(this);
    copy.setText("Copy logs");
    copy.setOnClickListener(v->{
      ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
      if(clipboard!=null) clipboard.setPrimaryClip(ClipData.newPlainText("MirrorKeyDiag",lines.toString()));
      log("LOGS_COPIED");
    });
    actions.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
    Button clear=new Button(this);
    clear.setText("Clear logs");
    clear.setOnClickListener(v->{lines.setLength(0); if(output!=null)output.setText("");log("LOGS_CLEARED");editor.requestFocus();});
    actions.addView(clear,new LinearLayout.LayoutParams(0,-2,1));
    root.addView(actions);
    ScrollView scroll=new ScrollView(this);
    output=new TextView(this);
    output.setTypeface(Typeface.MONOSPACE);
    output.setTextSize(10);
    output.setTextIsSelectable(true);
    scroll.addView(output);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,4));
    setContentView(root);
    log("ACTIVITY_CREATED displayId="+getDisplay().getDisplayId());
    editor.requestFocus();
  }
  @Override protected void onResume(){super.onResume();log("ACTIVITY_RESUMED");}
  @Override protected void onPause(){log("ACTIVITY_PAUSED");super.onPause();}
  @Override public boolean dispatchKeyEvent(KeyEvent k){log("ACTIVITY_DISPATCH "+event(k));return super.dispatchKeyEvent(k);}
  class Probe extends EditText {
    Probe(Context context){super(context);}
    @Override public boolean onKeyPreIme(int key,KeyEvent k){log("VIEW_PRE_IME "+event(k));return super.onKeyPreIme(key,k);}
    @Override public boolean dispatchKeyEvent(KeyEvent k){log("VIEW_DISPATCH "+event(k));return super.dispatchKeyEvent(k);}
    @Override public boolean onKeyDown(int key,KeyEvent k){log("VIEW_DOWN "+event(k));return super.onKeyDown(key,k);}
    @Override public boolean onKeyUp(int key,KeyEvent k){log("VIEW_UP "+event(k));return super.onKeyUp(key,k);}
    @Override public InputConnection onCreateInputConnection(EditorInfo attrs){
      InputConnection base=super.onCreateInputConnection(attrs);
      log("CONNECTION_CREATED base="+(base!=null)+" inputType=0x"+Integer.toHexString(attrs.inputType));
      if(base==null)return null;
      return new InputConnectionWrapper(base,false){
        @Override public boolean commitText(CharSequence s,int cursor){log("IC_COMMIT_TEXT ["+printable(s)+"] cursor="+cursor);return super.commitText(s,cursor);}
        @Override public boolean setComposingText(CharSequence s,int cursor){log("IC_COMPOSING_TEXT ["+printable(s)+"] cursor="+cursor);return super.setComposingText(s,cursor);}
        @Override public boolean sendKeyEvent(KeyEvent k){log("IC_SEND_KEY "+event(k));return super.sendKeyEvent(k);}
        @Override public boolean deleteSurroundingText(int a,int b){log("IC_DELETE before="+a+" after="+b);return super.deleteSurroundingText(a,b);}
        @Override public boolean finishComposingText(){log("IC_FINISH_COMPOSING");return super.finishComposingText();}
      };
    }
  }
}

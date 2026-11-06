# V6.9 experimental Turkish Q punctuation and AltGr bridge (PowerShell 5.1).
# Requires the APK version with --ei cp support. Runs only when Xiaomi mirror has focus.
param([string]$Adb='C:\platform-tools\adb.exe',[Parameter(Mandatory=$true)][string]$Token)
$ErrorActionPreference='Stop'
if (!(Test-Path $Adb)) { throw "ADB executable missing: $Adb" }
Add-Type -ReferencedAssemblies @('System.Windows.Forms','System.Drawing') -TypeDefinition @'
using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;
public static class TurkishQBridge71 {
  [StructLayout(LayoutKind.Sequential)]
  private struct Kbd { public int vkCode, scanCode, flags, time; public IntPtr extra; }
  private delegate IntPtr HookProc(int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr SetWindowsHookEx(int id,HookProc callback,IntPtr module,uint thread);
  [DllImport("user32.dll")] private static extern bool UnhookWindowsHookEx(IntPtr hook);
  [DllImport("user32.dll")] private static extern IntPtr CallNextHookEx(IntPtr hook,int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr GetForegroundWindow();
  [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr hwnd,out uint pid);
  [DllImport("user32.dll")] private static extern short GetAsyncKeyState(int vk);
  [DllImport("user32.dll")] private static extern short GetKeyState(int vk);
  [DllImport("kernel32.dll")] private static extern IntPtr GetModuleHandle(string module);
  private static readonly BlockingCollection<int> queue=new BlockingCollection<int>();
  private static readonly bool[] suppressed=new bool[256];
  private static HookProc hookProc=Handle;
  private static IntPtr hook;
  private static string adb, token;
  private static bool Down(int vk) { return GetAsyncKeyState(vk)<0; }
  private static bool MirrorFocus() {
    try {
      uint pid;
      GetWindowThreadProcessId(GetForegroundWindow(),out pid);
      string process=Process.GetProcessById((int)pid).ProcessName;
      return process.Equals("MiScreenShare",StringComparison.OrdinalIgnoreCase) ||
             process.Equals("XiaomiPCManager",StringComparison.OrdinalIgnoreCase);
    } catch { return false; }
  }
  private static int Letter(int sc,int vk,bool upper) {
    switch(sc) {
      case 0x1A: return upper ? 0x11E : 0x11F; // g-breve
      case 0x1B: return upper ? 0xDC : 0xFC; // u-diaeresis
      case 0x27: return upper ? 0x15E : 0x15F; // s-cedilla
      case 0x28: return upper ? 0x130 : 0x69; // dotted i
      case 0x17: return upper ? 0x49 : 0x131; // dotless i
      case 0x33: return upper ? 0xD6 : 0xF6; // o-diaeresis
      case 0x34: return upper ? 0xC7 : 0xE7; // c-cedilla
      default: return -1;
    }
  }
  private static int PlainKey(int sc, bool upper) {
    // Windows physical scan codes for the alphanumeric section, Turkish Q layout.
    switch(sc) {
      case 0x10: return upper ? 0x51 : 0x71;
      case 0x11: return upper ? 0x57 : 0x77;
      case 0x12: return upper ? 0x45 : 0x65;
      case 0x13: return upper ? 0x52 : 0x72;
      case 0x14: return upper ? 0x54 : 0x74;
      case 0x15: return upper ? 0x59 : 0x79;
      case 0x16: return upper ? 0x55 : 0x75;
      case 0x18: return upper ? 0x4F : 0x6F;
      case 0x19: return upper ? 0x50 : 0x70;
      case 0x1E: return upper ? 0x41 : 0x61;
      case 0x1F: return upper ? 0x53 : 0x73;
      case 0x20: return upper ? 0x44 : 0x64;
      case 0x21: return upper ? 0x46 : 0x66;
      case 0x22: return upper ? 0x47 : 0x67;
      case 0x23: return upper ? 0x48 : 0x68;
      case 0x24: return upper ? 0x4A : 0x6A;
      case 0x25: return upper ? 0x4B : 0x6B;
      case 0x26: return upper ? 0x4C : 0x6C;
      case 0x2C: return upper ? 0x5A : 0x7A;
      case 0x2D: return upper ? 0x58 : 0x78;
      case 0x2E: return upper ? 0x43 : 0x63;
      case 0x2F: return upper ? 0x56 : 0x76;
      case 0x30: return upper ? 0x42 : 0x62;
      case 0x31: return upper ? 0x4E : 0x6E;
      case 0x32: return upper ? 0x4D : 0x6D;
      case 0x02: return 0x31; case 0x03: return 0x32;
      case 0x04: return 0x33; case 0x05: return 0x34;
      case 0x06: return 0x35; case 0x07: return 0x36;
      case 0x08: return 0x37; case 0x09: return 0x38;
      case 0x0A: return 0x39; case 0x0B: return 0x30;
      case 0x39: return 0x20; // Space
      case 0x1C: return 0x0A; // Enter -> newline in supported editable fields
      case 0x0E: return 0x08; // Backspace request to Android service
      default: return -1;
    }
  }
  private static int Punctuation(int sc,bool shift,bool altGr) {
    // Windows Turkish Q physical scan codes. Dead-key combinations intentionally excluded.
    if(altGr) {
      switch(sc) {
        case 0x29: return 0x3C; // <
        case 0x02: return 0x3E; // >
        case 0x03: return 0xA3; // pound
        case 0x04: return 0x23; // #
        case 0x05: return 0x24; // $
        case 0x06: return 0xBD; // one half
        case 0x08: return 0x7B; // {
        case 0x09: return 0x5B; // [
        case 0x0A: return 0x5D; // ]
        case 0x0B: return 0x7D; // }
        case 0x0C: return 0x5C; // backslash
        case 0x0D: return 0x7C; // vertical bar
        case 0x56: return 0x7C; // vertical bar
        case 0x10: return 0x40; // AltGr+Q = @
        case 0x12: return 0x20AC; // euro
        case 0x14: return 0x20BA; // Turkish lira
        case 0x1E: return 0xE6; // ae
        case 0x1F: return 0xDF; // sharp s
        case 0x17: return 0x69; // dot above i
        default: return -1;
      }
    }
    switch(sc) {
      case 0x29: return shift ? 0xE9 : 0x22;
      case 0x02: return shift ? 0x21 : -1;
      case 0x03: return shift ? 0x27 : -1;
      // Shift+3 is a dead circumflex. Do not incorrectly send literal '^'.
      case 0x05: return shift ? 0x2B : -1;
      case 0x06: return shift ? 0x25 : -1;
      case 0x07: return shift ? 0x26 : -1;
      case 0x08: return shift ? 0x2F : -1;
      case 0x09: return shift ? 0x28 : -1;
      case 0x0A: return shift ? 0x29 : -1;
      case 0x0B: return shift ? 0x3D : -1;
      case 0x0C: return shift ? 0x3F : 0x2A;
      case 0x0D: return shift ? 0x5F : 0x2D;
      case 0x2B: return shift ? 0x3B : 0x2C;
      case 0x56: return shift ? 0x3E : 0x3C;
      case 0x35: return shift ? 0x3A : 0x2E;
      default: return -1;
    }
  }
  private static IntPtr Handle(int n,IntPtr w,IntPtr l) {
    if(n<0) return CallNextHookEx(hook,n,w,l);
    int msg=w.ToInt32();
    Kbd key=(Kbd)Marshal.PtrToStructure(l,typeof(Kbd));
    int vk=key.vkCode;
    if(vk<0 || vk>255) return CallNextHookEx(hook,n,w,l);
    bool down=msg==0x100||msg==0x104,up=msg==0x101||msg==0x105;
    if(up && suppressed[vk]) { suppressed[vk]=false; return (IntPtr)1; }
    if(down && suppressed[vk]) return (IntPtr)1; // swallow key auto-repeat in this prototype
    if(down && MirrorFocus()) {
      bool altGr=Down(0xA5); // right Alt
      bool ctrl=Down(0x11), alt=Down(0x12);
      if(Down(0x5B)||Down(0x5C)|| (ctrl&&!altGr) || (alt&&!altGr)) return CallNextHookEx(hook,n,w,l);
      bool shift=Down(0x10)||Down(0xA0)||Down(0xA1);
      bool caps=(GetKeyState(0x14)&1)!=0;
      int sc=key.scanCode&0xFF;
      int cp=-1;
      if(altGr) cp=Punctuation(sc,shift,true);
      else {
        cp=Letter(sc,vk,shift ^ caps);
        if(cp<0) cp=Punctuation(sc,shift,false);
        if(cp<0) cp=PlainKey(sc,shift ^ caps);
      }
      if(cp>=0) {
        queue.Add(cp);
        suppressed[vk]=true;
        return (IntPtr)1;
      }
    }
    return CallNextHookEx(hook,n,w,l);
  }
  public static void Start(string exe,string key) {
    adb=exe; token=key;
    Thread worker=new Thread(()=>{
      foreach(int cp in queue.GetConsumingEnumerable()) {
        try {
          string arguments="shell am broadcast -a com.example.xiaomikeyboardbridge.SEND -n com.example.xiaomikeyboardbridge/.TextReceiver --es token "+token+" --ei cp "+cp;
          ProcessStartInfo start=new ProcessStartInfo(adb,arguments);
          start.UseShellExecute=false; start.CreateNoWindow=true;
          start.RedirectStandardOutput=true; start.RedirectStandardError=true;
          using(Process p=Process.Start(start)) {
            string output=p.StandardOutput.ReadToEnd();
            string error=p.StandardError.ReadToEnd();
            p.WaitForExit();
            System.Console.WriteLine("U+"+cp.ToString("X4")+" | "+output.Trim());
            if(p.ExitCode!=0 || !(output.Contains("INSERT_VERIFIED")||output.Contains("COMPOSING_FALLBACK_VERIFIED")||output.Contains("GBOARD_TEXT_VERIFIED")||output.Contains("GBOARD_NO_CHANGE"))) {
              System.Console.WriteLine("WARNING: insertion not verified: "+error.Trim());
            }
          }
        } catch(Exception e) { System.Console.WriteLine("ERROR: "+e.Message); }
      }
    });
    worker.IsBackground=true; worker.Start();
    hook=SetWindowsHookEx(13,hookProc,GetModuleHandle(null),0);
    if(hook==IntPtr.Zero) throw new Exception("Keyboard hook failed");
    try { Application.Run(new ApplicationContext()); }
    finally { UnhookWindowsHookEx(hook); }
  }
}
'@
Write-Host "V7.1: Gboard all printable basic keys, Space, Enter, Backspace; experimental."
Write-Host "Requires V7.1 Android APK, Gboard default and accessibility enabled. Test only in an empty note."
[TurkishQBridge71]::Start($Adb,$Token)

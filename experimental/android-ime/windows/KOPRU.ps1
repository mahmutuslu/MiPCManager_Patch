# PowerShell 5.1 experimental. Start with: .\KOPRU.ps1 -Token <phone-token>
param([string]$Adb='C:\platform-tools\adb.exe',[Parameter(Mandatory=$true)][string]$Token)
if (!(Test-Path $Adb)) { throw "adb.exe bulunamadi: $Adb" }
Add-Type -ReferencedAssemblies @('System.Windows.Forms','System.Drawing') -TypeDefinition @'
using System; using System.Collections.Concurrent; using System.Diagnostics; using System.Runtime.InteropServices; using System.Threading; using System.Windows.Forms;
public static class TurkishBridgeHook {
  [StructLayout(LayoutKind.Sequential)] private struct Kbd { public int vkCode; public int scanCode; public int flags; public int time; public IntPtr extra; }
  private delegate IntPtr HookProc(int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr SetWindowsHookEx(int id,HookProc cb,IntPtr h,uint thread);
  [DllImport("user32.dll")] private static extern bool UnhookWindowsHookEx(IntPtr hook);
  [DllImport("user32.dll")] private static extern IntPtr CallNextHookEx(IntPtr hook,int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr GetForegroundWindow();
  [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr hwnd,out uint pid);
  [DllImport("user32.dll")] private static extern short GetAsyncKeyState(int vk);
  [DllImport("kernel32.dll")] private static extern IntPtr GetModuleHandle(string name);
  private static IntPtr hook; private static HookProc handler = Handle;
  private static BlockingCollection<string> queue = new BlockingCollection<string>();
  private static string adb, token; private static bool[] ignored=new bool[256];
  // Turkish Q VK mapping observed in Windows diagnostics; independent of the target's active input language.
  private static string TurkishChar(int vk, bool shift) {
    switch(vk) {
      case 0xDB: return shift ? "\u011E" : "\u011F";  // VK_OEM_4
      case 0xDD: return shift ? "\u00DC" : "\u00FC";  // VK_OEM_6
      case 0xBA: return shift ? "\u015E" : "\u015F";  // VK_OEM_1
      case 0x49: return shift ? "I" : "\u0131";  // dotless i / I
      case 0xDE: return shift ? "\u0130" : "i"; // dotted i / capital dotted I
      case 0xBF: return shift ? "\u00D6" : "\u00F6";  // VK_OEM_2
      case 0xDC: return shift ? "\u00C7" : "\u00E7";  // VK_OEM_5
      default: return null;
    }
  }
  private static bool MirrorFocus() {
    try {
      uint p;
      GetWindowThreadProcessId(GetForegroundWindow(),out p);
      string name=Process.GetProcessById((int)p).ProcessName;
      return name.Equals("MiScreenShare",StringComparison.OrdinalIgnoreCase) ||
             name.Equals("XiaomiPCManager",StringComparison.OrdinalIgnoreCase);
    } catch { return false; }
  }
  private static IntPtr Handle(int n,IntPtr w,IntPtr l) {
    if(n>=0) { int msg=w.ToInt32(); Kbd k=(Kbd)Marshal.PtrToStructure(l,typeof(Kbd));
      bool down=msg==0x100||msg==0x104, up=msg==0x101||msg==0x105;
      if(k.vkCode>=0&&k.vkCode<256&&(down||up)) {
        if(up&&ignored[k.vkCode]) { ignored[k.vkCode]=false; return (IntPtr)1; }
        if(down&&MirrorFocus()) {
          if(GetAsyncKeyState(0x11)>=0 && GetAsyncKeyState(0x12)>=0 && GetAsyncKeyState(0x5B)>=0 && GetAsyncKeyState(0x5C)>=0) {
            string ch=TurkishChar(k.vkCode,GetAsyncKeyState(0x10)<0 || GetAsyncKeyState(0xA0)<0 || GetAsyncKeyState(0xA1)<0);
            if(ch!=null) {
              if(!ignored[k.vkCode]) queue.Add(ch);
              ignored[k.vkCode]=true; return (IntPtr)1;
            }
          }
        }
      }
    }
    return CallNextHookEx(hook,n,w,l);
  }
  public static void Start(string adbPath,string secret) {
    adb=adbPath; token=secret;
    System.Console.OutputEncoding = new System.Text.UTF8Encoding(false);
    Thread worker=new Thread(()=>{ foreach(string ch in queue.GetConsumingEnumerable()) {
      try { ProcessStartInfo psi=new ProcessStartInfo(adb,"shell am broadcast -a com.example.xiaomikeyboardbridge.SEND -n com.example.xiaomikeyboardbridge/.TextReceiver --es token "+token+" --es char "+ch);
        psi.UseShellExecute=false;psi.CreateNoWindow=true;psi.RedirectStandardOutput=true;psi.RedirectStandardError=true;
        using(Process p=Process.Start(psi)){ string output=p.StandardOutput.ReadToEnd(); string error=p.StandardError.ReadToEnd(); p.WaitForExit();
          System.Console.WriteLine("TUS: U+"+((int)ch[0]).ToString("X4")+" / "+output.Trim());
          if(p.ExitCode!=0 || !output.Contains("INSERT_VERIFIED")) System.Console.WriteLine("UYARI: Android gonderimi kesin dogrulanamadi. "+error.Trim());
        }
      } catch(Exception e){System.Console.WriteLine("Gonderim hatasi: "+e.Message);} }
    }); worker.IsBackground=true;worker.Start();
    hook=SetWindowsHookEx(13,handler,GetModuleHandle(null),0);
    if(hook==IntPtr.Zero)throw new Exception("Klavyeye baglanilamadi.");
    Application.Run(new ApplicationContext());
    UnhookWindowsHookEx(hook);
  }
}
'@
Write-Host 'V6.6: Turkce Q fiziksel tuslarini Xiaomi penceresinde yakala, Android IME uzerinden ekle.'
Write-Host 'Not: ADB gecikmesi olabilir. Normal harflerle arka arkaya yazarken sira degisebilir.'
Write-Host 'Kapatmak icin bu pencereyi kapat veya Ctrl+C kullan.'
[TurkishBridgeHook]::Start($Adb,$Token)

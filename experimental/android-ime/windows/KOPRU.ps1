# PowerShell 5.1 experimental. Start with: .\KOPRU.ps1 -Token <phone-token>
param([string]$Adb='C:\platform-tools\adb.exe',[Parameter(Mandatory=$true)][string]$Token)
if (!(Test-Path $Adb)) { throw "adb.exe bulunamadi: $Adb" }
Add-Type -ReferencedAssemblies @('System.Windows.Forms','System.Drawing') -TypeDefinition @'
using System; using System.Collections.Concurrent; using System.Diagnostics; using System.Runtime.InteropServices; using System.Text; using System.Threading; using System.Windows.Forms;
public static class TurkishBridgeHook {
  [StructLayout(LayoutKind.Sequential)] private struct Kbd { public int vkCode; public int scanCode; public int flags; public int time; public IntPtr extra; }
  private delegate IntPtr HookProc(int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr SetWindowsHookEx(int id,HookProc cb,IntPtr h,uint thread);
  [DllImport("user32.dll")] private static extern bool UnhookWindowsHookEx(IntPtr hook);
  [DllImport("user32.dll")] private static extern IntPtr CallNextHookEx(IntPtr hook,int n,IntPtr w,IntPtr l);
  [DllImport("user32.dll")] private static extern IntPtr GetForegroundWindow();
  [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr hwnd,out uint pid);
  [DllImport("user32.dll")] private static extern bool GetKeyboardState(byte[] s);
  [DllImport("user32.dll")] private static extern IntPtr GetKeyboardLayout(uint thread);
  [DllImport("user32.dll")] private static extern int ToUnicodeEx(uint vk,uint sc,byte[] key, StringBuilder buf,int len,uint flags,IntPtr layout);
  [DllImport("kernel32.dll")] private static extern IntPtr GetModuleHandle(string name);
  private static IntPtr hook; private static HookProc handler = Handle;
  private static BlockingCollection<string> queue = new BlockingCollection<string>();
  private static string adb, token; private static bool[] ignored=new bool[256];
  private static bool MirrorFocus() { try { uint p; GetWindowThreadProcessId(GetForegroundWindow(),out p); return Process.GetProcessById((int)p).ProcessName.Equals("MiScreenShare",StringComparison.OrdinalIgnoreCase); } catch { return false; } }
  private static IntPtr Handle(int n,IntPtr w,IntPtr l) {
    if(n>=0) { int msg=w.ToInt32(); Kbd k=(Kbd)Marshal.PtrToStructure(l,typeof(Kbd));
      bool down=msg==0x100||msg==0x104, up=msg==0x101||msg==0x105;
      if(k.vkCode>=0&&k.vkCode<256&&(down||up)) {
        if(up&&ignored[k.vkCode]) { ignored[k.vkCode]=false; return (IntPtr)1; }
        if(down&&MirrorFocus()) {
          byte[] state=new byte[256]; GetKeyboardState(state);
          if((state[0x11]&0x80)==0&&(state[0x12]&0x80)==0&&(state[0x5B]&0x80)==0&&(state[0x5C]&0x80)==0) {
            StringBuilder chars=new StringBuilder(8); uint pid; uint tid=GetWindowThreadProcessId(GetForegroundWindow(),out pid);
            int cnt=ToUnicodeEx((uint)k.vkCode,(uint)k.scanCode,state,chars,8,0,GetKeyboardLayout(tid));
            if(cnt==1 && "ğĞüÜşŞıİöÖçÇ".Contains(chars.ToString())) {
              if(!ignored[k.vkCode]) queue.Add(chars.ToString());
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
    Thread worker=new Thread(()=>{ foreach(string ch in queue.GetConsumingEnumerable()) {
      try { ProcessStartInfo psi=new ProcessStartInfo(adb,"shell am broadcast -a com.example.xiaomikeyboardbridge.SEND -n com.example.xiaomikeyboardbridge/.TextReceiver --es token "+token+" --es char "+ch);
        psi.UseShellExecute=false;psi.CreateNoWindow=true;psi.RedirectStandardOutput=true;psi.RedirectStandardError=true;
        using(Process p=Process.Start(psi)){ p.StandardOutput.ReadToEnd();p.StandardError.ReadToEnd();p.WaitForExit(); }
      } catch(Exception e){System.Console.WriteLine("Gonderim hatasi: "+e.Message);} }
    }); worker.IsBackground=true;worker.Start();
    hook=SetWindowsHookEx(13,handler,GetModuleHandle(null),0);
    if(hook==IntPtr.Zero)throw new Exception("Klavyeye baglanilamadi.");
    Application.Run(new ApplicationContext());
    UnhookWindowsHookEx(hook);
  }
}
'@
Write-Host 'Yalniz MiScreenShare odaktayken Turkce Q karakterlerini Android IME ye gonderir.'
Write-Host 'Kapatmak icin bu pencereyi kapat veya Ctrl+C kullan.'
[TurkishBridgeHook]::Start($Adb,$Token)

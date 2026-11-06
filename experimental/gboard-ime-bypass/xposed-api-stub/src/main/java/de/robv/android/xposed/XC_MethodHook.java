package de.robv.android.xposed;
public abstract class XC_MethodHook {
  public static final class Unhook {}
  public static class MethodHookParam {
    public Object[] args;
    public Object getResult() { return null; }
    public void setResult(Object result) {}
  }
  protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
  protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
}

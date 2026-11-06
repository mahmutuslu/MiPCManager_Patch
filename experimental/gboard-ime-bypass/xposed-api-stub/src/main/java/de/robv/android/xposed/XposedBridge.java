package de.robv.android.xposed;
import java.util.Set;
public final class XposedBridge {
  private XposedBridge() {}
  public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> clazz, String methodName, XC_MethodHook callback) { throw new UnsupportedOperationException("Compile-time only"); }
  public static void log(String message) {}
}

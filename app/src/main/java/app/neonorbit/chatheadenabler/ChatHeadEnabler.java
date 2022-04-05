package app.neonorbit.chatheadenabler;

import java.lang.reflect.Method;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChatHeadEnabler implements IXposedHookLoadPackage {
  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
    if (param.packageName.equals(DataProvider.TARGET_PACKAGE) &&
        param.processName.equals(DataProvider.TARGET_PACKAGE)) {
      try {
        registerHooks(param.classLoader);
      } catch (Throwable throwable) {
        fallback(throwable);
      }
    }
  }

  private static void registerHooks(ClassLoader classLoader) {
    DataProvider provider = new DataProvider(classLoader);
    Set<Method> targetMethods = provider.getTargetMethods();
    XC_MethodHook hook = XC_MethodReplacement.returnConstant(false);
    if (targetMethods != null) {
      targetMethods.forEach(m -> XposedBridge.hookMethod(m, hook));
    } else {
      Util.runOnApplication((context) -> {
        Set<Method> methods = provider.getTargetMethods(context);
        methods.forEach(method -> XposedBridge.hookMethod(method, hook));
      }, ChatHeadEnabler::fallback);
    }
  }

  private static void fallback(Throwable throwable) {
    Util.applyUnstableHook();
    Log.warnFallback(throwable);
  }
}

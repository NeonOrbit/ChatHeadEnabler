package app.neonorbit.chatheadenabler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChatHeadEnabler implements IXposedHookLoadPackage {
  public static final String PACKAGE = "com.facebook.orca";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
    if (param.packageName.equals(PACKAGE) &&
        param.processName.equals(PACKAGE)) {
      try {
        registerHooks();
      } catch (Throwable throwable) {
        fallback(throwable);
      }
    }
  }

  private static void registerHooks() {
    Util.runOnApplication((context) -> {
      var provider = new DataProvider(context);
      var hook = XC_MethodReplacement.returnConstant(false);
      provider.getMethods().forEach(m -> XposedBridge.hookMethod(m, hook));
    }, exception -> {
      throw new RuntimeException(exception);
    });
  }

  private static void fallback(Throwable throwable) {
    Util.applyUnstableHook();
    Log.warnFallback(throwable);
  }
}

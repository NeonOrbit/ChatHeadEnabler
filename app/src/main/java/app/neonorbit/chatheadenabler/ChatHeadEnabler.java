package app.neonorbit.chatheadenabler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ChatHeadEnabler implements IXposedHookLoadPackage {
  public static final String PACKAGE = "com.facebook.orca";

  @Override
  public void handleLoadPackage(LoadPackageParam param) {
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
      ChatHeadSettings.attach(context);
      var provider = new DataProvider(context);
      var mode = ChatHeadSettings.getMode(context);
      var hook = XC_MethodReplacement.returnConstant(mode);
      provider.getMethods().forEach(m-> XposedBridge.hookMethod(m, hook));
    }, ChatHeadEnabler::fallback);
  }

  private static void fallback(Throwable throwable) {
    if (ChatHeadSettings.isDefault) {
      Util.applyUnstableHook();
    }
    ChatHeadSettings.markFallback();
    Log.warnFallback(throwable);
  }
}

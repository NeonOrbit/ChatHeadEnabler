package app.neonorbit.chatheadenabler;

import android.os.Build;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChatHeadEnabler implements IXposedHookLoadPackage {

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
    if (!lpparam.packageName.equals(DataProvider.TARGET_PACKAGE) ||
        !lpparam.processName.equals(DataProvider.TARGET_PACKAGE)) {
      return;
    }
    Util.log("Applying");
    try {
      hookTargetApp(lpparam.classLoader);
    } catch (Exception e) {
      fallback(e);
    }
  }

  private void hookTargetApp(ClassLoader classLoader) {
    final DataProvider provider = new DataProvider(classLoader);
    final Method method = provider.getTargetMethod();
    final XC_MethodReplacement replace = XC_MethodReplacement.returnConstant(false);
    if (method != null) {
      XposedBridge.hookMethod(method, replace);
    } else {
      Util.runOnAppContext(classLoader, context -> {
        try {
          Method _method = provider.getTargetMethod(context);
          XposedBridge.hookMethod(_method, replace);
        } catch (Exception e) {
          fallback(e);
        }
      });
    }
  }

  private void fallback(Exception exception) {
    Util.warnFallback(exception);
    XposedHelpers.setStaticIntField(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.Q);
  }

}

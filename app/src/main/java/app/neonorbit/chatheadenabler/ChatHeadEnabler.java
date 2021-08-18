package app.neonorbit.chatheadenabler;

import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.setStaticIntField;

public class ChatHeadEnabler implements IXposedHookLoadPackage {

  private static final String   MODULE_NAME     = "ChatHeadEnabler";
  private static final String   TARGET_PACKAGE  = "com.facebook.orca";
  private static final int      SPOOF_VERSION   = Build.VERSION_CODES.Q;

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    if (!lpparam.packageName.equals(TARGET_PACKAGE))
      return;

    XposedBridge.log("Applying " + MODULE_NAME + " module to " + lpparam.packageName);

    setStaticIntField(Build.VERSION.class, "SDK_INT", SPOOF_VERSION);

  }
}

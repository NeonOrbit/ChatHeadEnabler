package app.neonorbit.chatheadenabler;

import android.app.Application;
import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Util {

  public static @Nullable Context getContext() {
    try {
      Context context;
      Class<?> acThread = XposedHelpers.findClass("android.app.ActivityThread", null);
      context = (Application) XposedHelpers.callStaticMethod(acThread, "currentApplication");
      if (context != null) return context;
      Object currentAcThread = XposedHelpers.callStaticMethod(acThread, "currentActivityThread");
      return (Context) XposedHelpers.callMethod(currentAcThread, "getSystemContext");
    } catch (Throwable ignore) {
      return null;
    }
  }

  public static void runOnAppContext(ClassLoader classLoader, Consumer<Context> consumer) {
    final AtomicReference<XC_MethodHook.Unhook> appHook = new AtomicReference<>();
    appHook.set(findAndHookMethod("android.app.Application", classLoader,
                                  "onCreate", new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) {
        XC_MethodHook.Unhook hooked = appHook.get();
        if (hooked != null) hooked.unhook();
        Context context = (Context) param.thisObject;
        if (context == null) return;
        consumer.accept(context);
      }
    }));
  }

}

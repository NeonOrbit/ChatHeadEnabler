package app.neonorbit.chatheadenabler;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Util {
  public static final String APP_NAME = "ChatHeadEnabler";

  public static void log(String msg) {
    Log.i(APP_NAME, msg);
    XposedBridge.log(APP_NAME + ": " + msg);
  }

  public static void wLog(String msg) {
    log("[warning] " + msg);
  }

  public static void debugLog(String msg) {
    if (BuildConfig.DEBUG) log(getTime() + " " + msg);
  }

  public static void eLog(Throwable t) {
    CharSequence err = Arrays.stream(t.getStackTrace())
                             .map(s -> "\t\tat " + s.toString())
                             .collect(Collectors.joining("\n"));
    log("[exception] \n\t\t" + t.getClass().getName() + ": " + t.getMessage() + "\n" + err);
  }

  public static void warnFallback(Throwable throwable) {
    String toast = APP_NAME + ": Failed to hook.\nPlease check log for details.";
    Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
    wLog("Failed to hook dynamically, falling back to API spoofing method.\n" +
         " - Note: Chat head might not work in landscape mode, please report.\n...");
    eLog(throwable);
  }

  public static String getTime() {
    Date date = new Date(System.currentTimeMillis());
    return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(date);
  }

  public static void spoofAPILevel() {
    XposedHelpers.setStaticIntField(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.Q);
  }

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

package app.neonorbit.chatheadenabler;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Util {
  public static final String APP_NAME = "ChatHeadEnabler";
  private static final List<String> DEBUG_LOGS = new ArrayList<>();

  public static @Nullable Context getContext() {
    try {
      Context context;
      Class<?> acThread = XposedHelpers.findClass("android.app.ActivityThread", null);
      context = (Application) XposedHelpers.callStaticMethod(acThread, "currentApplication");
      if (context != null) return context;
      Object currentAcThread = XposedHelpers.callStaticMethod(acThread, "currentActivityThread");
      return (Context) XposedHelpers.callMethod(currentAcThread, "getSystemContext");
    } catch (Throwable ignored) {
      Util.dLog("Failed to get Context");
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
        consumer.accept(context);
      }
    }));
  }

  public static void spoofAPILevel() {
    XposedHelpers.setStaticIntField(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.Q);
  }

  public static String getTime() {
    Date date = new Date(System.currentTimeMillis());
    return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(date);
  }
  
  public static void log(String msg) {
    XposedBridge.log(APP_NAME + ": " + msg);
  }

  public static void wLog(String msg) {
    log("[warning] " + msg);
  }

  public static void dLog(String msg) {
    msg = getTime() + " " + msg;
    if (BuildConfig.DEBUG) {
      log(msg);
    } else {
      msg = APP_NAME + ": " + msg;
      DEBUG_LOGS.add(msg);
    }
  }

  public static void eLog(String msg, Throwable t) {
    log("[error] " + msg);
    if (t == null) return;
    CharSequence err = Arrays.stream(t.getStackTrace())
                             .map(s -> "\t\tat " + s.toString())
                             .collect(Collectors.joining("\n"));
    log("[exception] \n\t\t" + t.getClass().getName() + ": " + t.getMessage() + "\n" + err);
  }

  public static void warnFallback(Throwable throwable) {
    String toast = APP_NAME + ": Failed to hook.\nPlease check log for more details.";
    try { Toast.makeText(getContext(), toast, Toast.LENGTH_LONG).show(); } catch (Throwable ignored) {}
    wLog("Fallback Mode: You might experience some bugs, please report.");
    eLog("Failed to hook target app", throwable);
    if (BuildConfig.DEBUG) return;
    CharSequence debugLogs = "Debug logs: " + DEBUG_LOGS.size() + "\n";
    if (!DEBUG_LOGS.isEmpty()) {
      debugLogs += DEBUG_LOGS.stream().map(s -> "\t\t" + s).collect(Collectors.joining("\n"));
      DEBUG_LOGS.clear();
    }
    log("[debug] " + debugLogs);
  }

}

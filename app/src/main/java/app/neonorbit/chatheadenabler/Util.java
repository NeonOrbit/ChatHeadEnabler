package app.neonorbit.chatheadenabler;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import app.neonorbit.chatheadenabler.dex.Constants;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class Util {
  public static void restartApp(Context context) {
    Intent intent = Intent.makeRestartActivityTask(
        context.getPackageManager().getLaunchIntentForPackage(
            context.getPackageName()
        ).getComponent()
    );
    context.startActivity(intent);
    Runtime.getRuntime().exit(0);
  }

  public static Context getContext() {
    try {
      Context context;
      Class<?> acThread = XposedHelpers.findClass("android.app.ActivityThread", null);
      context = (Application) XposedHelpers.callStaticMethod(acThread, "currentApplication");
      if (context != null) return context;
      Object currentAcThread = XposedHelpers.callStaticMethod(acThread, "currentActivityThread");
      return (Context) XposedHelpers.callMethod(currentAcThread, "getSystemContext");
    } catch (Throwable ignored) {
      return null;
    }
  }

  public static void runOnApplication(Consumer<Context> consumer, Consumer<Throwable> onFailure) {
    AtomicReference<XC_MethodHook.Unhook> hook = new AtomicReference<>();
    hook.set(XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
      protected void afterHookedMethod(MethodHookParam param) {
        Unhook hooked = hook.get();
        if (hooked != null) hooked.unhook();
        try {
          consumer.accept((Context) param.thisObject);
        } catch (Throwable throwable) {
          onFailure.accept(throwable);
        }
      }
    }));
  }

  public static XC_MethodHook.Unhook hookAfter(Method method, Consumer<MethodHookParam> consumer) {
    return XposedBridge.hookMethod(method, new XC_MethodHook() {
      protected void afterHookedMethod(MethodHookParam param) { consumer.accept(param); }
    });
  }

  public static void runCatching(Runnable runnable, String msg) {
    try {
      runnable.run();
    } catch (Throwable throwable) {
      Log.w(msg + ": [" + throwable.getClass().getName() + "] -> " + throwable.getMessage());
    }
  }

  public static String getPackageVersion(@Nullable Context context, @NonNull String packageName) {
    try {
      if (context == null) context = Util.getContext();
      PackageManager pm = Objects.requireNonNull(context).getPackageManager();
      return String.valueOf(pm.getPackageInfo(packageName, 0).getLongVersionCode());
    } catch (Throwable t) {
      return null;
    }
  }

  public static void applyUnstableHook() {
    XC_MethodReplacement replacement = XC_MethodReplacement.returnConstant(true);
    XposedHelpers.findAndHookMethod(ActivityManager.class, Constants.REFERENCE_METHOD, replacement);
  }

  public static void showToast(@Nullable Context context, @NonNull String toast) {
    try {
      if (context == null) context = getContext();
      Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    } catch (Throwable ignored) {}
  }

  public static String getTime() {
    Date date = new Date(System.currentTimeMillis());
    return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(date);
  }

  public static int parseDpi(Context context, int dpi) {
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dpi,
        context.getResources().getDisplayMetrics()
    );
  }
}

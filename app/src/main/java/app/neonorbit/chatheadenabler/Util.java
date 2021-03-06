package app.neonorbit.chatheadenabler;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import app.neonorbit.chatheadenabler.dex.Constants;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class Util {
  public static Context getContext() {
    try {
      Context context;
      Class<?> acThread = XposedHelpers.findClass("android.app.ActivityThread", null);
      context = (Application) XposedHelpers.callStaticMethod(acThread, "currentApplication");
      if (context != null) return context;
      Object currentAcThread = XposedHelpers.callStaticMethod(acThread, "currentActivityThread");
      return (Context) XposedHelpers.callMethod(currentAcThread, "getSystemContext");
    } catch (Throwable ignored) {
      Log.w("Failed to get Context");
      return null;
    }
  }

  public static void runOnApplication(Consumer<Context> consumer, Consumer<Throwable> onFailure) {
    AtomicReference<XC_MethodHook.Unhook> hook = new AtomicReference<>();
    hook.set(XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
      @Override
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

  public static String getPackageVersion(@Nullable Context context, @NonNull String packageName) {
    try {
      if (context == null) context = Util.getContext();
      PackageManager pm = Objects.requireNonNull(context).getPackageManager();
      return String.valueOf(pm.getPackageInfo(packageName, 0).getLongVersionCode());
    } catch (Throwable t) {
      Log.w("Failed to get version: " + t.getMessage());
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
}

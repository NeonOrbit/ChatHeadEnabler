package app.neonorbit.chatheadenabler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;

import app.neonorbit.chatheadenabler.dex.DexFilter;
import app.neonorbit.chatheadenabler.dex.ApkAnalyzer;
import app.neonorbit.chatheadenabler.dex.ClassData;
import app.neonorbit.chatheadenabler.dex.RefType;
import de.robv.android.xposed.XSharedPreferences;

import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;

public class DataProvider {
  public  static final String TARGET_PACKAGE    = "com.facebook.orca";
  private static final String PREF_KEY_CLASS    = "clazz";
  private static final String PREF_KEY_METHOD   = "method";
  private static final String PREF_KEY_VERSION  = "version";
  private static final String SHARED_PREF_FILE  = BuildConfig.APPLICATION_ID + "_pref";

  private final DexFilter fileFilter   = new DexFilter(
                                              RefType.get().string(),
                                              s -> s.contains("notification_bubbles"));

  private final DexFilter classFilter  = new DexFilter(
                                              RefType.get().string().method().field(),
                                              s -> s.contains("notification_bubbles") &&
                                                   s.contains("isLowRamDevice") &&
                                                   s.contains("SDK_INT"));

  private final DexFilter methodFilter = new DexFilter(
                                              RefType.get().field().method(),
                                              s -> s.contains("SDK_INT") &&
                                                   s.contains("isLowRamDevice"));

  private boolean needsFetch;
  private final ClassLoader classLoader;

  public DataProvider(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public Method getTargetMethod() {
    Util.debugLog("Reading data through xposed");
    return getTargetMethod(getXPreferences());
  }

  public Method getTargetMethod(Context context) {
    if (!context.getPackageName().equals(TARGET_PACKAGE)) {
      return null;
    } else if (needsFetch) {
      return fetchData(context);
    }
    Util.debugLog("Reading data through hooked app");
    Method method = getTargetMethod(getPreferences(context));
    return (method != null) ? method : fetchData(context);
  }

  private Method getTargetMethod(SharedPreferences preferences) {
    if (preferences == null) return null;
    String clazz   = preferences.getString(PREF_KEY_CLASS, "");
    String method  = preferences.getString(PREF_KEY_METHOD, "");
    String version = preferences.getString(PREF_KEY_VERSION, "");
    return getValidMethod(clazz, method, version);
  }

  private Method fetchData(Context context) {
    Util.debugLog("Fetching new data");
    String apk = context.getApplicationInfo().sourceDir;
    ApkAnalyzer apkAnalyzer = new ApkAnalyzer(apk);
    ClassData data = apkAnalyzer.findMethod(fileFilter, classFilter, methodFilter);
    Method method = getFetchedMethod(data);
    if (method == null) return null;
    saveData(context, data);
    return method;
  }

  private void saveData(Context context, ClassData data) {
    String version = getMessengerVersion(context);
    SharedPreferences preferences = getPreferences(context);
    if (preferences == null || version == null) return;
    context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE).edit()
           .putString(PREF_KEY_VERSION, version)
           .putString(PREF_KEY_CLASS, data.clazz)
           .putString(PREF_KEY_METHOD, data.method)
           .apply();
    Util.debugLog("Saved new data");
  }

  private Method getValidMethod(String clazz, String method, String version) {
    String targetVersion = getMessengerVersion(null);
    boolean isValid = targetVersion != null && targetVersion.equals(version);
    if (!isValid && !version.isEmpty()) {
      needsFetch = true;
      Util.debugLog("App version changed");
    } else if (!version.isEmpty()) {
      Util.debugLog("Retrieved: " + clazz + "." + method);
    }
    return !isValid ? null : findMethodExactIfExists(clazz, classLoader, method);
  }

  private Method getFetchedMethod(ClassData data) {
    if (data == null) {
      Util.debugLog("Failed to fetch new data");
    } else {
      Method method = findMethodExactIfExists(data.clazz, classLoader, data.method);
      String validity = method != null ? "valid" : "invalid";
      Util.debugLog("Fetched (" + validity + "): " + data.clazz + "." + data.method);
      return method;
    }
    return null;
  }

  private SharedPreferences getPreferences(Context context) {
    if (context.getPackageName().equals(TARGET_PACKAGE)) {
      return context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
    }
    Util.debugLog("Failed to get SharedPreferences");
    return null;
  }

  private SharedPreferences getXPreferences() {
    XSharedPreferences preferences = new XSharedPreferences(TARGET_PACKAGE, SHARED_PREF_FILE);
    if (preferences.getFile().isFile()) {
      if (preferences.getFile().canRead() || preferences.makeWorldReadable()) {
        return preferences;
      }
      Util.debugLog("Failed to get XSharedPreferences");
    } else {
      needsFetch = true;
    }
    return null;
  }

  private String getMessengerVersion(@Nullable Context context) {
    if (context == null) context = Util.getContext();
    try {
      PackageManager pm = Objects.requireNonNull(context).getPackageManager();
      PackageInfo pInfo = pm.getPackageInfo(TARGET_PACKAGE, 0);
      return String.valueOf(pInfo.getLongVersionCode());
    } catch (Throwable ignore) {
      return null;
    }
  }

}

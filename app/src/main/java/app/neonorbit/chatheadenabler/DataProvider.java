package app.neonorbit.chatheadenabler;

import static app.neonorbit.chatheadenabler.BuildConfig.APPLICATION_ID;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import app.neonorbit.chatheadenabler.dex.DataFetcher;
import app.neonorbit.chatheadenabler.dex.ReflectionMagic;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import io.github.neonorbit.dexplore.result.MethodData;

public class DataProvider {
  public  static final String TARGET_PACKAGE    = "com.facebook.orca";
  private static final String PREF_KEY_VERSION  = "version";
  private static final String PREF_KEY_DATA_SET = "data-set";
  private static final String SHARED_PREFS_FILE = APPLICATION_ID + ".pref";

  private boolean needsFetch = false;
  private final ClassLoader classLoader;

  public DataProvider(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Nullable
  public Set<Method> getTargetMethods() {
    Log.d("Reading data through xposed");
    return retrieveData(getXPreferences());
  }

  @NonNull
  public Set<Method> getTargetMethods(@NonNull Context context) {
    if (!context.getPackageName().equals(TARGET_PACKAGE)) {
      String name = context.getPackageName();
      throw new RuntimeException("Invalid context: " + name);
    } else if (this.needsFetch) {
      return fetchData(context);
    }
    Log.d("Reading data through hooked app");
    Set<Method> method = retrieveData(getPreferences(context));
    return (method != null) ? method : fetchData(context);
  }

  @NonNull
  private Set<Method> fetchData(@NonNull Context context) {
    Log.d("Fetching new data");
    String apk = context.getApplicationInfo().sourceDir;
    Class<?> target = ReflectionMagic.findTarget(classLoader);
    Set<MethodData> data = new DataFetcher(apk).fetch(target);
    if (data != null) {
      Set<Method> methods = loadMethods(data);
      if (methods != null) {
        saveData(context, data);
        return methods;
      }
    }
    throw new RuntimeException("Failed to fetch new data");
  }

  private Set<Method> retrieveData(@Nullable SharedPreferences pref) {
    if (pref == null) return null;
    String version = pref.getString(PREF_KEY_VERSION, null);
    String appVersion = Util.getPackageVersion(null, TARGET_PACKAGE);
    if (version == null || appVersion == null) {
      return null;
    } else if (!version.equals(appVersion)) {
      this.needsFetch = true;
      Log.d("Version changed: " + version + " -> " + appVersion);
      return null;
    }
    Set<String> serialized = pref.getStringSet(PREF_KEY_DATA_SET, null);
    return loadMethods(deserialize(serialized));
  }

  private void saveData(@NonNull Context context, @NonNull Set<MethodData> data) {
    String version = Util.getPackageVersion(context, TARGET_PACKAGE);
    SharedPreferences preferences = getPreferences(context);
    if (preferences == null || version == null) return;
    preferences.edit()
               .putString(PREF_KEY_VERSION, version)
               .putStringSet(PREF_KEY_DATA_SET, serialize(data))
               .apply();
    this.needsFetch = false;
    Log.d("Saved new data");
  }

  @Nullable
  private Set<Method> loadMethods(@Nullable Set<MethodData> data) {
    if (data == null || data.isEmpty()) return null;
    try {
      Log.d("Loading: " + data);
      Set<Method> methods = new TreeSet<>(Comparator.comparing(Method::getName));
      for (MethodData d : data) {
        Method method = XposedHelpers.findMethodExact(d.clazz, classLoader, d.method);
        methods.add(method);
      }
      return methods;
    } catch (Throwable t) {
      Log.w("Failed to load methods: " + t.getMessage());
      return null;
    }
  }

  @NonNull
  private static Set<String> serialize(@NonNull Set<MethodData> data) {
    return Objects.requireNonNull(data)
                  .stream().map(MethodData::serialize)
                  .collect(Collectors.toCollection(TreeSet::new));
  }

  @Nullable
  private static Set<MethodData> deserialize(@Nullable Set<String> serialized) {
    if (serialized == null) return null;
    try {
      return serialized.stream()
                       .map(MethodData::deserialize)
                       .collect(Collectors.toCollection(TreeSet::new));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private SharedPreferences getPreferences(Context context) {
    return context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
  }

  private SharedPreferences getXPreferences() {
    XSharedPreferences preferences = new XSharedPreferences(TARGET_PACKAGE, SHARED_PREFS_FILE);
    if (preferences.getFile().isFile()) {
      if (preferences.getFile().canRead() || preferences.makeWorldReadable()) {
        return preferences;
      }
      Log.w("Failed to get XSharedPreferences");
    } else {
      this.needsFetch = true;
    }
    return null;
  }
}

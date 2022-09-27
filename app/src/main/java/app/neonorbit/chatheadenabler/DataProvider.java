package app.neonorbit.chatheadenabler;

import static app.neonorbit.chatheadenabler.ChatHeadEnabler.PACKAGE;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import app.neonorbit.chatheadenabler.dex.DexFetcher;
import app.neonorbit.chatheadenabler.dex.ReflectionMagic;
import de.robv.android.xposed.XposedHelpers;
import io.github.neonorbit.dexplore.result.MethodData;

public class DataProvider {
  private final Context context;

  public DataProvider(Context context) {
    String pkg = context.getPackageName();
    if (!pkg.equals(PACKAGE)) {
      throw new IllegalArgumentException("Invalid context: " + pkg);
    }
    this.context = context;
  }

  @NonNull
  public Set<Method> getMethods() {
    Log.d("Reading saved data");
    Set<Method> method = retrieveData();
    return (method != null) ? method : fetchData();
  }

  @NonNull
  private Set<Method> fetchData() {
    Log.d("Fetching new data");
    String apk = context.getApplicationInfo().sourceDir;
    Class<?> target = ReflectionMagic.findTarget(context.getClassLoader());
    Set<MethodData> result = new DexFetcher(apk).fetch(target);
    if (result != null) {
      Set<Method> methods = loadMethods(result);
      if (methods != null) {
        saveData(result);
        return methods;
      }
    }
    throw new RuntimeException("Failed to fetch new data");
  }

  private Set<Method> retrieveData() {
    String prefVersion = DataPreferences.getPrefVersion(context);
    String appVersion = Util.getPackageVersion(context, PACKAGE);
    if (prefVersion == null || appVersion == null) {
      return null;
    } else if (!prefVersion.equals(appVersion)) {
      Log.d("Version changed: " + prefVersion + " -> " + appVersion);
      return null;
    }
    Set<String> serialized = DataPreferences.getHookData(context);
    return loadMethods(deserialize(serialized));
  }

  private void saveData(@NonNull Set<MethodData> dataSet) {
    String version = Util.getPackageVersion(context, PACKAGE);
    DataPreferences.setPrefVersion(context, version);
    DataPreferences.setHookData(context, serialize(dataSet));
    Log.d("Saved new data");
  }

  @Nullable
  private Set<Method> loadMethods(@Nullable Set<MethodData> dataSet) {
    if (dataSet == null || dataSet.isEmpty()) return null;
    try {
      Log.d("Loading: " + dataSet);
      ClassLoader classLoader = context.getClassLoader();
      Set<Method> methods = new TreeSet<>(Comparator.comparing(Method::getName));
      for (MethodData data : dataSet) {
        Method method = XposedHelpers.findMethodExact(data.clazz, classLoader, data.method);
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
    return data.stream().map(MethodData::serialize).collect(Collectors.toCollection(TreeSet::new));
  }

  @Nullable
  private static Set<MethodData> deserialize(@Nullable Set<String> serialized) {
    if (serialized == null) return null;
    try {
      return serialized.stream()
                       .map(MethodData::deserialize)
                       .collect(Collectors.toCollection(TreeSet::new));
    } catch (IllegalArgumentException e) {
      Log.w("Deserialization failed: " + e.getMessage());
      return null;
    }
  }
}

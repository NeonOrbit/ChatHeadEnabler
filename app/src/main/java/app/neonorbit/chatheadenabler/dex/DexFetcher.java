package app.neonorbit.chatheadenabler.dex;

import static io.github.neonorbit.dexplore.filter.ReferenceTypes.Scope.VIRTUAL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;

import app.neonorbit.chatheadenabler.Log;
import io.github.neonorbit.dexplore.DexFactory;
import io.github.neonorbit.dexplore.DexOptions;
import io.github.neonorbit.dexplore.Dexplore;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.filter.ReferenceFilter;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.util.DexLog;
import io.github.neonorbit.dexplore.util.DexLogger;

public class DexFetcher {
  private static final DexFilter DEX_FILTER;
  private static final ClassFilter CLASS_FILTER;
  private static final MethodFilter METHOD_FILTER;

  static {
    DexFetcher.initDexLogger();
    ReferenceTypes types = ReferenceTypes.builder().addField().addMethod().setScope(VIRTUAL).build();
    ReferenceFilter cond = ReferenceFilter.fieldsContain(Constants.REFERENCE_FIELD).and(
                           ReferenceFilter.methodsContain(Constants.REFERENCE_METHOD));

    DEX_FILTER = DexFilter.builder()
                          .setReferenceTypes(ReferenceTypes.builder().addMethod().build())
                          .setReferenceFilter(ReferenceFilter.contains(Constants.REFERENCE_METHOD))
                          .setPreferredDexNames(Constants.TARGET_PREFERRED_DEX)
                          .build();

    CLASS_FILTER = ClassFilter.builder()
                          .setReferenceTypes(types)
                          .setReferenceFilter(cond)
                          .setModifiers(Modifier.PUBLIC | Modifier.FINAL)
                          .defaultSuperClass()
                          .noInterfaces()
                          .build();

    METHOD_FILTER = MethodFilter.builder()
                          .setReferenceTypes(types)
                          .setReferenceFilter(cond)
                          .setModifiers(Modifier.PUBLIC)
                          .setReturnType(boolean.class.getName())
                          .setParamSize(0)
                          .build();
  }

  private final Dexplore dexplore;

  public DexFetcher(@NonNull String path) {
    DexOptions options = new DexOptions();
    options.rootDexOnly = true;
    options.enableCache = false;
    this.dexplore = DexFactory.load(path, options);
  }

  public Set<MethodData> fetch(@Nullable Class<?> target) {
    MethodData data = (target == null) ? null : fastFetch(target);
    if (data == null) data = fastFetch();
    if (data == null) data = deepFetch();
    return getRequiredMethods(data);
  }

  private MethodData fastFetch(@NonNull Class<?> target) {
    Log.d("Fetching [fast:precise]...");
    String clazz = target.getName();
    return dexplore.findMethod(DEX_FILTER.toBuilder()
                                         .setDefinedClasses(clazz)
                                         .allowPreferredDexOnly(true)
                                         .build(),
                               CLASS_FILTER.toBuilder().setClasses(clazz).build(),
                               METHOD_FILTER);
  }

  private MethodData fastFetch() {
    Log.d("Fetching [fast:relative]...");
    MethodData method;
    method = dexplore.findMethod(DexFilter.builder()
                                          .setDefinedClasses(Constants.MONTAGE_CLASS)
                                          .setPreferredDexNames(Constants.HELPER_PREFERRED_DEX)
                                          .allowPreferredDexOnly(true)
                                          .build(),
                                 ClassFilter.ofClass(Constants.MONTAGE_CLASS),
                                 MethodFilter.ofMethod(Constants.MONTAGE_METHOD));
    if (method == null || method.getReferencePool().getTypeSection().size() != 1) {
      return null;
    }
    String clazz = method.getReferencePool().getTypeSection().get(0).getType();
    return dexplore.findMethod(DEX_FILTER.toBuilder()
                                         .setDefinedClasses(clazz)
                                         .allowPreferredDexOnly(true)
                                         .build(),
                               CLASS_FILTER.toBuilder().setClasses(clazz).build(),
                               METHOD_FILTER);
  }

  private MethodData deepFetch() {
    Log.d("Fetching [deep:reference]...");
    return dexplore.findMethod(DEX_FILTER, CLASS_FILTER, METHOD_FILTER);
  }

  private static Set<MethodData> getRequiredMethods(MethodData data) {
    if (data == null) return null;
    String signature = data.getSignature();
    Set<MethodData> dataSet = new TreeSet<>();
    dataSet.add(data);
    data.getClassData()
        .getMethods().stream()
        .filter(m -> m.params.length == 0 &&
                     m.returnType.equals(boolean.class.getName()) &&
                     m.getReferencePool().methodSignaturesContain(signature))
        .forEach(dataSet::add);
    return dataSet;
  }

  private static void initDexLogger() {
    DexLog.enable();
    DexLog.setLogger(new DexLogger() {
      @Override
      public void debug(String msg) {
        Log.d(msg);
      }
      @Override
      public void warn(String msg) {
        Log.w(msg);
      }
    });
  }
}

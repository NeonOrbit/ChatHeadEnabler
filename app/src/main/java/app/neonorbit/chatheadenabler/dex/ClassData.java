package app.neonorbit.chatheadenabler.dex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

public class ClassData {
  public final String clazz;
  public final String method;

  private ClassData(String clazz, String method) {
    this.clazz  = clazz;
    this.method = method;
  }

  @Nullable
  public static ClassData from(String clazz, String method) {
    return (clazz.isEmpty() || method.isEmpty()) ? null : new ClassData(clazz, method);
  }

  @Nullable
  public static ClassData from(@Nullable DexBackedClassDef dexClassDef) {
    return (dexClassDef == null) ? null : new ClassData(getClassName(dexClassDef), "");
  }

  @Nullable
  public static ClassData from(@Nullable DexBackedMethod dexMethod) {
    return (dexMethod == null) ? null : new ClassData(getClassName(dexMethod.classDef), dexMethod.getName());
  }

  private static String getClassName(@NotNull DexBackedClassDef classDef) {
    String name = classDef.getType();
    return name.substring(1, name.length() - 1).replace('/', '.');
  }
}

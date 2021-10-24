package app.neonorbit.chatheadenabler.dex;

import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.reference.DexBackedFieldReference;
import org.jf.dexlib2.dexbacked.reference.DexBackedMethodReference;
import org.jf.dexlib2.dexbacked.reference.DexBackedStringReference;
import org.jf.dexlib2.dexbacked.reference.DexBackedTypeReference;

import java.util.StringJoiner;
import java.util.stream.Collectors;

public class DexUtil {
  public static DexBackedClassDef javaToDexClass(Class<?> javaClass,
                                                 DexBackedDexFile dexFile) {
    String classType = ReflectionUtils.javaToDexName(javaClass.getName());
    return dexFile.getClasses().stream()
                  .filter(c -> c.getType().equals(classType))
                  .findFirst().orElse(null);
  }

  public static boolean verifyDex(DexFilter filter, DexBackedDexFile dexFile) {
    RefType type = filter.getType();
    StringJoiner buffer = new StringJoiner("\n");
    if (type.isString()) {
      buffer.add(dexFile.getStringReferences().stream()
            .map(DexBackedStringReference::getString)
            .collect(Collectors.joining("\n")));
    }
    if (type.isField()) {
      buffer.add(dexFile.getFieldSection().stream()
            .map(DexBackedFieldReference::getName)
            .collect(Collectors.joining("\n")));
    }
    if (type.isMethod()) {
      buffer.add(dexFile.getMethodSection().stream()
            .map(DexBackedMethodReference::getName)
            .collect(Collectors.joining("\n")));
    }
    if (type.isTypeDes()) {
      buffer.add(dexFile.getTypeReferences().stream()
            .map(DexBackedTypeReference::getType)
            .collect(Collectors.joining("\n")));
    }
    return filter.verify(buffer.toString());
  }
}

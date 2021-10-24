package app.neonorbit.chatheadenabler.dex;

import org.jetbrains.annotations.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.DualReferenceInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;

import java.util.StringJoiner;

public class DexAnalyzer {
  private final DexBackedDexFile dexFile;

  DexAnalyzer(DexBackedDexFile dexFile) {
    this.dexFile = dexFile;
  }

  @Nullable
  public ClassData locateClass(DexFilter classFilter) {
    return ClassData.from(locateDexClass(classFilter));
  }

  @Nullable
  public ClassData locateMethod(DexFilter classFilter, DexFilter methodFilter) {
    return ClassData.from(locateDexMethod(locateDexClass(classFilter), methodFilter));
  }

  @Nullable
  public ClassData locateMethod(Class<?> javaClass, DexFilter methodFilter) {
    DexBackedClassDef dexClass = DexUtil.javaToDexClass(javaClass, dexFile);
    return ClassData.from(locateDexMethod(dexClass, methodFilter));
  }

  private DexBackedClassDef locateDexClass(DexFilter classFilter) {
    for (DexBackedClassDef dexClass : dexFile.getClasses()) {
      if (classFilter.verify(decodeClassReferences(dexClass, classFilter.getType()))) {
        return dexClass;
      }
    }
    return null;
  }

  private DexBackedMethod locateDexMethod(DexBackedClassDef dexClass, DexFilter methodFilter) {
    if (dexClass == null) return null;
    for (DexBackedMethod dexMethod : dexClass.getVirtualMethods()) {
      if (methodFilter.verify(decodeMethodReferences(dexMethod, methodFilter.getType()))) {
        return dexMethod;
      }
    }
    return null;
  }

  private String decodeClassReferences(DexBackedClassDef dexClass, RefType type) {
    StringJoiner buffer = new StringJoiner("\n");
    dexClass.getVirtualMethods().forEach(method -> {
      decodeMethodReferences(method, type, buffer);
    });
    return buffer.toString();
  }

  private String decodeMethodReferences(DexBackedMethod dexMethod, RefType type) {
    return decodeMethodReferences(dexMethod, type, new StringJoiner("\n"));
  }

  private String decodeMethodReferences(DexBackedMethod dexMethod,
                                        RefType type, StringJoiner buffer) {
    MethodImplementation implementation = dexMethod.getImplementation();
    if (implementation == null) { return buffer.toString(); }
    for (Instruction instruction : implementation.getInstructions()) {
      if (instruction instanceof ReferenceInstruction) {
        decodeReference(((ReferenceInstruction) instruction).getReference(), type, buffer);
        if (instruction instanceof DualReferenceInstruction) {
          decodeReference(((DualReferenceInstruction) instruction).getReference2(), type, buffer);
        }
      }
    }
    return buffer.toString();
  }

  private void decodeReference(Reference reference, RefType type, StringJoiner buffer) {
    try {
      if (reference instanceof StringReference) {
        if (type.isString()) buffer.add(((StringReference) reference).getString());
      } else if (reference instanceof FieldReference) {
        if (type.isField()) buffer.add(((FieldReference) reference).getName());
      } else if (reference instanceof MethodReference) {
        if (type.isMethod()) buffer.add(((MethodReference) reference).getName());
      } else if (reference instanceof TypeReference) {
        if (type.isTypeDes()) buffer.add(((TypeReference) reference).getType());
      }
    } catch (Exception ignore) { }
  }
}

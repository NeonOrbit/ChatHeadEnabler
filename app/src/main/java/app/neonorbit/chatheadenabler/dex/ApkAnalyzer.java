package app.neonorbit.chatheadenabler.dex;

import org.jetbrains.annotations.Nullable;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry;

import java.io.File;
import java.io.IOException;

import app.neonorbit.chatheadenabler.Util;

public class ApkAnalyzer {
  private final MultiDexContainer<? extends DexBackedDexFile> container;

  public ApkAnalyzer(String path) {
    this.container = loadDexContainer(path);
  }

  @Nullable
  public ClassData findClass(DexFilter fileFilter,
                             DexFilter classFilter) {
    return getClassData(false, fileFilter, classFilter, null);
  }

  @Nullable
  public ClassData findMethod(DexFilter fileFilter,
                              DexFilter classFilter,
                              DexFilter methodFilter) {
    return getClassData(true, fileFilter, classFilter, methodFilter);
  }

  @Nullable
  public ClassData findMethod(Class<?> javaClass,
                              DexFilter methodFilter) {
    return (ClassData) performDexOperation(null, (name, dexFile) -> {
      Util.dLog("Searching In: " + name);
      DexAnalyzer dexAnalyzer = new DexAnalyzer(dexFile);
      return dexAnalyzer.locateMethod(javaClass, methodFilter);
    });
  }

  private ClassData getClassData(boolean getMethod,
                                 DexFilter fileFilter,
                                 DexFilter classFilter,
                                 DexFilter methodFilter) {
    return (ClassData) performDexOperation(fileFilter, (name, dexFile) -> {
      Util.dLog("Analyzing Dex: " + name);
      DexAnalyzer dexAnalyzer = new DexAnalyzer(dexFile);
      return !getMethod ? dexAnalyzer.locateClass(classFilter) :
                          dexAnalyzer.locateMethod(classFilter, methodFilter);
    });
  }

  private Object performDexOperation(DexFilter filter, DexOperation operation) {
    if (container == null) return null;
    DexBackedDexFile dexFile;
    DexEntry<? extends DexBackedDexFile> dexEntry;
    try {
      for (String dexName: container.getDexEntryNames()) {
        if (!dexName.startsWith("classes")) {
          continue;
        }
        Util.dLog("Loading Dex: " + dexName);
        dexEntry = container.getEntry(dexName);
        if (dexEntry == null) {
          continue;
        }
        dexFile = dexEntry.getDexFile();
        if (filter == null || DexUtil.verifyDex(filter, dexFile)) {
          Object result = operation.apply(dexName, dexFile);
          if (result != null) return result;
        }
      }
    } catch (IOException e) {
      Util.eLog("Failed to load dex file", e);
    }
    return null;
  }

  private MultiDexContainer<? extends DexBackedDexFile> loadDexContainer(String path) {
    try {
      File apk = new File(path);
      return DexFileFactory.loadDexContainer(apk, Opcodes.getDefault());
    } catch (IOException e) {
      Util.eLog("Failed to load dex container", e);
    }
    return null;
  }
}

package app.neonorbit.chatheadenabler.dex;

import org.jetbrains.annotations.Nullable;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.reference.DexBackedStringReference;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import de.robv.android.xposed.XposedBridge;

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

  private ClassData getClassData(boolean getMethod,
                                 DexFilter fileFilter,
                                 DexFilter classFilter,
                                 DexFilter methodFilter) {
    if (container == null) return null;
    DexAnalyzer dexAnalyzer;
    DexBackedDexFile dexFile;
    DexEntry<? extends DexBackedDexFile> dexEntry;
    try {
      for (String entry: container.getDexEntryNames()) {
        if (!entry.contains("classes")) continue;

        XposedBridge.log("Loading Dex: " + entry);

        dexEntry = container.getEntry(entry);
        if (dexEntry == null) continue;

        dexFile = dexEntry.getDexFile();

        if (!verifyDexFilter(dexFile, fileFilter)) {
          continue;
        }

        XposedBridge.log("Analyzing Dex: " + entry);

        dexAnalyzer = new DexAnalyzer(dexFile);
        ClassData result = !getMethod ? dexAnalyzer.locateClass(classFilter) :
                                        dexAnalyzer.locateMethod(classFilter, methodFilter);
        if (result != null) {
          return result;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private boolean verifyDexFilter(DexBackedDexFile dexFile, DexFilter filter) {
    CharSequence pool = dexFile.getStringReferences().stream()
                               .map(DexBackedStringReference::getString)
                               .collect(Collectors.joining(" "));
    return filter.verify(pool.toString());
  }

  private MultiDexContainer<? extends DexBackedDexFile> loadDexContainer(String path) {
    try {
      File apk = new File(path);
      return DexFileFactory.loadDexContainer(apk, Opcodes.getDefault());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}

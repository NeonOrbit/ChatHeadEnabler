package app.neonorbit.chatheadenabler.dex;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;

interface DexOperation {
    Object apply(String dexName, DexBackedDexFile dexFile);
}

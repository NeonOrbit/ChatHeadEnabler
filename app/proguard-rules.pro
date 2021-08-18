# Preserve line number information
-keepattributes SourceFile,LineNumberTable

# Preserve code structure
-dontoptimize
-dontobfuscate

# Preserve app source code
-keepclasseswithmembers class app.neonorbit.chatheadenabler.** {*;}
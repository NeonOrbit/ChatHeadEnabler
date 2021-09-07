# Proguard
-dontoptimize
-repackageclasses 'xneonp'
-keepattributes SourceFile, LineNumberTable

# Preserve app source code
-keepclasseswithmembers class app.neonorbit.chatheadenabler.** {*;}
-keepclasseswithmembers,allowshrinking class org.jf.** {*;}

# Ignore unnecessary warnings
-dontwarn java.lang.ClassValue
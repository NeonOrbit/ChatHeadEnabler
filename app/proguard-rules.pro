# Proguard
-dontoptimize
-repackageclasses 'xch'
-keepattributes SourceFile, LineNumberTable

# Preserve app source code
-keep class app.neonorbit.chatheadenabler.** {*;}
-keep,allowshrinking class org.jf.** {*;}

# Ignore unnecessary warnings
-dontwarn java.lang.ClassValue
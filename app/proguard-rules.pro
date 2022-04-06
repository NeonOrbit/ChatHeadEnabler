# Proguard
-dontoptimize
-repackageclasses 'xch'
-keepattributes SourceFile, LineNumberTable

# Preserve source code
-keep class app.neonorbit.chatheadenabler.** {*;}
-keep,allowshrinking class io.github.neonorbit.dexplore.** {*;}
-keepclassmembers enum * {
                    public static **[] values();
                    public static ** valueOf(java.lang.String);
                  }

# Ignore unnecessary warnings
-dontwarn java.lang.ClassValue

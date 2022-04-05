package app.neonorbit.chatheadenabler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.robv.android.xposed.XposedBridge;

public final class Log {
  private static final String MODULE = "ChatHeadEnabler";
  private static final String REPORT_URL = "https://github.com/NeonOrbit/ChatHeadEnabler/issues";
  private static final String UPDATE_URL = "https://github.com/NeonOrbit/ChatHeadEnabler/releases";

  private static final List<String> DEBUG_LOGS = new ArrayList<>();

  private static void log(String msg) {
    XposedBridge.log(MODULE + ": " + msg);
  }

  public static void d(String msg) {
    msg = Util.getTime() + " " + msg;
    if (BuildConfig.DEBUG)
      log(msg);
    else
      DEBUG_LOGS.add(msg);
  }

  public static void w(String msg) {
    log("[warning] " + msg);
  }

  public static void e(String msg, Throwable throwable) {
    log("[failure] " + msg);
    if (throwable != null) {
      String trace = android.util.Log.getStackTraceString(throwable);
      log("[exception] " + throwable.getClass().getName() + ": "
                         + throwable.getMessage() + "\n" + trace);
    }
  }

  public static void warnFallback(Throwable throwable) {
    Util.showToast(null, MODULE + ": Hook failure\nCheck log for more details");
    Log.w("Fallback Mode: You might experience some bugs.");
    log("   Check for new update:  " + UPDATE_URL);
    log("   OR report to:  " + REPORT_URL);
    Log.e("Failed to register hooks", throwable);
    if (!BuildConfig.DEBUG) {
      String debugLogs = "Debug Logs ["+ BuildConfig.VERSION_NAME + "]: L"
                                       + DEBUG_LOGS.size() + "\n";
      if (!DEBUG_LOGS.isEmpty()) {
        debugLogs += DEBUG_LOGS.stream()
                               .map(s -> "\t\t" + MODULE + ": " + s)
                               .collect(Collectors.joining("\n"));
        DEBUG_LOGS.clear();
      }
      log(debugLogs);
    }
  }
}

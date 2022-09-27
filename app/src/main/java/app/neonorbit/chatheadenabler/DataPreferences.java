package app.neonorbit.chatheadenabler;

import static app.neonorbit.chatheadenabler.BuildConfig.APPLICATION_ID;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class DataPreferences {
  private static final String KEY_VERSION = "version";
  private static final String KEY_DATA_SET = "data_set";
  private static final String FILE = APPLICATION_ID + ".pref";

  public static String getPrefVersion(Context context) {
    return getPref(context).getString(KEY_VERSION, null);
  }

  public static void setPrefVersion(Context context, String value) {
    getPref(context).edit().putString(KEY_VERSION, value).apply();
  }

  public static Set<String> getHookData(Context context) {
    return getPref(context).getStringSet(KEY_DATA_SET, null);
  }

  public static void setHookData(Context context, Set<String> value) {
    getPref(context).edit().putStringSet(KEY_DATA_SET, value).apply();
  }

  private static SharedPreferences getPref(Context context) {
    return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
  }
}

package app.neonorbit.chatheadenabler;

import static app.neonorbit.chatheadenabler.BuildConfig.APPLICATION_ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

@SuppressLint("ApplySharedPref")
public class DataPreferences {
  private static final String KEY_BUBBLE = "bubble";
  private static final String KEY_SETTING = "setting";
  private static final String KEY_VERSION = "version";
  private static final String KEY_DATA_SET = "data_set";
  private static final String KEY_FB_SETTING = "setting_cls";
  private static final String FILE = APPLICATION_ID + ".pref";

  public static boolean isBubble(Context context) {
    return getPref(context).getBoolean(KEY_BUBBLE, false);
  }

  public static void setBubblePref(Context context, boolean value) {
    getPref(context).edit().putBoolean(KEY_BUBBLE, value).commit();
  }

  public static boolean isSettingDisabled(Context context) {
    return getPref(context).getBoolean(KEY_SETTING, false);
  }

  public static void disableSetting(Context context) {
    getPref(context).edit().putBoolean(KEY_SETTING, true).commit();
  }

  public static String getSettingCache(Context context) {
    return getPref(context).getString(KEY_FB_SETTING, null);
  }

  public static void setSettingCache(Context context, String value) {
    getPref(context).edit().putString(KEY_FB_SETTING, value).apply();
  }

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

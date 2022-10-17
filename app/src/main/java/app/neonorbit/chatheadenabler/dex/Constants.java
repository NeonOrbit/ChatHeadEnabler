package app.neonorbit.chatheadenabler.dex;

public final class Constants {
  public static final String[] TARGET_PREFERRED_DEX = {"classes2.dex"};
  public static final String[] HELPER_PREFERRED_DEX = {"classes5.dex", "classes6.dex"};
  public static final String[] SETTINGS_PREFERRED_DEX = {"classes3.dex", "classes5.dex"};

  public static final String REFERENCE_FIELD = "SDK_INT";
  public static final String REFERENCE_METHOD = "isLowRamDevice";

  public static final String CHAT_HEAD_MENU_CLASS = "com.facebook.messaging.chatheads.plugins.core."
                                                  + "threadsettingsmenuitem.OpenChatHeadMenuItem";

  public static final String CHAT_HEAD_ROW_CLASS = "com.facebook.messaging.msys." +
                                                   "advancedcrypto.plugins.threadsettingsrow." +
                                                   "chatheads.ThreadSettingsOpenChatHeadRow";

  public static final String MONTAGE_CLASS = "com.facebook.messaging.montage.viewer."
                                           + "MontageViewerBubblesActivity";

  public static final String MONTAGE_METHOD = "onPostResume";

  public static final String REDEX_INTERNAL_FIELD = "__redex_internal_original_name";
  public static final String ME_LITHO_PREFERENCE_FRAGMENT = "MessengerMeLithoPreferenceFragment";
}

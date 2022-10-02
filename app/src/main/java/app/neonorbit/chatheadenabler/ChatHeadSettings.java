package app.neonorbit.chatheadenabler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import app.neonorbit.chatheadenabler.dex.Constants;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ChatHeadSettings {
  public static boolean isDefault = true;
  private static boolean isFallback = false;

  public static void markFallback() {
    isFallback = true;
  }

  public static boolean getMode(Context context) {
    var mode = DataPreferences.isBubble(context);
    if (mode) isDefault = false;
    return mode;
  }

  public static void attach(Context context) {
    if (DataPreferences.isSettingDisabled(context)) return;
    onPreferenceActivity(context.getClassLoader(), (activity) -> {
      try {
        View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        ViewGroup panel = (ViewGroup) ((ViewGroup) root).getChildAt(0);
        FrameLayout layout = buildLayout(activity);
        panel.addView(layout);
      } catch (Throwable t) {
        Log.w("Failed to attach module settings: " + t.getMessage());
      }
    });
  }

  private static FrameLayout buildLayout(Context context) {
    ImageButton button = new ImageButton(context);
    button.setImageResource(android.R.drawable.ic_menu_more);
    button.setBackgroundColor(Color.TRANSPARENT);
    AlertDialog setting = buildSettingDialog(context);
    AlertDialog disable = buildDisableDialog(context);
    button.setOnClickListener(view -> setting.show());
    button.setOnLongClickListener(view -> {
      disable.show(); return true;
    });

    int width = ViewGroup.LayoutParams.WRAP_CONTENT;
    int height = ViewGroup.LayoutParams.MATCH_PARENT;
    var lp = new FrameLayout.LayoutParams(width, height);
    lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    lp.setMarginEnd((int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 2,
        context.getResources().getDisplayMetrics()
    ));

    FrameLayout layout = new FrameLayout(context);
    layout.setLayoutParams(lp);
    layout.addView(button);
    layout.setZ(1000);
    return layout;
  }

  private static AlertDialog buildSettingDialog(Context context) {
    String current = getMode(context) ? "bubble°" : "chat head";
    AlertDialog dialog = new AlertDialog
        .Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        .setMessage(isFallback ? "WARNING!\n\n" +
            "Something went wrong, bubble mode might not work. Please report.\n" :
            "Choose your preferred feature.\n\n" +"-» Current: " + current +"\n"
        )
        .setPositiveButton("Chat Head", (d, w) -> {
          DataPreferences.setBubblePref(context, false);
          Util.restartApp(context);
        })
        .setNeutralButton("Bubble", (d, w) -> {
          DataPreferences.setBubblePref(context, true);
          Util.restartApp(context);
        })
        .setNegativeButton("Cancel", null)
        .create();
    dialog.setOnShowListener(dialogInterface -> {
      try {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.CYAN);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.YELLOW);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#233b43")));
      } catch (Throwable ignore) {}
    });
    return dialog;
  }

  private static AlertDialog buildDisableDialog(Context context) {
    return new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
        .setMessage("Remove the drop-down icon completely?\n\n" +
            "[You cannot undo this action without clearing messenger app data]"
        )
        .setPositiveButton("Remove", (d, w) -> {
          DataPreferences.disableSetting(context);
          Util.restartApp(context);
        })
        .setNegativeButton("Cancel", null)
        .create();
  }

  private static void onPreferenceActivity(ClassLoader classLoader, Consumer<Activity> consumer) {
    try {
      Class<?> cls = XposedHelpers.findClass(Constants.ME_PREFERENCE_CLASS, classLoader);
      Method[] methods = XposedHelpers.findMethodsByExactParameters(cls, void.class, Bundle.class);
      XposedBridge.hookMethod(methods[0], new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) {
          consumer.accept((Activity) param.thisObject);
        }
      });
    } catch (Throwable t) {
      Log.w("PreferenceActivity hook failed: " + t.getMessage());
    }
  }
}

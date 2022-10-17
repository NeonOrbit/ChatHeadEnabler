package app.neonorbit.chatheadenabler;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static app.neonorbit.chatheadenabler.DataPreferences.isSettingDisabled;
import static app.neonorbit.chatheadenabler.Util.runCatching;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.function.Consumer;
import java.util.function.Function;

import app.neonorbit.chatheadenabler.dex.ReflectionMagic;
import app.neonorbit.chatheadenabler.dex.SettingsClassPicker;
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

  public static void attach(Context appContext) {
    if (isSettingDisabled(appContext)) return;
    overrideSettingsFragment(appContext, (rootView) -> {
      try {
        var context = rootView.getContext();
        var layout = buildLayout(context);
        if (rootView instanceof FrameLayout) {
          ((ViewGroup) rootView).addView(layout);
          return rootView;
        }
        var wrapped = new FrameLayout(context);
        wrapped.addView(rootView);
        wrapped.addView(layout);
        return wrapped;
      } catch (Throwable t) {
        Log.w("Failed to attach settings: " + t.getMessage());
        return rootView;
      }
    });
  }

  private static FrameLayout buildLayout(Context context) {
    ImageButton button = new ImageButton(context);
    button.setImageDrawable(HdCodedSvg.create(
        Util.parseDpi(context, 24), Color.parseColor("#809D9B")
    ));
    button.setBackgroundColor(Color.TRANSPARENT);
    AlertDialog setting = buildSettingDialog(context);
    AlertDialog disable = buildDisableDialog(context);
    if (!(context instanceof Activity) && Settings.canDrawOverlays(context)) {
      setting.getWindow().setType(TYPE_APPLICATION_OVERLAY);
      disable.getWindow().setType(TYPE_APPLICATION_OVERLAY);
    }
    button.setOnClickListener(view -> setting.show());
    button.setOnLongClickListener(view -> {
      disable.show(); return true;
    });
    int width = ViewGroup.LayoutParams.WRAP_CONTENT;
    int height = ViewGroup.LayoutParams.WRAP_CONTENT;
    var params = new FrameLayout.LayoutParams(width, height);
    params.gravity = Gravity.TOP | Gravity.END;
    var margin = Util.parseDpi(context, 14);
    params.setMargins(0, margin, margin, 0);
    FrameLayout layout = new FrameLayout(context);
    layout.setLayoutParams(params);
    layout.addView(button);
    layout.setZ(10000);
    return layout;
  }

  private static AlertDialog buildSettingDialog(Context context) {
    var appContext = context.getApplicationContext();
    var current = getMode(appContext) ? "bubble°" : "chat head";
    var dialog = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        .setMessage(isFallback ? "WARNING!\n\n" +
            "Something went wrong, bubble mode might not work. Please report.\n" :
            "Choose your preferred feature.\n\n" + "-» Current: " + current + "\n"
        )
        .setPositiveButton("Chat Head", (d, w) -> {
          DataPreferences.setBubblePref(appContext, false);
          Util.restartApp(appContext);
        })
        .setNeutralButton("Bubble", (d, w) -> {
          DataPreferences.setBubblePref(appContext, true);
          Util.restartApp(appContext);
        })
        .setNegativeButton("Cancel", null)
        .create();
    dialog.setOnShowListener(dialogInterface -> {
      try {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.CYAN);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.YELLOW);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#233B43")));
      } catch (Throwable ignore) {}
    });
    return dialog;
  }

  private static AlertDialog buildDisableDialog(Context context) {
    var appContext = context.getApplicationContext();
    return new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
        .setMessage("Remove the toggle icon completely?\n\n" +
            "[You cannot undo this action without clearing messenger app data]"
        )
        .setPositiveButton("Remove", (d, w) -> {
          DataPreferences.disableSetting(appContext);
          Util.restartApp(appContext);
        })
        .setNegativeButton("Cancel", null)
        .create();
  }

  private static void overrideSettingsFragment(Context context, Function<View, View> override) {
    getSettingsFragment(context, (fragment) -> runCatching(() -> {
      var method = XposedHelpers.findMethodExact(fragment, "onCreateView",
          LayoutInflater.class, ViewGroup.class, Bundle.class
      );
      Util.hookAfter(method, param -> {
        var view = (View) param.getResult();
        param.setResult(override.apply(view));
      });
    }, "Failed to override fragment"));
  }

  private static void getSettingsFragment(Context context, Consumer<Class<?>> consumer) {
    var cached = ReflectionMagic.loadIfSettingsFragment(
        DataPreferences.getSettingCache(context), context.getClassLoader()
    );
    if (cached == null) {
      runCatching(() -> new SettingsClassPicker(context).pick(clazz -> {
        DataPreferences.setSettingCache(context, clazz.getName());
        consumer.accept(clazz);
      }), "Failed to pick fragment");
    } else {
      Log.d("Fragment: " + cached);
      consumer.accept(cached);
    }
  }
}

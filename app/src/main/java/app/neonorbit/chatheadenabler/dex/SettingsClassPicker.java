package app.neonorbit.chatheadenabler.dex;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import app.neonorbit.chatheadenabler.Log;
import app.neonorbit.chatheadenabler.Util;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XposedHelpers;
import io.github.neonorbit.dexplore.DexFactory;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.ReferenceFilter;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;

public class SettingsClassPicker {
  private final Context context;
  private final AtomicReference<Unhook> reference;

  public SettingsClassPicker(Context context) {
    this.context = context;
    this.reference = new AtomicReference<>();
  }

  public void pick(Consumer<Class<?>> consumer) {
    observe(consumer, Executors.newSingleThreadScheduledExecutor().schedule(() -> {
          if (!Thread.interrupted() && reference.get() != null) fetch(consumer);
        }, 4, TimeUnit.MINUTES)
    );
  }

  private void observe(Consumer<Class<?>> consumer, ScheduledFuture<?> scheduled) {
    var method = XposedHelpers.findMethodExact("androidx.fragment.app.Fragment",
        context.getClassLoader(), "onCreate", Bundle.class
    );
    Log.d("Observing settings fragment...");
    reference.set(Util.hookAfter(method, param -> {
      var clazz = param.thisObject.getClass();
      if (ReflectionMagic.isSettingsFragment(clazz)) {
        scheduled.cancel(true);
        onResult(clazz, consumer);
      }
    }));
  }

  private void fetch(Consumer<Class<?>> consumer) {
    Log.d("Fetching settings fragment...");
    var result = DexFactory.load(context.getApplicationInfo().sourceDir).findClass(
        DexFilter.builder().setPreferredDexNames(Constants.SETTINGS_PREFERRED_DEX).build(),
        ClassFilter.builder().setReferenceTypes(ReferenceTypes.builder().addString().build())
            .setReferenceFilter(ReferenceFilter.contains(Constants.ME_LITHO_PREFERENCE_FRAGMENT))
            .skipModifiers(Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.INTERFACE).build()
    );
    onResult(Objects.requireNonNull(ReflectionMagic.loadIfSettingsFragment(
        Objects.requireNonNull(result).clazz, context.getClassLoader()
    )), consumer);
  }

  private synchronized void onResult(Class<?> result, Consumer<Class<?>> consumer) {
    Log.d("Result[" + Thread.currentThread().getName() + "]: " + result);
    Unhook hooked = reference.get();
    if (hooked != null) {
      hooked.unhook();
      reference.set(null);
      consumer.accept(result);
    }
  }
}

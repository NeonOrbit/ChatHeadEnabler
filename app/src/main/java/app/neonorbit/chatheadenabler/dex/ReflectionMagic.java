package app.neonorbit.chatheadenabler.dex;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.neonorbit.chatheadenabler.Log;
import de.robv.android.xposed.XposedHelpers;

public final class ReflectionMagic {
  public static Class<?> loadIfSettingsFragment(String clazz, ClassLoader cl) {
    if (clazz == null || cl == null) return null;
    var loadedClass = XposedHelpers.findClassIfExists(clazz, cl);
    return isSettingsFragment(loadedClass) ? loadedClass : null;
  }

  public static boolean isSettingsFragment(Class<?> clazz) {
    try {
      var internal = getStaticObjectField(clazz, Constants.REDEX_INTERNAL_FIELD);
      return Constants.ME_LITHO_PREFERENCE_FRAGMENT.equals(internal);
    } catch (Throwable ignore) {
      return false;
    }
  }

  public static Class<?> findTarget(ClassLoader classLoader) {
    try {
      Method first = getMagicMethod(findClass(Constants.CHAT_HEAD_MENU_CLASS, classLoader), 8, 5);
      Method second = getMagicMethod(findClass(Constants.CHAT_HEAD_ROW_CLASS, classLoader), 5, 3);
      if (first == null || second == null) return null;
      if (first.getParameterTypes()[3].getName().equals(second.getParameterTypes()[1].getName())) {
        return second.getParameterTypes()[1];
      }
    } catch (Throwable t) {
      Log.d("ReflectionMagic failed: " + t.getMessage());
    }
    return null;
  }

  private static Method getMagicMethod(Class<?> clazz, int paramSize, int minimumParamSize) {
    Method similar = null;
    String returnType = boolean.class.getName();
    String firstParam = Context.class.getName();
    for(Method method : clazz.getDeclaredMethods()) {
      if (!method.isSynthetic() &&
          Modifier.isStatic(method.getModifiers()) &&
          method.getParameterCount() >= minimumParamSize &&
          method.getReturnType().getName().equals(returnType) &&
          method.getParameterTypes()[0].getName().equals(firstParam)) {
        if (method.getParameterCount() == paramSize)
          return method;
        similar = method;
      }
    }
    return similar;
  }
}

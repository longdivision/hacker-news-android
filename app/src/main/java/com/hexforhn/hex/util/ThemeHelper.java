package com.hexforhn.hex.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import com.hexforhn.hex.R;

public class ThemeHelper {
    public static void applyTheme(Activity activity) {
        if (shouldSetTheme(activity)) {
            activity.setTheme(getDesiredTheme(activity));
        }
    }

    public static void updateTheme(Activity activity) {
        if (shouldSetTheme(activity)) {
            activity.recreate();
        }
    }

    private static boolean shouldSetTheme(Activity activity) {
        return getCurrentTheme(activity) != getDesiredTheme(activity);
    }

    private static int getDesiredTheme(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean useDarkTheme = sharedPref.getBoolean(activity.getString(R.string.enableDarkThemeSettingKey), false);

        return useDarkTheme ? R.style.AppThemeDark : R.style.AppThemeLight;
    }

    private static int getCurrentTheme(Activity activity) {
        TypedValue outValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.themeName, outValue, true);

        return outValue.string.toString().equals("AppThemeDark") ? R.style.AppThemeDark : R.style.AppThemeLight;
    }
}

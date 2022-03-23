package cn.byteroute.io.ui.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.AttrRes;

class ThemeResolver {

    public static int getColor(Context context, @AttrRes int attr) {
        Activity activity = (Activity) context;
        activity.requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

        return getColor(context, attr, 0);
    }

    public static int getColor(Context context, @AttrRes int attr, int defaultColor) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, defaultColor);
        } finally {
            a.recycle();
        }
    }

    public static int getDimen(Context context, @AttrRes int attr, int defaultSize) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getDimensionPixelSize(0, defaultSize);
        } finally {
            a.recycle();
        }
    }

}

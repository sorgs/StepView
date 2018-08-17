package com.sorgs.stepview.utils;

import android.content.Context;

/**
 * description: 单位换算工具.
 *
 * @author Sorgs.
 * @date 2018/8/17.
 */
public class SizeUtils {
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

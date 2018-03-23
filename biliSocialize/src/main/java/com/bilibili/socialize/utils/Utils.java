package com.bilibili.socialize.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author NeilLee
 * @since 2018/3/12 11:53
 */

public class Utils {
    public static boolean isMobileQQInstalled(Context context) {
        return isMobileQQSupportShare(context);
    }

    // copy from tencent sdk
    private static boolean isMobileQQSupportShare(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo("com.tencent.mobileqq", 0);
            return compareVersion(packageInfo.versionName, "4.1") >= 0;
        } catch (PackageManager.NameNotFoundException var4) {
            return false;
        }
    }

    private static int compareVersion(String versionName, String defaultValue) {
        if (versionName == null && defaultValue == null) {
            return 0;
        } else if (versionName != null && defaultValue == null) {
            return 1;
        } else if (versionName == null && defaultValue != null) {
            return -1;
        } else {
            String[] var2 = versionName.split("\\.");
            String[] var3 = defaultValue.split("\\.");
            try {
                int var4;
                for (var4 = 0; var4 < var2.length && var4 < var3.length; ++var4) {
                    int var5 = Integer.parseInt(var2[var4]);
                    int var6 = Integer.parseInt(var3[var4]);
                    if (var5 < var6) {
                        return -1;
                    }
                    if (var5 > var6) {
                        return 1;
                    }
                }
                return var2.length > var4 ? 1 : (var3.length > var4 ? -1 : 0);
            } catch (NumberFormatException e) {
                return versionName.compareTo(defaultValue);
            }
        }
    }
}

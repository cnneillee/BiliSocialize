/*
 * Copyright (C) 2015 Bilibili <jungly.ik@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bilibili.socialize.share.core.handler.qq;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bilibili.socialize.share.R;
import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SharePlatformConfig;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareConfigException;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.handler.BaseShareHandler;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.Map;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/8
 */
public abstract class BaseQQShareHandler extends BaseShareHandler {
    private static final String TAG = "BShare.qq.base_handler";

    private String mAppId;
    protected Tencent mTencent;

    public BaseQQShareHandler(Activity context, BiliShareConfiguration configuration) {
        super(context, configuration);
    }

    private Map<String, String> getAppConfig() {
        SharePlatformConfig platformConfig = mShareConfiguration.getPlatformConfig();
        return platformConfig.getPlatformDevInfo(SocializeMedia.QQ);
    }

    @Override
    public void checkConfig() throws Exception {
        if (!TextUtils.isEmpty(mAppId)) {
            return;
        }

        Map<String, String> appConfig = getAppConfig();
        if (appConfig == null || TextUtils.isEmpty(mAppId = appConfig.get(SharePlatformConfig.APP_ID))) {
            throw new ShareConfigException("Please set QQ platform dev info.");
        }
    }

    @Override
    public void init() throws Exception {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppId, getContext().getApplicationContext());
        }
    }

    /**
     * 必须在主线程分享
     *
     * @param activity
     * @param params
     */
    protected void doShareToQQ(final Activity activity, final Bundle params) {
        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "real start share");
                postProgressStart();
                onShare(activity, mTencent, params, mUiListener);
                if (activity != null && !isMobileQQSupportShare(activity.getApplicationContext())) {
                    Log.d(TAG, "qq has not install");
                    String msg = activity.getString(R.string.bili_share_sdk_not_install_qq);
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    if (getShareListener() != null) {
                        getShareListener().onError(getShareMedia(), BiliShareStatusCode.ST_CODE_SHARE_ERROR_NOT_INSTALL, new ShareException(msg));
                    }
                }
            }
        });
    }

    protected abstract void onShare(Activity activity, Tencent tencent, Bundle params, IUiListener iUiListener);

    @Override
    protected boolean isNeedActivityContext() {
        return true;
    }

    protected final IUiListener mUiListener = new IUiListener() {
        @Override
        public void onCancel() {
            Log.d(TAG, "share cancel");
            if (getShareListener() != null) {
                getShareListener().onCancel(getShareMedia());
            }
        }

        @Override
        public void onComplete(Object response) {
            Log.d(TAG, "share success");
            if (getShareListener() != null) {
                getShareListener().onSuccess(getShareMedia(), BiliShareStatusCode.ST_CODE_SUCCESSED);
            }
        }

        @Override
        public void onError(UiError e) {
            Log.d(TAG, "share failed");
            if (getShareListener() != null) {
                getShareListener().onError(getShareMedia(), BiliShareStatusCode.ST_CODE_SHARE_ERROR_EXCEPTION, new ShareException(e.errorMessage));
            }
        }
    };

    //copy from tencent sdk
    private boolean isMobileQQSupportShare(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo("com.tencent.mobileqq", 0);
            return compareVersion(packageInfo.versionName, "4.1") >= 0;
        } catch (PackageManager.NameNotFoundException var4) {
            return false;
        }
    }

    private int compareVersion(String versionName, String defaultValue) {
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

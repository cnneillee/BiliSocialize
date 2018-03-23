/*
 * Copyright 2018 Neil Lee <cnneillee@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bilibili.socialize.login.core.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.handler.wx.WXLoginHandler;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;

/**
 * @author NeilLee
 * @since 2018/1/31 10:18
 */

public class WXLoginAssistActivity extends BaseAssistActivity<WXLoginHandler> {
    private static final String TAG = "BLogin.WXLoginAstActy";
    public static final String ACTION_RESULT = "com.bilibili.login.wechat.result";

    public static final String BUNDLE_STATUS_CODE = "status_code";
    public static final String BUNDLE_STATUS_MSG = "status_msg";
    public static final String BUNDLE_DATA = "data";

    private boolean mIsFirstIntent;

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected WXLoginHandler resolveHandler(SocializeMedia media, LoginConfiguration loginConfig) {
        if (SocializeMedia.WEIXIN.equals(media)) {
            return new WXLoginHandler(this, loginConfig);
        }
        return null;
    }

    public static void start(Activity activity, LoginConfiguration config, int reqCode) {
        Intent intent = new Intent(activity, WXLoginAssistActivity.class);
        intent.putExtra(KEY_TYPE, SocializeMedia.WEIXIN.name());
        intent.putExtra(KEY_CONFIG, config);
        activity.startActivityForResult(intent, reqCode);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            IntentFilter filter = new IntentFilter(ACTION_RESULT);
            registerReceiver(mResultReceiver, filter);
            Log.d(TAG, "broadcast has register");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            mIsFirstIntent = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, String.format("act resume: isFirst(%s),hasGetResult(%s)", mIsFirstIntent, mHasGetResult));
        if (mIsFirstIntent) {
            mIsFirstIntent = false;
            return;
        }
        if (mHasGetResult) {
            return;
        }

        Log.e(TAG, "gonna finish share with incorrect callback (cancel)");
        finishWithCancelResult();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        try {
            unregisterReceiver(mResultReceiver);
            Log.d(TAG, "broadcast has unregister");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            int code = intent.getIntExtra(BUNDLE_STATUS_CODE, -1);
            String msg = intent.getStringExtra(BUNDLE_STATUS_MSG);
            String data = intent.getStringExtra(BUNDLE_DATA);
            if (code == BiliShareStatusCode.ST_CODE_SUCCESSED) {
                Log.d(TAG, "get result from broadcast: success");
                finishWithSuccessResult(data);
            } else if (code == BiliShareStatusCode.ST_CODE_ERROR) {
                Log.d(TAG, "get result from broadcast: failed");
                finishWithFailResult(msg);
            } else if (code == BiliShareStatusCode.ST_CODE_ERROR_CANCEL) {
                Log.d(TAG, "get result from broadcast: cancel");
                finishWithCancelResult();
            }
        }
    };
}

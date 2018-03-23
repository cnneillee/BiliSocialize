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

package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.handler.sina.SinaShareHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * 处理微博分享
 *
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/15 14:00
 */
public class SinaAssistActivity extends BaseAssistActivity<SinaShareHandler> {
    private static final String TAG = "BShare.sina.assist";

    private boolean mIsActivityResultCanceled;
    private boolean mHasOnNewIntentCalled;
    private Handler mHandler = new Handler();

    private boolean mIsFirstIntent;

    public static void start(Activity act, BaseShareParam params, BiliShareConfiguration configuration, int reqCode) {
        Intent intent = new Intent(act, SinaAssistActivity.class);
        intent.putExtra(KEY_PARAM, params);
        intent.putExtra(KEY_CONFIG, configuration);
        intent.putExtra(KEY_TYPE, SocializeMedia.SINA.name());
        act.startActivityForResult(intent, reqCode);
        act.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mIsFirstIntent = true;
        }
    }

    @Override
    protected SinaShareHandler resolveHandler(SocializeMedia media, BiliShareConfiguration shareConfig) {
        if (media == SocializeMedia.SINA) {
            return new SinaShareHandler(this, shareConfig);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, String.format("activity onResume: OnNewIntentCalled(%s), OnActivityResult(%s), isFinishing(%s)",
                mHasOnNewIntentCalled, mIsActivityResultCanceled, isFinishing()));
        if (mHasOnNewIntentCalled || mHasGetResult || isFinishing()) {
            return;
        }

        boolean isAppInstall = mShareHandler.mShareHandler != null && mShareHandler.isSinaClientInstalled();
        if (isAppInstall && mIsActivityResultCanceled) {
            Log.e(TAG, "gonna finish share with incorrect callback (cancel)");
            finishWithCancelResult();
            return;
        }

        //卸载微博，首次分享可能会失败，并且没有回调。
        if (mIsFirstIntent) {
            mIsFirstIntent = false;
            Log.d(TAG, "post pending finish task delay 1500");
            mHandler.postDelayed(mPendingTask, 1500);
            return;
        }

        Log.d(TAG, "post pending finish task delay 5000");
        mHandler.postDelayed(mPendingTask, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mPendingTask);
    }

    private Runnable mPendingTask = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "finish share with pending task (cancel)");
            finishWithCancelResult();
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mHasOnNewIntentCalled = true;
        Log.d(TAG, "activity onNewIntent");
        mShareHandler.onActivityNewIntent(this, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIsActivityResultCanceled = resultCode == Activity.RESULT_CANCELED;
        Log.d(TAG, String.format("activity onResult: resultCode(%d), canceled(%s)", resultCode, mIsActivityResultCanceled));
        mShareHandler.onActivityResult(this, requestCode, resultCode, data, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }

    @Override
    public void onSuccess(SocializeMedia type, int code) {
        super.onSuccess(type, code);
        release();
    }

    @Override
    public void onError(SocializeMedia type, int code, Throwable error) {
        super.onError(type, code, error);
        release();
    }

    @Override
    public void onCancel(SocializeMedia type) {
        super.onCancel(type);
        release();
    }

    @Override
    protected String tag() {
        return TAG;
    }

}

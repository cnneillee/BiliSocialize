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

package com.bilibili.socialize.login.core.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeListeners;

/**
 * @author NeilLee
 * @since 2018/1/29 18:10
 */

public abstract class AbsLoginHandler implements ILoginHandler {
    private static final String TAG = "BLogin.handler.abs";

    protected Context mContext;
    protected LoginConfiguration mConfiguration;

    private SocializeListeners.LoginListener mLoginListener;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AbsLoginHandler(Activity context, LoginConfiguration configuration) {
        initContext(context);
        mConfiguration = configuration;
    }

    protected void initContext(Activity context) {
        if (isNeedActivityContext()) {
            mContext = context;
        } else {
            mContext = context.getApplicationContext();
        }
    }

    /**
     * 该平台是否必须用Activity类型的Context来调起分享
     */
    protected boolean isNeedActivityContext() {
        return false;
    }

    public Context getContext() {
        return mContext;
    }

    public SocializeListeners.LoginListener getLoginListener() {
        return mLoginListener;
    }

    // ILoginHandler 默认实现

    @Override
    public boolean isDisposable() {
        return false;
    }

    @Override
    public void release() {
        mLoginListener = null;
        mContext = null;
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
    }

    @Override
    public void login(SocializeListeners.LoginListener listener) throws Exception {
        mLoginListener = listener;
    }

/*
    protected void doWorkOnThread(final Runnable runnable) {
        mConfiguration.getTaskExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (runnable != null) {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getLoginListener() != null) {
                            getLoginListener().onError(null, ST_CODE_LOGIN_ERROR_EXCEPTION, new LoginException("login failed"));
                        }
                    }
                }
            }
        });
    }

    protected void doOnMainThread(Runnable runnable) {
        if (mMainHandler != null) {
            mMainHandler.post(runnable);
        }
    }

    protected void postProgressStart() {
        //postProgress(R.string.bili_share_sdk_share_start);
    }

    protected void postProgress(final int msgRes) {
        if (getContext() != null) {
            postProgress(getContext().getString(msgRes));
        }
    }

    protected void postProgress(final String msg) {
        doOnMainThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (getLoginListener() != null) {
                            getLoginListener().onProgress(null, msg);
                        }
                    }
                }
        );
    }
*/

    // 生命周期同步接口实现

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState, SocializeListeners.LoginListener listener) {
        initContext(activity);
        mLoginListener = listener;
    }

    @Override
    public void onActivityNewIntent(Activity activity, Intent intent) {
        initContext(activity);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SocializeListeners.LoginListener listener) {
        initContext(activity);
        mLoginListener = listener;
    }

    @Override
    public void onActivityDestroy() {
        release();
    }
}

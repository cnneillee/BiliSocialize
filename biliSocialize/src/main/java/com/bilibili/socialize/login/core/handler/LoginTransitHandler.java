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
import android.util.Log;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.ui.BiliLoginDelegateActivity;

/**
 * @author NeilLee
 * @since 2018/1/29 17:51
 */

public class LoginTransitHandler extends AbsLoginHandler {
    private static final String TAG = "BShare.transit.";
    private SocializeMedia mTypeName;
    private String mClientName;

    public LoginTransitHandler(Activity context, LoginConfiguration configuration, SocializeMedia type, String clientName) {
        super(context, configuration);
        mTypeName = type;
        mClientName = clientName;
    }

    @Override
    public final void login(final SocializeListeners.LoginListener listener) throws Exception {
        super.login(listener);
        final Context context = getContext();
        BiliLoginDelegateActivity.start((Activity) context, mConfiguration, mTypeName, mClientName);
    }

    @Override
    protected final boolean isNeedActivityContext() {
        return true;
    }

    private String tag() {
        return TAG + mTypeName;
    }

    @Override
    public SocializeMedia getMediaType() {
        return mTypeName;
    }

    public void onStart(SocializeMedia type) {
        Log.d(tag(), "on share start");
        SocializeListeners.LoginListener listener = getLoginListener();
        if (listener == null) {
            return;
        }
        listener.onStart(type);
    }

    public void onProgress(SocializeMedia type, String progressDesc) {
        Log.d(tag(), "on share progress");
        SocializeListeners.LoginListener listener = getLoginListener();
        if (listener == null) {
            return;
        }
        listener.onProgress(type, progressDesc);
    }

    public void onSuccess(SocializeMedia type, int code, String data) {
        Log.d(tag(), "on share success");
        SocializeListeners.LoginListener listener = getLoginListener();
        if (listener == null) {
            return;
        }
        listener.onSuccess(type, code, data);
    }

    public void onError(SocializeMedia type, int code, Throwable error) {
        Log.d(tag(), "on share failed");
        SocializeListeners.LoginListener listener = getLoginListener();
        if (listener == null) {
            return;
        }
        listener.onError(type, code, error);
    }

    public void onCancel(SocializeMedia type) {
        Log.d(tag(), "on share cancel");
        SocializeListeners.LoginListener listener = getLoginListener();
        if (listener == null) {
            return;
        }
        listener.onCancel(type);
    }
}

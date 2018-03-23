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

package com.bilibili.socialize.login.core;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.login.core.handler.ILoginHandler;
import com.bilibili.socialize.login.core.handler.LoginTransitHandler;
import com.bilibili.socialize.login.core.handler.wx.WXLoginHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_ERROR;

/**
 * @author NeilLee
 * @since 2018/1/30 17:32
 */

public class BiliLogin {
    private static final String TAG = "BLogin.main.client";
    private static final Map<String, BiliLogin> CLIENT_MAP = new HashMap<>();
    private ILoginHandler mCurrentLoginHandler;
    private Map<SocializeMedia, ILoginHandler> mHandlerMap = new HashMap<>();
    private LoginConfiguration mLoginConfiguration;

    private SocializeListeners.LoginListener mOuterLoginListener;

    private String mName;

    public static BiliLogin get(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        BiliLogin router = CLIENT_MAP.get(name);
        if (router == null) {
            synchronized (CLIENT_MAP) {
                router = CLIENT_MAP.get(name);
                if (router == null) {
                    Log.d(TAG, String.format("create new login client named(%s)", name));
                    BiliLogin value = new BiliLogin(name);
                    CLIENT_MAP.put(name, value);
                    router = value;
                }
                return router;
            }
        } else {
            Log.d(TAG, String.format("find existed login client named(%s)", name));
        }

        return router;
    }

    public ILoginHandler getCurrentLoginHandler() {
        return mCurrentLoginHandler;
    }

    private BiliLogin(String name) {
        mName = name;
        Log.d(TAG, String.format("construct login client named(%s)", name));
    }

    /**
     * 登录之前必须先配置.
     *
     * @param configuration 配置信息
     */
    public void config(LoginConfiguration configuration) {
        mLoginConfiguration = configuration;
    }

    public void login(Activity activity, SocializeMedia loginType, SocializeListeners.LoginListener listener) {
        if (mLoginConfiguration == null) {
            throw new IllegalArgumentException("BiliLoginConfiguration must be initialized before invoke login");
        }

        if (mCurrentLoginHandler != null) {
            Log.w(TAG, "release leaked login handler");
            release(mCurrentLoginHandler.getMediaType());
        }

        mCurrentLoginHandler = newHandler(activity, loginType, mLoginConfiguration);

        if (mCurrentLoginHandler != null) {
            try {
                mOuterLoginListener = listener;

                mInnerLoginListener.onStart(loginType);
                mCurrentLoginHandler.login(mInnerLoginListener);

                if (mCurrentLoginHandler.isDisposable()) {
                    Log.d(TAG, "release disposable login handler");
                    release(mCurrentLoginHandler.getMediaType());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, String.format("error when login due to %s", e.getMessage()));
                mInnerLoginListener.onError(loginType, ST_CODE_ERROR, e.getCause());
            }
        }
    }

    private ILoginHandler newHandler(Activity activity, SocializeMedia media, LoginConfiguration configuration) {
        ILoginHandler handler = null;
        switch (media) {
            case QQ:
                handler = new LoginTransitHandler(activity, configuration, SocializeMedia.QQ, mName);
                break;
            case WEIXIN:
                handler = new WXLoginHandler(activity, configuration);
                break;
        }
        mHandlerMap.put(media, handler);
        return handler;
    }

    private void release(SocializeMedia type) {
        Log.d(TAG, String.format("========》release client:(%s) 《========", type.name()));
        mOuterLoginListener = null;
        if (mCurrentLoginHandler != null) {
            mCurrentLoginHandler.release();
        }
        mCurrentLoginHandler = null;
        remove(type);
    }

    private void remove(SocializeMedia type) {
        mHandlerMap.remove(type);
    }

    private ILoginHandler.InnerLoginListener mInnerLoginListener = new ILoginHandler.InnerLoginListener() {

        @Override
        public void onProgress(SocializeMedia type, String progressDesc) {
            Log.d(TAG, String.format("login on progress:(%s %s)", type, progressDesc));
            if (mOuterLoginListener != null) {
                mOuterLoginListener.onProgress(type, progressDesc);
            }
        }

        @Override
        public void onStart(SocializeMedia type) {
            Log.d(TAG, String.format("start to login:(%s)", type));
            if (mOuterLoginListener != null) {
                mOuterLoginListener.onStart(type);
            }
        }

        @Override
        public void onSuccess(SocializeMedia type, int code, JSONObject tokens) {
            Log.d(TAG, String.format("login success:(%s %d)", type, code));
            if (mOuterLoginListener != null) {
                mOuterLoginListener.onSuccess(type, code, tokens);
            }
            release(type);
        }

        @Override
        public void onError(SocializeMedia type, int code, Throwable error) {
            Log.d(TAG, String.format("login error due to:%s (%s %d)", error.getMessage(), type, code));
            if (mOuterLoginListener != null) {
                mOuterLoginListener.onError(type, code, error);
            }
            release(type);
        }

        @Override
        public void onCancel(SocializeMedia type) {
            Log.d(TAG, String.format("login cancel:(%s)", type));
            if (mOuterLoginListener != null) {
                mOuterLoginListener.onCancel(type);
            }
            release(type);
        }
    };
}

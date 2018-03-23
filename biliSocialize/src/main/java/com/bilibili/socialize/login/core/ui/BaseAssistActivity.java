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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.handler.BaseLoginHandler;

import org.json.JSONObject;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_EXCEPTION;
import static com.bilibili.socialize.login.core.ui.BiliLoginDelegateActivity.*;

/**
 * @author NeilLee
 * @since 2018/1/31 09:53
 */

public abstract class BaseAssistActivity<H extends BaseLoginHandler> extends Activity
        implements SocializeListeners.LoginListener {
    public static final String KEY_TYPE = "key.type";
    public static final String KEY_CONFIG = "key.config";

    protected H mLoginHandler;
    protected SocializeMedia mMediaType;
    protected LoginConfiguration mLoginConfig;

    protected boolean mHasGetResult;

    protected abstract String tag();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveParams();
        mLoginHandler = resolveHandler(mMediaType, mLoginConfig);
        if (mLoginHandler == null) {
            String msg = String.format("media type is not correct:%s", mMediaType);
            Log.w(tag(), msg);
            finishWithFailResult(msg);
        } else {
            boolean bingo = initHandler(savedInstanceState);
            if (bingo) {
                startLogin(savedInstanceState);
            }
        }
    }

    public void resolveParams() {
        Intent intent = getIntent();
        String type = intent.getStringExtra(KEY_TYPE);
        mLoginConfig = intent.getParcelableExtra(KEY_CONFIG);
        if (!TextUtils.isEmpty(type)) {
            mMediaType = SocializeMedia.valueOf(type);
        }
    }

    protected abstract H resolveHandler(SocializeMedia media, LoginConfiguration loginConfig);

    private boolean initHandler(Bundle savedInstanceState) {
        try {
            mLoginHandler.checkConfig();
            mLoginHandler.init();
            Log.d(tag(), "login handler init success");
            mLoginHandler.onActivityCreated(this, savedInstanceState, this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(tag(), String.format("login handler init failed:%s", e.getMessage()));
            finishWithFailResult("login handler init failed");
            return false;
        }
    }

    private void startLogin(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                Log.d(tag(), "call login");
                mLoginHandler.login(this);
            }
        } catch (Exception e) {
            onError(mMediaType, ST_CODE_LOGIN_ERROR_EXCEPTION, e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag(), "activity onDestroy");
        mLoginHandler.onActivityDestroy();
    }

    protected void finishWithSuccessResult(JSONObject tokens) {
        setResult(Activity.RESULT_OK, BiliLoginDelegateActivity.createResult(RESULT_SUCCESS, tokens));
        finish();
    }

    protected void finishWithFailResult(String msg) {
        setResult(Activity.RESULT_OK, BiliLoginDelegateActivity.createResult(RESULT_FAIL, msg));
        finish();
    }

    protected void finishWithCancelResult() {
        setResult(Activity.RESULT_OK, BiliLoginDelegateActivity.createResult(RESULT_CANCEL));
        finish();
    }

    protected void release() {
        if (mLoginHandler != null) {
            mLoginHandler.release();
        }
    }

    // implements SocializeListeners.LoginListener

    @Override
    public void onStart(SocializeMedia type) {
        Log.d(tag(), "on inner share start");
    }

    @Override
    public void onProgress(SocializeMedia type, String progressDesc) {
        Log.d(tag(), "on inner share progress");
    }

    @Override
    public void onSuccess(SocializeMedia type, int code, JSONObject tokens) {
        Log.i(tag(), "----->on inner share success<-----");
        mHasGetResult = true;
        finishWithSuccessResult(tokens);
    }

    @Override
    public void onError(SocializeMedia type, int code, Throwable error) {
        Log.i(tag(), "----->on inner share fail<-----");
        mHasGetResult = true;
        finishWithFailResult(error != null ? error.getMessage() : null);
    }

    @Override
    public void onCancel(SocializeMedia type) {
        Log.i(tag(), "----->on inner share cancel<-----");
        mHasGetResult = true;
        finishWithCancelResult();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

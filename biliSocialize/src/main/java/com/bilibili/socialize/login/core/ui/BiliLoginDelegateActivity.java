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

import com.bilibili.socialize.login.core.BiliLogin;
import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.error.LoginException;
import com.bilibili.socialize.login.core.handler.ILoginHandler;
import com.bilibili.socialize.login.core.handler.LoginTransitHandler;

import org.json.JSONObject;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_ERROR;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_SUCCESS;

/**
 * @author NeilLee
 * @since 2018/1/31 13:28
 */

public class BiliLoginDelegateActivity extends Activity {
    private static final String TAG = "BLogin.delegate.acty";

    public static final String KEY_CONFIG = "BLogin.key_config";
    public static final String KEY_TYPE = "BLogin.key_type";
    public static final String KEY_CLIENT_NAME = "BLogin.client_name";

    public static final String REP_KEY_RESULT = "BLogin.rep_key_result";
    public static final String REP_KEY_EXTRA = "BLogin.rep_key_extra";

    public static final int REQ_CODE = 2004;
    public static final int RESULT_FAIL = -2;
    public static final int RESULT_CANCEL = -1;
    public static final int RESULT_SUCCESS = 0;

    private SocializeMedia mMediaType;
    private LoginConfiguration mConfig;
    private String mClientName;

    public static void start(Activity act, LoginConfiguration configuration, SocializeMedia type, String clientName) {
        Intent intent = new Intent(act, BiliLoginDelegateActivity.class);
        intent.putExtra(KEY_CONFIG, configuration);
        intent.putExtra(KEY_TYPE, type.name());
        intent.putExtra(KEY_CLIENT_NAME, clientName);
        act.startActivity(intent);
        act.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveParams();
        if (mMediaType == null) {
            Log.d(TAG, "finish due to null socialize media");
            finishWithCancelResult();
            return;
        }
        if (savedInstanceState == null) {
            startAssistActivity();
        }
    }

    private void resolveParams() {
        Intent intent = getIntent();
        String type = intent.getStringExtra(KEY_TYPE);
        mMediaType = SocializeMedia.valueOf(type);
        mConfig = intent.getParcelableExtra(KEY_CONFIG);
        mClientName = intent.getStringExtra(KEY_CLIENT_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && data != null) {
            int result = data.getIntExtra(REP_KEY_RESULT, RESULT_CANCEL);
            if (result == RESULT_SUCCESS) {
                Log.d(TAG, "act result: success");
                finishWithSuccessResult(data.getStringExtra(REP_KEY_EXTRA));
                return;
            } else if (result == RESULT_FAIL) {
                String extra = data.getStringExtra(REP_KEY_EXTRA);
                Log.d(TAG, String.format("act result: failed, msg: %s", extra));
                finishWithFailResult(extra);
                return;
            } else if (result == RESULT_CANCEL) {
                Log.d(TAG, "act result: cancel");
                finishWithCancelResult();
                return;
            }
        }

        Log.d(TAG, "act result: finish with unexpected result");
        finishWithCancelResult();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void finishWithCancelResult() {
        onCancel(mMediaType);
    }

    private void finishWithSuccessResult(String stringExtra) {
        onSuccess(mMediaType, stringExtra);
    }

    private void finishWithFailResult(String msg) {
        onError(mMediaType, ST_CODE_ERROR, new LoginException(msg));
    }

    private void onSuccess(SocializeMedia type, String data) {
        Log.d(TAG, "on inner login success");
        LoginTransitHandler handler = getLoginHandler();
        if (handler != null) {
            // todo 这里传递结果
            handler.onSuccess(type, ST_CODE_SUCCESS, data);
        }
        finish();
    }

    private void onError(SocializeMedia type, int code, Throwable error) {
        Log.i(TAG, "----->on inner share fail<-----");
        LoginTransitHandler handler = getLoginHandler();
        if (handler != null) {
            handler.onError(type, code, error);
        }
        finish();
    }

    private void onCancel(SocializeMedia type) {
        Log.i(TAG, "----->on inner login cancel<-----");
        LoginTransitHandler handler = getLoginHandler();
        if (handler != null) {
            handler.onCancel(type);
        }
        finish();
    }

    private LoginTransitHandler getLoginHandler() {
        if (TextUtils.isEmpty(mClientName)) {
            Log.e(TAG, "null client name");
            return null;
        }

        BiliLogin login = BiliLogin.get(mClientName);
        ILoginHandler handler = login.getCurrentLoginHandler();
        if (handler == null) {
            Log.e(TAG, "null handler");
            return null;
        }
        if (!(handler instanceof LoginTransitHandler)) {
            Log.e(TAG, "wrong handler type");
            return null;
        }
        return (LoginTransitHandler) handler;
    }

    private void startAssistActivity() {
        switch (mMediaType) {
            case QQ:
                QQLoginAssistActivity.start(this, mConfig, REQ_CODE);
                break;
            case WEIXIN:
                WXLoginAssistActivity.start(this, mConfig, REQ_CODE);
                break;
            case SINA:
                break;
        }
    }

    public static Intent createResult(int result, JSONObject tokens) {
        Intent intent = new Intent();
        intent.putExtra(REP_KEY_RESULT, result);
        intent.putExtra(REP_KEY_EXTRA, tokens.toString());
        return intent;
    }

    public static Intent createResult(int result, String msg) {
        Intent intent = new Intent();
        intent.putExtra(REP_KEY_RESULT, result);
        intent.putExtra(REP_KEY_EXTRA, msg);
        return intent;
    }

    public static Intent createResult(int result) {
        Intent intent = new Intent();
        intent.putExtra(REP_KEY_RESULT, result);
        return intent;
    }
}

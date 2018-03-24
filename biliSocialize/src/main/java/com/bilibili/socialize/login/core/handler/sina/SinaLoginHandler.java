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

package com.bilibili.socialize.login.core.handler.sina;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.LoginPlatformConfig;
import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.error.LoginConfigException;
import com.bilibili.socialize.login.core.error.LoginException;
import com.bilibili.socialize.login.core.handler.BaseLoginHandler;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_AUTH_FAILED;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_EXCEPTION;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_SUCCESS;
import static com.sina.weibo.sdk.auth.Oauth2AccessToken.KEY_ACCESS_TOKEN;
import static com.sina.weibo.sdk.auth.Oauth2AccessToken.KEY_EXPIRES_IN;
import static com.sina.weibo.sdk.auth.Oauth2AccessToken.KEY_PHONE_NUM;
import static com.sina.weibo.sdk.auth.Oauth2AccessToken.KEY_REFRESH_TOKEN;
import static com.sina.weibo.sdk.auth.Oauth2AccessToken.KEY_UID;

/**
 * @author NeilLee
 * @since 2018/3/23 20:58
 */

public class SinaLoginHandler extends BaseLoginHandler
        implements WbAuthListener {
    private static final String TAG = "BLogin.sina.sinaHandler";

    private SsoHandler mSsoHandler;
    private String mAppKey;
    private String mRedirectUrl;
    private String mScope;

    public SinaLoginHandler(Activity context, LoginConfiguration configuration) {
        super(context, configuration);
        Map<String, String> devInfo = configuration.getPlatformConfig().getPlatformDevInfo(getMediaType());
        mAppKey = devInfo.get(LoginPlatformConfig.APP_KEY);
        mRedirectUrl = devInfo.get(LoginPlatformConfig.REDIRECT_URL);
        mScope = devInfo.get(LoginPlatformConfig.SCOPE);
    }

    @Override
    public void init() throws Exception {
        AuthInfo authInfo = new AuthInfo(getContext(), mAppKey, mRedirectUrl, mScope);
        WbSdk.install(getContext(), authInfo);
        mSsoHandler = new SsoHandler((Activity) getContext());
    }

    @Override
    public void login(SocializeListeners.LoginListener listener) throws Exception {
        super.login(listener);
        mSsoHandler.authorize(this);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SocializeListeners.LoginListener listener) {
        super.onActivityResult(activity, requestCode, resultCode, data, listener);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public void checkConfig() throws Exception {
        if (TextUtils.isEmpty(mAppKey) || TextUtils.isEmpty(mRedirectUrl) || TextUtils.isEmpty(mScope)) {
            Log.e(TAG, "login config error due to null appKey, redirect or scope");
            throw new LoginConfigException("empty appKey, appKey or scope");
        }
    }

    @Override
    public SocializeMedia getMediaType() {
        return SocializeMedia.SINA;
    }

    @Override
    protected boolean isNeedActivityContext() {
        return true;
    }

    // implements WbAuthListener
    @Override
    public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_UID, oauth2AccessToken.getUid());
            jsonObject.put(KEY_ACCESS_TOKEN, oauth2AccessToken.getToken());
            jsonObject.put(KEY_REFRESH_TOKEN, oauth2AccessToken.getRefreshToken());
            jsonObject.put(KEY_PHONE_NUM, oauth2AccessToken.getPhoneNum());
            jsonObject.put(KEY_EXPIRES_IN, oauth2AccessToken.getExpiresTime());
            if (getLoginListener() != null) {
                getLoginListener().onSuccess(getMediaType(), ST_CODE_SUCCESS, jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (getLoginListener() != null) {
                getLoginListener().onError(getMediaType(), ST_CODE_LOGIN_ERROR_EXCEPTION,
                        new LoginException("error when decoding token of sina", ST_CODE_LOGIN_ERROR_EXCEPTION));
            }
        }
    }

    @Override
    public void cancel() {
        if (getLoginListener() != null) {
            getLoginListener().onCancel(getMediaType());
        }
    }

    @Override
    public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
        if (getLoginListener() != null) {
            getLoginListener().onError(getMediaType(), ST_CODE_LOGIN_ERROR_AUTH_FAILED,
                    new LoginException(wbConnectErrorMessage.getErrorMessage(), ST_CODE_LOGIN_ERROR_AUTH_FAILED));
        }
    }
}

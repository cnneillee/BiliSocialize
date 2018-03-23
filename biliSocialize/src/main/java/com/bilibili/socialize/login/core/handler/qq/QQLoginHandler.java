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

package com.bilibili.socialize.login.core.handler.qq;

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
import com.bilibili.socialize.utils.Utils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Map;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_EXCEPTION;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_NOT_INSTALL;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_SUCCESS;

/**
 * @author NeilLee
 * @since 2018/1/29 17:53
 */

public class QQLoginHandler extends BaseLoginHandler {
    private static final String TAG = "BLogin.qq.qq_handler";

    private static final String DEFAULT_SCOPE = "get_user_info";

    private String mAppId;
    private String mScope;
    private Tencent mTencent;

    private IUiListener mUILoginListener = new BaseUiListener() {
        @Override
        void doComplete(JSONObject o) {
            Log.i(TAG, o.toString());
            if (getLoginListener() != null) {
                getLoginListener().onProgress(SocializeMedia.QQ, "Getting user info");
            }
            initOpenidAndToken(o);
            UserInfo info = new UserInfo(getContext(), mTencent.getQQToken());
            info.getUserInfo(mUIGetInfoListener);
        }
    };

    private void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String accessToken = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expire = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openid = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(expire)
                    && !TextUtils.isEmpty(openid)) {
                mTencent.setAccessToken(accessToken, expire);
                mTencent.setOpenId(openid);
                if (getLoginListener() != null) {
                    getLoginListener().onSuccess(SocializeMedia.QQ, ST_CODE_SUCCESS, jsonObject.toString());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (getLoginListener() != null) {
                getLoginListener().onError(SocializeMedia.QQ, ST_CODE_LOGIN_ERROR_EXCEPTION,
                        new LoginException("Empty authored result", ST_CODE_LOGIN_ERROR_EXCEPTION));
            }
        }
    }

    private IUiListener mUIGetInfoListener = new BaseUiListener() {
        @Override
        void doComplete(JSONObject o) {
            Log.i(TAG, o.toString());
        }
    };

    public QQLoginHandler(Activity context, LoginConfiguration configuration) {
        super(context, configuration);
        Map<String, String> devInfo = configuration.getPlatformConfig()
                .getPlatformDevInfo(getMediaType());
        mAppId = devInfo.get(LoginPlatformConfig.APP_ID);
        mScope = devInfo.get(LoginPlatformConfig.SCOPE);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SocializeListeners.LoginListener listener) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mUILoginListener);
        }
        super.onActivityResult(activity, requestCode, resultCode, data, listener);
    }

    @Override
    public void init() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppId, getContext().getApplicationContext());
        }
    }

    @Override
    public void login(SocializeListeners.LoginListener listener) throws Exception {
        super.login(listener);
        // 首先，检查是否安装手机QQ
        if (!Utils.isMobileQQInstalled(getContext())) {
            if (getLoginListener() != null) {
                getLoginListener().onError(SocializeMedia.QQ, ST_CODE_LOGIN_ERROR_NOT_INSTALL,
                        new LoginException("Mobile QQ not installed", ST_CODE_LOGIN_ERROR_NOT_INSTALL));
            }
            throw new LoginException("Mobile QQ not installed", ST_CODE_LOGIN_ERROR_NOT_INSTALL);
        }
        // 检查
        if (!mTencent.isSessionValid()) {
            if (getLoginListener() != null) {
                getLoginListener().onStart(SocializeMedia.QQ);
            }
            if (TextUtils.isEmpty(mScope)) {
                mScope = DEFAULT_SCOPE;
            }
            mTencent.login((Activity) getContext(), mScope, mUILoginListener);
        }
    }

    @Override
    public void checkConfig() throws Exception {
        if (TextUtils.isEmpty(mAppId)) {
            Log.e(TAG, "login config error due to null APPID");
            throw new LoginConfigException("empty APPID");
        }
    }

    @Override
    public SocializeMedia getMediaType() {
        return SocializeMedia.QQ;
    }

    @Override
    protected boolean isNeedActivityContext() {
        return true;
    }

    abstract class BaseUiListener implements IUiListener {
        abstract void doComplete(JSONObject o);

        @Override
        public void onComplete(Object o) {
            if (o == null) {
                Log.e(TAG, "login error due to onComplete return null params");
                if (getLoginListener() != null) {
                    getLoginListener().onError(SocializeMedia.QQ, ST_CODE_LOGIN_ERROR_EXCEPTION,
                            new LoginException("auth error", ST_CODE_LOGIN_ERROR_EXCEPTION));
                }
                return;
            }
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject.length() == 0) {
                Log.e(TAG, "login error due to onComplete return empty params");
                if (getLoginListener() != null) {
                    getLoginListener().onError(SocializeMedia.QQ, ST_CODE_LOGIN_ERROR_EXCEPTION,
                            new LoginException("auth error", ST_CODE_LOGIN_ERROR_EXCEPTION));
                }
                return;
            }
            doComplete(jsonObject);
        }

        @Override
        public void onError(UiError uiError) {
            Log.e(TAG, String.format("login error due to %s", uiError.errorMessage));
            if (getLoginListener() != null) {
                getLoginListener().onError(SocializeMedia.QQ, ST_CODE_LOGIN_ERROR_EXCEPTION,
                        new LoginException("auth error", ST_CODE_LOGIN_ERROR_EXCEPTION));
            }
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "login was cancelled");
            if (getLoginListener() != null) {
                getLoginListener().onCancel(SocializeMedia.QQ);
            }
        }
    }
}

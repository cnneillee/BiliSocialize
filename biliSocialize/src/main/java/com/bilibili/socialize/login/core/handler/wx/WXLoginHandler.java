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

package com.bilibili.socialize.login.core.handler.wx;

import android.app.Activity;
import android.widget.Toast;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.handler.BaseLoginHandler;
import com.bilibili.socialize.share.R;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareException;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import static com.bilibili.socialize.login.core.SocializeMedia.WEIXIN;

/**
 * @author NeilLee
 * @since 2018/1/29 20:32
 */

public class WXLoginHandler extends BaseLoginHandler {
    private String mAppId;
    private IWXAPI mWXApi;

    public WXLoginHandler(Activity context, LoginConfiguration configuration) {
        super(context, configuration);
    }

    @Override
    public void init() throws Exception {
        if (mWXApi == null) {
            mWXApi = WXAPIFactory.createWXAPI(getContext(), mAppId, true);
            if (mWXApi.isWXAppInstalled()) {
                mWXApi.registerApp(mAppId);
            }
        }

        if (!mWXApi.isWXAppInstalled()) {
            if (!mWXApi.isWXAppInstalled()) {
                String msg = getContext().getString(R.string.bili_share_sdk_not_install_wechat);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                throw new ShareException(msg, BiliShareStatusCode.ST_CODE_SHARE_ERROR_NOT_INSTALL);
            }
        }
    }

    @Override
    public void login(SocializeListeners.LoginListener listener) throws Exception {
        super.login(listener);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        mWXApi.sendReq(req);
    }

    @Override
    public void checkConfig() throws Exception {

    }

    @Override
    public SocializeMedia getMediaType() {
        return WEIXIN;
    }
}

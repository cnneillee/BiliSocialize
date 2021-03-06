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
import android.util.Log;

import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @date 2015/10/8
 */
public abstract class BaseWXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "BShare.wx.entryAct";
    private IWXAPI mIWXAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isAutoCreateWXAPI() && mIWXAPI == null) {
            initWXApi();
            Log.d(TAG, "wxApi init");
        }
    }

    private void initWXApi() {
        mIWXAPI = WXAPIFactory.createWXAPI(this, getAppId(), true);
        if (mIWXAPI.isWXAppInstalled()) {
            mIWXAPI.registerApp(getAppId());
        }
        try {
            mIWXAPI.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "handle intent fail：" + e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        if (mIWXAPI != null) {
            mIWXAPI.handleIntent(intent, this);
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "onReq");
        if (isAutoFinishAfterOnReq()) {
            finish();
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onResp");
        parseResult(resp);
        if (isAutoFinishAfterOnResp()) {
            finish();
        }
    }

    private void parseResult(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.d(TAG, "parse resp: success");
                try {
                    String data = authDataToJson((SendAuth.Resp) resp);
                    sendResult(BiliShareStatusCode.ST_CODE_SUCCESSED, null, data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendResult(BiliShareStatusCode.ST_CODE_ERROR, "error when decoding token of wechat", null);
                }
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Log.d(TAG, "parse resp: cancel");
                sendResult(BiliShareStatusCode.ST_CODE_ERROR_CANCEL, null, null);
                break;

            case BaseResp.ErrCode.ERR_SENT_FAILED:
                Log.d(TAG, "parse resp: fail");
                sendResult(BiliShareStatusCode.ST_CODE_ERROR, resp.errStr, null);
                break;
        }
    }

    private String authDataToJson(SendAuth.Resp resp) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", resp.code);
        jsonObject.put("state", resp.state);
        jsonObject.put("url", resp.url);
        jsonObject.put("lang", resp.lang);
        jsonObject.put("country", resp.country);
        return jsonObject.toString();
    }

    /**
     * WXEntryActivity在主进程，假如从非主进程中调起分享，可能会收不到分享结果，所以用广播来通知。
     *
     * @param statusCode
     * @param msg
     */
    private void sendResult(int statusCode, String msg, String data) {
        Intent intent = new Intent(WXLoginAssistActivity.ACTION_RESULT);
        intent.putExtra(WXLoginAssistActivity.BUNDLE_STATUS_CODE, statusCode);
        intent.putExtra(WXLoginAssistActivity.BUNDLE_STATUS_MSG, msg);
        intent.putExtra(WXLoginAssistActivity.BUNDLE_DATA, data);
        sendBroadcast(intent);
    }

    protected boolean isAutoFinishAfterOnReq() {
        return true;
    }

    protected boolean isAutoFinishAfterOnResp() {
        return true;
    }

    protected boolean isAutoCreateWXAPI() {
        return true;
    }

    protected abstract String getAppId();

}

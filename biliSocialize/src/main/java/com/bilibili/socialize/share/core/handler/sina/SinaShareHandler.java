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

package com.bilibili.socialize.share.core.handler.sina;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SharePlatformConfig;
import com.bilibili.socialize.share.core.SocializeListeners;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.InvalidParamException;
import com.bilibili.socialize.share.core.error.ShareConfigException;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.error.UnSupportedException;
import com.bilibili.socialize.share.core.handler.BaseShareHandler;
import com.bilibili.socialize.share.core.helper.AccessTokenKeeper;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;
import com.bilibili.socialize.share.core.shareparam.ShareImage;
import com.bilibili.socialize.share.core.shareparam.ShareParamAudio;
import com.bilibili.socialize.share.core.shareparam.ShareParamImage;
import com.bilibili.socialize.share.core.shareparam.ShareParamText;
import com.bilibili.socialize.share.core.shareparam.ShareParamVideo;
import com.bilibili.socialize.share.core.shareparam.ShareParamWebPage;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.WeiboAppManager;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAppInfo;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import java.io.File;
import java.util.Map;

/**
 * 支持所有的类型。
 *
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/9
 */
public class SinaShareHandler extends BaseShareHandler implements WbShareCallback {
    private static final String TAG = "BShare.sina.handler";
    public static final String DEFAULT_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String DEFAULT_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    public WbShareHandler mShareHandler = null;
    private static SsoHandler mSsoHandler;
    private WeiboMultiMessage mWeiboMessage;
    private String mAppKey;

    public SinaShareHandler(Activity context, BiliShareConfiguration configuration) {
        super(context, configuration);
    }

    @Override
    public void checkConfig() throws Exception {
        if (!TextUtils.isEmpty(mAppKey)) {
            return;
        }

        Map<String, String> appConfig = mShareConfiguration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA);
        if (appConfig == null || TextUtils.isEmpty(mAppKey = appConfig.get(SharePlatformConfig.APP_KEY))) {
            throw new ShareConfigException("Please set Sina platform dev info.");
        }
    }

    @Override
    public void init() throws Exception {
        if (mShareHandler == null) {
            Map<String, String> appConfig = mShareConfiguration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA);
            final AuthInfo mAuthInfo = new AuthInfo(getContext(), mAppKey,
                    appConfig.get(SharePlatformConfig.REDIRECT_URL), appConfig.get(SharePlatformConfig.SCOPE));
            WbSdk.install(getContext().getApplicationContext(), mAuthInfo);
            mShareHandler = new WbShareHandler((Activity) getContext());
            mShareHandler.registerApp();
        }
    }

    @Override
    public void onActivityNewIntent(Activity activity, Intent intent) {
        super.onActivityNewIntent(activity, intent);
        if (mShareHandler != null)
            try {
                Log.d(TAG, "doResultIntent when activity new intent");
                mShareHandler.doResultIntent(intent, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent
            data, SocializeListeners.ShareListener listener) {
        super.onActivityResult(activity, requestCode, resultCode, data, listener);
        if (mSsoHandler != null && TextUtils.isEmpty(getToken())) {
            Log.d(TAG, "authorizeCallBack when activity result");
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    /**
     * Contont cannot be empty
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareText(final ShareParamText params) throws ShareException {
        checkContent(params);

        final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = getTextObj(params);

        allInOneShare(weiboMessage);
    }

    /**
     * Contont and image cannot be empty or null
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareImage(final ShareParamImage params) throws ShareException {
        checkContent(params);
        checkImage(params.getImage());

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

                weiboMessage.textObject = getTextObj(params);
                weiboMessage.imageObject = getImageObj(params.getImage());

                allInOneShare(weiboMessage);
            }
        });
    }

    /**
     * Contont and image cannot be empty or null
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareWebPage(final ShareParamWebPage params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
                weiboMessage.textObject = getTextObj(params);

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                allInOneShare(weiboMessage);
            }
        });
    }

    @Override
    protected void shareAudio(final ShareParamAudio params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }
        if (params.getAudio() == null) {
            throw new InvalidParamException("Audio is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
                weiboMessage.textObject = getTextObj(params);

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                allInOneShare(weiboMessage);
            }
        });
    }

    @Override
    protected void shareVideo(final ShareParamVideo params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }
        if (params.getVideo() == null) {
            throw new InvalidParamException("Video is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
                weiboMessage.textObject = getTextObj(params);

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                allInOneShare(weiboMessage);
            }
        });
    }

    private void checkContent(BaseShareParam params) throws ShareException {
        if (TextUtils.isEmpty(params.getContent())) {
            throw new InvalidParamException("Content is empty or illegal");
        }
    }

    private void checkImage(ShareImage image) throws ShareException {
        if (image == null) {
            throw new InvalidParamException("Image cannot be null");
        }

        if (image.isLocalImage()) {
            if (TextUtils.isEmpty(image.getLocalPath()) || !new File(image.getLocalPath()).exists()) {
                throw new InvalidParamException("Image path is empty or illegal");
            }
        } else if (image.isNetImage()) {
            if (TextUtils.isEmpty(image.getNetImageUrl())) {
                throw new InvalidParamException("Image url is empty or illegal");
            }
        } else if (image.isResImage())
            throw new UnSupportedException("Unsupport image type");
        else if (image.isBitmapImage()) {
            if (image.getBitmap().isRecycled()) {
                throw new InvalidParamException("Cannot share recycled bitmap.");
            }
        } else
            throw new UnSupportedException("Invaild image");
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj(BaseShareParam params) {
        TextObject textObject = new TextObject();

        if (params != null) {
            textObject.title = params.getTitle();
            textObject.text = params.getContent();
            textObject.actionUrl = params.getTargetUrl();
            if (!TextUtils.isEmpty(textObject.actionUrl)) {
                textObject.text = String.format("%s %s", textObject.text, textObject.actionUrl);
            }
        }

        return textObject;
    }

    /**
     * 创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj(ShareImage image) {
        ImageObject imageObject = new ImageObject();

        if (image == null) {
            return imageObject;
        }

        if (image.isLocalImage()) {
            imageObject.imagePath = image.getLocalPath();
        } else {
            imageObject.imageData = mImageHelper.buildThumbData(image);
        }
        return imageObject;
    }

    private void allInOneShare(final WeiboMultiMessage weiboMessage) {
        final String token = getToken();
        if (TextUtils.isEmpty(token)) {
            mWeiboMessage = weiboMessage;
            Log.d(TAG, "authorize when allInOneShare");
            mSsoHandler = new SsoHandler((Activity) getContext());
            mSsoHandler.authorize(mAuthListener);
        } else {
            mWeiboMessage = null;
            mSsoHandler = null;
            doOnMainThread(new Runnable() {
                @Override
                public void run() {
                    postProgressStart();
                    Log.d(TAG, "share message when allInOneShare");
                    mShareHandler.shareMessage(weiboMessage, false);
                }
            });
        }
    }

    private WbAuthListener mAuthListener = new WbAuthListener() {

        @Override
        public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
            Log.d(TAG, "auth success");
            mSsoHandler = null;
            if (oauth2AccessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(getContext(), oauth2AccessToken);
                if (mWeiboMessage != null) {
                    allInOneShare(mWeiboMessage);
                }
                return;
            }

            SocializeListeners.ShareListener listener = getShareListener();
            if (listener == null) {
                return;
            }

            listener.onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_AUTH_FAILED, new ShareException("无效的token"));
        }

        @Override
        public void cancel() {
            Log.d(TAG, "auth cancel");
            if (getShareListener() != null) {
                getShareListener().onCancel(SocializeMedia.SINA);
            }
            mSsoHandler = null;
        }

        @Override
        public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
            Log.d(TAG, "auth failure");
            if (getShareListener() != null) {
                getShareListener().onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_AUTH_FAILED,
                        new Exception(wbConnectErrorMessage.getErrorMessage()));
            }
            mSsoHandler = null;
        }
    };

    @Override
    public void onWbShareSuccess() {
        Log.d(TAG, "share success");
        SocializeListeners.ShareListener listener = getShareListener();
        if (listener == null) {
            return;
        }
        listener.onSuccess(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SUCCESSED);
    }

    @Override
    public void onWbShareCancel() {
        Log.d(TAG, "share cancel");
        SocializeListeners.ShareListener listener = getShareListener();
        if (listener == null) {
            return;
        }
        listener.onCancel(SocializeMedia.SINA);
    }

    @Override
    public void onWbShareFail() {
        Log.d(TAG, "share fail");
        SocializeListeners.ShareListener listener = getShareListener();
        if (listener == null) {
            return;
        }
        listener.onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_SHARE_FAILED, new ShareException("unknown reason"));
    }

    private String getToken() {
        Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        String token = null;
        if (mAccessToken != null) {
            token = mAccessToken.getToken();
        }
        return token;
    }

    @Override
    public void release() {
        super.release();
        mSsoHandler = null;
        mShareHandler = null;
        mWeiboMessage = null;
        Log.d(TAG, "release");
    }

    public boolean isSinaClientInstalled() {
        WbAppInfo wbAppInfo = WeiboAppManager.getInstance(getContext().getApplicationContext()).getWbAppInfo();
        return wbAppInfo != null && wbAppInfo.isLegal();
    }

    @Override
    protected boolean isNeedActivityContext() {
        return true;
    }

    @Override
    public SocializeMedia getShareMedia() {
        return SocializeMedia.SINA;
    }

}

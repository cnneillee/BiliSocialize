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

package com.bilibili.socialize.share.core.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeListeners;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.helper.ShareImageHelper;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/8
 */
public abstract class AbsShareHandler implements IShareHandler {
    private static final String TAG = "BShare.handler.abs";

    protected Context mContext;

    protected BiliShareConfiguration mShareConfiguration;

    private SocializeListeners.ShareListener mShareListener;

    protected ShareImageHelper mImageHelper;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AbsShareHandler(Activity context, BiliShareConfiguration configuration) {
        initContext(context);
        mShareConfiguration = configuration;
        mImageHelper = new ShareImageHelper(mContext, configuration, mImageCallback);
    }

    private void initContext(Activity context) {
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

    @Override
    public boolean isDisposable() {
        return false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState, SocializeListeners.ShareListener listener) {
        initContext(activity);
        mShareListener = listener;
    }

    @Override
    public void onActivityNewIntent(Activity activity, Intent intent) {
        initContext(activity);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SocializeListeners.ShareListener listener) {
        initContext(activity);
        mShareListener = listener;
    }

    @Override
    public void onActivityDestroy() {
        release();
    }

    @Override
    public void release() {
        mShareListener = null;
        mContext = null;
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
    }

    @Override
    public void share(BaseShareParam params, SocializeListeners.ShareListener listener) throws Exception {
        mShareListener = listener;
    }

    protected void doOnWorkThread(final Runnable runnable) {
        mShareConfiguration.getTaskExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();

                    if (getShareListener() != null) {
                        doOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                getShareListener().onError(getShareMedia(), BiliShareStatusCode.ST_CODE_SHARE_ERROR_IMAGE, new ShareException("Share failed"));
                            }
                        });
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
                        if (getShareListener() != null) {
                            getShareListener().onProgress(getShareMedia(), msg);
                        }
                    }
                }
        );
    }

    protected SocializeListeners.ShareListener getShareListener() {
        return mShareListener;
    }

    public void setShareListener(SocializeListeners.ShareListener shareListener) {
        mShareListener = shareListener;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    private ShareImageHelper.Callback mImageCallback = new ShareImageHelper.Callback() {
        @Override
        public void onProgress(int msgId) {
            postProgress(msgId);
        }

        @Override
        public void onImageDownloadFailed() {
            if (getShareListener() != null) {
                getShareListener().onError(getShareMedia(), BiliShareStatusCode.ST_CODE_SHARE_ERROR_IMAGE, new ShareException("Image compress failed"));
            }
        }
    };
}

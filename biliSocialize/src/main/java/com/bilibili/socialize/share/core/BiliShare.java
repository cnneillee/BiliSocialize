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

package com.bilibili.socialize.share.core;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.handler.IShareHandler;
import com.bilibili.socialize.share.core.handler.ShareTransitHandler;
import com.bilibili.socialize.share.core.handler.generic.CopyShareHandler;
import com.bilibili.socialize.share.core.handler.generic.GenericShareHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/9/31
 */
public class BiliShare {
    private static final String TAG = "BShare.main.client";
    private static final Map<String, BiliShare> CLIENT_MAP = new HashMap<>();
    private static final String CLIENT_NAME_DEFAULT = "_share_client_name_inner_default_";

    private IShareHandler mCurrentHandler;
    private Map<SocializeMedia, IShareHandler> mHandlerMap = new HashMap<>();
    private BiliShareConfiguration mShareConfiguration;
    private SocializeListeners.ShareListener mOuterShareListener;

    private String mName;

    public static BiliShare get(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name can not be empty");
        }
        BiliShare router = CLIENT_MAP.get(name);
        if (router == null) {
            synchronized (CLIENT_MAP) {
                router = CLIENT_MAP.get(name);
                if (router == null) {
                    Log.d(TAG, String.format("create new share client named(%s)", name));
                    BiliShare value = new BiliShare(name);
                    CLIENT_MAP.put(name, value);
                    router = value;
                }
                return router;
            }
        } else {
            Log.d(TAG, String.format("find existed share client named(%s)", name));
        }
        return router;
    }

    public static BiliShare global() {
        return get(CLIENT_NAME_DEFAULT);
    }

    private BiliShare(String name) {
        mName = name;
    }

    /**
     * 分享之前必须先配置.
     *
     * @param configuration
     */
    public void config(BiliShareConfiguration configuration) {
        mShareConfiguration = configuration;
    }

    public void share(Activity activity, SocializeMedia type, BaseShareParam params, SocializeListeners.ShareListener listener) {
        if (mShareConfiguration == null) {
            throw new IllegalArgumentException("BiliShareConfiguration must be initialized before invoke share");
        }

        if (mCurrentHandler != null) {
            Log.w(TAG, "release leaked share handler");
            release(mCurrentHandler.getShareMedia());
        }

        mCurrentHandler = newHandler(activity, type, mShareConfiguration);

        if (mCurrentHandler != null) {
            try {
                mOuterShareListener = listener;

                if (params == null) {
                    Log.e(TAG, "null share params");
                    throw new IllegalArgumentException(("Share param cannot be null"));
                }

                mInnerProxyListener.onStart(type);
                mCurrentHandler.share(params, mInnerProxyListener);

                if (mCurrentHandler.isDisposable()) {
                    Log.d(TAG, "release disposable share handler");
                    release(mCurrentHandler.getShareMedia());
                }

            } catch (ShareException e) {
                Log.d(TAG, "share exception", e);
                mInnerProxyListener.onError(type, e.getCode(), e);
            } catch (Exception e) {
                Log.d(TAG, "share exception", e);
                mInnerProxyListener.onError(type, BiliShareStatusCode.ST_CODE_SHARE_ERROR_EXCEPTION, e);
            }
        } else {
            Log.e(TAG, "create handler failed");
            mInnerProxyListener.onError(type, BiliShareStatusCode.ST_CODE_SHARE_ERROR_UNEXPLAINED, new Exception("Unknown share type"));
        }
    }

    public IShareHandler currentHandler() {
        return mCurrentHandler;
    }

    private void release(SocializeMedia type) {
        Log.d(TAG, String.format("========》release client:(%s) 《========", type.name()));
        mOuterShareListener = null;
        if (mCurrentHandler != null) {
            mCurrentHandler.release();
        }
        mCurrentHandler = null;
        remove(type);
    }

    private IShareHandler newHandler(Activity context, SocializeMedia type, BiliShareConfiguration shareConfiguration) {
        IShareHandler handler = null;
        switch (type) {
            case WEIXIN:
            case WEIXIN_MONMENT:
            case QQ:
            case QZONE:
            case SINA:
                handler = new ShareTransitHandler(context, shareConfiguration, type, mName);
                break;

            case COPY:
                handler = new CopyShareHandler(context, shareConfiguration);
                break;

            default:
                handler = new GenericShareHandler(context, shareConfiguration);
        }

        Log.d(TAG, String.format("create handler type(%s)", handler.getClass().getSimpleName()));
        mHandlerMap.put(type, handler);

        return handler;
    }

    private void remove(SocializeMedia type) {
        mHandlerMap.remove(type);
    }

    private IShareHandler.InnerShareListener mInnerProxyListener = new IShareHandler.InnerShareListener() {

        @Override
        public void onStart(SocializeMedia type) {
            Log.d(TAG, String.format("start share:(%s)", type));
            if (mOuterShareListener != null) {
                mOuterShareListener.onStart(type);
            }
        }

        @Override
        public void onProgress(SocializeMedia type, String progressDesc) {
            Log.d(TAG, String.format("share on progress:(%s, %s)", type, progressDesc));
            if (mCurrentHandler != null && mCurrentHandler.getContext() != null) {
                Toast.makeText(mCurrentHandler.getContext(), progressDesc, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSuccess(SocializeMedia type, int code) {
            Log.d(TAG, "share success");
            if (mOuterShareListener != null) {
                mOuterShareListener.onSuccess(type, code);
            }
            release(type);
        }

        @Override
        public void onError(SocializeMedia type, int code, Throwable error) {
            Log.d(TAG, "share failed");
            if (mOuterShareListener != null) {
                mOuterShareListener.onError(type, code, error);
            }
            release(type);
        }

        @Override
        public void onCancel(SocializeMedia type) {
            Log.d(TAG, "share canceled");
            if (mOuterShareListener != null) {
                mOuterShareListener.onCancel(type);
            }
            release(type);
        }

    };

}

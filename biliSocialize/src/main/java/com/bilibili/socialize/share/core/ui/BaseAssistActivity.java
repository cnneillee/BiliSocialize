package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeListeners;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.handler.BaseShareHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2017/04/28
 */
public abstract class BaseAssistActivity<H extends BaseShareHandler> extends Activity
        implements SocializeListeners.ShareListener {
    public static final String KEY_PARAM = "share_param";
    public static final String KEY_CONFIG = "share_config";
    public static final String KEY_TYPE = "share_type";

    protected BiliShareConfiguration mShareConfig;
    protected BaseShareParam mShareParam;
    protected SocializeMedia mSocializeMedia;

    protected H mShareHandler;
    protected boolean mHasGetResult;

    protected abstract String tag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveParams();
        boolean bingo = true;
        bingo = checkConfigArgs();
        if (bingo) {
            bingo = checkSocializeArgs();
        }
        if (bingo) {
            mShareHandler = resolveHandler(mSocializeMedia, mShareConfig);
            if (mShareHandler == null) {
                bingo = false;
                String msg = String.format("media type is not correct:%s", mSocializeMedia);
                Log.w(tag(), msg);
                finishWithFailResult(msg);
            } else {
                bingo = true;
            }
        }
        if (bingo) {
            bingo = initHandler(savedInstanceState);
        }
        if (bingo) {
            startShare(savedInstanceState);
        }
    }

    protected void resolveParams() {
        Intent intent = getIntent();
        mShareConfig = intent.getParcelableExtra(KEY_CONFIG);
        mShareParam = intent.getParcelableExtra(KEY_PARAM);
        String type = intent.getStringExtra(KEY_TYPE);
        if (!TextUtils.isEmpty(type)) {
            mSocializeMedia = SocializeMedia.valueOf(type);
        }
    }

    protected boolean checkConfigArgs() {
        if (mShareConfig == null) {
            Log.e(tag(), "null share config");
            finishWithFailResult("null share config");
            return false;
        }
        return true;
    }

    protected boolean checkSocializeArgs() {
        if (mSocializeMedia == null) {
            Log.e(tag(), "null media type");
            finishWithFailResult("null media type");
            return false;
        }
        return true;
    }

    protected abstract H resolveHandler(SocializeMedia media, BiliShareConfiguration shareConfig);

    protected boolean initHandler(Bundle savedInstanceState) {
        try {
            mShareHandler.checkConfig();
            mShareHandler.init();
            Log.d(tag(), "share handler init success");
            mShareHandler.onActivityCreated(this, savedInstanceState, this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(tag(), String.format("share handler init failed: %s", e.getMessage()));
            finishWithFailResult("share handler init failed");
            return false;
        }
    }

    protected boolean startShare(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                if (mShareParam == null) {
                    Log.e(tag(), "null share params");
                    onError(mSocializeMedia, BiliShareStatusCode.ST_CODE_SHARE_ERROR_EXCEPTION,
                            new ShareException("share param error"));
                    return false;
                } else {
                    Log.d(tag(), "call share");
                    mShareHandler.share(mShareParam, this);
                }
            }
        } catch (Exception e) {
            onError(mSocializeMedia, BiliShareStatusCode.ST_CODE_SHARE_ERROR_EXCEPTION, e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void finishWithSuccessResult() {
        setResult(0, BiliShareDelegateActivity.createResult(BiliShareDelegateActivity.RESULT_SUCCESS));
        finish();
    }

    protected void finishWithFailResult(String msg) {
        setResult(0, BiliShareDelegateActivity.createResult(BiliShareDelegateActivity.RESULT_FAIL, msg));
        finish();
    }

    protected void finishWithCancelResult() {
        setResult(0, BiliShareDelegateActivity.createResult(BiliShareDelegateActivity.RESULT_CANCEL));
        finish();
    }

    @Override
    public void onStart(SocializeMedia type) {
        Log.d(tag(), "on inner share start");
        sendBroadcast(BiliShareDelegateActivity.createStartIntent());
    }

    @Override
    public void onProgress(SocializeMedia type, String progressDesc) {
        Log.d(tag(), "on inner share progress");
        sendBroadcast(BiliShareDelegateActivity.createProgressIntent(progressDesc));
    }

    @Override
    public void onSuccess(SocializeMedia type, int code) {
        Log.i(tag(), "----->on inner share success<-----");
        mHasGetResult = true;
        finishWithSuccessResult();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag(), "activity onDestroy");
    }

    protected void release() {
        if (mShareHandler != null) {
            mShareHandler.release();
        }
    }

}

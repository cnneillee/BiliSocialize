package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.handler.wx.BaseWxShareHandler;
import com.bilibili.socialize.share.core.handler.wx.WxChatShareHandler;
import com.bilibili.socialize.share.core.handler.wx.WxMomentShareHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * 微信分享后，如果停留在微信，再返回，就得不到回调，该Activity为了解决这个问题。
 *
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2017/04/27
 */
public class WxAssistActivity extends BaseAssistActivity<BaseWxShareHandler> {
    private static final String TAG = "BShare.wx.assist";
    public static final String ACTION_RESULT = "com.bilibili.share.wechat.result";

    public static final String BUNDLE_STATUS_CODE = "status_code";
    public static final String BUNDLE_STATUS_MSG = "status_msg";

    private boolean mIsFirstIntent;

    public static void start(Activity act, BaseShareParam params, BiliShareConfiguration configuration, SocializeMedia type, int reqCode) {
        Intent intent = new Intent(act, WxAssistActivity.class);
        intent.putExtra(KEY_PARAM, params);
        intent.putExtra(KEY_CONFIG, configuration);
        intent.putExtra(KEY_TYPE, type.name());
        act.startActivityForResult(intent, reqCode);
        act.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            IntentFilter filter = new IntentFilter(ACTION_RESULT);
            registerReceiver(mResultReceiver, filter);
            Log.d(TAG, "broadcast has register");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            mIsFirstIntent = true;
        }
    }

    @Override
    protected BaseWxShareHandler resolveHandler(SocializeMedia media, BiliShareConfiguration shareConfig) {
        if (media == SocializeMedia.WEIXIN) {
            Log.d(TAG, "create wx chat share handler");
            return new WxChatShareHandler(this, shareConfig);
        } else if (media == SocializeMedia.WEIXIN_MONMENT) {
            Log.d(TAG, "create wx moment share handler");
            return new WxMomentShareHandler(this, shareConfig);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, String.format("act resume: isFirst(%s),hasGetResult(%s)", mIsFirstIntent, mHasGetResult));
        if (mIsFirstIntent) {
            mIsFirstIntent = false;
            return;
        }
        if (mHasGetResult) {
            return;
        }

        Log.e(TAG, "gonna finish share with incorrect callback (cancel)");
        finishWithCancelResult();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        try {
            unregisterReceiver(mResultReceiver);
            Log.d(TAG, "broadcast has unregister");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            int code = intent.getIntExtra(BUNDLE_STATUS_CODE, -1);
            String msg = intent.getStringExtra(BUNDLE_STATUS_MSG);
            if (code == BiliShareStatusCode.ST_CODE_SUCCESSED) {
                Log.d(TAG, "get result from broadcast: success");
                finishWithSuccessResult();
            } else if (code == BiliShareStatusCode.ST_CODE_ERROR) {
                Log.d(TAG, "get result from broadcast: failed");
                finishWithFailResult(msg);
            } else if (code == BiliShareStatusCode.ST_CODE_ERROR_CANCEL) {
                Log.d(TAG, "get result from broadcast: cancel");
                finishWithCancelResult();
            }
        }
    };
}

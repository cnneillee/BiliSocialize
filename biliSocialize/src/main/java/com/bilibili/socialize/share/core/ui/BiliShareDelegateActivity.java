package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShare;
import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.handler.IShareHandler;
import com.bilibili.socialize.share.core.handler.ShareTransitHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * 为了解决多进程问题。
 *
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2017/05/04
 */
public class BiliShareDelegateActivity extends Activity {
    private static final String TAG = "BShare.delegate.act";
    private static final String KEY_PARAM = "share_param";
    private static final String KEY_CONFIG = "share_config";
    private static final String KEY_TYPE = "share_type";
    private static final String KEY_CLIENT_NAME = "client_name";

    private static final int REQ_CODE = 1024;
    public static final String REP_KEY_RESULT = "share_result";
    public static final String REP_KEY_EXTRA = "share_extra";
    public static final int RESULT_CANCEL = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 2;

    public static final String ACTION = "bilishare.delegate.assist.action";
    private static final String KEY_SHARE_ACTION = "share_action";
    private static final String KEY_SHARE_EXTRA = "share_extra";
    public static final int ACTION_START = 1;
    public static final int ACTION_PROGRESS = 2;

    private BaseShareParam mShareParam;
    private BiliShareConfiguration mShareConfig;
    private SocializeMedia mSocializeMedia;
    private String mClientName;

    public static void start(Activity act, BaseShareParam params, BiliShareConfiguration configuration, SocializeMedia type, String clientName) {
        Intent intent = new Intent(act, BiliShareDelegateActivity.class);
        intent.putExtra(KEY_PARAM, params);
        intent.putExtra(KEY_CONFIG, configuration);
        intent.putExtra(KEY_TYPE, type.name());
        intent.putExtra(KEY_CLIENT_NAME, clientName);
        act.startActivity(intent);
        act.overridePendingTransition(0, 0);
    }

    public static Intent createResult(int result) {
        return createResult(result, null);
    }

    public static Intent createResult(int result, String extra) {
        Intent intent = new Intent();
        intent.putExtra(REP_KEY_RESULT, result);
        intent.putExtra(REP_KEY_EXTRA, extra);
        return intent;
    }

    public static Intent createStartIntent() {
        Intent intent = new Intent(ACTION);
        intent.putExtra(KEY_SHARE_ACTION, ACTION_START);
        return intent;
    }

    public static Intent createProgressIntent(String extra) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(KEY_SHARE_ACTION, ACTION_PROGRESS);
        intent.putExtra(KEY_SHARE_EXTRA, extra);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveParams();
        if (mSocializeMedia == null) {
            Log.d(TAG, "finish due to null socialize media");
            finishWithCancelResult();
            return;
        }

        if (savedInstanceState == null) {
            switch (mSocializeMedia) {
                case SINA:
                    Log.d(TAG, "gonna start sina assist act");
                    SinaAssistActivity.start(this, mShareParam, mShareConfig, REQ_CODE);
                    break;

                case WEIXIN:
                case WEIXIN_MONMENT:
                    Log.d(TAG, "gonna start wx assist act");
                    WxAssistActivity.start(this, mShareParam, mShareConfig, mSocializeMedia, REQ_CODE);
                    break;

                case QQ:
                case QZONE:
                    Log.d(TAG, "gonna start qq assist act");
                    QQAssistActivity.start(this, mShareParam, mShareConfig, mSocializeMedia, REQ_CODE);
                    break;

                default: {
                    finishWithCancelResult();
                    return;
                }
            }
        }

        try {
            IntentFilter filter = new IntentFilter(ACTION);
            registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resolveParams() {
        Intent intent = getIntent();
        mShareParam = intent.getParcelableExtra(KEY_PARAM);
        mShareConfig = intent.getParcelableExtra(KEY_CONFIG);
        String type = intent.getStringExtra(KEY_TYPE);
        mClientName = intent.getStringExtra(KEY_CLIENT_NAME);
        if (!TextUtils.isEmpty(type)) {
            mSocializeMedia = SocializeMedia.valueOf(type);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && data != null) {
            int result = data.getIntExtra(REP_KEY_RESULT, RESULT_CANCEL);
            if (result == RESULT_SUCCESS) {
                Log.d(TAG, "act result: success");
                finishWithSuccessResult();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishWithSuccessResult() {
        onSuccess(mSocializeMedia, BiliShareStatusCode.ST_CODE_SUCCESSED);
    }

    private void finishWithFailResult(String msg) {
        onError(mSocializeMedia, BiliShareStatusCode.ST_CODE_ERROR, new ShareException(msg));
    }

    private void finishWithCancelResult() {
        onCancel(mSocializeMedia);
    }

    private void onStart(SocializeMedia type) {
        Log.d(TAG, "on inner share start");
        ShareTransitHandler handler = getShareHandler();
        if (handler != null) {
            handler.onStart(type);
        }
    }

    private void onProgress(SocializeMedia type, String progressDesc) {
        Log.d(TAG, "on inner share progress");
        ShareTransitHandler handler = getShareHandler();
        if (handler != null) {
            handler.onProgress(type, progressDesc);
        }
    }

    private void onSuccess(SocializeMedia type, int code) {
        Log.i(TAG, "----->on inner share success<-----");
        ShareTransitHandler handler = getShareHandler();
        if (handler != null) {
            handler.onSuccess(type, code);
        }
        finish();
    }

    private void onError(SocializeMedia type, int code, Throwable error) {
        Log.i(TAG, "----->on inner share fail<-----");
        ShareTransitHandler handler = getShareHandler();
        if (handler != null) {
            handler.onError(type, code, error);
        }
        finish();
    }

    private void onCancel(SocializeMedia type) {
        Log.i(TAG, "----->on inner share cancel<-----");
        ShareTransitHandler handler = getShareHandler();
        if (handler != null) {
            handler.onCancel(type);
        }
        finish();
    }

    private ShareTransitHandler getShareHandler() {
        if (TextUtils.isEmpty(mClientName)) {
            Log.e(TAG, "null client name");
            return null;
        }

        BiliShare share = BiliShare.get(mClientName);
        IShareHandler handler = share.currentHandler();
        if (handler == null) {
            Log.e(TAG, "null handler");
            return null;
        }
        if (!(handler instanceof ShareTransitHandler)) {
            Log.e(TAG, "wrong handler type");
            return null;
        }

        return (ShareTransitHandler) handler;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            int action = intent.getIntExtra(KEY_SHARE_ACTION, 0);
            if (action == ACTION_START) {
                onStart(mSocializeMedia);
            } else if (action == ACTION_PROGRESS) {
                String msg = intent.getStringExtra(KEY_SHARE_EXTRA);
                onProgress(mSocializeMedia, msg);
            }
        }
    };
}

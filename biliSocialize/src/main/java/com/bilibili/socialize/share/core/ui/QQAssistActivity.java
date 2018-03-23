package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.handler.qq.BaseQQShareHandler;
import com.bilibili.socialize.share.core.handler.qq.QQChatShareHandler;
import com.bilibili.socialize.share.core.handler.qq.QQZoneShareHandler;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2017/04/28
 */
public class QQAssistActivity extends BaseAssistActivity<BaseQQShareHandler> {
    private static final String TAG = "BShare.qq.assist";

    private boolean mIsFirstIntent;

    public static void start(Activity act, BaseShareParam params, BiliShareConfiguration configuration, SocializeMedia type, int reqCode) {
        Intent intent = new Intent(act, QQAssistActivity.class);
        intent.putExtra(KEY_PARAM, params);
        intent.putExtra(KEY_CONFIG, configuration);
        intent.putExtra(KEY_TYPE, type.name());
        act.startActivityForResult(intent, reqCode);
        act.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mIsFirstIntent = true;
        }
    }

    @Override
    protected BaseQQShareHandler resolveHandler(SocializeMedia media, BiliShareConfiguration shareConfig) {
        if (media == SocializeMedia.QQ) {
            Log.d(TAG, "create qq chat share handler");
            return new QQChatShareHandler(this, shareConfig);
        } else if (media == SocializeMedia.QZONE) {
            Log.d(TAG, "create qq zone share handler");
            return new QQZoneShareHandler(this, shareConfig);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "activity onResult");
        if (mShareHandler != null) {
            mShareHandler.onActivityResult(this, requestCode, resultCode, data, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    protected String tag() {
        return TAG;
    }

}

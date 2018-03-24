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
import android.util.Log;

import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.SocializeMedia;
import com.bilibili.socialize.login.core.handler.sina.SinaLoginHandler;

/**
 * @author NeilLee
 * @since 2018/3/23 21:42
 */

public class SinaLoginAssistActivity extends BaseAssistActivity<SinaLoginHandler> {

    private static final String TAG = "BLogin.sina.assist";

    @Override
    protected String tag() {
        return TAG;
    }

    public static void start(Activity activity, LoginConfiguration config, int reqCode) {
        Intent intent = new Intent(activity, SinaLoginAssistActivity.class);
        intent.putExtra(KEY_CONFIG, config);
        intent.putExtra(KEY_TYPE, SocializeMedia.SINA.name());
        activity.startActivityForResult(intent, reqCode);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected SinaLoginHandler resolveHandler(SocializeMedia media, LoginConfiguration loginConfig) {
        if (media.equals(SocializeMedia.SINA)) {
            return new SinaLoginHandler(this, loginConfig);
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "activity onNewIntent");
        mLoginHandler.onActivityNewIntent(this, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(tag(), String.format("activity onResult: resultCode(%d), cancelled(%s)",
                resultCode, resultCode == Activity.RESULT_CANCELED));
        mLoginHandler.onActivityResult(this, requestCode, resultCode, data, this);
    }
}

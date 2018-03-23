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

package cn.neillee.bilisocialize;

import android.content.Context;
import android.widget.Toast;

import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;

/**
 * @author NeilLee
 * @since 2018/3/19 16:42
 */

public class SimpleLoginListener implements SocializeListeners.LoginListener {
    private Context mContext;

    public SimpleLoginListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onStart(SocializeMedia type) {

    }

    @Override
    public void onProgress(SocializeMedia type, String progressDesc) {

    }

    @Override
    public void onSuccess(SocializeMedia type, int code, String tokens) {
        Toast.makeText(mContext, R.string.login_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(SocializeMedia type, int code, Throwable error) {
        Toast.makeText(mContext, R.string.login_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(SocializeMedia type) {
        Toast.makeText(mContext, R.string.login_cancel, Toast.LENGTH_SHORT).show();
    }
}

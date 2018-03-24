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

package cn.neillee.bilisocialize.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bilibili.socialize.login.core.BiliLogin;
import com.bilibili.socialize.login.core.LoginConfiguration;
import com.bilibili.socialize.login.core.LoginPlatformConfig;
import com.bilibili.socialize.login.core.SocializeMedia;

import cn.neillee.bilisocialize.Constant;
import cn.neillee.bilisocialize.R;
import cn.neillee.bilisocialize.SimpleLoginListener;
import cn.neillee.bilisocialize.utils.StringUtils;

/**
 * @author NeilLee
 * @since 2018/1/31 14:06
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTVLogInfo;
    private BiliLogin mBiliLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        initViews();
        initBiliLogin();
    }

    private void initBiliLogin() {
        LoginConfiguration configuration =
                new LoginConfiguration
                        .Builder()
                        .qq(Constant.QQ_APP_ID)
                        .wx(Constant.WECHAT_APP_ID)
                        .sina(Constant.SINA_APP_ID)
                        .build();
        configuration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA).put(LoginPlatformConfig.SCOPE, Constant.SINA_SCOPE);
        configuration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA).put(LoginPlatformConfig.REDIRECT_URL, Constant.SINA_REDIRECT_URL);
        mBiliLogin = BiliLogin.global();
        mBiliLogin.config(configuration);
    }

    private void initViews() {
        findViewById(R.id.sign_qq).setOnClickListener(this);
        findViewById(R.id.sign_wechat).setOnClickListener(this);
        findViewById(R.id.sign_sina).setOnClickListener(this);
        mTVLogInfo = findViewById(R.id.tv_log_info);
        syncSignedInfo(getString(R.string.dummy_text));
    }

    @Override
    public void onClick(View v) {
        SocializeMedia media;
        switch (v.getId()) {
            case R.id.sign_qq:
                media = SocializeMedia.QQ;
                break;
            case R.id.sign_wechat:
                media = SocializeMedia.WEIXIN;
                break;
            case R.id.sign_sina:
                media = SocializeMedia.SINA;
                break;
            default:
                return;
        }
        mBiliLogin.login(this, media, new SimpleLoginListener(getBaseContext()) {
            @Override
            public void onSuccess(SocializeMedia type, int code, String tokens) {
                syncSignedInfo(tokens);
            }
        });
    }

    private void syncSignedInfo(String text) {
        mTVLogInfo.setText(StringUtils.stringToJSON(text));
    }
}

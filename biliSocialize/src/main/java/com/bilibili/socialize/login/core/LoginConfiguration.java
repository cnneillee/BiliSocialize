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

package com.bilibili.socialize.login.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author NeilLee
 * @since 2018/1/29 18:22.
 */

public class LoginConfiguration implements Parcelable {

    private LoginPlatformConfig mPlatformConfig;
    private Executor mTaskExecutor;

    private LoginConfiguration(Builder builder) {
        mTaskExecutor = Executors.newCachedThreadPool();
        mPlatformConfig = builder.mPlatformConfig;
    }

    protected LoginConfiguration(Parcel in) {
        this.mTaskExecutor = Executors.newCachedThreadPool();
        this.mPlatformConfig = in.readParcelable(LoginConfiguration.class.getClassLoader());
    }

    public static final Creator<LoginConfiguration> CREATOR = new Creator<LoginConfiguration>() {
        @Override
        public LoginConfiguration createFromParcel(Parcel in) {
            return new LoginConfiguration(in);
        }

        @Override
        public LoginConfiguration[] newArray(int size) {
            return new LoginConfiguration[size];
        }
    };

    public LoginPlatformConfig getPlatformConfig() {
        return mPlatformConfig;
    }

    public Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPlatformConfig, 0);
    }

    public static class Builder {
        LoginPlatformConfig mPlatformConfig = new LoginPlatformConfig();

        public Builder qq(String appId) {
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.QQ, LoginPlatformConfig.APP_ID, appId);
            return this;
        }

        public Builder wx(String appId) {
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.WEIXIN, LoginPlatformConfig.APP_ID, appId);
            return this;
        }

        public Builder sina(String appKey) {
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.SINA, LoginPlatformConfig.APP_KEY, appKey);
            return this;
        }

        public LoginConfiguration build() {
            checkFields();
            return new LoginConfiguration(this);
        }

        private void checkFields() {

        }
    }
}

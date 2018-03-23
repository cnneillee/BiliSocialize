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

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bilibili.socialize.share.R;
import com.bilibili.socialize.share.core.handler.sina.SinaShareHandler;
import com.bilibili.socialize.share.download.DefaultImageDownloader;
import com.bilibili.socialize.share.download.IImageDownloader;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/8
 */
public class BiliShareConfiguration implements Parcelable {

    private String mImageCachePath;
    private int mDefaultShareImage;

    private IImageDownloader mImageDownloader;
    private Executor mTaskExecutor;
    private SharePlatformConfig mPlatformConfig;

    private BiliShareConfiguration(Builder builder) {
        mImageCachePath = builder.mImageCachePath;
        mDefaultShareImage = builder.mDefaultShareImage;
        mImageDownloader = builder.mImageLoader;
        mTaskExecutor = Executors.newCachedThreadPool();
        mPlatformConfig = builder.mPlatformConfig;
    }

    public String getImageCachePath(Context context) {
        if (TextUtils.isEmpty(mImageCachePath)) {
            mImageCachePath = Builder.getDefaultImageCacheFile(context.getApplicationContext());
        }
        return mImageCachePath;
    }

    public int getDefaultShareImage() {
        return mDefaultShareImage;
    }

    public IImageDownloader getImageDownloader() {
        return mImageDownloader;
    }

    public Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    public SharePlatformConfig getPlatformConfig() {
        return mPlatformConfig;
    }

    public static class Builder {
        public static final String IMAGE_CACHE_FILE_NAME = "shareImage";

        private Context mContext;
        private String mImageCachePath;
        private int mDefaultShareImage = -1;

        private IImageDownloader mImageLoader;
        private SharePlatformConfig mPlatformConfig = new SharePlatformConfig();

        public Builder(Context context) {
            mContext = context.getApplicationContext();
        }

        public Builder imageCachePath(String path) {
            mImageCachePath = path;
            return this;
        }

        public Builder defaultShareImage(int defaultImage) {
            mDefaultShareImage = defaultImage;
            return this;
        }

        public Builder sina(String appKey) {
            return sina(appKey, null, null);
        }

        public Builder sina(String appKey, String redirectUrl, String scope) {
            if (TextUtils.isEmpty(redirectUrl)) {
                redirectUrl = SinaShareHandler.DEFAULT_REDIRECT_URL;
            }
            if (TextUtils.isEmpty(scope)) {
                scope = SinaShareHandler.DEFAULT_SCOPE;
            }
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.SINA, SharePlatformConfig.APP_KEY, appKey
                    , SharePlatformConfig.REDIRECT_URL, redirectUrl
                    , SharePlatformConfig.SCOPE, scope);
            return this;
        }

        public Builder imageDownloader(IImageDownloader loader) {
            mImageLoader = loader;
            return this;
        }

        public Builder qq(String appId) {
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.QQ, SharePlatformConfig.APP_ID, appId);
            return this;
        }

        public Builder weixin(String appId) {
            mPlatformConfig.addPlatformDevInfo(SocializeMedia.WEIXIN, SharePlatformConfig.APP_ID, appId);
            return this;
        }

        public BiliShareConfiguration build() {
            checkFields();
            return new BiliShareConfiguration(this);
        }

        private void checkFields() {
            File imageCacheFile = null;
            if (!TextUtils.isEmpty(mImageCachePath)) {
                imageCacheFile = new File(mImageCachePath);
                if (!imageCacheFile.isDirectory()) {
                    imageCacheFile = null;
                } else if (!imageCacheFile.exists() && !imageCacheFile.mkdirs()) {
                    imageCacheFile = null;
                }
            }
            if (imageCacheFile == null) {
                mImageCachePath = getDefaultImageCacheFile(mContext);
            }

            if (mImageLoader == null) {
                mImageLoader = new DefaultImageDownloader();
            }

            if (mDefaultShareImage == -1) {
                mDefaultShareImage = R.drawable.default_share_image;
            }
        }

        private static String getDefaultImageCacheFile(Context context) {
            String imageCachePath = null;
            File extCacheFile = context.getExternalCacheDir();
            if (extCacheFile == null) {
                extCacheFile = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            }
            if (extCacheFile != null) {
                imageCachePath = extCacheFile.getAbsolutePath() + File.separator + IMAGE_CACHE_FILE_NAME + File.separator;
                File imageCacheFile = new File(imageCachePath);
                imageCacheFile.mkdirs();
            }
            return imageCachePath;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mImageCachePath);
        dest.writeInt(this.mDefaultShareImage);
        dest.writeParcelable(mPlatformConfig, 0);
    }

    protected BiliShareConfiguration(Parcel in) {
        this.mImageCachePath = in.readString();
        this.mDefaultShareImage = in.readInt();
        this.mPlatformConfig = in.readParcelable(SharePlatformConfig.class.getClassLoader());
        this.mImageDownloader = new DefaultImageDownloader();
        this.mTaskExecutor = Executors.newCachedThreadPool();
    }

    public static final Parcelable.Creator<BiliShareConfiguration> CREATOR = new Parcelable.Creator<BiliShareConfiguration>() {
        public BiliShareConfiguration createFromParcel(Parcel source) {
            return new BiliShareConfiguration(source);
        }

        public BiliShareConfiguration[] newArray(int size) {
            return new BiliShareConfiguration[size];
        }
    };
}

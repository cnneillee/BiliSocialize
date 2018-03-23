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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/9/31 17:51
 */
public class SharePlatformConfig implements Parcelable {

    public static final String APP_ID = "app_id";
    public static final String APP_KEY = "app_key";
    public static final String REDIRECT_URL = "redirect_url";
    public static final String SCOPE = "scope";

    private SparseArray<Map<String, String>> mConfig = new SparseArray<>();

    public boolean hasAlreadyConfig() {
        return mConfig.size() > 0;
    }

    private void addPlatformDevInfo(SocializeMedia media, HashMap<String, String> value) {
        mConfig.put(media.ordinal(), value);
    }

    public void addPlatformDevInfo(SocializeMedia media, String... appInfo) {
        if (appInfo == null || appInfo.length % 2 != 0) {
            throw new RuntimeException("Please check your share app config info");
        }

        HashMap<String, String> infoMap = new HashMap<>();
        int length = appInfo.length / 2;
        for (int i = 0; i < length; i++) {
            infoMap.put(appInfo[i * 2], appInfo[i * 2 + 1]);
        }
        addPlatformDevInfo(media, infoMap);
    }

    public Map<String, String> getPlatformDevInfo(SocializeMedia media) {
        return mConfig.get(media.ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int size = mConfig.size();
        dest.writeInt(size);
        int index = 0;
        do {
            int key = mConfig.keyAt(index);
            dest.writeInt(key);

            Map<String, String> value = mConfig.get(key);
            int subSize = value.size();
            dest.writeInt(subSize);
            Set<String> keySet = value.keySet();
            for (String subKey : keySet) {
                dest.writeString(subKey);
                dest.writeString(value.get(subKey));
            }
            index++;
        } while (index < size);
    }

    public SharePlatformConfig() {
    }

    protected SharePlatformConfig(Parcel in) {
        int size = in.readInt();
        int index = 0;
        do {
            int key = in.readInt();
            int subSize = in.readInt();
            Map<String, String> value = new HashMap<>();
            for (int subIndex = 0; subIndex < subSize; subIndex++) {
                value.put(in.readString(), in.readString());
            }
            mConfig.put(key, value);
            index++;
        } while (index < size);
    }

    public static final Parcelable.Creator<SharePlatformConfig> CREATOR = new Parcelable.Creator<SharePlatformConfig>() {
        @Override
        public SharePlatformConfig createFromParcel(Parcel source) {
            return new SharePlatformConfig(source);
        }

        @Override
        public SharePlatformConfig[] newArray(int size) {
            return new SharePlatformConfig[size];
        }
    };
}

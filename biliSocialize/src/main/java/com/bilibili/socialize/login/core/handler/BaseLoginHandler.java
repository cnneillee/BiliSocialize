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

package com.bilibili.socialize.login.core.handler;

import android.app.Activity;

import com.bilibili.socialize.login.core.LoginConfiguration;

/**
 * @author NeilLee
 * @since 2018/1/29 18:10
 */

public abstract class BaseLoginHandler extends AbsLoginHandler {
    public BaseLoginHandler(Activity context, LoginConfiguration configuration) {
        super(context, configuration);
    }

    public abstract void init() throws Exception;

    /**
     * 检查配置，比如appKey，appSecret
     */
    public abstract void checkConfig() throws Exception;
}

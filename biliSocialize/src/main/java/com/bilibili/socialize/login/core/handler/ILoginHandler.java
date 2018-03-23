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

import com.bilibili.socialize.login.core.IActivityLifecycleMirror;
import com.bilibili.socialize.login.core.SocializeListeners;
import com.bilibili.socialize.login.core.SocializeMedia;

/**
 * @author NeilLee
 * @since 2018/1/29 17:51
 */

public interface ILoginHandler extends IActivityLifecycleMirror {

    /**
     * 是否是一次性的LoginHandler.
     * GENERIC/COPY这种分享方式，不需要或者无法得知第三方app的分享结果，用此方法来标记。
     *
     * @return 如果为true, 则调用login()后就release();
     */
    boolean isDisposable();

    void release();

    void login(SocializeListeners.LoginListener listener) throws Exception;

    SocializeMedia getMediaType();

    interface InnerLoginListener extends SocializeListeners.LoginListener {
        void onProgress(SocializeMedia type, String progressDesc);
    }
}

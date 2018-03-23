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

package com.bilibili.socialize.share.core.handler;

import android.content.Context;

import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.SocializeListeners;
import com.bilibili.socialize.share.core.IActivityLifecycleMirror;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/9/31 18:21
 */
public interface IShareHandler extends IActivityLifecycleMirror {

    void share(BaseShareParam params, SocializeListeners.ShareListener listener) throws Exception;

    void release();

    Context getContext();

    SocializeMedia getShareMedia();

    /**
     * 是否是一次性的ShareHandler.
     * GENERIC/COPY这种分享方式，不需要或者无法得知第三方app的分享结果，用此方法来标记。
     *
     * @return 如果为true, 则调用share()后就release()b;
     */
    boolean isDisposable();

    interface InnerShareListener extends SocializeListeners.ShareListener {

        void onProgress(SocializeMedia type, String progressDesc);

    }
}

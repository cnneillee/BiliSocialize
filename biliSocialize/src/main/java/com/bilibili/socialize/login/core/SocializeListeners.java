package com.bilibili.socialize.login.core;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_ERROR;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_ERROR_CANCEL;
import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_SUCCESS;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/9/31
 */
public abstract class SocializeListeners {

    private SocializeListeners() {
    }

    public interface LoginListener {

        void onStart(SocializeMedia type);

        void onProgress(SocializeMedia type, String progressDesc);

        void onSuccess(SocializeMedia type, int code, String tokens);

        void onError(SocializeMedia type, int code, Throwable error);

        void onCancel(SocializeMedia type);

    }

    public abstract class LoginListenerAdapter implements LoginListener {

        public void onStart(SocializeMedia type) {

        }

        protected abstract void onComplete(SocializeMedia type, int code, Throwable error);

        @Override
        public void onProgress(SocializeMedia type, String progressDesc) {

        }

        public final void onSuccess(SocializeMedia type, int code) {
            onComplete(type, ST_CODE_SUCCESS, null);
        }

        public final void onError(SocializeMedia type, int code, Throwable error) {
            onComplete(type, ST_CODE_ERROR, error);
        }

        public final void onCancel(SocializeMedia type) {
            onComplete(type, ST_CODE_ERROR_CANCEL, null);
        }
    }
}
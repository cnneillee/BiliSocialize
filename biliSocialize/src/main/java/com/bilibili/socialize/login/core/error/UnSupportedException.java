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

package com.bilibili.socialize.login.core.error;

import static com.bilibili.socialize.login.core.error.BiliLoginStatusCode.ST_CODE_LOGIN_ERROR_PARAM_UNSUPPORTED;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/9
 */
public class UnSupportedException extends LoginException {

    public UnSupportedException(String detailMessage) {
        super(detailMessage);
        setCode(ST_CODE_LOGIN_ERROR_PARAM_UNSUPPORTED);
    }
}

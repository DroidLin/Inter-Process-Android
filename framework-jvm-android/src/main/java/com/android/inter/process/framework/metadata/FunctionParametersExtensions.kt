package com.android.inter.process.framework.metadata

import com.android.inter.process.framework.AndroidRequest
import com.android.inter.process.framework.AndroidResponse

/**
 * @author: liuzhongao
 * @since: 2024/9/15 13:07
 */

private const val KEY_REQUEST = "key_request"
private const val KEY_RESPONSE = "key_response"

internal var FunctionParameters.request: AndroidRequest?
    set(value) {
        this[KEY_REQUEST] = value
    }
    get() = this[KEY_REQUEST]

internal var FunctionParameters.response: AndroidResponse?
    set(value) {
        this[KEY_RESPONSE] = value
    }
    get() = this[KEY_RESPONSE]
package com.android.inter.process.compiler.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

internal val RandomHash: String
    get() {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(UUID.randomUUID().toString().encodeToByteArray())
        return BigInteger(1, messageDigest.digest()).toString(16)
    }

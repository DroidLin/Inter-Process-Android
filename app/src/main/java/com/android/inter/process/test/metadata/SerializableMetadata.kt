package com.android.inter.process.test.metadata

import java.io.Serializable

data class SerializableMetadata(
    val data: String,
    val longNumber: Long,
    val intNumber: Int,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -8958686386397138624L

        val Default = SerializableMetadata("empty", 11L, -11)
    }
}
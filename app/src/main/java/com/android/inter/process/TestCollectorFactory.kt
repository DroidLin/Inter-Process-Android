package com.android.inter.process

import com.android.inter.process.framework.ObjectPool
import com.android.inter.process.framework.annotation.CustomCollector

@CustomCollector
class TestCollectorFactory : ObjectPool.Collector {
    override fun collect() {
    }
}
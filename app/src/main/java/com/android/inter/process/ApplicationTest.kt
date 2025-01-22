package com.android.inter.process

import com.android.inter.process.framework.ServiceFactory
import com.android.inter.process.framework.annotation.IPCInterface
import com.android.inter.process.framework.annotation.IPCServiceFactory

@IPCInterface
interface ApplicationTest {
}

@IPCServiceFactory(interfaceClazz = ApplicationTest::class)
class ApplicationTestFactory : ServiceFactory<ApplicationTest> {
    override fun serviceCreate(): ApplicationTest {
        return ApplicationTestImpl()
    }
}

private class ApplicationTestImpl : ApplicationTest {

}
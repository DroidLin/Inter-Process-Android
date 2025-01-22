package com.android.inter.process

import com.android.inter.process.framework.ServiceFactory
import com.android.inter.process.framework.annotation.IPCService
import com.android.inter.process.framework.annotation.IPCServiceFactory

@IPCService
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
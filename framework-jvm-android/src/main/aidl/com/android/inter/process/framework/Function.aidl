// Function.aidl
package com.android.inter.process.framework;

import com.android.inter.process.framework.metadata.FunctionParameters;

// Declare any non-default types here with import statements

interface Function {

    void call(inout FunctionParameters functionParameters);
}
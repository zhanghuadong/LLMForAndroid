// ILLMAidlInterface.aidl
package com.htfyun.eink.zhd.llm;

// Declare any non-default types here with import statements

import com.htfyun.eink.zhd.llm.ILlmCallback;



interface ILLMAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int nativeInit(String modelPath,int maxNewTokens,int maxContextLen);
    int nativeInitReady();
    int nativeGenerative(String prompt, ILlmCallback callback);
    int nativeClearCache();
    void nativeDestroy();
    int nativeAbort();
    int nativeIsRunning();
    int nativeReleasePromptCache();

}
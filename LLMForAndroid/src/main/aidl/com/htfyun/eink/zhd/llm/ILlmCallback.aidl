// ILlmCallback.aidl
package com.htfyun.eink.zhd.llm;

// Declare any non-default types here with import statements

interface ILlmCallback {
        void onToken(String token);
        void onFinish();
        void onError(String message);
}
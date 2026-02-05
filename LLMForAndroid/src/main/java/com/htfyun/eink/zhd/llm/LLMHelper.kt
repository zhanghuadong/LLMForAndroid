package com.htfyun.eink.zhd.llm

object LLMHelper {

    init {
        System.loadLibrary("rkllmrt")
        System.loadLibrary("llm_for_android")
    }

    /** 初始化 LLM；返回 0 成功 */
    external fun nativeInit(
        modelPath: String,
        maxNewTokens: Int,
        maxContextLen: Int
    ): Int

    /** 释放模型 */
    external fun nativeDestroy()

    /** 清 KV 缓存 */
    external fun nativeClearCache(): Int

    /** 推理；callback 可为 null，不为 null 时流式回调 onToken/onFinish/onError */
    external fun nativeGenerative(prompt: String, callback: LlmCallback?): Int

    external fun nativeAbort(): Int

    external fun nativeIsRunning(): Int

    external fun nativeReleasePromptCache(): Int


    interface LlmCallback {
        fun onToken(token: String?)
        fun onFinish()
        fun onError()
    }
}
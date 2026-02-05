package com.htfyun.eink.zhd.llm

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import android.os.RemoteCallbackList
import android.os.RemoteException
/**
 *
 * author: zhd
 * date: 2026/2/2
 * desc: 给第三方调用的服务
 */
class LLMService: Service(){

    companion object{
        const val TAG="LLMService"
        private const val STATUS_IDLE = 0
        private const val STATUS_INITIALIZING = 1
        private const val STATUS_READY = 2
        private const val STATUS_PROCESSING = 3
        private const val STATUS_ERROR = 4
        private const val STATUS_FINISH = 5
    }

    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    // 状态管理
    private val currentStatus = AtomicInteger(STATUS_IDLE)
    private val statusMessage = AtomicReference("服务启动中")

    // 回调列表
    private val callbackList = RemoteCallbackList<ILlmCallback>()

    // LLM助手（假设存在）
    private val llmHelper = AtomicReference<Any?>(null)

    // 任务管理
    private val taskCounter = AtomicInteger(0)
    private val activeTasks = ConcurrentHashMap<Int, Job>()

    //默认加载的模型路径
    //private val Qwen3="${Environment.getExternalStorageDirectory().path}/Qwen3-1.7B-rk3576-w4a16_g128.rkllm"
    private var defaultModelPath="${Environment.getExternalStorageDirectory().path}/DeepSeek-R1-Distill-Qwen-1.5B_W4A16_RK3576.rkllm"

    private var  retReady=-1

    // AIDL接口实现
    val mBinder = object : ILLMAidlInterface.Stub() {
        override fun nativeInit(
            modelPath: String,
            maxNewTokens: Int,
            maxContextLen: Int
        ): Int {
            //retReady = LLMHelper.nativeInit(modelPath, maxNewTokens, maxContextLen)
            initializeService(modelPath, maxNewTokens, maxContextLen)
            return retReady
        }

        override fun nativeInitReady(): Int {
            return retReady
        }

        @Throws(RemoteException::class)
        override fun nativeGenerative(
            prompt: String,
            callback: ILlmCallback?
        ): Int {
            val taskId = taskCounter.incrementAndGet()
            Log.d(TAG, "开始生成任务, ID: $taskId, prompt: $prompt status=${currentStatus.get()}")

            // 检查状态
            if (currentStatus.get() != STATUS_READY) {
                Log.d(TAG, "服务未就绪，当前状态: ${statusMessage.get()}")
                callback?.onError("服务未就绪，当前状态: ${statusMessage.get()}")
                return -1
            }

            // 启动协程任务
            val job = serviceScope.launch {
                try {
                    // 通知开始
                    //LLM处理
                    val result = LLMHelper.nativeGenerative(prompt,object: LLMHelper.LlmCallback{
                        override fun onToken(token: String?) {
                            //返回结果
                            callback?.onToken(token)
                        }

                        override fun onFinish() {
                            Log.d(TAG, "生成任务完成, ID: $taskId")
                            callback?.onFinish()
                        }

                        override fun onError() {
                            callback?.onError("未知原因")
                        }
                    } )

                } catch (e: Exception) {
                    Log.e(TAG, "生成任务失败, ID: $taskId", e)
                    callback?.onError("生成失败: ${e.message}")
                } finally {
                    // 清理任务
                    activeTasks.remove(taskId)
                }
            }

            activeTasks[taskId] = job
            return taskId
        }

        override fun nativeClearCache(): Int {
            //清缓存
            return LLMHelper.nativeClearCache()
        }

        override fun nativeDestroy() {
            //退出时释放
            cleanup()
            //LLMHelper.nativeDestroy()
        }

        override fun nativeAbort(): Int {
            return LLMHelper.nativeAbort()
        }

        override fun nativeIsRunning(): Int {
            return LLMHelper.nativeIsRunning()
        }

        override fun nativeReleasePromptCache(): Int {
            return LLMHelper.nativeReleasePromptCache()
        }
    }



    /**
     * 初始化服务
     */
    private fun initializeService(modelPath: String=defaultModelPath,maxNewTokens: Int=512,
                                  maxContextLen: Int=2048) {
        serviceScope.launch {
            try {
                currentStatus.set(STATUS_INITIALIZING)
                statusMessage.set("正在初始化服务...")
                val ret = LLMHelper.nativeInit(modelPath, maxNewTokens, maxContextLen)
                if (ret != 0) {
                    // 初始化失败
                    throw Exception("初始化失败,ret=${ret};modelPath=${modelPath}")
                }
                // 初始化完成
                currentStatus.set(STATUS_READY)
                statusMessage.set("服务就绪")
                retReady=0

                Log.d(TAG, "服务初始化完成")

            } catch (e: Exception) {
                currentStatus.set(STATUS_ERROR)
                statusMessage.set("初始化失败: ${e.message}")
                Log.e(TAG, "服务初始化失败", e)
            }
        }
    }


    /**
     * 模拟LLM生成处理
     */
    private suspend fun processLLMGeneration(prompt: String): String {
        Log.d(TAG, "处理LLM生成, prompt: $prompt")

        // 模拟处理时间
        //delay(2000)

        currentStatus.set(STATUS_PROCESSING)
        statusMessage.set("服务正在进行中")

        // 返回模拟结果
        return """
            根据您的问题 "$prompt"，我的分析如下：
            
            1. 这是一个测试生成的文本
            2. 当前服务运行在独立进程
            3. 进程ID: ${Process.myPid()}
            4. 内存使用: ${Runtime.getRuntime().totalMemory() / 1024 / 1024}MB
            
            以上内容由LLM远程服务生成。
        """.trimIndent()
    }



    /**
     * 清理资源
     */
    private fun cleanup() {
        // 取消协程作用域
        serviceScope.cancel("Service destroyed")

        // 清理回调
        callbackList.kill()

        // 清理任务
        activeTasks.clear()

        retReady=-1
        LLMHelper.nativeDestroy()
        Log.d(TAG, "服务资源清理完成")
    }




    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LLM远程服务创建, 进程ID: ${Process.myPid()}")
        // 初始化服务
        initializeService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "服务被绑定")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "服务解绑")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "服务销毁")
        // 清理资源
        cleanup()
    }
}



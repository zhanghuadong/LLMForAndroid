package com.htfyun.eink.zhd.llm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
/**
 *
 * author: zhd
 * date: 2026/2/2
 * desc: 源码调用LLM例子
 */
class MainActivity :BasePermissionActivity()  {

    companion object{
        const val TAG="MainActivity"
    }

    lateinit var txtResult:TextView
    lateinit var btnGenerate:Button
    lateinit var btnGenerateTurn:Button
    lateinit var btnGenerateStop:Button
    lateinit var btnGenerateDestroy:Button
    lateinit var btnGenerateForThird:Button
    val sBuilder=StringBuilder()

    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        txtResult=findViewById<TextView>(R.id.txtResult)
        btnGenerate=findViewById<Button>(R.id.btnGenerate)
        btnGenerateTurn=findViewById<Button>(R.id.btnGenerateTurn)
        btnGenerateStop=findViewById<Button>(R.id.btnGenerateStop)
        btnGenerateDestroy=findViewById<Button>(R.id.btnGenerateDestroy)

        btnGenerateForThird=findViewById<Button>(R.id.btnGenerateForThird)

        //需要sd卡读写权限,用户自己去申请...
        requestStoragePermissions()
        initLLM()
        //非三方apk，直接给源码方式调用
        btnGenerate.setOnClickListener {

            //在子线程/协程中处理
            serviceScope.launch {
                // 流式推理
                LLMHelper.nativeGenerative("把下面这段话翻译成英文:讲一个500字的老鹰抓小鸡的故事!", object : LLMHelper.LlmCallback {
                    override fun onToken(token: String?) {
                        runOnUiThread {
                            appendToTextView(token ?: "")
                        }
                    }
                    override fun onFinish() {
                        runOnUiThread {
                            println("-------- onFinish-----------")
                            txtResult.text =sBuilder.toString()
                            println(sBuilder.toString())
                        }
                    }
                    override fun onError() {
                        runOnUiThread {
                            System.out.printf("-------- onError-----------")
                        }
                    }
                })
            }

        }

        //打开其他activity,三方方式
        btnGenerateForThird.setOnClickListener {
            startActivity(Intent(this,OtherAppActivity::class.java))
        }


        //切换模型
        btnGenerateTurn.setOnClickListener {
            //源码API方式，非三方
            val Qwen3="${Environment.getExternalStorageDirectory().path}/Qwen3-1.7B-rk3576-w4a16_g128.rkllm"
            LLMHelper.nativeClearCache()
            LLMHelper.nativeInit(Qwen3,512,2048)
        }


        //中断推理
        btnGenerateStop.setOnClickListener {
            try {
                LLMHelper.nativeAbort()
            }catch (e: RemoteException){
                Log.e(TAG,"出错了.e=${e.message}")
            }
        }

        //清理资源
        btnGenerateDestroy.setOnClickListener {
                try {
                    LLMHelper.nativeDestroy()
                }catch (e: RemoteException){
                    Log.e(TAG,"出错了.e=${e.message}")
                }
        }
    }


    private fun initLLM(){
        // 初始化（modelPath 为模型文件所在目录路径）
        val deepseek="/sdcard/DeepSeek-R1-Distill-Qwen-1.5B_W4A16_RK3576.rkllm"
        val Qwen3="/sdcard/Qwen3-1.7B-rk3576-w4a16_g128.rkllm"

        val modelPath=Qwen3
        val ret = LLMHelper.nativeInit(modelPath, 512, 2048)
        if (ret != 0) {
            // 初始化失败
            System.out.printf("-------- 初始化失败-----------")
            return
        }
    }

    private fun requestStoragePermissions() {
        checkAndRequestStoragePermissions { granted ->
            if (granted) {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            updatePermissionStatus()
        }
    }

    override fun onAllPermissionsGranted() {
        super.onAllPermissionsGranted()
        updatePermissionStatus()
        Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
    }


    private fun updatePermissionStatus() {
        val hasPermission = hasStoragePermissions()
        val statusText = if (hasPermission) {
            "✅ 已获得存储权限"
        } else {
            "❌ 未获得存储权限"
        }

//        tvStatus.text = statusText
//        btnWriteFile.isEnabled = hasPermission
//        btnReadFile.isEnabled = hasPermission
//
//        // 显示Android版本信息
//        val versionInfo = """
//            Android版本: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})
//            需要管理所有文件: ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.R}
//        """.trimIndent()
//
//        findViewById<TextView>(R.id.tv_version_info).text = versionInfo
    }

    private fun appendToTextView(token: String){
        sBuilder.append(token)
        txtResult.text =sBuilder.toString()
    }


    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清缓存
        LLMHelper.nativeClearCache()
        // 退出时释放
        LLMHelper.nativeDestroy()
    }
}
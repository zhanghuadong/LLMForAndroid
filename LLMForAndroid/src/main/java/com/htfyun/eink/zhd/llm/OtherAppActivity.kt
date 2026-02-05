package com.htfyun.eink.zhd.llm

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
/**
 *
 * author: zhd
 * date: 2026/2/2
 * desc: 三方apk调用LLM例子
 */
class OtherAppActivity :AppCompatActivity()  {

    companion object{
        const val TAG="OtherAppActivity"
        const val SERVICE_ACTION="com.htfyun.eink.zhd.llm.LLMService"
    }

    lateinit var txtResult:TextView
    lateinit var btnGenerate:Button
    lateinit var btnGenerateTurn:Button
    lateinit var btnGenerateStop:Button
    lateinit var btnGenerateDestroy:Button
    lateinit var btnGenerateForThird:Button
    private var llMAidlInterface:ILLMAidlInterface?=null
    private var isBound = false
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
        //btnGenerateForThird.visibility= View.GONE

        //三方apk调用方式
        //initLLMConnection()

        //非三方apk，直接给源码方式调用
        btnGenerate.setOnClickListener {

            try {
                if(isBound && llMAidlInterface?.nativeInitReady()==0){
                    llMAidlInterface?.nativeClearCache()
                    llMAidlInterface?.nativeGenerative("讲一个500字的老鹰抓小鸡的故事!",object :
                        ILlmCallback.Stub() {
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

                        override fun onError(message: String?) {
                            runOnUiThread {
                                System.out.printf("-------- onError-----------message=${message}")
                            }
                        }
                    })
                }else{
                    Toast.makeText(this,"LLM服务初始化尚未完成",Toast.LENGTH_LONG).show()
                }
            }catch (e: RemoteException){
                Log.e(TAG,"出错了.e=${e.message}")
            }
        }


        //中断推理
        btnGenerateStop.setOnClickListener {
            if(isBound){
                try {
                    llMAidlInterface?.nativeAbort()
                }catch (e: RemoteException){
                    Log.e(TAG,"出错了.e=${e.message}")
                }
            }
        }

        //清理资源
        btnGenerateDestroy.setOnClickListener {
            if(isBound){
                try {
                    llMAidlInterface?.nativeDestroy()
                }catch (e: RemoteException){
                    Log.e(TAG,"出错了.e=${e.message}")
                }
            }
        }

        //切换模型
        btnGenerateTurn.setOnClickListener {
            //三方apk方式
            if(isBound){
                try {
                    val Qwen3="${Environment.getExternalStorageDirectory().path}/Qwen3-1.7B-rk3576-w4a16_g128.rkllm"
                    llMAidlInterface?.nativeInit(Qwen3,512,2048)
                }catch (e: RemoteException){
                    Log.e(TAG,"出错了.e=${e.message}")
                }
            }
        }

        //设置重新加载
        btnGenerateForThird.setOnClickListener {

        }
    }

    /**
     *  绑定三方服务,并通过llmConnection进行调用
     */
    private fun initLLMConnection(){
        val intent=Intent()
        intent.setClassName("com.htfyun.eink.zhd.rkllm", SERVICE_ACTION)
        //val intent=Intent(this,LLMService::class.java)
        bindService(intent,llmConnection, BIND_AUTO_CREATE)
    }

    //绑定service后通过llMAidlInterface去调用
//    llMAidlInterface.nativeInit()
//    llMAidlInterface.nativeClearCache()
//    llMAidlInterface.nativeDestroy()
//    llMAidlInterface.nativeGenerative()

    private val llmConnection=object : ServiceConnection{
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            Log.d(TAG, "AIDL服务连接")
            llMAidlInterface=ILLMAidlInterface.Stub.asInterface(service)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "AIDL服务断开连接")
            llMAidlInterface = null
            isBound = false
        }

    }

    private fun appendToTextView(token: String){
        sBuilder.append(token)
        txtResult.text =sBuilder.toString()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart")
        initLLMConnection()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
        unbindService(llmConnection)
    }
}
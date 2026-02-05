package com.htfyun.eink.zhd.llm

// BasePermissionActivity.kt
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class BasePermissionActivity : AppCompatActivity() {

    // 权限请求启动器
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var manageStorageLauncher: ActivityResultLauncher<Intent>

    // 权限请求回调
    private var permissionCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化权限请求启动器
        //initPermissionLaunchers()
    }

     fun initPermissionLaunchers() {
        // 标准权限请求
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }

            if (allGranted) {
                permissionCallback?.invoke(true)
                onAllPermissionsGranted()
            } else {
                val deniedPermissions = permissions.filter { !it.value }.keys
                onPermissionsDenied(deniedPermissions.toList())
                permissionCallback?.invoke(false)
            }
        }

        // 管理所有文件权限请求
        manageStorageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    permissionCallback?.invoke(true)
                    onAllPermissionsGranted()
                } else {
                    permissionCallback?.invoke(false)
                    onManageStorageDenied()
                }
            }
        }
    }

    /**
     * 检查并请求存储权限
     */
    fun checkAndRequestStoragePermissions(callback: (Boolean) -> Unit = {}) {
        permissionCallback = callback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要管理所有文件权限
            if (Environment.isExternalStorageManager()) {
                callback.invoke(true)
                onAllPermissionsGranted()
            } else {
                showManageStorageDialog()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10
            requestStoragePermissionsApi29()
        } else {
            // Android 9 及以下
            requestStoragePermissionsLegacy()
        }
    }

    private fun requestStoragePermissionsLegacy() {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (shouldShowRationale(permissions)) {
            showRationaleDialog(permissions)
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun requestStoragePermissionsApi29() {
        val permissions = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (shouldShowRationale(permissions)) {
            showRationaleDialog(permissions)
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun showManageStorageDialog() {
        AlertDialog.Builder(this)
            .setTitle("文件管理权限")
            .setMessage("应用需要访问所有文件的权限，以便管理您的文件。请授予权限。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageLauncher.launch(intent)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                permissionCallback?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }

    private fun showRationaleDialog(permissions: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("权限说明")
            .setMessage("应用需要存储权限来读取和写入文件。")
            .setPositiveButton("确定") { _, _ ->
                permissionLauncher.launch(permissions.toTypedArray())
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                permissionCallback?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }

    private fun shouldShowRationale(permissions: List<String>): Boolean {
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    protected open fun onAllPermissionsGranted() {
        // 子类实现
    }

    protected open fun onPermissionsDenied(deniedPermissions: List<String>) {
        showDeniedMessage(deniedPermissions)
    }

    protected open fun onManageStorageDenied() {
//        Snackbar.make(
//            findViewById(android.R.id.content),
//            "需要文件管理权限才能继续操作",
//            Snackbar.LENGTH_LONG
//        ).setAction("去设置") {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", packageName, null)
//            intent.data = uri
//            startActivity(intent)
//        }.show()


        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showDeniedMessage(deniedPermissions: List<String>) {
        val message = when {
            Manifest.permission.READ_EXTERNAL_STORAGE in deniedPermissions ->
                "需要读取存储权限来访问文件"
            Manifest.permission.WRITE_EXTERNAL_STORAGE in deniedPermissions ->
                "需要写入存储权限来保存文件"
            else -> "需要存储权限才能继续操作"
        }

//        Snackbar.make(
//            findViewById(android.R.id.content),
//            message,
//            Snackbar.LENGTH_LONG
//        ).setAction("去设置") {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", packageName, null)
//            intent.data = uri
//            startActivity(intent)
//        }.show()


        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    /**
     * 检查是否已授予所有必要权限
     */
    fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
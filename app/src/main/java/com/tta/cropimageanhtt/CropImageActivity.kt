package com.tta.cropimageanhtt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tta.cropimageanhtt.crop.CropIwaView
import com.tta.cropimageanhtt.crop.config.CropIwaSaveConfig
import com.tta.cropimageanhtt.databinding.ActivityCropImageBinding
import com.tta.cropimageanhtt.utils.MultiClickUtils
import com.tta.cropimageanhtt.utils.Utils.ACTION_CROP_IMAGE
import com.tta.cropimageanhtt.utils.Utils.ACTION_CROP_IMAGE_FROM_CAMERA
import com.tta.cropimageanhtt.utils.Utils.ALPHA_DISABLE
import com.tta.cropimageanhtt.utils.Utils.ALPHA_ENABLE
import com.tta.cropimageanhtt.utils.Utils.CROP_PATH
import com.tta.cropimageanhtt.utils.Utils.FOLDER_CACHE_CROP_IMAGE
import java.io.File
import java.io.IOException
import java.net.URLConnection

class CropImageActivity : AppCompatActivity(), CropIwaView.ReadyCropImage, View.OnClickListener {
    private lateinit var mBinding: ActivityCropImageBinding

    private var mPhotoFile: File? = null

    private var mUri: Uri? = null

    private val mCheckPermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.CAMERA] == true ||
                it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true ||
                it[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            ) {
                handleGetImage()
            } else {
//                msg_permission_denied
                finish()
            }
        }

    private val mOpenGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (it.data?.data != null) {
                    mBinding.containerCrop.visibility = View.VISIBLE
                    mBinding.cropView.setImageUri(it.data?.data)
                }
            } else {
                if (!isFinishing) finish()
            }
        }

    private val mTakePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            when {
                it -> {
                    mUri?.let { uri ->
                        mBinding.containerCrop.visibility = View.VISIBLE
                        mBinding.cropView.setImageUri(uri)
                    }
                }

                mPhotoFile != null -> {
                    mPhotoFile?.delete()
                    if (!isFinishing) finish()
                }

                else -> {
                    if (!isFinishing) finish()
                }
            }
        }

    private val takePictureIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (mUri != null) {
                mBinding.containerCrop.visibility = View.VISIBLE
                mBinding.cropView.setImageUri(mUri) // Đảm bảo mUri không null
            } else {
                Toast.makeText(this, "Photo capture failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGetImage() {
        intent.let {
            val action = intent.getStringExtra(ACTION_CROP_IMAGE)
            if (action != null) {
                if (action == ACTION_CROP_IMAGE_FROM_CAMERA) {
//                    takePhoto()
                    openCamera()
                } else {
                    openGallery()
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        mOpenGallery.launch(Intent.createChooser(intent, "Select"))
    }

    private fun takePhoto() {
        // Tạo folder cache
        val folderCache = File(cacheDir, "images")
        if (!folderCache.exists()) {
            folderCache.mkdirs()
        }

        try {
            // Tạo file tạm thời để lưu ảnh
            mPhotoFile = File.createTempFile(
                System.currentTimeMillis().toString(),
                ".jpg",
                folderCache
            )

            // Tạo Uri từ file với FileProvider
            mUri = mPhotoFile?.let { file ->
                FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
            }

            // Kiểm tra mUri có null không, sau đó gọi mTakePhoto
            mUri?.let { uri ->
                mTakePhoto.launch(uri)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val folderCache = File(cacheDir, "images")
        if (!folderCache.exists()) {
            folderCache.mkdirs()
        }

        try {
            // Tạo file tạm thời để lưu ảnh
            mPhotoFile = File.createTempFile(
                System.currentTimeMillis().toString(),
                ".png",
                folderCache
            )

            // Tạo Uri từ file với FileProvider
            mUri = mPhotoFile?.let { file ->
                FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
            }

            // Kiểm tra mUri có null không, sau đó gọi mTakePhoto
            mUri?.let { uri ->
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

                // Cấp quyền ghi tạm thời cho ứng dụng máy ảnh
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                takePictureIntentLauncher.launch(takePictureIntent)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissionCrop(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_DENIED ||
                    (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                            != PackageManager.PERMISSION_DENIED) ||
                    (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                            != PackageManager.PERMISSION_DENIED)
        } else {
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCropImageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initView()
        initEvent()

//        if (hasPermissionCrop()) {
        handleGetImage()
//        } else {
//            mCheckPermissions.launch(
//                arrayOf(
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//            )
//        }

        handleActionCropView(false)
    }

    private fun initEvent() {
        mBinding.toolbar.toolbarBack.setOnClickListener(this)
        mBinding.tvCancel.setOnClickListener(this)
        mBinding.tvDone.setOnClickListener(this)
    }

    private fun initView() {
        mBinding.cropView.configureImage().setMinScale(0.01f).apply()
        mBinding.cropView.setReadyCropImage(this)
        mBinding.containerCrop.visibility = View.GONE
    }

    private fun handleActionCropView(isAvailable: Boolean) {
        mBinding.tvCancel.isClickable = isAvailable
        mBinding.tvDone.isClickable = isAvailable
        mBinding.tvDone.alpha =
            if (isAvailable) ALPHA_ENABLE else ALPHA_DISABLE
        mBinding.tvCancel.alpha =
            if (isAvailable) ALPHA_ENABLE else ALPHA_DISABLE
    }

    override fun onReady() {
//        Timber.d("Cuong OnReady")
        handleActionCropView(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            mBinding.toolbar.toolbarBack.id,
            mBinding.tvCancel.id -> {
                onBackPressed()
            }

            mBinding.tvDone.id -> {
                if (MultiClickUtils.instance!!.isAvailableClick) {
                    cropImage()
                }
            }
        }
    }

    private fun cropImage() {
        val folderCache = File(cacheDir.absolutePath, FOLDER_CACHE_CROP_IMAGE)
        if (!folderCache.exists()) {
            val result = folderCache.mkdirs()
            if (!result) return
        }
        try {
            val croppedFile =
                File.createTempFile(
                    System.currentTimeMillis().toString(),
                    ".jpg",
                    folderCache
                )

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                croppedFile
            )

            mBinding.cropView.crop(
                CropIwaSaveConfig.Builder(uri)
                    .setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setSize(250, 250)
                    .setQuality(70)
                    .build()
            )
            mBinding.cropView.setCropSaveCompleteListener {
                val intent = Intent()
                intent.putExtra(CROP_PATH, croppedFile.path)
                setResult(RESULT_OK, intent)
                onBackPressed()
            }
            mBinding.cropView.setErrorListener {
//                ToastUtils.showShort(R.string.msg_has_error)
            }
        } catch (e: IOException) {
//            ToastUtils.showShort(R.string.msg_has_error)
            e.printStackTrace()
        }
    }

    private fun isValidImageFile(filePath: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(filePath)
        return mimeType != null && mimeType.startsWith("image");
    }

    private fun getSizeImage(uri: Uri): Int {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Audio.Media.SIZE)
            cursor = contentResolver.query(uri, proj, null, null, null)
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                cursor.moveToFirst()
//                Timber.d("Cuong: " + cursor.getString(columnIndex))
                return Integer.valueOf(cursor.getString(columnIndex))
            }
            0
        } finally {
            cursor?.close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo_uri", mUri?.toString()) // Lưu URI dưới dạng chuỗi
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val uriString = savedInstanceState.getString("photo_uri")
        if (uriString != null) {
            mUri = Uri.parse(uriString) // Khôi phục lại URI từ chuỗi
        }
    }
}
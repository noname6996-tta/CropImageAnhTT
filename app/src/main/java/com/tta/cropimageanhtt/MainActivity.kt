package com.tta.cropimageanhtt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tta.cropimageanhtt.databinding.ActivityMainBinding
import com.tta.cropimageanhtt.utils.Utils.ACTION_CROP_IMAGE
import com.tta.cropimageanhtt.utils.Utils.ACTION_CROP_IMAGE_FROM_CAMERA
import com.tta.cropimageanhtt.utils.Utils.ACTION_CROP_IMAGE_FROM_GALLERY
import com.tta.cropimageanhtt.utils.Utils.CROP_PATH

class MainActivity : AppCompatActivity() {

    private var mPathImage: String? = null
    private lateinit var binding: ActivityMainBinding

    private val mOpenCropImageActivity =
        registerForActivityResult(CropImageActivityContract()) { path ->
            path?.let {
                if (it.isNotEmpty()) {
                    binding.imgAvatar.setImageBitmap(BitmapFactory.decodeFile(it))
                    mPathImage = it
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
    }

    private fun initView() = with(binding) {
        btnCamera.setOnClickListener {
            mOpenCropImageActivity.launch(ACTION_CROP_IMAGE_FROM_CAMERA)
        }
        btnGallery.setOnClickListener {
            mOpenCropImageActivity.launch(ACTION_CROP_IMAGE_FROM_GALLERY)
        }
    }
}

class CropImageActivityContract : ActivityResultContract<String, String>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, CropImageActivity::class.java).apply {
            putExtra(ACTION_CROP_IMAGE, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        val data = intent?.getStringExtra(CROP_PATH)
        if (resultCode == Activity.RESULT_OK && data != null) {
            return data
        }
        return ""
    }

}
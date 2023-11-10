package com.example.kotlininterview

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*


class MyActivity : Activity() {
    private val lifecycleScope = MainScope()
    private var image: ImageView? = null
    private val APP_STORAGE_ACCESS_REQUEST_CODE = 501 // Any value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        setContentView(root)

        val button =
            MaterialButton(context).apply {
                text = "Show image"
                setOnClickListener { loadImage() }

            }
        image = ImageView(context)
        root.addView(image, 1000, 1000)
        root.addView(
            button, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val intent = Intent(
            ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
        )

        startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE)
    }

    private fun loadImage() {
        val coroutine: Deferred<Bitmap?> = lifecycleScope.async(Dispatchers.IO) {
            val d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val lf = d.listFiles()
            val bitmap = lf
                ?.filter { it.name.endsWith(".jpeg") }
                ?.randomOrNull()
                ?.let { BitmapFactory.decodeFile(it.path) }

            return@async bitmap

        }
        coroutine.invokeOnCompletion {
            lifecycleScope.launch(Dispatchers.Main) {
                image?.setImageBitmap(coroutine.getCompleted())
                Toast.makeText(this@MyActivity, "Image Loaded!", Toast.LENGTH_LONG).show()
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this@MyActivity, "Perm ok", Toast.LENGTH_LONG).show()
            }
        }
    }

}

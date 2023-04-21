package com.picker.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.picker.app.adapter.ImageAdapter
import com.picker.example.databinding.ActivityMainBinding
import com.picker.mylibrary.FileUtils
import com.picker.mylibrary.MediaModel
import com.picker.mylibrary.Picker
import java.util.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var picker: Picker
    private var stringBuilder = StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        picker = Picker(this)

        binding.clickListener = this

    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.btnMultipleImage -> {
                picker.pickImage(
                    multipleSelection = true,
                    originalFilesCallBack = { list ->
                        clearAndSetUpText(list)
                    }, compressedFilesCallBack = { list ->
                        addText(list)
                    })
            }
            binding.btnGallery -> {
                picker.pickImage(
                    originalFilesCallBack = { list ->
                        clearAndSetUpText(list)
                    }, compressedFilesCallBack = { list ->
                        addText(list)
                    })
            }
            binding.btnVideo -> {
                picker.pickVideo(callBack = { list ->
                    clearAndSetUpText(list)
                })
            }
            binding.btnMultipleVideo -> {
                picker.pickMultipleVideo(callBack = { list ->
                    clearAndSetUpText(list)
                })
            }

            binding.btnCamera -> {
                picker.captureImage(
                    originalFilesCallBack = { list ->
                        clearAndSetUpText(list)
                    }, compressedFilesCallBack = { list ->
                        addText(list)
                    })
            }
            binding.btnAll -> {
                picker.pickImageAndVideo(
                    multipleSelection = true,
                    originalFilesCallBack = { list ->
                        clearAndSetUpText(list)
                    }, compressedFilesCallBack = { list ->
                        addText(list)
                    })
            }
            binding.btnFile -> {
                picker.pickFile(mimeTypes = arrayOf("application/pdf", "text"),
                    callBack = { list ->
                        clearAndSetUpText(list)
                    }
                )
            }
            binding.btnMultipleFile -> {
                picker.pickMultipleFile(
                    callBack = { list ->
                        clearAndSetUpText(list)
                    }
                )
            }
            binding.btnVideoCapture -> {
                picker.captureVideo(
                    callBack = { list ->
                        clearAndSetUpText(list)
                    }
                )
            }

        }
    }

    private fun addText(list: ArrayList<MediaModel>) {
        runOnUiThread {
            stringBuilder.append("\n\nCompressed Images :")
            list.forEachIndexed { index, file ->
                stringBuilder.append("\n")
                stringBuilder.append(
                    "${index + 1})     ${
                        FileUtils.getFolderSizeLabel(
                            file.file
                        )
                    }"
                )
                binding.txtLog.text = stringBuilder.toString()
            }
        }
    }

    private fun clearAndSetUpText(list: ArrayList<MediaModel>) {
        binding.pickImage.adapter = ImageAdapter(list)
        binding.txtLog.text = ""
        stringBuilder.clear()
        stringBuilder = StringBuilder()
        runOnUiThread {
            stringBuilder.append("Original Images :")
            list.forEachIndexed { index, file ->
                stringBuilder.append("\n")
                stringBuilder.append(
                    "${index + 1})    ${
                        FileUtils.getFolderSizeLabel(
                            file.file
                        )
                    }"
                )
                binding.txtLog.text = stringBuilder.toString()
            }
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
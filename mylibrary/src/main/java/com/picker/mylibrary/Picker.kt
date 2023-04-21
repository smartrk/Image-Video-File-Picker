package com.picker.mylibrary

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.net.toUri
import com.permissionx.guolindev.PermissionX
import com.picker.mylibrary.FileUtils.getUriFromFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class Picker(
    private var activity: AppCompatActivity
) {
    private var settingCallBack: (() -> Unit)? = null
    private var settingCallBackLauncher: ActivityResultLauncher<Intent>
    private var fileSelectionLauncher: ActivityResultLauncher<Intent>
    private var pickMediaGallery: ActivityResultLauncher<PickVisualMediaRequest>
    private var pickMultipleMediaGallery: ActivityResultLauncher<PickVisualMediaRequest>
    private var pickMultipleImageVideo: ActivityResultLauncher<PickVisualMediaRequest>
    private var pickImageVideo: ActivityResultLauncher<PickVisualMediaRequest>
    private var pickMultipleVideo: ActivityResultLauncher<PickVisualMediaRequest>
    private var takePicture: ActivityResultLauncher<Uri>
    private var captureVideo: ActivityResultLauncher<Uri>
    private var capturedUri: Uri? = null
    private var capturedPath: String? = null
    var isVideoAndImage = false
    val maxSelectionLimit = 5
    private var multipleListner: ((ArrayList<MediaModel>) -> Unit?)? =
        null
    private var multipleCompressedListner: ((ArrayList<MediaModel>) -> Unit?)? =
        null
    private var pickVideoGallery: ActivityResultLauncher<PickVisualMediaRequest>
    private var isMultipleSelect = false
    lateinit var videoSelectionCallBacks: (ArrayList<MediaModel>) -> Unit
    lateinit var multipleVideoCallBack: (ArrayList<MediaModel>) -> Unit
    private val compressedFileList = kotlin.collections.ArrayList<MediaModel>()
    private val fileList = kotlin.collections.ArrayList<MediaModel>()

    init {
        fileSelectionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                    val returnUri: Intent? = result.data

                    if (returnUri!!.clipData != null) {
                        val count: Int = returnUri.clipData!!.itemCount
                        repeat(count) { pos ->
                            returnUri.clipData!!.getItemAt(pos).uri?.let { documentUri ->
                                val path = FileUtils.getFilePath(activity, documentUri)
                                path?.let {
                                    fileList.add(
                                        MediaModel(
                                            File(path),
                                            path,
                                            path.toUri(),
                                            Which.FILE
                                        )
                                    )
                                }
                            }
                            if (pos == count - 1) {
                                multipleListner?.invoke(fileList)
                            }
                        }
                    } else if (returnUri.data != null) {
//                        val imagePath: String = data.getData().getPath()
                        returnUri.data?.let { documentUri ->
                            val path = FileUtils.getFilePath(activity, documentUri)
                            path?.let {
                                fileList.add(MediaModel(File(path), path, path.toUri(), Which.FILE))
                                multipleListner?.invoke(fileList)
                            }
                        }
                    }
                }
            }
        settingCallBackLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                settingCallBack?.invoke()
            }

        pickMediaGallery =
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { fileUri ->
                if (fileUri != null) {
                    FileUtils.getFileFromUri(activity, fileUri)?.let {
                        fileList.add(MediaModel(it, fileUri.path.toString(), fileUri))
                        multipleListner?.invoke(fileList)
                        checkSizeAndCompressedImage(compressedFileList.size)
                    } ?: run {
                        val utilFile = FileUtils.getFilePath(activity, fileUri)
                        if (utilFile.isNullOrEmpty().not()) {
                            fileList.add(
                                MediaModel(
                                    File(
                                        FileUtils.getFilePath(activity, fileUri).toString()
                                    ), fileUri.path.toString(), fileUri
                                )
                            )
                            multipleListner?.invoke(fileList)
                            checkSizeAndCompressedImage(compressedFileList.size)
                        } else {
                            fileList.add(
                                MediaModel(
                                    File(fileUri.path.toString()),
                                    fileUri.path.toString(),
                                    fileUri
                                )
                            )
                            multipleListner?.invoke(fileList)
                            checkSizeAndCompressedImage(compressedFileList.size)
                        }
                    }
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }

        pickImageVideo =
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { fileUri ->
                if (fileUri != null) {
                    FileUtils.getFileFromUri(activity, fileUri)?.let {
                        fileList.add(MediaModel(it, fileUri.path.toString(), fileUri))
                        multipleListner?.invoke(fileList)
                        checkSizeAndCompressedImage(compressedFileList.size)
                    } ?: run {
                        val utilFile = FileUtils.getFilePath(activity, fileUri)
                        if (utilFile.isNullOrEmpty().not()) {
                            fileList.add(
                                MediaModel(
                                    File(
                                        FileUtils.getFilePath(activity, fileUri).toString()
                                    ), fileUri.path.toString(), fileUri
                                )
                            )
                            multipleListner?.invoke(fileList)
                            checkSizeAndCompressedImage(compressedFileList.size)
                        } else {
                            fileList.add(
                                MediaModel(
                                    File(fileUri.path.toString()),
                                    fileUri.path.toString(),
                                    fileUri
                                )
                            )
                            multipleListner?.invoke(fileList)
                            checkSizeAndCompressedImage(compressedFileList.size)
                        }
                    }
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }

        pickMultipleVideo =
            activity.registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(
                    5
                )
            ) { fileUriList ->
                if (fileUriList.isNotEmpty()) {
                    if (fileUriList.size > maxSelectionLimit) {
                        Toast.makeText(
                            activity,
                            "You can select maximum $maxSelectionLimit images only",
                            Toast.LENGTH_SHORT
                        ).show()
                        val newlist = ArrayList<@JvmSuppressWildcards Uri>()
                        repeat(maxSelectionLimit) {
                            newlist.add(fileUriList[it])
                        }
                        maintainMultipleFiles(newlist, isImage = false)
                    } else {
                        maintainMultipleFiles(fileUriList, isImage = false)
                    }
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }

        pickMultipleMediaGallery =
            activity.registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(
                    5
                )
            ) { fileUriList ->
                if (fileUriList.isNotEmpty()) {
                    if (fileUriList.size > maxSelectionLimit) {
                        Toast.makeText(
                            activity,
                            "You can select maximum $maxSelectionLimit images only",
                            Toast.LENGTH_SHORT
                        ).show()
                        val newlist = ArrayList<@JvmSuppressWildcards Uri>()
                        repeat(maxSelectionLimit) {
                            newlist.add(fileUriList[it])
                        }
                        maintainMultipleFiles(newlist)
                    } else {
                        maintainMultipleFiles(fileUriList)
                    }
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }

        pickMultipleImageVideo =
            activity.registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(
                    5
                )
            ) { fileUriList ->
                if (fileUriList.isNotEmpty()) {
                    if (fileUriList.size > maxSelectionLimit) {
                        Toast.makeText(
                            activity,
                            "You can select maximum $maxSelectionLimit",
                            Toast.LENGTH_SHORT
                        ).show()
                        val newlist = ArrayList<@JvmSuppressWildcards Uri>()
                        repeat(maxSelectionLimit) {
                            newlist.add(fileUriList[it])
                        }
                        maintainMultipleFiles(newlist)
                    } else {
                        maintainMultipleFiles(fileUriList)
                    }
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }
        takePicture =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    capturedUri?.let {
                        fileList.add(
                            MediaModel(
                                File(capturedPath.toString()),
                                capturedPath.toString(),
                                it
                            )
                        )
                        multipleListner?.invoke(fileList)
                        checkSizeAndCompressedImage(compressedFileList.size)
                    }
                }
            }
        captureVideo =
            activity.registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success: Boolean ->
                if (success) {
                    capturedUri?.let {
                        fileList.add(
                            MediaModel(
                                File(capturedPath.toString()),
                                capturedPath.toString(),
                                it, Which.VIDEO
                            )
                        )
                        multipleListner?.invoke(fileList)
                    }
                }
            }

        pickVideoGallery =
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { fileUri ->
                if (fileUri != null) {
                    FileUtils.getFileFromUri(activity, fileUri)?.let {
                        fileList.add(
                            MediaModel(
                                it,
                                fileUri.path.toString(),
                                fileUri,
                                Which.VIDEO
                            )
                        )
                    } ?: run {
                        val utilFile = FileUtils.getFilePath(activity, fileUri)
                        if (utilFile.isNullOrEmpty().not()) {
                            fileList.add(
                                MediaModel(
                                    File(
                                        FileUtils.getFilePath(activity, fileUri).toString()
                                    ), fileUri.path.toString(), fileUri, Which.VIDEO
                                )
                            )
                        } else {
                            fileList.add(
                                MediaModel(
                                    File(fileUri.path.toString()),
                                    fileUri.path.toString(),
                                    fileUri, Which.VIDEO
                                )
                            )
                        }
                    }
                    videoSelectionCallBacks.invoke(fileList)
                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }
    }

    private fun maintainMultipleFiles(
        fileUriList: List<@JvmSuppressWildcards Uri>,
        isImage: Boolean = true
    ) {
        fileUriList.forEachIndexed { index, fileUri ->
            FileUtils.getFileFromUri(activity, fileUri)?.let {
                fileList.add(MediaModel(it, fileUri.path.toString(), fileUri))
            } ?: run {
                val utilFile = FileUtils.getFilePath(activity, fileUri)
                if (utilFile.isNullOrEmpty().not()) {
                    fileList.add(
                        MediaModel(
                            File(
                                FileUtils.getFilePath(activity, fileUri).toString()
                            ), fileUri.path.toString(), fileUri
                        )
                    )
                } else {
                    fileList.add(
                        MediaModel(
                            File(fileUri.path.toString()),
                            fileUri.path.toString(),
                            fileUri
                        )
                    )
                }
            }

        }

        fileList.forEach {item->
            val cR: ContentResolver = activity.contentResolver
            val type: String? = cR.getType(item.uri)
            type?.let {
                if (it.startsWith("video")) {
                    item.which = Which.VIDEO
                } else if (it.startsWith("image")) {
                    item.which = Which.IMAGE
                } else {
                    item.which = Which.FILE
                }
            }
        }


        multipleListner?.invoke(fileList)
        checkSizeAndCompressedImage(compressedFileList.size)
    }

    private fun checkSizeAndCompressedImage(
        pos: Int,
    ) {
        if (fileList.size == compressedFileList.size) {
            multipleCompressedListner?.invoke(
                compressedFileList
            )
        } else {
            val item = fileList[pos]
            val size = FileUtils.getFolderSize(item.file)
            val cR: ContentResolver = activity.contentResolver
            val type: String? = cR.getType(item.uri)
            type?.let {
                if (it.startsWith("video")) {
                    item.which = Which.VIDEO
                } else if (it.startsWith("image")) {
                    item.which = Which.IMAGE
                } else {
                    item.which = Which.FILE
                }
            }


            if (size >= 300 && type.toString().startsWith("video").not()) {
                CoroutineScope(Dispatchers.IO).launch {
                    compressImage(item.file) { compressedPath, compressedFile, compressedUri ->
                        compressedFileList.add(
                            MediaModel(
                                compressedFile,
                                compressedPath,
                                compressedUri, item.which
                            )
                        )
                        checkSizeAndCompressedImage(compressedFileList.size)
                    }
                }
            } else {
                compressedFileList.add(item)
                checkSizeAndCompressedImage(compressedFileList.size)
            }
        }
    }

    fun pickCaptureSelection(
        multipleSelection: Boolean = false,
        originalFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        compressedFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
    ) {
        isMultipleSelect = multipleSelection
        multipleListner = originalFilesCallBack
        multipleCompressedListner = compressedFilesCallBack
        val dialog = Dialog(activity, R.style.custom_style_dialog_galley)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_choose_photo_linear)
        dialog.setCanceledOnTouchOutside(true)

        dialog.setOnCancelListener {
        }
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val txtCamera = dialog.findViewById<ConstraintLayout>(R.id.consCamera)
        val txtGallary = dialog.findViewById<ConstraintLayout>(R.id.consGallery)

        // Handle Camera option click
        txtCamera.setOnClickListener {
            takeCameraImage(getPermission())
            dialog.dismiss()
        }

        // Handle Gallery option click
        txtGallary.setOnClickListener {
            checkPermissionAndGetPhoto()
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            dialog.dismiss()
        }
    }

    fun pickImage(
        multipleSelection: Boolean = false,
        originalFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        compressedFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
    ) {
        fileList.clear()
        compressedFileList.clear()

        isMultipleSelect = multipleSelection
        multipleListner = originalFilesCallBack
        multipleCompressedListner = compressedFilesCallBack
        checkPermissionAndGetPhoto()
    }

    fun pickImageAndVideo(
        multipleSelection: Boolean = false,
        originalFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        compressedFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
    ) {
        fileList.clear()
        compressedFileList.clear()

        isMultipleSelect = multipleSelection
        multipleListner = originalFilesCallBack
        multipleCompressedListner = compressedFilesCallBack
        checkPermissionAndGetPhoto(false)
    }

    fun captureImage(
        multipleSelection: Boolean = false,
        originalFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        compressedFilesCallBack: (ArrayList<MediaModel>) -> Unit = { list -> },
    ) {
        fileList.clear()
        compressedFileList.clear()

        isMultipleSelect = multipleSelection
        multipleListner = originalFilesCallBack
        multipleCompressedListner = compressedFilesCallBack
        takeCameraImage(getPermission())
    }

    fun pickFile(
        callBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        mimeTypes: Array<String> = arrayOf("application/pdf", "text")
    ) {
        fileList.clear()
        compressedFileList.clear()
        multipleListner = callBack
        val chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "*/*"
        chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        fileSelectionLauncher.launch(chooseFile)
    }

    fun pickMultipleFile(
        callBack: (ArrayList<MediaModel>) -> Unit = { list -> },
        mimeTypes: Array<String> = arrayOf("application/pdf", "text")
    ) {
        fileList.clear()
        compressedFileList.clear()
        multipleListner = callBack
        val chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "*/*"
        chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        fileSelectionLauncher.launch(chooseFile)
    }

    fun captureVideo(
        callBack: (ArrayList<MediaModel>) -> Unit = { list -> },
    ) {
        fileList.clear()
        compressedFileList.clear()

        multipleListner = callBack
        takeCameraImage(getPermission(), true)
    }

    private fun getPermission(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(
                Manifest.permission.CAMERA
            )
        } else {
            arrayListOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }


    private fun getPermissions(
        array: List<String>,
        permissionMsg: String = activity.getString(R.string.app_needs_permission_to_continue),
        permissionGranted: () -> Unit
    ) {
        PermissionX.init(activity).permissions(array).request { allGranted, _, _ ->
            if (allGranted) {
                permissionGranted()
            } else {
                DialogUtils.showCustomDialogWithButton(
                    activity,
                    message = permissionMsg,
                    positiveBtnName = activity.getString(R.string.settings),
                    negativeBtnName = activity.getString(R.string.cancel),
                    positiveListener = {
                        activity.openNotificationSetting()
                    }
                )
            }
        }
    }

    private fun Context.openNotificationSetting() {
        val intent = (Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
        settingCallBackLauncher.launch(intent)
    }


    private fun checkPermissionAndGetPhoto(isImage: Boolean = true) {
        val permissionList = arrayListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        isVideoAndImage = isImage.not()
        getPermissions(
            permissionList, activity.getString(R.string.permission_msg_gallery)
        ) {
            if (isImage) {
                if (isMultipleSelect) {
                    pickMultipleMediaGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    pickMediaGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            } else {
                if (isMultipleSelect) {
                    pickMultipleImageVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                } else {
                    pickImageVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            }

        }
    }

    private fun takeCameraImage(array: List<String>, isVideo: Boolean = false) {
        getPermissions(array, activity.getString(R.string.permission_msg_camera)) {
            try {
                val photoFile = FileUtils.createImageFile(activity)
                capturedPath = photoFile.absolutePath
                capturedUri = activity.getUriFromFile(photoFile)
                if (isVideo) {
                    captureVideo.launch(capturedUri)
                } else {
                    takePicture.launch(capturedUri)
                }
            } catch (ex: Exception) {
                Log.e("Rk", ex.message.toString())
            }
        }
    }

    //video
    fun pickVideo(callBack: (ArrayList<MediaModel>) -> Unit = { list -> }) {
        fileList.clear()
        compressedFileList.clear()
        videoSelectionCallBacks = callBack
        getPermissions(
            getPermissionVideo(), activity.getString(R.string.permission_msg_gallery)
        ) {
            pickVideoGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
    }

    //video
    fun pickMultipleVideo(callBack: (ArrayList<MediaModel>) -> Unit = { list -> }) {
        fileList.clear()
        compressedFileList.clear()
        multipleVideoCallBack = callBack
        getPermissions(
            getPermissionVideo(), activity.getString(R.string.permission_msg_gallery)
        ) {
            pickMultipleVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
    }


    private fun getPermissionVideo(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private suspend fun compressImage(
        sourceImageFile: File,
        callBack: (String, File, Uri) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            CompressImage.compressImageCallBack(
                activity,
                sourceImageFile
            ) { s, file, uri ->
                FileUtils.setRotation(sourceImageFile, file)
                callBack.invoke(s, file, uri)
            }

        } catch (e: Exception) {
            Log.e("PError", e.localizedMessage!!.toString())
        }

    }


}

data class MediaModel(
    val file: File, val path: String, val uri: Uri, var which: Which = Which.IMAGE
)

enum class Which {
    IMAGE, VIDEO, FILE
}

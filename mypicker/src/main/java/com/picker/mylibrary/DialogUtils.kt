package com.picker.mylibrary

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.picker.mylibrary.databinding.CustomDialogWithButtonBinding

object DialogUtils {

    fun showCustomDialogWithButton(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveBtnName: String? = null,
        negativeBtnName: String? = null,
        alertBtnName: String? = null,
        isAlert: Boolean = false,
        isCancelable: Boolean = false,
        positiveListener: () -> Unit = {},
        negativeListener: () -> Unit = {},
        onDismissListener: () -> Unit = {}
    ) {
        try {
            val dialog = AppCompatDialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(isCancelable)
            dialog.setCanceledOnTouchOutside(isCancelable)

            dialog.setOnDismissListener {
                onDismissListener.invoke()
            }

            val binding: CustomDialogWithButtonBinding =
                CustomDialogWithButtonBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(binding.root)

            //  dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation


            title?.let {
                binding.txtTitle.isVisible = true
            }
            message?.let {
                binding.txtMessage.text = it
            }

            positiveBtnName?.let {
                binding.btnPositive.text = it

            }
            negativeBtnName?.let {
                binding.btnNagetive.text = it
            }

            alertBtnName?.let {
                binding.btnAlert.text = it
            }

            binding.consButton.isVisible = isAlert.not()
            binding.btnAlert.isVisible = isAlert


            binding.btnPositive.setOnClickListener {
                positiveListener.invoke()
                dialog.dismiss()
            }

            binding.btnNagetive.setOnClickListener {
                negativeListener.invoke()
                dialog.dismiss()
            }
            binding.btnAlert.setOnClickListener {
                positiveListener.invoke()
                dialog.dismiss()
            }


            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
            dialog.show()
        } catch (e: Exception) {
            Log.e("exc", e.message.toString())
        }
    }

}
package com.innov.geotracking.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Window
import android.widget.RelativeLayout
import com.google.android.material.textfield.TextInputEditText
import com.innov.geotracking.R
import com.innov.geotracking.databinding.DialogLogOutBinding
import com.innov.geotracking.databinding.DialogPermissionBinding

class DialogUtils {
    companion object {
        private lateinit var attendanceCheckInDialog: Dialog
        private lateinit var markYourAttendanceDialog: Dialog
        private lateinit var completeYourProfileDialog: Dialog
        private lateinit var logOutDialog: Dialog
        private lateinit var reimbursementPreApprovalDialog: Dialog
        private lateinit var yearPickerDialog: Dialog
        private lateinit var signUpCompletedDialog: Dialog
        private lateinit var birthdayWishDialog: Dialog
        private lateinit var pfUanDialog: Dialog
        private lateinit var acknowledgeDialog: Dialog
        private lateinit var permissionDialog: Dialog


        fun showLogOutDialog(
            context: Context,
            listener: DialogManager,
            msg: String,
            title: String,
            btnText: String,
            isAirtelDeclaration: Boolean = false
        ) {
            val binding = DialogLogOutBinding.inflate(LayoutInflater.from(context))
            logOutDialog = Dialog(context)
            logOutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            logOutDialog.setContentView(binding.root)
            logOutDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            logOutDialog.window?.attributes?.windowAnimations = R.style.DialogTheme
            logOutDialog.window?.setLayout(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            logOutDialog.setCancelable(false)
            logOutDialog.setCanceledOnTouchOutside(false)
            binding.apply {
                btnYes.setOnClickListener {
                    logOutDialog.dismiss()
                    if (isAirtelDeclaration) {
                        listener.onAirtelDeclarationYesClick()
                    } else {
                        listener.onOkClick()
                    }
                }
                imgClose.setOnClickListener {
                    logOutDialog.dismiss()
                }
                tvMsg.text = msg
                tvTitle.text = title
                btnYes.text = btnText
            }
            logOutDialog.show()
        }


        fun showPermissionDialog(
            activity: Activity,
            msg: String,
            title: String,
            positiveBtn: String,
            negativeBtn: String,
            isFinish: Boolean = true,
            isOtherAction: Boolean = false,
            listener: DialogManager? = null
        ) {
            val binding = DialogPermissionBinding.inflate(LayoutInflater.from(activity))
            permissionDialog = Dialog(activity)
            permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            permissionDialog.setContentView(binding.root)
            permissionDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            permissionDialog.window?.attributes?.windowAnimations = R.style.DialogTheme
            permissionDialog.window?.setLayout(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            permissionDialog.setCancelable(false)
            permissionDialog.setCanceledOnTouchOutside(false)

            binding.apply {
                tvDeny.setOnClickListener {
                    permissionDialog.dismiss()
                    if (isFinish) {
                        activity.finish()
                    } else {
                        permissionDialog.dismiss()
                    }
                }
                tvGoToSettings.setOnClickListener {
                    if (isOtherAction) {
                        permissionDialog.dismiss()
                        listener?.onContinueClick()
                    } else {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            "com.innov.geotracking", null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        activity.startActivity(intent)
                        if (isFinish) {
                            activity.finish()
                        } else {
                            permissionDialog.dismiss()
                        }
                    }
                }
                tvMsg.text = msg
                tvAllowPermission.text = title
                tvDeny.text = negativeBtn
                tvGoToSettings.text = positiveBtn
            }
            permissionDialog.show()
        }










        fun closeAcknowledgeDialog() {
            if (::acknowledgeDialog.isInitialized) {
                acknowledgeDialog.dismiss()
            }
        }

        fun closeAttendanceMarkDialog() {
            if (::markYourAttendanceDialog.isInitialized) {
                markYourAttendanceDialog.dismiss()
            }
        }

        fun closeCompleteYourProfileDialog() {
            if (::completeYourProfileDialog.isInitialized) {
                completeYourProfileDialog.dismiss()
            }
        }

        fun closeReimbursementPreApprovalDialog() {
            if (::reimbursementPreApprovalDialog.isInitialized) {
                reimbursementPreApprovalDialog.dismiss()
            }
        }

    }

    interface DialogManager {
        fun onOkClick() {}
        fun onAirtelDeclarationYesClick() {}
        fun onSelectYear(Year: String) {}

        fun onPaySlipYearSelected(year: String){}
        fun onContinueClick() {}
        fun onConfirmClick(isCheck: Boolean) {}
        fun onMarkAttendanceClick() {}
        fun onAttachmentClick(date: String, name: String, amt: String) {}
        fun onChooseDateClick(view: TextInputEditText) {}
    }
}
package com.kds3393.just.justviewer2.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import com.kds3393.just.justviewer2.databinding.DlgAlertBinding
import common.lib.base.show

/**
 * Created by Administrator on 2016-07-23.
 * 알림 다이어로그
 */
class DlgAlert(context: Context?, private var title: String?, alert: String?, onDialogListener: OnDialogListener?) : BaseDialog(context) {
    private var mesages: String? = alert
    lateinit var binding : DlgAlertBinding

    init {
        this.mOnDialogListener = onDialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DlgAlertBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        if (!TextUtils.isEmpty(title)) {
            binding.alertTitleText.show()
            binding.alertTitleText.text = title
        }

        binding.alertText.text = mesages
        binding.cancelBtn.setOnClickListener {
            mOnDialogListener?.onCancel()
            dismiss()
        }
        binding.okBtn.setOnClickListener {
            mOnDialogListener?.onOk()
            dismiss()
        }
    }

    override fun cancel() {
        super.cancel()
        mOnDialogListener?.onCancel()
        dismiss()
    }
}
package com.kds3393.just.justviewer2.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.databinding.DlgMessageBinding

class DlgMessage : DialogBase(), View.OnClickListener {
    private var binding: DlgMessageBinding? = null

    var mImgResource = 0
    var mTitle: String? = null
    var mMessage: String? = null
    var mBtn01: String? = null
    var mBtn02: String? = null
    var mBtn03: String? = null
    var mBtnImgRes01 = 0
    var mBtnImgRes02 = 0
    var mBtnImgRes03 = 0
    var mIsCancelable = true

    fun apply(imgResource:Int = 0,
              title:String? = null,
              message:String? = null,
              btn01:String? = null,
              btn02:String? = null,
              btn03:String? = null,
              btn01ImgRes:Int = 0,
              btn02ImgRes:Int = 0,
              btn03ImgRes:Int = 0,
              cancelable:Boolean = true) : DlgMessage {
        mImgResource = set("mImgResource",imgResource)
        mTitle = set("mTitle",title)
        mMessage = set("mMessage",message)
        mBtn01 = set("mBtn01",btn01)
        mBtn02 = set("mBtn02",btn02)
        mBtn03 = set("mBtn03",btn03)
        mBtnImgRes01 = set("mBtnImgRes01",btn01ImgRes)
        mBtnImgRes02 = set("mBtnImgRes02",btn02ImgRes)
        mBtnImgRes03 = set("mBtnImgRes03",btn03ImgRes)
        mIsCancelable = set("mIsCancelable",cancelable)
        return this
    }

    protected var mOnMsgDialogListener: OnMsgDialogListener? = null

    interface OnMsgDialogListener {
        fun onBtn01()
        fun onBtn02()
        fun onBtn03()
    }

    fun setOnDialogListener(l: OnMsgDialogListener?) {
        mOnMsgDialogListener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.NewDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = super.onCreateDialog(savedInstanceState)
        dlg.setCancelable(mIsCancelable)
        dlg.setCanceledOnTouchOutside(mIsCancelable) //        dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //        dlg.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //        dlg.getWindow().setNavigationBarColor(Color.RED);
        return dlg
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DlgMessageBinding.inflate(LayoutInflater.from(container!!.context))
        if (mImgResource > 0) {
            binding!!.dlgImg.setImageResource(mImgResource)
        }
        if (TextUtils.isEmpty(mTitle)) {
            binding!!.dlgMsgTitleTv.visibility = View.GONE
        } else {
            binding!!.dlgMsgTitleTv.text = mTitle
        }
        if (TextUtils.isEmpty(mMessage)) {
            binding!!.dlgMsgMessageTv.visibility = View.GONE
        } else {
            binding!!.dlgMsgMessageTv.text = mMessage
        }
        setButton(binding!!.dlgMsgBtnLayout01, binding!!.dlgMsgBtn01, binding!!.dlgMsgIcon01, mBtn01, mBtnImgRes01)
        setButton(binding!!.dlgMsgBtnLayout02, binding!!.dlgMsgBtn02, binding!!.dlgMsgIcon02, mBtn02, mBtnImgRes02)
        setButton(binding!!.dlgMsgBtnLayout03, binding!!.dlgMsgBtn03, binding!!.dlgMsgIcon03, mBtn03, mBtnImgRes03)
        return binding!!.root
    }

    private fun setButton(layout: View, tv: TextView, iv: ImageView, txt: String?, imgRes: Int) {
        if (!TextUtils.isEmpty(txt)) {
            layout.setOnClickListener(this)
            tv.text = txt
            if (imgRes != 0) {
                iv.setImageResource(imgRes)
                iv.visibility = View.VISIBLE
            } else {
                iv.visibility = View.GONE
            }
        } else {
            layout.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        if (v === binding!!.dlgMsgBtnLayout01) {
            dismiss()
            if (mOnMsgDialogListener != null) {
                mOnMsgDialogListener!!.onBtn01()
            }
        } else if (v === binding!!.dlgMsgBtnLayout02) {
            dismiss()
            if (mOnMsgDialogListener != null) {
                mOnMsgDialogListener!!.onBtn02()
            }
        } else if (v === binding!!.dlgMsgBtnLayout03) {
            dismiss()
            if (mOnMsgDialogListener != null) {
                mOnMsgDialogListener!!.onBtn03()
            }
        }
    }

    companion object {
        private const val TAG = "DlgMessage"
    }
}
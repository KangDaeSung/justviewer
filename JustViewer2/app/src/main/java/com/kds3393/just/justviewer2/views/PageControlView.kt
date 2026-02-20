package com.kds3393.just.justviewer2.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import common.lib.utils.LayoutUtils
import common.lib.utils.Size
import common.lib.utils.Utils
import com.kds3393.just.justviewer2.config.SettingImageViewer
import com.kds3393.just.justviewer2.config.SharedPrefHelper
import com.kds3393.just.justviewer2.databinding.VPageControlBinding

class PageControlView : RelativeLayout {
    private var mMoveButtonEditMode = false
    private var mOnPageControlListener: OnPageControlListener? = null

    interface OnPageControlListener {
        fun onPrev()
        fun onNext()
    }

    fun setOnPageControlListener(l: OnPageControlListener?) {
        mOnPageControlListener = l
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private var binding: VPageControlBinding? = null
    private fun init(context: Context) {
        binding = VPageControlBinding.inflate(LayoutInflater.from(getContext()), this)
        if (!SettingImageViewer.getUsePageMoveBtn(getContext())) {
            visibility = GONE
            return
        }
        initButton()
        binding!!.pageMovePrev.setText("이전 페이지")
        binding!!.pageMovePrev.setOnClickListener {
            if (mOnPageControlListener != null) {
                mOnPageControlListener!!.onPrev()
            }
        }
        binding!!.pageMoveNext.setText("다음 페이지")
        binding!!.pageMoveNext.setOnClickListener {
            if (mOnPageControlListener != null) {
                mOnPageControlListener!!.onNext()
            }
        }
        binding!!.pageMovePrev.setBoundView(binding!!.pageMoveNext)
        binding!!.pageMoveNext.setBoundView(binding!!.pageMovePrev)
        binding!!.pageEditClose.text = "설정 완료"
        binding!!.pageEditClose.setOnClickListener {
            setCloseEditMode()
            SharedPrefHelper.setImageLeftBtnPoint(getContext(), binding!!.pageMovePrev.left, binding!!.pageMovePrev.top)
            SharedPrefHelper.setImageRightBtnPoint(getContext(), binding!!.pageMoveNext.left, binding!!.pageMoveNext.top)
            SharedPrefHelper.setImageLeftBtnSize(getContext(), binding!!.pageMovePrev.width, binding!!.pageMovePrev.height)
            SharedPrefHelper.setImageRightBtnSize(getContext(), binding!!.pageMoveNext.width, binding!!.pageMoveNext.height)
        }
    }

    fun setViewer(viewer: View?) {
        if (!SettingImageViewer.getUsePageMoveBtn(context)) {
            return
        }
        binding!!.pageMovePrev.setFunctionView(viewer, binding!!.pageMovePrevResize)
        binding!!.pageMoveNext.setFunctionView(viewer, binding!!.pageMoveNextResize)
        binding!!.pageMovePrev.setMoveMode(MoveScaleButton.MODE_FUNCTION)
        binding!!.pageMoveNext.setMoveMode(MoveScaleButton.MODE_FUNCTION)
    }

    fun cancelEditMode() {
        setCloseEditMode()
        initButton()
    }

    private fun initButton() {
        val prevSize = SharedPrefHelper.getImageLeftBtnSize(context)
        val prevPoint = SharedPrefHelper.getImageLeftBtnPoint(context)
        LayoutUtils.setRelativeLayoutParams(binding!!.pageMovePrev, prevSize.Width, prevSize.Height, prevPoint.x, prevPoint.y, -1)
        val nextSize = SharedPrefHelper.getImageRightBtnSize(context)
        val nextPoint = SharedPrefHelper.getImageRightBtnPoint(context)
        LayoutUtils.setRelativeLayoutParams(binding!!.pageMoveNext, nextSize.Width, nextSize.Height, nextPoint.x, nextPoint.y, -1)
        val resizeBtnSize = Utils.dp2px(40f)
        var x = (prevPoint.x + prevSize.Width) - resizeBtnSize
        var y = (prevPoint.y + prevSize.Height) - resizeBtnSize
        LayoutUtils.setRelativeLayoutParams(binding!!.pageMovePrevResize, resizeBtnSize, resizeBtnSize, x, y, -1)
        binding!!.pageMovePrevResize.setScaleButton(binding!!.pageMovePrev)
        binding!!.pageMovePrevResize.setMaxScaleSize(Size.DisplayWidth, Size.DisplayHeight)
        x = (nextPoint.x + nextSize.Width) - resizeBtnSize
        y = (nextPoint.y + nextSize.Height) - resizeBtnSize
        LayoutUtils.setRelativeLayoutParams(binding!!.pageMoveNextResize, resizeBtnSize, resizeBtnSize, x, y, -1)
        binding!!.pageMoveNextResize.setScaleButton(binding!!.pageMoveNext)
        binding!!.pageMoveNextResize.setMaxScaleSize(Size.DisplayWidth, Size.DisplayHeight)
    }

    fun setCloseEditMode() {
        mMoveButtonEditMode = false
        binding!!.pageMovePrev.setMoveMode(MoveScaleButton.MODE_FUNCTION)
        binding!!.pageMoveNext.setMoveMode(MoveScaleButton.MODE_FUNCTION)
        binding!!.pageEditClose.visibility = INVISIBLE
    }

    fun setMode(type: Int) {
        if (!SettingImageViewer.getUsePageMoveBtn(context)) {
            return
        }
        binding!!.pageMovePrev.setMoveMode(type)
        binding!!.pageMoveNext.setMoveMode(type)
        if (type == MoveScaleButton.MODE_FUNCTION) {
            binding!!.pageMovePrevResize.setViewPoint(binding!!.pageMovePrev.width, binding!!.pageMovePrev.height, binding!!.pageMovePrev.left, binding!!.pageMovePrev.top)
            binding!!.pageMoveNextResize.setViewPoint(binding!!.pageMoveNext.width, binding!!.pageMoveNext.height, binding!!.pageMoveNext.left, binding!!.pageMoveNext.top)
        }
    }

    var isMoveButtonEditMode: Boolean
        get() = mMoveButtonEditMode
        set(editmode) {
            mMoveButtonEditMode = editmode
            if (mMoveButtonEditMode) {
                binding!!.pageMovePrev.setMoveMode(MoveScaleButton.MODE_EDIT)
                binding!!.pageMoveNext.setMoveMode(MoveScaleButton.MODE_EDIT)
                binding!!.pageEditClose.visibility = VISIBLE
            } else {
            }
        }

    fun setSavePref() {
        if (binding!!.pageMovePrev != null) {
            SharedPrefHelper.setImageLeftBtnPoint(context, binding!!.pageMovePrev.left, binding!!.pageMovePrev.top)
            SharedPrefHelper.setImageRightBtnPoint(context, binding!!.pageMoveNext.left, binding!!.pageMoveNext.top)
            SharedPrefHelper.setImageLeftBtnSize(context, binding!!.pageMovePrev.width, binding!!.pageMovePrev.height)
            SharedPrefHelper.setImageRightBtnSize(context, binding!!.pageMoveNext.width, binding!!.pageMoveNext.height)
        }
    }

    companion object {
        private const val TAG = "PageControlView"
    }
}
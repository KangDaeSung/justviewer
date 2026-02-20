package com.kds3393.just.justviewer2.renamer

import android.content.Context
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import com.kds3393.just.justviewer2.config.SettingImageViewer
import com.kds3393.just.justviewer2.databinding.VRenameToolsBinding
import common.lib.utils.Utils

class RenameToolsView : RelativeLayout, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private var mMode = MODE_REPLACE
    private var mOnToolsListener: OnToolsListener? = null

    interface OnToolsListener {
        fun onOK(cmd: String)
        fun onCancel()
        fun onChange(isPreview: Boolean, cmd: String)
    }

    fun setOnToolsListener(l: OnToolsListener?) {
        mOnToolsListener = l
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    //VRenameToolsBinding binding;
    //super(binding.getRoot());
    //this.binding = binding;
    //private SimpleSpinnerDropdownItemBinding binding;
    //binding = SimpleSpinnerDropdownItemBinding.inflate(LayoutInflater.from(parent.getContext()));
    //SimpleSpinnerDropdownItemBinding binding;
    //super(binding.getRoot());
    //this.binding = binding;
    private var binding: VRenameToolsBinding? = null
    private fun init(context: Context) {
        binding = VRenameToolsBinding.inflate(LayoutInflater.from(context), this, true)
        if (!SettingImageViewer.getUsePageMoveBtn(getContext())) {
            visibility = GONE
            return
        }
        binding!!.okBtn.setOnClickListener(this)
        binding!!.cancelBtn.setOnClickListener(this)
        binding!!.okPreview.setOnClickListener(this)
        binding!!.renameToolsNumber1DecBtn.setOnClickListener(this)
        binding!!.renameToolsNumber1IncBtn.setOnClickListener(this)
        binding!!.renameToolsNumber2DecBtn.setOnClickListener(this)
        binding!!.renameToolsNumber2IncBtn.setOnClickListener(this)
    }

    fun show(mode: Int) {
        mMode = mode
        if (mMode == MODE_REPLACE) {
            binding!!.renameToolsTitle.text = "교체"
            binding!!.renameToolsInput1Title.text = "찾을 문자"
            binding!!.renameToolsInput2Title.text = "바꿀 문자"
            binding!!.vSpinnerLayout.visibility = GONE
            binding!!.renameToolsInput1Layout.visibility = VISIBLE
            binding!!.renameToolsInput2Layout.visibility = VISIBLE
            binding!!.renameToolsNumber1DecBtn.visibility = GONE
            binding!!.renameToolsNumber1IncBtn.visibility = GONE
            binding!!.renameToolsNumber2DecBtn.visibility = GONE
            binding!!.renameToolsNumber2IncBtn.visibility = GONE
            binding!!.renameToolsNumber1DecBtn.isEnabled = true
            binding!!.renameToolsNumber1IncBtn.isEnabled = true
            binding!!.renameToolsNumber2DecBtn.isEnabled = true
            binding!!.renameToolsNumber2IncBtn.isEnabled = true
            binding!!.renameToolsInput1.isEnabled = true
            binding!!.renameToolsInput2.isEnabled = true
            binding!!.renameToolsInput1.privateImeOptions = "defaultInputmode=english;"
            binding!!.renameToolsInput2.privateImeOptions = "defaultInputmode=english;"
            binding!!.renameToolsInput1.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
            binding!!.renameToolsInput2.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
            binding!!.renameToolsInput1.setText("")
            binding!!.renameToolsInput2.setText("")
        } else if (mMode == MODE_DELETE) {
            binding!!.renameToolsTitle.text = "삭제"
            binding!!.renameToolsInput1Title.text = "삭제할 위치"
            binding!!.renameToolsInput2Title.text = "삭제할 길이"
            binding!!.vSpinnerLayout.visibility = VISIBLE
            binding!!.renameToolsInput1Layout.visibility = VISIBLE
            binding!!.renameToolsInput2Layout.visibility = VISIBLE
            binding!!.renameToolsNumber1DecBtn.visibility = VISIBLE
            binding!!.renameToolsNumber1IncBtn.visibility = VISIBLE
            binding!!.renameToolsNumber2DecBtn.visibility = VISIBLE
            binding!!.renameToolsNumber2IncBtn.visibility = VISIBLE
            if (binding!!.vSpinner.selectedItemPosition == 0 || binding!!.vSpinner.selectedItemPosition == 1) {
                binding!!.renameToolsInput1.isEnabled = false
                binding!!.renameToolsNumber1DecBtn.isEnabled = false
                binding!!.renameToolsNumber1IncBtn.isEnabled = false
                binding!!.renameToolsInput2.isEnabled = true
                binding!!.renameToolsNumber2DecBtn.isEnabled = true
                binding!!.renameToolsNumber2IncBtn.isEnabled = true
            } else {
                binding!!.renameToolsInput1.isEnabled = true
                binding!!.renameToolsNumber1DecBtn.isEnabled = true
                binding!!.renameToolsNumber1IncBtn.isEnabled = true
                binding!!.renameToolsInput2.isEnabled = true
                binding!!.renameToolsNumber2DecBtn.isEnabled = true
                binding!!.renameToolsNumber2IncBtn.isEnabled = true
            }
            binding!!.renameToolsInput1.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            binding!!.renameToolsInput2.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            binding!!.renameToolsInput1.setText("0")
            binding!!.renameToolsInput2.setText("1")
            val adapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item, MENU_DELETD)
            binding!!.vSpinner.prompt = MENU_DELETD[0] // 스피너 제목
            binding!!.vSpinner.adapter = adapter
            binding!!.vSpinner.onItemSelectedListener = this
        }
        visibility = VISIBLE
    }

    fun hide() {
        Utils.hideKeyboard(binding!!.renameToolsInput1)
        visibility = GONE
    }

    override fun onClick(v: View) {
        if (v === binding!!.okBtn) {
            if (mOnToolsListener != null) {
                if (mMode == MODE_REPLACE) {
                    onReplace(false, binding!!.renameToolsInput1.text.toString(), binding!!.renameToolsInput2.text.toString())
                } else if (mMode == MODE_DELETE) {
                    onDelete(false, binding!!.renameToolsInput1.text.toString(), binding!!.renameToolsInput2.text.toString())
                }
            }
        } else if (v === binding!!.cancelBtn) {
            if (mOnToolsListener != null) {
                mOnToolsListener!!.onCancel()
            }
        } else if (v === binding!!.okPreview) {
            if (mOnToolsListener != null) {
                onReplace(true, binding!!.renameToolsInput1.text.toString(), binding!!.renameToolsInput2.text.toString())
            }
        } else if (v === binding!!.renameToolsNumber1DecBtn) {
            val txt = binding!!.renameToolsInput1.text.toString()
            var number = Integer.valueOf(txt)
            number--
            if (number < 0) {
                return
            }
            binding!!.renameToolsInput1.setText("" + number)
        } else if (v === binding!!.renameToolsNumber1IncBtn) {
            val txt = binding!!.renameToolsInput1.text.toString()
            var number = Integer.valueOf(txt)
            number++
            binding!!.renameToolsInput1.setText("" + number)
        } else if (v === binding!!.renameToolsNumber2DecBtn) {
            val txt = binding!!.renameToolsInput2.text.toString()
            var number = Integer.valueOf(txt)
            number--
            if (number < 0) {
                return
            }
            binding!!.renameToolsInput2.setText("" + number)
        } else if (v === binding!!.renameToolsNumber2IncBtn) {
            val txt = binding!!.renameToolsInput2.text.toString()
            var number = Integer.valueOf(txt)
            number++
            binding!!.renameToolsInput2.setText("" + number)
        }
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
        if (i == 0 || i == 1) {
            binding!!.renameToolsInput1.isEnabled = false
            binding!!.renameToolsNumber1DecBtn.isEnabled = false
            binding!!.renameToolsNumber1IncBtn.isEnabled = false
        } else {
            binding!!.renameToolsInput1.isEnabled = true
            binding!!.renameToolsNumber1DecBtn.isEnabled = true
            binding!!.renameToolsNumber1IncBtn.isEnabled = true
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    private fun onReplace(isPreview: Boolean, target: String, replacement: String) {
        if (TextUtils.isEmpty(target)) {
            mOnToolsListener!!.onCancel()
        } else {
            if (isPreview) {
                mOnToolsListener!!.onChange(isPreview, CMD_REPLACE + ":" + target + ":" + replacement)
            } else {
                mOnToolsListener!!.onOK(CMD_REPLACE + ":" + target + ":" + replacement)
            }
        }
    }

    private fun onDelete(isPreview: Boolean, pos: String, length: String) {
        val subPos = binding!!.vSpinner.selectedItemPosition
        var sub = SUB_DELETE_FIRST
        if (subPos == 1) {
            sub = SUB_DELETE_LAST
        } else if (subPos == 2) {
            sub = SUB_DELETE_FROM_FIRST
        } else if (subPos == 3) {
            sub = SUB_DELETE_FROM_LAST
        }
        if (TextUtils.isEmpty(pos)) {
            mOnToolsListener!!.onCancel()
        } else {
            if (isPreview) {
                mOnToolsListener!!.onChange(isPreview, CMD_DELETE + ":" + sub + ":" + pos + ":" + length)
            } else {
                mOnToolsListener!!.onOK(CMD_DELETE + ":" + sub + ":" + pos + ":" + length)
            }
        }
    }

    companion object {
        private const val TAG = "PageControlView"
        const val CMD_REPLACE = "R"
        const val CMD_DELETE = "D"
        const val SUB_DELETE_FIRST = "F"
        const val SUB_DELETE_LAST = "L"
        const val SUB_DELETE_FROM_FIRST = "FF"
        const val SUB_DELETE_FROM_LAST = "FL"
        const val MODE_REPLACE = 0 //Cmd:R Ex)R:target:replacement
        const val MODE_DELETE = 1 //Cmd:R Ex)D:target:replacement
        private val MENU_DELETD = arrayOf(
            "맨 앞에", "맨 뒤에", "앞에서부터", "뒤에서부터")
    }
}
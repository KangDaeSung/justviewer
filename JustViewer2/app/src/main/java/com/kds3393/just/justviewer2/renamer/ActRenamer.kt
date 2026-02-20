package com.kds3393.just.justviewer2.renamer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import common.lib.utils.FileUtils
import com.cunoraz.tagview.Tag
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.databinding.ActRenameListRowBinding
import com.kds3393.just.justviewer2.databinding.ActRenamerBinding
import com.kds3393.just.justviewer2.renamer.RenameToolsView.OnToolsListener
import androidx.core.view.isVisible
import androidx.core.graphics.toColorInt

class ActRenamer : ActBase(), View.OnClickListener {
    private var mRenameListAdapter: RenameListAdapter? = null
    private var mOldFileList: ArrayList<String?>? = null
    private val mNewFilePathList = ArrayList<String>()
    private val mNewFileNameList = ArrayList<String>()
    private val mNewFileExtList = ArrayList<String>()
    private var binding: ActRenamerBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActRenamerBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.colorPrimaryDark)

        if (intent != null) {
            mOldFileList = intent.getStringArrayListExtra(EXTRA_RENAME_FILE_LIST)
            mOldFileList?.forEach {
                mNewFileNameList.add(FileUtils.getFileName(it!!))
                mNewFileExtList.add(FileUtils.getExtension(it))
                mNewFilePathList.add(FileUtils.getParentPath(it))
                Log.e(TAG, "KDS3393_TEST_FileUtils.getParentPath(path) = " + FileUtils.getParentPath(it))
            }
        }
        binding!!.actFileList.setHasFixedSize(true)
        binding!!.actFileList.layoutManager = LinearLayoutManager(this)
        mRenameListAdapter = RenameListAdapter(mOldFileList)
        binding!!.actFileList.adapter = mRenameListAdapter
        binding!!.frmRenameReplace.setOnClickListener(this)
        binding!!.frmRenameDelete.setOnClickListener(this)
        binding!!.actCmdList.setOnTagClickListener { tag, i -> }
        binding!!.actCmdList.setOnTagDeleteListener { tagView, tag, i -> tagView.remove(i) }
        binding!!.actRenameTools.setOnToolsListener(object : OnToolsListener {
            override fun onOK(cmd: String) {
                onRename(true, false, cmd)
            }

            override fun onCancel() {
                binding!!.actRenameTools.hide()
                binding!!.actFileFuncLayout.visibility = View.VISIBLE
            }

            override fun onChange(isPreview: Boolean, cmd: String) {
                onRename(false, isPreview, cmd)
            }
        })
    }

    override fun onBackPressed() {
        if (binding!!.actRenameTools.isVisible) {
            binding!!.actRenameTools.hide()
            binding!!.actFileFuncLayout.visibility = View.VISIBLE
            return
        }
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onClick(v: View) {
        if (v === binding!!.frmRenameReplace) {
            binding!!.actRenameTools.show(RenameToolsView.MODE_REPLACE)
            binding!!.actFileFuncLayout.visibility = View.GONE
        } else if (v === binding!!.frmRenameDelete) {
            binding!!.actRenameTools.show(RenameToolsView.MODE_DELETE)
            binding!!.actFileFuncLayout.visibility = View.GONE
        }
    }

    private fun onRename(isAll: Boolean, isPreview: Boolean, cmd: String) {
        if (!isPreview) {
            makeTag(binding!!.actCmdList.tags.size - 1, cmd)
        }
        if (isAll) {
            mNewFileNameList.clear()
            mOldFileList?.forEach {
                mNewFileNameList.add(FileUtils.getFileName(it!!))
            }
            for (c in binding!!.actCmdList.tags) {
                runCommand(c.text)
            }
        } else {
            runCommand(cmd)
        }
    }

    private fun runCommand(cmd: String) {
        val cmds = cmd.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (cmds[0] == RenameToolsView.CMD_REPLACE) {
            val replacement = if (cmds.size > 2) cmds[2] else ""
            runReplace(cmds[1], replacement)
        } else if (cmds[0] == RenameToolsView.CMD_DELETE) {
            runDelete(cmds[1], cmds[2], cmds[3])
        }
    }

    private fun runReplace(target: String, replacement: String) {
        for (i in mNewFileNameList.indices) {
            val replace = mNewFileNameList[i].replace(target, replacement)
            mNewFileNameList[i] = replace
        }
        mRenameListAdapter!!.notifyDataSetChanged()
    }

    private fun runDelete(subCmd: String, tpos: String, tlength: String) {
        var pos = Integer.valueOf(tpos)
        var length = Integer.valueOf(tlength)
        for (i in mNewFileNameList.indices) {
            val text = mNewFileNameList[i]
            var result = text
            if (subCmd == RenameToolsView.SUB_DELETE_FIRST) {  //맨 앞에
                if (length > text.length) {
                    length = text.length
                }
                result = text.substring(length, text.length)
            } else if (subCmd == RenameToolsView.SUB_DELETE_LAST) { //맨 뒤에
                var inLength = 0
                if (length > text.length) {
                    length = text.length
                    inLength = 0
                } else {
                    inLength = text.length - length
                }
                result = text.substring(0, inLength)
            } else if (subCmd == RenameToolsView.SUB_DELETE_FROM_FIRST) { //앞에서부터
                if (pos + length > text.length) {
                    length = if (pos >= text.length) {
                        return
                    } else {
                        text.length - pos
                    }
                }
                result = text.substring(pos, length)
            } else if (subCmd == RenameToolsView.SUB_DELETE_FROM_LAST) { //뒤에서부터
                pos = text.length - pos
                if (pos + length > text.length) {
                    length = if (pos >= text.length) {
                        return
                    } else {
                        text.length - pos
                    }
                }
                result = text.substring(pos, length)
            }
            mNewFileNameList[i] = result
        }
        mRenameListAdapter!!.notifyDataSetChanged()
    }

    private fun makeTag(position: Int, text: String): Tag {
        val tag = Tag(text)
        tag.id = position
        tag.tagTextSize = 15f
        tag.tagTextColor = "#4a4a4a".toColorInt()
        tag.layoutBorderColor = "#74ce70".toColorInt()
        tag.background = resources.getDrawable(R.drawable.rect_s_74ce70_2_7dp)
        tag.isDeletable = true
        tag.deleteIndicatorColor = "#bcbcbc".toColorInt()
        tag.deleteIndicatorSize = 15f
        binding!!.actCmdList.addTag(tag)
        return tag
    }

    inner class RenameListAdapter(dataArray: List<String?>?) : BaseQuickAdapter<String?, FileRecyclerViewHolder>(R.layout.act_rename_list_row, dataArray) {
        val MODE_CHECK_OFF = 0
        val MODE_CHECK_ON = 1
        val MODE_CHECK_DISABLE = 2

        val checkedMap = HashMap<Int, Boolean?>()
        private var mCheckedMode = MODE_CHECK_OFF
        fun clearChecked() {
            if (mCheckedMode != MODE_CHECK_DISABLE) {
                mCheckedMode = MODE_CHECK_OFF
            }
            checkedMap.clear()
            notifyDataSetChanged()
        }

        val checkedItems: ArrayList<String?>
            get() {
                val array = ArrayList<String?>()
                for (key in checkedMap.keys) {
                    if (checkedMap[key]!!) {
                        array.add(getItem(key))
                    }
                }
                return array
            }
        val isCheckedMode: Boolean
            get() = mCheckedMode == MODE_CHECK_ON

        override fun onBindViewHolder(viewHolder: FileRecyclerViewHolder, position: Int) {
            viewHolder.init(position, this)
            super.onBindViewHolder(viewHolder, position)
        }

        override fun convert(helper: FileRecyclerViewHolder, item: String?) {
            helper.setData(item, mNewFileNameList[helper.bindingAdapterPosition] + mNewFileExtList[helper.bindingAdapterPosition])
        }
    }

    class FileRecyclerViewHolder(view: View?) : BaseViewHolder(view), View.OnClickListener {
        private var mPosition = 0
        private var mAdapter: RenameListAdapter? = null
        private var mOldData: String? = null
        private var mNewData: String? = null
        private val binding: ActRenameListRowBinding

        init {
            binding = ActRenameListRowBinding.bind(view!!)
        }

        fun init(position: Int, adapter: RenameListAdapter?) {
            mPosition = position
            mAdapter = adapter
        }

        fun setData(oldData: String?, newData: String?) {
            mOldData = oldData
            mNewData = newData
            if (mOldData == null) {
                return
            }
            binding.fileNameOld.text = FileUtils.getName(mOldData!!)
            binding.fileNameNew.text = mNewData
            binding.fileFavoChk.visibility = View.GONE
            binding.fileFavoChkDisable.visibility = View.GONE
            binding.fileFavoChk.setOnClickListener(this)
            binding.fileFavoChk.isSelected = (if (mAdapter!!.checkedMap[mPosition] == null) false else mAdapter!!.checkedMap[mPosition])!!
        }

        override fun onClick(v: View) {}

        companion object {
            private const val TAG = "StarRecyclerViewHolder"
        }
    }

    companion object {
        private const val TAG = "ActRenamer"
        const val EXTRA_RENAME_FILE_LIST = "EXTRA_RENAME_FILE_LIST"
    }
}
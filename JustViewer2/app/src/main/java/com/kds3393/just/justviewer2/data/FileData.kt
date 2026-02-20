package com.kds3393.just.justviewer2.data

import android.text.TextUtils
import common.lib.utils.FileUtils
import java.io.File
import java.io.Serializable

class FileData : ItemBase, Serializable {

    // --- 기존 FavoItemData 속성 ---
    var mType = 0               // Local or Network
    var mId: Long = -1          // DB ID
    var mOrderIndex = 0         // 정렬 순서

    // --- 네트워크 속성 통합 (mNetId, mNetPw/mNetPass -> mNetPass) ---
    var mNetId: String? = null
    var mNetPass: String? = null

    // --- 기존 FileData 속성 ---
    var mIsFavoriteFile = false
    var mIsBookmarked = false
    var mDisplayName: String? = null // 표시 이름 (FavoItemData의 mName 역할 통합)
    var mExt: String? = null         // 확장자
    var mIsDirectory = false         // 디렉토리 유무

    constructor() : super("")

    constructor(file: File) : super(file.path) {
        mDisplayName = FileUtils.getName(file.name)
        mExt = FileUtils.getExtension(file.name)
        mIsDirectory = file.isDirectory
    }

    constructor(type: Int, path: String, name: String?) : super(path) {
        mType = type
        mDisplayName = if (TextUtils.isEmpty(name)) {
            FileUtils.getFileName(path)
        } else {
            name
        }
        if (path.isNotEmpty()) {
            mIsDirectory = File(path).isDirectory
        }
    }

    override fun toString(): String {
        return "id = $mId, name = $mDisplayName, isDir = $mIsDirectory, path = $mPath, type = $mType, orderIndex = $mOrderIndex, NetId = $mNetId, NetPass = $mNetPass"
    }

    companion object {
        const val TYPE_LOCAL_DIR = 0  // 로컬 폴더
        const val TYTP_NETWORK = 1    // 네트워크 드라이브
        const val TYPE_LOCAL_FILE = 2 // 로컬 파일
    }
}
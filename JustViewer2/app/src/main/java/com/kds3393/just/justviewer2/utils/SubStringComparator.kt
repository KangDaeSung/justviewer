package com.kds3393.just.justviewer2.utils

import com.kds3393.just.justviewer2.data.FileData
import org.apache.tools.zip.ZipEntry
import java.io.File
import java.text.Collator

class SubStringComparator : Comparator<Any> {
    private val collator = Collator.getInstance()
    private var mIsASC = true

    constructor() {
        mIsASC = true
    }

    constructor(isASC: Boolean) {
        mIsASC = isASC
    }

    override fun compare(obj1: Any, obj2: Any): Int {
        var maxLength = 0
        var str1: String? = null
        var str2: String? = null
        if (obj1 is FileData && obj2 is FileData) {
            str1 = getComparatorStr(obj1.mPath)
            str2 = getComparatorStr(obj2.mPath)
        } else {
            str1 = getComparatorStr(obj1)
            str2 = getComparatorStr(obj2)
        }
        if (str1.length != str2.length) {
            maxLength = Math.max(str1.length, str2.length)
            if (str1.length < str2.length) {
                str1 = getFitStringLength(str1, str2, maxLength)
            } else {
                str2 = getFitStringLength(str2, str1, maxLength)
            }
        }
        return if (mIsASC) collator.compare(str1, str2) else collator.compare(str2, str1)
    }

    private fun getComparatorStr(obj: Any): String {
        if (obj is File) {
            return obj.name
        } else if (obj is ZipEntry) {
            return obj.name
        } else if (obj is String) {
            return obj
        }
        return ""
    }

    private fun getFitStringLength(str1: String?, str2: String?, max: Int): String? {
        var str1 = str1
        var temp = ""
        for (i in 0 until max - str1!!.length) {
            temp += "0"
        }
        val str1Array = ArrayList<String?>()
        val str2Array = ArrayList<String?>()
        parseString(str1, str1Array)
        parseString(str2, str2Array)
        str1 = ""
        var isPass = false
        for (i in str1Array.indices) {
            if (!isPass && str2Array.size > i && str1Array[i]!!.length != str2Array[i]!!.length) {
                if (str1Array[i]!![0] >= '0' && str1Array[i]!![0] <= '9') {
                    str1 = str1 + temp + str1Array[i]
                    isPass = true
                    continue
                }
            }
            str1 += str1Array[i]
        }
        return str1
    }

    private fun parseString(str: String?, strArray: ArrayList<String?>): Boolean {
        if (str!!.length <= 0) return true
        var isNum = false
        if (str[0] >= '0' && str[0] <= '9') {
            isNum = true
        }
        for (i in 0 until str.length) {
            if (str[i] >= '0' && str[i] <= '9') {
                if (!isNum) {
                    strArray.add(str.substring(0, i))
                    if (i + 1 < str.length) parseString(str.substring(i, str.length), strArray)
                    return true
                }
            } else {
                if (isNum) {
                    strArray.add(str.substring(0, i))
                    if (i + 1 < str.length) parseString(str.substring(i, str.length), strArray)
                    return true
                }
            }
        }
        strArray.add(str)
        return true
    }
}
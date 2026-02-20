package com.kds3393.just.justviewer2.data

import java.io.Serializable

class TextItemData : ItemBase, Serializable {
    var mId = -1L       //DB ID
    var mPageNum = 0    //현재 페이지
    var mHighLine = 0   //해당 값 이전의 글은 숨긴다.

    constructor() {}
    constructor(path: String, page: Int) {
        mPath = path
        mPageNum = page
    }

    override fun toString(): String {
        return "TextItemData : id = $mId page = $mPageNum highline = $mHighLine path = $mPath"
    }
}
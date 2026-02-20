package common.lib.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import java.util.*

class SpanBuilder : SpannableStringBuilder() {
    fun Bold(text: String, vararg targetText: String): SpanBuilder {
        val offset = length
        append(text)
        if (targetText.size > 0) {
            for (target in targetText) {
                if (TextUtils.isEmpty(target)) {
                    continue
                }
                val start = offset + text.indexOf(target)
                setSpan(StyleSpan(Typeface.BOLD), start, start + target.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            setSpan(StyleSpan(Typeface.BOLD), offset, offset + text.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return this
    }

    fun Color(text: String, color: Int, vararg targetText: String): SpanBuilder {
        val offset = length
        append(text)
        if (targetText.size > 0) {
            for (target in targetText) {
                if (TextUtils.isEmpty(target)) {
                    continue
                }
                var end = 0
                var start = text.indexOf(target)
                while (start >= 0) {
                    end = start + target.length
                    setSpan(ForegroundColorSpan(color), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    start = text.indexOf(target, end)
                }
            }
        } else {
            setSpan(ForegroundColorSpan(color), offset, offset + text.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return this
    }

    fun BgColor(text: String?, color: Int, vararg targetText: String): SpanBuilder {
        var text = text
        val offset = length
        text?.let { append(it) }
        text = this.toString()
        if (targetText.size > 0) {
            for (target in targetText) {
                if (TextUtils.isEmpty(target)) {
                    continue
                }
                var end = 0
                var start = text.indexOf(target)
                while (start >= 0) {
                    end = start + target.length
                    setSpan(BackgroundColorSpan(color), start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    start = text.indexOf(target, end)
                }
            }
        } else {
            setSpan(ForegroundColorSpan(color), offset, offset + text.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return this
    }

    companion object {
        fun with(): SpanBuilder {
            return SpanBuilder()
        }

        fun Color(text: String, color: Int): SpannableString {
            val span = SpannableString(text)
            span.setSpan(ForegroundColorSpan(color), 0, text.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            return span
        }

        fun setHighLightedText(tv: TextView, textToHighlight: String, textColor: Int, bgColor: Int) {
            val tvt = tv.text.toString()
            var ofe = tvt.lowercase(Locale.getDefault()).indexOf(textToHighlight.lowercase(Locale.getDefault()), 0)
            val wordToSpan: Spannable = SpannableString(tv.text)
            var ofs = 0
            while (ofs < tvt.length && ofe != -1) {
                ofe = tvt.lowercase(Locale.getDefault()).indexOf(textToHighlight.lowercase(Locale.getDefault()), ofs)
                if (ofe == -1) break else { // set color here
                    wordToSpan.setSpan(BackgroundColorSpan(bgColor), ofe, ofe + textToHighlight.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    wordToSpan.setSpan(ForegroundColorSpan(textColor), ofe, ofe + textToHighlight.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                    tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
                }
                ofs = ofe + 1
            }
        }
    }
}
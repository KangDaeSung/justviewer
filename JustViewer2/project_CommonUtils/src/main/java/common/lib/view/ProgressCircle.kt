package common.lib.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import common.lib.base.CoroutineTask
import common.lib.utils.LayoutUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class ProgressCircle(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {
    var max = 100f
    var progress = 0f
        private set
    private var mIsInfinity = false
    private var mInfinityStartDegrees = -90f
    private var mDegrees = 0f
    private var mScale = 0.5f
    private var mPaints: Paint? = null
    private var mBGCirclePaints: Paint? = null
    private var mTextPaints: Paint? = null
    private var mTextSize = 36f
    private var mBigOval: RectF? = null
    private var mCircleStroke = 2
    private var mIsShowText = false
    private var mPersent = 0

    private fun init() {
        mPaints = Paint()
        mPaints!!.style = Paint.Style.STROKE
        mPaints!!.strokeWidth = mCircleStroke.toFloat()
        mPaints!!.color = Color.WHITE
        mPaints!!.isAntiAlias = true
        mPaints!!.strokeCap = Paint.Cap.BUTT
        mBGCirclePaints = Paint()
        mBGCirclePaints!!.style = Paint.Style.STROKE
        mBGCirclePaints!!.strokeWidth = mCircleStroke.toFloat()
        mBGCirclePaints!!.color = Color.BLACK
        mBGCirclePaints!!.isAntiAlias = true
        mBGCirclePaints!!.strokeCap = Paint.Cap.BUTT
        mTextPaints = Paint()
        mTextPaints!!.isAntiAlias = true
        mTextPaints!!.textSize = mTextSize
        mTextPaints!!.color = Color.BLACK
        mBigOval = RectF()
    }

    var isInfinity: Boolean
        get() = mIsInfinity
        set(isInfinity) {
            mIsInfinity = isInfinity
            if (mIsInfinity && mInfinityTask.isActive) mInfinityTask.execute()
        }

    fun setProgress(value: Int) {
        progress = value.toFloat()
        mDegrees = progress / max * 360.0f
        mPersent = (progress / max * 100).toInt()
        invalidate()
    }

    fun setCircleColor(color: Int) {
        mPaints!!.color = color
    }

    fun setBGCircleColor(color: Int) {
        mBGCirclePaints!!.color = color
    }

    fun setCircleScale(scale: Float) {
        mScale = scale
    }

    fun setStrokeWidth(stroke: Int) {
        mCircleStroke = stroke
        mPaints!!.strokeWidth = mCircleStroke.toFloat()
        mBGCirclePaints!!.strokeWidth = mCircleStroke.toFloat()
    }

    fun setShowPersent(isShow: Boolean) {
        mIsShowText = isShow
    }

    fun setPersentColor(color: Int) {
        mTextPaints!!.color = color
    }

    private fun drawArcs(canvas: Canvas, oval: RectF?, useCenter: Boolean, paint: Paint?) {
        canvas.drawArc(oval!!, -90f, mDegrees, useCenter, paint!!)
        canvas.drawArc(oval, -90f, mDegrees - 360, useCenter, mBGCirclePaints!!)
    }

    private fun drawInfinityArcs(canvas: Canvas, oval: RectF?, useCenter: Boolean, paint: Paint?) {
        canvas.drawArc(oval!!, mInfinityStartDegrees, 90f, useCenter, paint!!)
        canvas.drawArc(oval, mInfinityStartDegrees - 360, (90 - 360).toFloat(), useCenter, mBGCirclePaints!!)
    }

    override fun onDraw(canvas: Canvas) {
        if (progress >= 0) {
            setBound()    //canvas.drawColor(Color.alpha(Color.CYAN));
            if (mIsInfinity) {
                drawInfinityArcs(canvas, mBigOval, false, mPaints)
            } else {
                drawArcs(canvas, mBigOval, false, mPaints)
                if (mIsShowText) {
                    drawText(canvas)
                }
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        val testText = "$max%"
        val limitWidth = (mBigOval!!.width() * 0.4).toFloat()
        for (i in 0..23) {
            val texthor = mTextPaints!!.measureText(testText) / 2
            if (limitWidth < texthor) {
                mTextSize--
                mTextPaints!!.textSize = mTextSize
            }
        }
        val text = "$mPersent%"
        val textHeight = mTextPaints!!.descent() - mTextPaints!!.ascent()
        val texttopOffset = textHeight / 2 - mTextPaints!!.descent()
        val textLeftOffset = mTextPaints!!.measureText(text) / 2
        canvas.drawText("$mPersent%", width / 2 - textLeftOffset, height / 2 + texttopOffset, mTextPaints!!)
    }

    private fun setBound() {
        val width = width
        val height = height
        val size = ((if (width < height) width else height) * mScale).toInt()
        val left = (width - size) / 2
        val top = (height - size) / 2
        mBigOval!![left.toFloat(), top.toFloat(), (left + size).toFloat()] = (top + size).toFloat()
    }

    private val mInfinityTask = CoroutineTask(onPreExecute = {
        mInfinityStartDegrees = -90f
    }, doInBackground = { params ->
        if (params != null) {
            try {
                while (true) {
                    delay(100)
                    if (mIsInfinity) {
                        onProgressUpdate()
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }, onPostExecute = {

    })

    private fun onProgressUpdate() {
        if (mIsInfinity) {
            mInfinityStartDegrees += 10f
            if (mInfinityStartDegrees > 270) {
                mInfinityStartDegrees = -90f
            }
            invalidate()
        }
    }

    companion object {
        private const val TAG = "PregressCircle"
        @JvmOverloads
        fun make(context: Context?, parent: ViewGroup, width: Int, height: Int, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0): ProgressCircle {
            val view = ProgressCircle(context)
            parent.addView(view)
            common.lib.utils.LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom)
            return view
        }
    }

    init {
        init()
    }
}
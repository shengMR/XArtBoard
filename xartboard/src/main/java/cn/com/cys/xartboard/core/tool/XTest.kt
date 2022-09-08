package cn.com.cys.xartboard.core.tool

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import cn.com.cys.xartboard.helper.XLog

/**
 * Author: Damon
 * Date: 2022/8/26 17:05
 * Description 测试类
 */
class XTest(private var isTest: Boolean = false) {

    private val mTestPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
    }
    private val mTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.RED
            style = Paint.Style.FILL
        }
    }

    init {
        XLog.debug(isTest)
    }

    fun drawRectF(canvas: Canvas, rectF: RectF, color: Int) {
        if (!isTest) {
            return
        }
        mTestPaint.color = color
        canvas.drawRect(rectF, mTestPaint)
    }

    fun drawText(canvas: Canvas, text: String, x: Float, y: Float, color: Int) {
        if (!isTest) {
            return
        }
        mTextPaint.color = color
        mTextPaint.textSize = 48f
        canvas.drawText(text, x, y, mTextPaint)
    }

}
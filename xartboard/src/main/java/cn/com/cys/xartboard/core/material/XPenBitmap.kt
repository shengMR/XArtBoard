package cn.com.cys.xartboard.core.material

import android.graphics.*
import android.view.MotionEvent
import cn.com.cys.xartboard.core.tool.XGravity

/**
 * Author: Damon
 * Date: 2022/8/18 08:56
 * Description
 */
class XPenBitmap(
    private val srcBitmap: Bitmap,
    private val gravity: XGravity = XGravity.GravityCenter,
    val currentRect: RectF = RectF()
) : XMaterial() {

    private var bitmapScale = 1f
    private val bitmapFrame = RectF()
    private val bitmapMatrix = Matrix()
    private var wrapperPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    fun getMatrix(): Matrix {
        return bitmapMatrix
    }

    fun getScale(): Float{
        return bitmapScale
    }

    fun onWindowChange(isLandscape: Boolean, width: Int, height: Int) {

        bitmapFrame.set(0f, 0f, srcBitmap.width.toFloat(), srcBitmap.height.toFloat())
        val scale = Math.min(width * 1.0f / srcBitmap.width, height * 1.0f / srcBitmap.height)
        this.bitmapScale = scale
        bitmapMatrix.setScale(scale, scale, srcBitmap.width / 2f, srcBitmap.height / 2f)
        when (gravity) {
            XGravity.GravityStart -> {
                if (isLandscape) {
                    bitmapMatrix.postTranslate(0f, height / 2f - bitmapFrame.height() / 2f)
                } else {
                    bitmapMatrix.postTranslate(width / 2f - bitmapFrame.width() / 2f, 0f)
                }
            }
            XGravity.GravityEnd -> {
                if (isLandscape) {
                    bitmapMatrix.postTranslate(
                        width - bitmapFrame.width(),
                        height / 2f - bitmapFrame.height() / 2f
                    )
                } else {
                    bitmapMatrix.postTranslate(
                        width / 2f - bitmapFrame.width() / 2f,
                        height - bitmapFrame.height()
                    )
                }
            }
            else -> {
                bitmapMatrix.postTranslate(
                    width / 2f - bitmapFrame.width() / 2f,
                    height / 2f - bitmapFrame.height() / 2f
                )
            }
        }
        bitmapMatrix.mapRect(currentRect)
        when (gravity) {
            XGravity.GravityStart -> {
                bitmapMatrix.setTranslate(-currentRect.left, 0f)
                bitmapMatrix.mapRect(currentRect)
            }
            XGravity.GravityEnd -> {
                bitmapMatrix.setTranslate(width - currentRect.right, 0f)
                bitmapMatrix.mapRect(currentRect)
            }
            else -> {}
        }
    }

    override fun isTouchMe(x: Float, y: Float): Boolean {
        return false
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(srcBitmap, null, currentRect, null)
        if (isSelected) {
            // canvas.drawRect(currentRect, wrapperPaint)
        }
    }

    override fun touchDown(event: MotionEvent) {

    }

    override fun touchUp(event: MotionEvent) {

    }

    override fun touchScroll(distanceX: Float, distanceY: Float) {
        bitmapMatrix.setTranslate(-distanceX, -distanceY)
        bitmapMatrix.mapRect(currentRect)
    }

    override fun touchScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        bitmapMatrix.setScale(scaleFactor, scaleFactor, focusX, focusY)
        bitmapMatrix.mapRect(currentRect)
    }
}
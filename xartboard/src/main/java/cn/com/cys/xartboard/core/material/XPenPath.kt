package cn.com.cys.xartboard.core.material

import android.graphics.*
import android.view.MotionEvent
import cn.com.cys.xartboard.core.tool.XPenType
import kotlin.math.abs

/**
 * Author: Damon
 * Date: 2022/8/17 19:00
 * Description 画笔路径
 */
data class XPenPath(
    private val penColor: Int = Color.BLACK,
    private var penWidth: Float = 10f,
    private val penType: XPenType,
    val penPath: Path = Path(),
    private var pathRectF: RectF = RectF()
) : XMaterial() {

    private val pathPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = penColor
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        pathEffect = CornerPathEffect(50f)
        strokeWidth = penWidth
    }
    private val graphicsPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = penColor
            strokeWidth = penWidth
        }
    }
    private val wrapperPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = color
            strokeWidth = 5f
        }
    }

    private val pathMatrix = Matrix()
    private val firstPoint = PointF()
    private val lastPoint = PointF()
    private val graphicsRectF = RectF()

    fun setFirstPoint(x: Float, y: Float) {
        firstPoint.set(x, y)
    }

    fun setLastPoint(x: Float, y: Float) {
        lastPoint.set(x, y)
        when (penType) {
            XPenType.RECT,
            XPenType.SQUARE,
            XPenType.OVAL,
            XPenType.CIRCLE -> {
                val dx = lastPoint.x - firstPoint.x
                val dy = lastPoint.y - firstPoint.y
                val min = abs(dx.coerceAtMost(dy))
                if (dx < 0) {
                    if (dy < 0) {
                        if (penType == XPenType.SQUARE || penType == XPenType.CIRCLE) {
                            lastPoint.x = firstPoint.x - min
                            lastPoint.y = firstPoint.y - min
                        }
                        graphicsRectF.set(lastPoint.x, lastPoint.y, firstPoint.x, firstPoint.y)
                    } else {
                        if (penType == XPenType.SQUARE || penType == XPenType.CIRCLE) {
                            lastPoint.x = firstPoint.x - min
                            lastPoint.y = firstPoint.y + min
                        }
                        graphicsRectF.set(lastPoint.x, firstPoint.y, firstPoint.x, lastPoint.y)
                    }
                } else {
                    if (dy < 0) {
                        if (penType == XPenType.SQUARE || penType == XPenType.CIRCLE) {
                            lastPoint.x = firstPoint.x + min
                            lastPoint.y = firstPoint.y - min
                        }
                        graphicsRectF.set(firstPoint.x, lastPoint.y, lastPoint.x, firstPoint.y)
                    } else {
                        if (penType == XPenType.SQUARE || penType == XPenType.CIRCLE) {
                            lastPoint.x = firstPoint.x + min
                            lastPoint.y = firstPoint.y + min
                        }
                        graphicsRectF.set(firstPoint.x, firstPoint.y, lastPoint.x, lastPoint.y)
                    }
                }
                penPath.reset()
                if (penType == XPenType.RECT || penType == XPenType.SQUARE) {
                    penPath.addRect(graphicsRectF, Path.Direction.CW)
                } else if (penType == XPenType.OVAL || penType == XPenType.CIRCLE) {
                    penPath.addOval(graphicsRectF, Path.Direction.CW)
                }
            }
            else -> {

            }
        }
    }

    override fun isTouchMe(x: Float, y: Float): Boolean {
        penPath.computeBounds(pathRectF, false)
        return pathRectF.contains(x, y)
    }

    override fun draw(canvas: Canvas) {
        when (penType) {
            XPenType.LINE -> {
                canvas.drawPath(penPath, pathPaint)
            }
            XPenType.ERASER -> {
                pathPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                canvas.drawPath(penPath, pathPaint)
                pathPaint.xfermode = null
            }
            XPenType.RECT,
            XPenType.SQUARE,
            XPenType.OVAL,
            XPenType.CIRCLE -> {
                canvas.drawPath(penPath, graphicsPaint)
            }
            else -> {
                canvas.drawPath(penPath, pathPaint)
            }
        }
        if (isSelected) {
            // canvas.save()
            // wrapperPaint.strokeWidth = 5f
            // canvas.drawRect(pathRectF, wrapperPaint)
            // canvas.drawText("X", pathRectF.left - 30f, pathRectF.top - 30f, wrapperPaint)
            // canvas.restore()
        }
    }

    override fun touchDown(event: MotionEvent) {

    }

    override fun touchUp(event: MotionEvent) {

    }

    override fun touchScroll(distanceX: Float, distanceY: Float) {
        pathMatrix.setTranslate(-distanceX, -distanceY)
        penPath.transform(pathMatrix)
    }

    override fun touchScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        pathMatrix.setScale(scaleFactor, scaleFactor, focusX, focusY)
        penPath.transform(pathMatrix)
        pathPaint.strokeWidth = pathPaint.strokeWidth * scaleFactor
        graphicsPaint.strokeWidth = graphicsPaint.strokeWidth * scaleFactor
    }


}
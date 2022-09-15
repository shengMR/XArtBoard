package cn.com.cys.xartboard.core.tool

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import cn.com.cys.xartboard.XArtBoard
import cn.com.cys.xartboard.helper.XLog
import cn.com.cys.xartboard.core.material.XMaterial
import cn.com.cys.xartboard.core.material.XPenBitmap
import cn.com.cys.xartboard.core.material.XPenPath
import java.lang.ref.WeakReference

/**
 * Author: Damon
 * Date: 2022/8/17 19:00
 * Description 画笔
 */
class XPen {

    companion object {
        const val DEFAULT_PEN_WIDTH = 10f
        const val DEFAULT_ERASER_WIDTH = 10f
    }

    private var penColor = Color.RED
    private var penWidth = DEFAULT_PEN_WIDTH
    private var penEraserWidth = DEFAULT_ERASER_WIDTH
    private val retractPenPaths = mutableListOf<XMaterial>() // 记录撤回的路径
    private val penPaths = mutableListOf<XMaterial>()
    private val penBitmaps = mutableListOf<XPenBitmap>()

    private var penType = XPenType.NULL
    private var currentXPenBitmap: XPenBitmap? = null
    private var currentXPenPath: XMaterial? = null
    private var selectedXPenPath: XMaterial? = null
    private var xArtBoard: XArtBoard? = null

    private val penPathCompleteHandler = PathCompleteHandler(this)

    class PathCompleteHandler(xPen: XPen) : Handler() {

        private val weakPen = WeakReference(xPen)

        override fun handleMessage(msg: Message) {
            weakPen.get()?.let {
                when (msg.what) {
                    101 -> {
                        XLog.e("画笔路径合并")
                        val penPath = msg.obj as XPenPath
                        it.penPaths.add(penPath)
                        it.currentXPenPath = null
                        it.xArtBoard?.refreshCanvas()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun setArtBoard(xArtBoard: XArtBoard) {
        this.xArtBoard = xArtBoard
    }

    fun addPenBitmap(XPenBitmap: XPenBitmap) {
        penBitmaps.remove(XPenBitmap)
        penBitmaps.add(XPenBitmap)
    }

    fun setPenPaths(penPaths: List<XMaterial>){
        this.penPaths.clear()
        this.penPaths.addAll(penPaths)
        retractPenPaths.clear()
        xArtBoard?.refreshCanvas()
    }

    fun getPenPaths(): List<XMaterial>{
        return penPaths
    }

    fun getMaxScale(): Float{
        var scale = 1f
        penBitmaps.forEach {
            val bitmapScale = it.getScale()
            scale = if(scale > bitmapScale)  bitmapScale else scale
        }
        return 1f / scale
    }

    ////// ===================================== 按下 ========================================
    fun onTouchDown(event: MotionEvent) {
        if (penType == XPenType.SELECT) {
            touchDownMoveMode(event)
        } else if (penType == XPenType.LINE) {
            touchDownLine(event)
        } else if (penType == XPenType.ERASER) {
            touchDownEraser(event)
        } else if (penType == XPenType.RECT || penType == XPenType.SQUARE || penType == XPenType.OVAL || penType == XPenType.CIRCLE) {
            touchDownRect(event)
        }
    }

    private fun touchDownMoveMode(event: MotionEvent) {
        //  选中某条线或者某个图片
        selectedXPenPath?.isSelected = false
        selectedXPenPath = null
        // currentXPenBitmap?.isSelected = false
        // currentXPenBitmap = null
        var selectedIndex = penPaths.size - 1
        for (index in selectedIndex downTo 0) {
            val penPath = penPaths[index]
            if (penPath.isTouchMe(event.x, event.y)) {
                XLog.e("点击在路径上面")
                penPath.isSelected = true
                selectedIndex = index
                selectedXPenPath = penPath
                break
            } else {
                penPath.isSelected = false
            }
        }
        for (index in (selectedIndex - 1) downTo 0) {
            val penPath = penPaths[index]
            penPath.isSelected = false
        }

        if (selectedXPenPath != null) {
            return
        }
        // selectedIndex = penBitmaps.size - 1
        // for (index in selectedIndex downTo 0) {
        //     val bitmap = penBitmaps[index]
        //     val matrix = bitmap.mCurrentRect
        //     if (matrix.contains(event.x, event.y)) {
        //         CLog.e("点击在图片上面")
        //         bitmap.isSelected = true
        //         selectedIndex = index
        //         currentXPenBitmap = bitmap
        //         break
        //     } else {
        //         bitmap.isSelected = false
        //     }
        // }
        // for (index in (selectedIndex - 1) downTo 0) {
        //     val bitmap = penBitmaps[index]
        //     bitmap.isSelected = false
        // }
    }

    private fun touchDownLine(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.moveTo(event.x, event.y)
            }
        }
    }

    private fun touchDownEraser(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.moveTo(event.x, event.y)
            }
        }
    }

    private fun touchDownRect(event: MotionEvent) {
        currentXPenPath = XPenPath(
            penColor,
            if (penType == XPenType.ERASER) penEraserWidth else penWidth,
            penType
        )
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.setFirstPoint(event.x, event.y)
            }
        }
    }

    fun onTouchUp(event: MotionEvent) {

    }

    ////// ===================================== 滑动 ========================================
    fun onCanvasScroll(distanceX: Float, distanceY: Float) {
        penBitmaps.forEach {
            it.touchScroll(distanceX, distanceY)
        }
        penPaths.forEach {
            it.touchScroll(distanceX, distanceY)
        }
    }

    fun onPathScroll(event: MotionEvent, distanceX: Float, distanceY: Float) {
        retractPenPaths.clear()
        if (penType == XPenType.LINE) {
            pathScrollLine(event)
        } else if (penType == XPenType.ERASER) {
            pathScrollEraser(event)
        } else if (penType == XPenType.RECT || penType == XPenType.SQUARE || penType == XPenType.OVAL || penType == XPenType.CIRCLE) {
            pathScrollRect(event)
        }
    }

    private fun pathScrollLine(event: MotionEvent) {
        if (currentXPenPath == null) {
            XLog.e("画笔路径空闲")
            currentXPenPath = XPenPath(penColor, if (penType == XPenType.ERASER) penEraserWidth else penWidth, penType)
            currentXPenPath?.let {
                if (it is XPenPath) {
                    it.penPath.moveTo(event.x, event.y)
                }
            }
        }
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
            }
        }
    }

    private fun pathScrollEraser(event: MotionEvent) {
        if (currentXPenPath == null) {
            XLog.e("画笔路径空闲")
            currentXPenPath = XPenPath(penColor, if (penType == XPenType.ERASER) penEraserWidth else penWidth, penType)
            currentXPenPath?.let {
                if (it is XPenPath) {
                    it.penPath.moveTo(event.x, event.y)
                }
            }
        }
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
            }
        }
    }

    private fun pathScrollRect(event: MotionEvent) {
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.setLastPoint(event.x, event.y)
            }
        }
    }

    ////// ===================================== 抬起 ========================================
    // 多手指触摸中断
    fun onPathDone(event: MotionEvent) {
        if (penType == XPenType.LINE) {
            pathDoneLine(event)
        } else if (penType == XPenType.ERASER) {
            pathDoneEraser(event)
        } else if (penType == XPenType.RECT || penType == XPenType.SQUARE || penType == XPenType.OVAL || penType == XPenType.CIRCLE) {
            pathDoneRect(event)
        }
    }

    // 抬起优化
    fun onPathDoneDelay(event: MotionEvent) {
        if (penType == XPenType.LINE) {
            pathDoneLineDelay(event)
        } else if (penType == XPenType.ERASER) {
            pathDoneEraserDelay(event)
        } else if (penType == XPenType.RECT || penType == XPenType.SQUARE || penType == XPenType.OVAL || penType == XPenType.CIRCLE) {
            pathDoneRect(event)
        }
    }

    private fun pathDoneLine(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
                penPaths.add(it)
                currentXPenPath = null
                xArtBoard?.refreshCanvas()
            }
        }
    }

    private fun pathDoneLineDelay(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
                val message = Message.obtain().apply {
                    what = 101
                    obj = it
                }
                penPathCompleteHandler.sendMessageDelayed(message, 300)
            }
        }
    }

    private fun pathDoneEraser(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
                penPaths.add(it)
                currentXPenPath = null
                xArtBoard?.refreshCanvas()
            }
        }
    }

    private fun pathDoneEraserDelay(event: MotionEvent) {
        penPathCompleteHandler.removeMessages(101)
        currentXPenPath?.let {
            if (it is XPenPath) {
                it.penPath.lineTo(event.x, event.y)
                val message = Message.obtain().apply {
                    what = 101
                    obj = it
                }
                penPathCompleteHandler.sendMessageDelayed(message, 300)
            }
        }
    }

    private fun pathDoneRect(event: MotionEvent) {
        currentXPenPath?.let {
            penPaths.add(it)
            currentXPenPath = null
        }
    }

    ////// ===================================== 缩放 ========================================
    fun onTouchScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        for (penBitmap in penBitmaps) {
            penBitmap.touchScale(focusX, focusY, scaleFactor)
        }
        // 当前选中的图片缩放
        //            mCurrentPenBitmap?.let {
        //                val matrix = it.mCurrentRect
        //                if (matrix.contains(focusX, focusY)) {
        //                    mCurrentPenBitmap?.onTouchScale(focusX, focusY, scaleFactor)
        //                }
        //            }
        for (penPath in penPaths) {
            if (penPath is XPenPath) {
                penPath.touchScale(focusX, focusY, scaleFactor)
            }
        }
        // 当前选中的线条缩放
        //            mCurrentPenPath?.let {
        //                it.isTouchMe(focusX, focusY)
        //                it?.onTouchScale(focusX, focusY, scaleFactor)
        //            }
    }

    fun draw(canvas: Canvas) {
        for (index in 0 until penBitmaps.size) {
            val drawBitmap = penBitmaps[index]
            drawBitmap.draw(canvas)
        }
        val sl = canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), null)
        for (penPath in penPaths) {
            penPath.draw(canvas)
        }
        currentXPenPath?.draw(canvas)
        canvas.restoreToCount(sl)
    }

    fun onWindowChange(isLandscape: Boolean, width: Int, height: Int) {
        for (penBitmap in penBitmaps) {
            penBitmap.onWindowChange(isLandscape, width, height)
        }
    }

    fun setPenType(type: XPenType) {
        this.penType = type
    }

    fun getPenType() = penType

    fun setPenWidth(strokeWidth: Float) {
        this.penWidth = strokeWidth
    }

    fun getPenWidth(): Float {
        return penWidth
    }

    fun setPenEraserWidth(strokeWidth: Float) {
        this.penEraserWidth = strokeWidth
    }

    fun getPenEraserWidth(): Float {
        return penEraserWidth
    }

    fun setPenColor(color: Int) {
        penColor = color
    }

    fun getPenColor(): Int {
        return penColor
    }

    fun retract() {
        val penPath = penPaths.removeLastOrNull()
        penPath?.let {
            retractPenPaths.add(it)
        }
        xArtBoard?.refreshCanvas()
    }

    fun retrieve() {
        val penPath = retractPenPaths.removeLastOrNull()
        penPath?.let {
            penPaths.add(it)
        }
        xArtBoard?.refreshCanvas()
    }

    fun reset() {
        retractPenPaths.clear()
        penPaths.clear()
        xArtBoard?.refreshCanvas()
    }

    fun resetAll() {
        retractPenPaths.clear()
        penPaths.clear()
        penBitmaps.clear()
        xArtBoard?.refreshCanvas()
    }
}
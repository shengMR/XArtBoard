package cn.com.cys.xartboard.core.material

import android.graphics.Canvas
import android.view.MotionEvent

/**
 * Author: Damon
 * Date: 2022/8/26 09:27
 * Description
 */
abstract class XMaterial {

    /**
     * 是否选中（用于放大/旋转...）
     */
    var isSelected = false

    abstract fun draw(canvas: Canvas)

    abstract fun isTouchMe(x: Float, y: Float): Boolean

    abstract fun touchDown(event: MotionEvent)

    abstract fun touchUp(event: MotionEvent)

    abstract fun touchScroll(distanceX: Float, distanceY: Float)

    abstract fun touchScale(focusX: Float, focusY: Float, scaleFactor: Float)
}
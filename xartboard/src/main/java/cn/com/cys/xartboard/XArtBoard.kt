package cn.com.cys.xartboard

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import cn.com.cys.xartboard.helper.XLog
import cn.com.cys.xartboard.core.material.XMaterial
import cn.com.cys.xartboard.core.material.XPenBitmap
import cn.com.cys.xartboard.core.tool.*

/**
 * Author: Damon
 * Date: 2022/8/17 19:33
 * Description
 *
 * 绘制注意点：
 *
 * 1，ScrollX，ScrollY怎么改变，event都始终是控件的点击区域
 */
class XArtBoard : FrameLayout, ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        const val TAG = "XArtBoard"
    }

    // 工具
    private var viewFrame = RectF() // 画板固定矩形
    private var scaleViewFrame = RectF() // 画板缩放后的矩形
    private var artBoardMatrix = Matrix()
    private val xPen = XPen() // 画笔绘制一切东西
    private var maxScaleFactor = 3f // 最大放大系数
    private var isFixAnimPlay = false // 是否在动画中
    private var isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏

    // 手势
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var mFingerCount = 0 // 触摸手指头个数
    private var mInScaleMode = false // 是否在缩放
    private var mInMoveMode = false // 是否在移动

    // 测试类
    private val mXTest = XTest()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        initTools()
    }

    private fun initTools() {
        scaleGestureDetector = ScaleGestureDetector(context, this)
        gestureDetector = GestureDetector(context, XGestureDetector())
        xPen.setArtBoard(this)
    }

    //region 加载图片
    fun loadBitmap(bitmap: Bitmap, gravity: XGravity = XGravity.GravityCenter) {
        initBitmap(bitmap, gravity)
    }

    fun loadBitmap(@DrawableRes resourceId: Int, gravity: XGravity = XGravity.GravityCenter) {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
        initBitmap(bitmap, gravity)
    }

    private fun initBitmap(bitmap: Bitmap?, gravity: XGravity) {
        if (bitmap == null) {
            XLog.e("图片解析为空")
            return
        }
        val xPenBitmap = XPenBitmap(bitmap, gravity)
        XLog.e("图片的原始大小：Bitmap.width = ${bitmap.width}, Bitmap.height = ${bitmap.height}")
        xPenBitmap.currentRect.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        xPen.addPenBitmap(xPenBitmap)
        requestLayout()
    }
    //endregion

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewFrame.set(0f, 0f, width.toFloat(), height.toFloat())
        scaleViewFrame.set(viewFrame)
        xPen.onWindowChange(isLandscape, width, height)
        maxScaleFactor = xPen.getMaxScale()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        xPen.draw(canvas)
        drawTest(canvas)
    }

    private fun drawTest(canvas: Canvas) {
        mXTest.drawRectF(canvas, viewFrame, Color.GREEN)
        mXTest.drawRectF(canvas, scaleViewFrame, Color.BLUE)
        mXTest.drawText(canvas, "画笔条数：${xPen.getPenPaths().size}", 50f, 50f, Color.BLACK)
    }

    //region 事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isFixAnimPlay) {
            XLog.e("动画运行中无法触发")
            return super.onTouchEvent(event)
        }
        // 获取触摸点
        mFingerCount = event.pointerCount
        XLog.e("触摸手指数量：${mFingerCount}")

        var h = false
        // 如果是多余两个点则进行缩放并结束画笔绘制
        if (mFingerCount >= 2) {
            // 先交给缩放手势处理
            h = scaleGestureDetector?.onTouchEvent(event) ?: false
            xPen.onPathDone(event)
            mInMoveMode = true
        }
        // 移动
        h = h or (gestureDetector?.onTouchEvent(event) ?: false)
        // 如果不是缩放或者移动，则停止画笔
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                XLog.e("触摸操作：抬起")
                xPen.onTouchUp(event)
                xPen.onPathDoneDelay(event)
                autoFixCanvas()
                mInMoveMode = false
                mInScaleMode = false
            }
        }
        invalidate()
        return h or super.onTouchEvent(event)
    }

    private fun autoFixCanvas() {
        if (getScaleFactor() < 1f) {
            XLog.e("自动解决缩放问题：回到最小缩放位置")
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                addUpdateListener {
                    val changeX =
                        (viewFrame.centerX() - scaleViewFrame.centerX()) * it.animatedValue as Float
                    val changeY =
                        (viewFrame.centerY() - scaleViewFrame.centerY()) * it.animatedValue as Float
                    var changeScale =
                        (viewFrame.width() / scaleViewFrame.width()) * it.animatedValue as Float
                    changeScale = if (changeScale <= 1f) 1f else changeScale
                    XLog.e("自动解决缩放问题：changeScale = ${changeScale}")
                    artBoardMatrix.setScale(
                        changeScale,
                        changeScale,
                        scaleViewFrame.centerX(),
                        scaleViewFrame.centerY()
                    )
                    artBoardMatrix.postTranslate(changeX, changeY)
                    artBoardMatrix.mapRect(scaleViewFrame)
                    xPen.onCanvasScroll(-changeX, -changeY)
                    xPen.onTouchScale(
                        scaleViewFrame.centerX(),
                        scaleViewFrame.centerY(),
                        changeScale
                    )
                    invalidate()
                }
                addListener(
                    onStart = { isFixAnimPlay = true },
                    onEnd = { isFixAnimPlay = false },
                    onCancel = { isFixAnimPlay = false })
            }
            valueAnimator.start()
        } else {
            var dLeft: Float? = null
            var dTop: Float? = null
            if (scaleViewFrame.left > viewFrame.left) { // 图层被移动到了右边，需要往左边调
                dLeft = viewFrame.left - scaleViewFrame.left
            } else if (scaleViewFrame.right < viewFrame.right) { // 图层被移动到了左边，需要往右边调
                dLeft = viewFrame.right - scaleViewFrame.right
            }
            if (scaleViewFrame.top > viewFrame.top) { // 图层被移动到了下边，需要往上边调
                dTop = viewFrame.top - scaleViewFrame.top
            } else if (scaleViewFrame.bottom < viewFrame.bottom) { // 图层被移动到了上边，需要往下边调
                dTop = viewFrame.bottom - scaleViewFrame.bottom
            }

            if (dLeft == null && dTop == null) {
                return
            }
            val a1 =
                ValueAnimator.ofFloat(scaleViewFrame.left, scaleViewFrame.left + (dLeft ?: 0f))
                    ?.apply {
                        addUpdateListener {
                            val av = it.animatedValue as Float - scaleViewFrame.left
                            artBoardMatrix.setTranslate(av, 0f)
                            artBoardMatrix.mapRect(scaleViewFrame)
                            xPen.onCanvasScroll(-av, 0f)
                            invalidate()
                        }
                    }
            val a2 = ValueAnimator.ofFloat(scaleViewFrame.top, scaleViewFrame.top + (dTop ?: 0f))
                ?.apply {
                    addUpdateListener {
                        val av = it.animatedValue as Float - scaleViewFrame.top
                        artBoardMatrix.setTranslate(0f, av)
                        artBoardMatrix.mapRect(scaleViewFrame)
                        xPen.onCanvasScroll(0f, -av)
                        invalidate()
                    }
                }
            val animatorSet = AnimatorSet()
            animatorSet.duration = 400
            animatorSet.playTogether(a1, a2)
            animatorSet.start()
            animatorSet.addListener(
                onStart = { isFixAnimPlay = true },
                onEnd = { isFixAnimPlay = false },
                onCancel = { isFixAnimPlay = false })
        }
    }

    /**
     *  缩放比例
     */
    private fun getScaleFactor(): Float {
        return scaleViewFrame.width() / viewFrame.width()
    }
    //endregion

    //region 手势缩放
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (detector.scaleFactor > 1f && getScaleFactor() > maxScaleFactor) { // 放大并且在超过系数则不放大
            return true
        }
        XLog.e("缩放中：${detector.scaleFactor}")
        mInScaleMode = true
        artBoardMatrix.setScale(
            detector.scaleFactor,
            detector.scaleFactor,
            detector.focusX,
            detector.focusY
        )
        artBoardMatrix.mapRect(scaleViewFrame)
        xPen.onTouchScale(detector.focusX, detector.focusY, detector.scaleFactor)
        invalidate()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return mFingerCount >= 2
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mInScaleMode = false
    }
    //endregion

    //region 手势操作
    inner class XGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            XLog.e("手势操作：点击")
            if (mInMoveMode) {
                return true
            }
            xPen.onTouchDown(e)
            invalidate()
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            XLog.e("手势操作：移动")
            if (mInMoveMode) {
                artBoardMatrix.setTranslate(-distanceX, -distanceY)
                artBoardMatrix.mapRect(scaleViewFrame)
                XLog.e("手势操作：移动 ${scaleViewFrame}")
                xPen.onCanvasScroll(distanceX, distanceY)
                invalidate()
                return true
            }
            xPen.onPathScroll(e2, distanceX, distanceY)
            invalidate()
            return true
        }
    }
    //endregion

    fun refreshCanvas() = if(Looper.myLooper() == Looper.getMainLooper()) invalidate() else postInvalidate()

    //region ===================================== 公开 ========================================
    /**
     * 设置最大放大系数
     */
    fun setMaxScaleFactor(scale: Float) {
        this.maxScaleFactor = scale
    }

    /**
     * 设置画笔模式
     */
    fun setPenType(type: XPenType) {
        this.xPen.setPenType(type)
    }

    /**
     * 获取画笔模式
     */
    fun getPenType(): XPenType{
        return xPen.getPenType()
    }

    /**
     * 设置画笔宽度
     */
    fun setPenWidth(strokeWidth: Float) {
        this.xPen.setPenWidth(strokeWidth)
    }

    /**
     * 获取画笔宽度
     */
    fun getPenWidth(): Float {
        return xPen.getPenWidth()
    }

    /**
     * 设置橡皮擦宽度
     */
    fun setPenEraserWidth(strokeWidth: Float) {
        this.xPen.setPenEraserWidth(strokeWidth)
    }

    /**
     * 获取橡皮擦宽度
     */
    fun getPenEraserWidth(): Float {
        return xPen.getPenEraserWidth()
    }

    /**
     * 设置画笔颜色
     */
    fun setPenColor(color: Int) {
        this.xPen.setPenColor(color)
    }

    /**
     * 撤回
     */
    fun retract() {
        this.xPen.retract()
    }

    /**
     * 恢复
     */
    fun retrieve() {
        this.xPen.retrieve()
    }

    /**
     * 重置
     */
    fun reset() {
        this.xPen.reset()
    }

    /**
     * 设置画笔路径列表
     */
    fun setPenPaths(penPaths: List<XMaterial>){
        this.xPen.setPenPaths(penPaths)
    }

    /**
     * 获取画笔路径列表
     */
    fun getPenPaths(): List<XMaterial>{
        return xPen.getPenPaths()
    }

    /**
     * 获取图片
     */
    fun getBitmap(): Bitmap {
        return getResultBitmap()
    }

    /**
     * 获取最终的图片
     */
    private fun getResultBitmap(): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            scaleViewFrame.width().toInt(),
            scaleViewFrame.height().toInt(),
            Bitmap.Config.RGB_565
        )
        val dLeft = scaleViewFrame.left
        val dTop = scaleViewFrame.top
        xPen.onCanvasScroll(dLeft, dTop)
        artBoardMatrix.setTranslate(-dLeft, -dTop)
        artBoardMatrix.mapRect(scaleViewFrame)
        val canvas = Canvas(resultBitmap)
        background?.draw(canvas)
        drawTest(canvas)
        xPen.draw(canvas)
        xPen.onCanvasScroll(-dLeft, -dTop)
        artBoardMatrix.setTranslate(dLeft, dTop)
        artBoardMatrix.mapRect(scaleViewFrame)
        return resultBitmap
    }
    //endregion
}

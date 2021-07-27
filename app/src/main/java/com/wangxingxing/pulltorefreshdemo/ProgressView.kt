package com.wangxingxing.pulltorefreshdemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnStart

class ProgressView(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint
    private val progressWidth: Float
    private val progressColor: Int
    private val startColor: Int
    private var enable: Boolean = true
    private var animator: ValueAnimator? = null

    private var progress = 0
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    // 执行一些初始化的操作
    init {
        val t = context!!.theme.obtainStyledAttributes(
            attrs, R.styleable.ProgressView, defStyleAttr, 0
        )
        progressWidth = t!!.getDimension(
            R.styleable.ProgressView_progress_width, 10f
        )
        progressColor = t!!.getColor(
            R.styleable.ProgressView_progress_color,
            Color.parseColor("#000000")
        )
        startColor = t!!.getColor(
            R.styleable.ProgressView_start_color,
            Color.parseColor("#6200EE")
        )

        paint = Paint()
        paint.strokeWidth = progressWidth
        paint.style = Paint.Style.STROKE
    }

    /**
     * 开启动画
     */
    public fun startAnim() {
        if (animator == null) {
            animator = ValueAnimator.ofInt(0, 360)
            // 设置重复模式。RESTART：重新开始；REVERSE：反转
            animator?.repeatMode = ValueAnimator.RESTART
            // repeatCount：重复次数；ValueAnimator.INFINITE表示无限次数
            animator?.repeatCount = ValueAnimator.INFINITE
            // 设置动画时间长度，默认300ms
            animator?.duration = 5000
            // 设置动画update事件监听
            animator?.addUpdateListener {
                //println(it.animatedValue)
                //println(System.currentTimeMillis())
                progress = it.animatedValue as Int
            }
            // 动画启动时触发
            animator?.doOnStart {
                println("doOnStart")
            }
        }
        animator?.start()
    }

    fun destroy() {
        if (animator != null) {
            animator?.pause()
            animator = null
        }
    }

    /**
     * 当视图附加到窗口时调用
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        println("onAttachedToWindow")
        //startProgress()
        startAnim()
    }

    /**
     * 当视图从窗口分离时调用
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        println("onDetachedFromWindow")
        destroy()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(event)
    }

    /**
     * 处理事件 (消费事件)
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // gestureDetector!!.onTouchEvent(ev)
        if (event != null && enable) {
            //event.getX() // 到View左边界的距离
            //event.getRawX() // 到屏幕边界的距离
            val x = event.x
            val y = event.y
            val center: Float = width / 2f
            progress = getRotationBetweenLines(center, center, x, y)
            // 消费了事件，要返回true
            return true
        }
        return super.onTouchEvent(event)
    }

//    private fun startProgress() {
//        Thread {
//            while (progress < 360) {
//                progress++
//                try {
//                    Thread.sleep(10)
//                    postInvalidate()
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//                if (progress == 360) {
//                    progress = 0
//                }
//            }
//        }.start()
//    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//    }

    /**
     * 绘制视图内容
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        paint.color = startColor
        val center: Float = width / 2f
        val radius = (width - paddingLeft - paddingRight) / 2f - progressWidth
        canvas.drawCircle(center, center, radius, paint)
        val rectF = RectF(center - radius, center - radius, center + radius, center + radius)
        paint.color = progressColor
        canvas.drawArc(rectF, -90f, progress.toFloat(), false, paint)
    }

    /**
     * 获取两条线的夹角
     * @param centerX
     * @param centerY
     * @param xInView
     * @param yInView
     * @return
     */
    private fun getRotationBetweenLines(
        centerX: Float,
        centerY: Float,
        xInView: Float,
        yInView: Float
    ): Int {
        var rotation = 0.0
        val k1 = (centerY - centerY).toDouble() / (centerX * 2 - centerX)
        val k2 = (yInView - centerY).toDouble() / (xInView - centerX)
        val tmpDegree = Math.atan(Math.abs(k1 - k2) / (1 + k1 * k2)) / Math.PI * 180
        if (xInView > centerX && yInView < centerY) {  //第一象限
            rotation = 90 - tmpDegree
        } else if (xInView > centerX && yInView > centerY) //第二象限
        {
            rotation = 90 + tmpDegree
        } else if (xInView < centerX && yInView > centerY) { //第三象限
            rotation = 270 - tmpDegree
        } else if (xInView < centerX && yInView < centerY) { //第四象限
            rotation = 270 + tmpDegree
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0.0
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180.0
        }
        return rotation.toInt()
    }

}
package com.wangxingxing.pulltorefreshdemo

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import kotlin.math.abs

/**
 * author : 王星星
 * date : 2021/7/27 11:27
 * email : 1099420259@qq.com
 * description :
 */
class PullLayout(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr) {

    private val TAG = "PullLayout"

    private var mLastY = 0

    private var mInitTop = 0

    private var mIsDragging = false

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        orientation = VERTICAL
        mInitTop = -DisplayUtil.dp2px(context, 100f)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec + abs(mInitTop))
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (mIsDragging) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        Log.i(TAG, "onTouchEvent: action=$action")
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY = event.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val yDistance = event.y - mLastY
                translationY = y + yDistance
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsDragging = true
                val animator: ValueAnimator = ValueAnimator.ofFloat(y, mInitTop.toFloat())
                animator.addUpdateListener {
                    val value = animator.animatedValue as Float
                    if (value == mInitTop.toFloat()) {

                    }
                    translationY = value
                    invalidate()
                }
                animator.start()
            }
            else -> {

            }
        }
        return super.onTouchEvent(event)
    }
}
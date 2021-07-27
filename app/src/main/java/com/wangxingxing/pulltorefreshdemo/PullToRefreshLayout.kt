package com.wangxingxing.pulltorefreshdemo

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * author : 王星星
 * date : 2021/7/27 11:59
 * email : 1099420259@qq.com
 * description :
 */
class PullToRefreshLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : LinearLayout(context, attrs, defStyleAttr) {

    private var progressView: ProgressView? = null

    /**
     * 默认初始状态
     */
    private var mState = STATE_INIT

    /**
     * 是否被拖拽
     */
    private var mIsDragging = false

    private var mInitY = 0

    /**
     * 上一次Y的坐标
     */
    private var mLastMotionY = 0

    /**
     * 手指触发滑动的临界距离
     */
    private var mSlopTouch = 0

    /**
     * 触发刷新的临界值
     */
    private var mRefreshHeight = 200

    /**
     * 用户刷新监听器
     */
    private var mOnRefreshListener: OnRefreshListener? = null

    private var mOnPullListener: OnPullListener? = null

    fun setOnRefreshListener(onRefreshListener: OnRefreshListener) {
        mOnRefreshListener = onRefreshListener
    }

    fun getContentView(): ViewGroup {
        return mContentView!!
    }

    /**
     * 是否可拖拽, 因为在刷新头自由滑动和刷新状态的时候，
     * 我们应该保持界面不被破坏
     */
    private var mIsCanDrag = true

    /**
     * 刷新文字提示
     */
    private var mRefreshText: TextView? = null

    /**
     * 顶部刷新头
     */
    private var mHeaderView: View? = null

    /**
     * ContentView
     */
    private var mContentView: ViewGroup? = null

    /**
     * 头部布局
     */
    private var mHeaderLayoutParams: LayoutParams? = null

    /**
     * ContentView布局
     */
    private var mContentLayoutParams: LayoutParams? = null

    /**
     * 属性动画
     */
    private var mValueAnimator: ValueAnimator? = null

    private var progress = 0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        orientation = VERTICAL

        // 添加刷新头Header
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_refresh_header, null)
        mHeaderView?.tag = HEADER_VIEW
        mRefreshText = mHeaderView?.findViewById(R.id.tv_refresh_text)
        mHeaderLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            DisplayUtil.dp2px(context, 500f)
        )
        progressView = mHeaderView?.findViewById(R.id.progress_view)
        addView(mHeaderView, mHeaderLayoutParams)

        // 获取内容布局
        mContentView = RecyclerView(context)
        mContentView?.tag = CONTENT_VIEW
        mContentLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mContentView, mContentLayoutParams)

        // 3.一开始的时候要让Header看不见，设置向上的负paddingTop
        setPadding(0, -DisplayUtil.dp2px(context, 500f), 0, 0)

        val viewConfiguration = ViewConfiguration.get(context)
        mSlopTouch = viewConfiguration.scaledTouchSlop

        mState = 0
    }

    /**
     * 设置状态，每个状态下，做不同的事情
     *
     * @param state 状态
     */
    private fun setState(state: Int) {
        when (state) {
            STATE_INIT          -> setInitState()
            STATE_DRAGGING      -> setDragState()
            STATE_READY         -> setReadyState()
            STATE_REFRESHING    -> setRefreshState()
            STATE_FLING         -> setFlingState()
        }
        mState = state
    }

    /**
     * 自由滚动时，如何处理
     */
    private fun setFlingState() {
        mIsDragging = false
        mIsCanDrag = false

        // 自由滚动状态可以从两个状态进入：
        // 1.READY状态。
        // 2、其他状态。
        // 滑动均需要平滑滑动
        if (mState === STATE_READY) {
            Log.d(TAG, "setFlingState: 从Ready状态开始自由滑动")
            // 从准备状态进入，刷新头滑到 200 的位置
            smoothScroll(scrollY, -mRefreshHeight)
        } else {
            Log.d(TAG, "setFlingState: 松手后，从其他状态开始自由滑动")
            // 从刷新状态进入，刷新头直接回到最初默认的位置
            // 即: 滑出界面，ScrollY 变成 0
            smoothScroll(scrollY, 0)
        }
    }

    /**
     * 用户刷新时，如何处理
     */
    private fun setRefreshState() {
        mOnRefreshListener?.onRefresh()
        mIsCanDrag = false
        mRefreshText?.text = "正在刷新，请稍后..."
        progressView?.startAnim()
    }

    /**
     * 拖拽距离超过header高度时，如何处理
     */
    private fun setReadyState() {
        mRefreshText?.text = "松开刷新"
    }

    /**
     * 处理拖拽时方法
     */
    private fun setDragState() {
        mIsDragging = true
    }

    /**
     * 处理初始化状态方法
     */
    private fun setInitState() {
        // 只有在初始状态时，恢复成可拖拽
        mIsCanDrag = true
        mIsDragging = false
        mRefreshText?.text = "下拉刷新"
    }

    /**
     * 平滑滚动
     * @param startPos 开始位置
     * @param targetPos 结束位置
     */
    private fun smoothScroll(startPos: Int, targetPos: Int) {
        // 如果有动画正在播放，先停止
        if (mValueAnimator != null && mValueAnimator!!.isRunning) {
            mValueAnimator!!.cancel()
            mValueAnimator!!.end()
            mValueAnimator = null
        }

        // 然后开启动画
        mValueAnimator = ValueAnimator.ofInt(scrollY, targetPos)
        mValueAnimator?.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            scrollTo(0, value)
            if (scrollY == targetPos) {
                if (targetPos != 0) {
                    setState(STATE_REFRESHING)
                } else {
                    setState(STATE_INIT)
                }
            }
        }
        mValueAnimator?.duration = 300L
        mValueAnimator?.start()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (!mIsCanDrag) {
            return true
        }

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsDragging = false
            return false
        }

        if (mIsDragging && action == MotionEvent.ACTION_MOVE) {
            return true
        }

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                val diff = (ev.y - mLastMotionY).toInt()
                if (abs(diff) > mSlopTouch && diff > 1 && isReadyToPull()) {
                    mLastMotionY = ev.y.toInt()
                    mIsDragging = true
                }
            }
            MotionEvent.ACTION_DOWN -> if (isReadyToPull()) {
                setState(STATE_INIT)
                mInitY = ev.y.toInt()
                mLastMotionY = ev.y.toInt()
            }
            else -> {

            }
        }

        return mIsDragging
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (!mIsCanDrag) {
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> if (isReadyToPull()) {
                setState(STATE_INIT)
                mInitY = event.y.toInt()
                mLastMotionY = event.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> if (mIsDragging) {
                mLastMotionY = event.y.toInt()
                setState(STATE_DRAGGING)
                pullScroll()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsDragging = false
                setState(STATE_FLING)
            }
            else -> {

            }
        }
        return true
    }

    /**
     * 是否准备好触发下拉的状态了
     */
    private fun isReadyToPull(): Boolean {
        if (mContentView == null) {
            return false
        }

        if (mContentView is RecyclerView) {
            val contentView = mContentView as RecyclerView
            val manager = contentView.layoutManager as LinearLayoutManager
            if (contentView.adapter != null) {
                val child: View = contentView.getChildAt(0)
                val height = child.height
                return if (height > contentView.height) {
                    child.top == 0 && manager.findFirstVisibleItemPosition() == 0
                } else {
                    manager.findFirstCompletelyVisibleItemPosition() == 0
                }
            }
        }
        return false
    }

    /**
     * 下拉移动界面，拉出刷新头
     */
    private fun pullScroll() {
        // 滚动值 = 初始值 - 结尾值
        val scrollValue = (mInitY - mLastMotionY) / 3
        if (scrollValue > 0) {
            scrollTo(0, 0)
            return
        }
        progress = abs(scrollValue).toFloat() / mRefreshHeight.toFloat() * 100
        if (progress > 100) {
            progress = 100f
        }
        if (mOnPullListener != null) {
            mOnPullListener?.onPull(progress)
        }

        /*if (mIsDragging) {
           progressView?.setProgress((progress * 3.6).toInt())
       }*/
        Log.d(TAG, "pullScroll: progress=$progress")
        if (abs(scrollValue) > mRefreshHeight && mState == STATE_DRAGGING) {
            // 约定：如果偏移量超过 200(这个值，表示是否可以启动刷新的临界值，可任意定),
            // 那么状态变成 STATE_READY
            Log.d(TAG, "pullScroll: 超过了触发刷新的临界值")
            setState(STATE_READY)
        }
        scrollTo(0, scrollValue)
    }

    /**
     * 刷新完成，需要调用方主动发起，才能完成将刷新头收起
     */
    fun refreshComplete() {
        mRefreshText?.text = "刷新完成"
        setState(STATE_FLING)
        progressView?.visibility = View.GONE
    }

    companion object {
        const val TAG = "PullToRefreshLayout"

        const val HEADER_VIEW = "HEADER_VIEW"
        const val CONTENT_VIEW = "CONTENT_VIEW"
        const val STATE_INIT = 1 // 下拉初始状态（空闲状态）
        const val STATE_DRAGGING = 2 // 下拉手指拖拽状态（拖拽状态）
        const val STATE_READY = 3 // 准备好释放刷新（满足释放刷新状态）
        const val STATE_REFRESHING = 4 // 正在刷新（用户用于发起刷新请求）
        const val STATE_FLING = 5 // 松开手指顶部自然回弹 （释放时的高度大于、小于刷新头的高度）
    }

    interface OnRefreshListener {
        fun onRefresh()
    }

    interface OnPullListener {
        fun onPull(progress: Float)
    }
}
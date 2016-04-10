package com.tellh.android_pulltorefreshlibrary_collection.PullToRefreshLayoutTellH;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.yalantis.phoenix.util.Utils;

import java.security.InvalidParameterException;

public class PullToRefreshLayout extends ViewGroup {

    private static final int UNLIMIT_DRAG_HEIGHT = 0;
    private static final int LIMIT_DRAG_HEIGHT = 1;
    private static final int DRAG_MAX_DISTANCE = 120;
    private int mDragMode = LIMIT_DRAG_HEIGHT;
    /**
     * 下拉拖拽阻尼
     */
    private static final float DRAG_RATE = .4f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    public static final int STYLE_SUN = 0;
    public static final int MAX_OFFSET_ANIMATION_DURATION = 700;

    private static final int INVALID_POINTER = -1;

    private View mTarget;
    private RelativeLayout mRefreshView;
    private Interpolator mDecelerateInterpolator;
    private int mTouchSlop;
    /**
     * 触发刷新的下拉距离，也是mRefreshView刷新时停留的高度
     */
    private int mTotalDragDistance;
    /**
     * 下拉进度
     */
    private float mCurrentDragPercent;
    /**
     * mCurrentOffsetTop=mTarget.getTop();内容view的top值
     */
    private int mCurrentOffsetTop;

    private boolean mRefreshing;

    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private float mInitialMotionY;
    private int mFrom;
    private float mFromDragPercent;
    private boolean mNotify;
    private OnRefreshListener mListener;
    private int mTargetPaddingTop;

    private int mTargetPaddingBottom;
    private int mTargetPaddingRight;
    private int mTargetPaddingLeft;
    private int finishRefreshToPauseDuration = 0;

    private int mHeaderViewIndex;
    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, com.yalantis.phoenix.R.styleable.RefreshView);
        final int type = a.getInteger(com.yalantis.phoenix.R.styleable.RefreshView_type, STYLE_SUN);
        a.recycle();

        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Utils.convertDpToPixel(context, DRAG_MAX_DISTANCE);

        mRefreshView = new RelativeLayout(context);

        setRefreshStyle(type);

        addView(mRefreshView);

        //在构造函数上加上这句，防止自定义View的onDraw方法不执行的问题
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    public void setRefreshStyle(int type) {
        setRefreshing(false);
        switch (type) {
            case STYLE_SUN:
                break;
            default:
                throw new InvalidParameterException("Type does not exist");
        }
    }

    public View setRefreshView(int layoutId) {
        if (mRefreshView == null) {
            return null;
        }
        mRefreshView.removeAllViews();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRefreshView.setLayoutParams(layoutParams);
        return LayoutInflater.from(getContext()).inflate(layoutId, mRefreshView, true);
    }


    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * This method sets padding for the refresh (progress) view.暂时去掉padding功能
     */
//    public void setRefreshViewPadding(int left, int top, int right, int bottom) {
//        mRefreshView.setPadding(left, top, right, bottom);
//    }

    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTarget();

        if (mTarget == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshView.measure(widthMeasureSpec, MeasureSpec
                .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        mHeaderViewIndex = -1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mRefreshView) {
                mHeaderViewIndex = index;
                break;
            }
        }
        mTotalDragDistance = (int) (mRefreshView.getMeasuredHeight() * 0.8f);
    }

    private void ensureTarget() {
        if (mTarget != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mRefreshView) {
                    mTarget = child;
                    mTargetPaddingBottom = mTarget.getPaddingBottom();
                    mTargetPaddingLeft = mTarget.getPaddingLeft();
                    mTargetPaddingRight = mTarget.getPaddingRight();
                    mTargetPaddingTop = mTarget.getPaddingTop();
                }
            }
        }
    }

    /**
     * 该函数只干两件事
     * 1.记录手指按下的坐标
     * 2.判断是否拦截处理事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        //如果被禁用、到达顶部、正在刷新时，不拦截点击事件，不做任何处理
        if (!isEnabled() || canChildScrollUp() || mRefreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            //手指按下，记录点击坐标
            case MotionEvent.ACTION_DOWN:
//                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                //如果是滑动动作，将标志mIsBeingDragged置为true
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            //手指松开，标志复位
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        //如果是正在被下拉拖动，拦截该事件，不往下传递；反之，你懂的
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {

        //如果不是在被下拉拖动，不处理，直接返回
        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                //将要移到的位置对应的Y坐标，也是在未松手前，target下拉的总高度 int
                int targetY;
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;
                //未松手前,总共下拉的距离 float
                final float scrollTop = yDiff * DRAG_RATE;
                if (mCurrentDragPercent < 0) {
                    return false;
                }
                if (mDragMode == LIMIT_DRAG_HEIGHT) {
                    //控制下拉的最大距离
                    mCurrentDragPercent = scrollTop / mTotalDragDistance;
                    float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                    float extraOS = Math.abs(scrollTop) - mTotalDragDistance;
                    float slingshotDist = mTotalDragDistance;
                    float tensionSlingshotPercent = Math.max(0,
                            Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                            tensionSlingshotPercent / 4, 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent / 2;
                    targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);
                } else {
                    targetY = (int) scrollTop; //效果一样,但可以无限下拉
                }
                if (mListener != null&&!mRefreshing) {
                    mListener.onDragDistanceChange(scrollTop, mCurrentDragPercent, (scrollTop - mCurrentOffsetTop) / mTotalDragDistance);
                }

                mRefreshView.setVisibility(VISIBLE);
                //调整更新位置，传过去的值是每次的偏移量
                setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                break;
            }
            //做多指触控处理
            case MotionEventCompat.ACTION_POINTER_DOWN:
                //将最后一只按下的手指作为ActivePointer
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            //手指松开！
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //排除是无关手指
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                //计算松开瞬间下拉的距离
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                //标志复位
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;

                if (overScrollTop > mTotalDragDistance) {//触发刷新
                    setRefreshing(true, true);
                } else {//回滚
                    mRefreshing = false;
                    animateOffsetToStartPosition();
                }
                return false;//系列点击事件已经处理完，将处理权交还mTarget
            }
        }

        return true;//该系列点击事件未处理完，消耗此系列事件
    }

    /**
     * 回滚动画
     */
    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math.abs((long) (MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent));

        //当动画开始，通过applyTransformation(float interpolatedTime, Transformation t)方法的interpolatedTime参数（0.0~1.0）
        //内部调用setTargetOffsetTop(offset, false);两者更新位置和RefreshView的加载动画
        //当动画结束时，更新mCurrentOffsetTop=mTarget.getTop();
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        //动画上调至mTotalDragDistance这个高度
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        //回调onRefresh()
        mAnimateToCorrectPosition.setAnimationListener(mReadyToRefreshListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToCorrectPosition);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = mTotalDragDistance;
            targetTop = mFrom + (int) ((endTarget - mFrom) * interpolatedTime);
            int offset = targetTop - mTarget.getTop();

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
            if (mListener != null&&!mRefreshing) {
                float pos = mFrom + (endTarget - mFrom) * interpolatedTime;
                mListener.onDragDistanceChange(pos,
                        mCurrentDragPercent, (pos - mTarget.getTop()) / mTotalDragDistance);
            }
            setTargetOffsetTop(offset, false /* requires update */);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        //计算偏移量
        int offset = targetTop - mTarget.getTop();

        //更新RefreshView加载动画
        mCurrentDragPercent = targetPercent;
        if (mListener != null&&!mRefreshing) {
            float pos = mFrom - mFrom * interpolatedTime;
            mListener.onDragDistanceChange(pos,
                    mCurrentDragPercent, (pos - mTarget.getTop()) / mTotalDragDistance);
        }

        //更新mTarget和mRefreshView的位置
//        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop);
        setTargetOffsetTop(offset, false);

    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, true /* notify */);
        }

    }

    /**
     * @param refreshing 是否触发刷新
     * @param notify 是否回调onRefresh()方法
     */
    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                //开始刷新
                animateOffsetToCorrectPosition();//位置上调到合适的位置
            } else {
                //刷新完成
                if (mListener != null)
                    mListener.onFinish();

                //让mRefreshView停留几秒钟
                mRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateOffsetToStartPosition();
                    }
                },finishRefreshToPauseDuration);
            }
        }
    }

    private final Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrentOffsetTop = mTarget.getTop();//更新mCurrentOffsetTop
        }
    };

    private final Animation.AnimationListener mReadyToRefreshListener= new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {//开始刷新
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
            } else {//停止刷新
                animateOffsetToStartPosition();
            }
            //更新mCurrentOffsetTop
            mCurrentOffsetTop = mTarget.getTop();
//        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    /**
     * 处理多指触控的点击事件
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    /**
     * 通过调用offsetTopAndBottom方法
     * 更新mTarget和mBaseRefreshView的位置
     * 更新target下拉高度--mCurrentOffsetTop
     * @param offset         偏移位移
     * @param requiresUpdate 时候invalidate()
     */
    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
//        mRefreshView.bringToFront();
        mTarget.offsetTopAndBottom(offset);
        mRefreshView.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        if (requiresUpdate /*&& android.os.Build.VERSION.SDK_INT < 11*/) {
            invalidate();
        }
    }


    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        ensureTarget();
        if (mTarget == null)
            return;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        //mTarget MATCH_PARENT
        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop);
        //mRefreshView隐藏在mTarget的上面
        mRefreshView.layout(left, top - mRefreshView.getMeasuredHeight() + mCurrentOffsetTop, left + width - right, top + mCurrentOffsetTop - bottom);
    }

    public void setOnRefreshListener(OnRefreshListenerAdapter listener) {
        mListener = listener;
    }

    public void setFinishRefreshToPauseDuration(int finishRefreshToPauseDuration) {
        this.finishRefreshToPauseDuration = finishRefreshToPauseDuration;
    }

    public void setmDragMode(int dragMode) {
        this.mDragMode = dragMode;
    }


    public interface OnRefreshListener {
        void onRefresh();

        /**
         * 不要在此进行耗时的操作
         */
        void onFinish();

        void onDragDistanceChange(float distance, float percent, float offset);
    }

    public static class OnRefreshListenerAdapter implements OnRefreshListener {
        @Override
        public void onRefresh() {
        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onDragDistanceChange(float distance, float percent, float offset) {
        }
    }
}


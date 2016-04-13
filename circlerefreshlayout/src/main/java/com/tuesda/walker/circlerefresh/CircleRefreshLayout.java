package com.tuesda.walker.circlerefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by zhanglei on 15/7/20.
 */
public class CircleRefreshLayout extends FrameLayout {

    /**
     * 下拉阻尼
     */
    private static final int DRAG_RATE = 2;
    private static String TAG = "pullToRefresh";

    private static final long BACK_TOP_DUR = 600;
    private static final long REL_DRAG_DUR = 200;

    private int mHeaderBackColor = 0xff8b90af;
    private int mHeaderForeColor = 0xffffffff;
    /**
     * 下拉头部圆的半径
     */
    private int mHeaderCircleSmaller = 6;


    /**
     * 最大下拉高度
     */
    private float mPullHeight;
    /**
     * 触发刷新的下拉高度
     */
    private float mHeaderHeight;
    private View mChildView;
    private AnimationView mHeader;

    private boolean mIsRefreshing;

    /**
     * 手指按下的Y坐标
     */
    private float mTouchStartY;

    /**
     * 当前手指的Y坐标
     */
    private float mTouchCurY;

    private ValueAnimator mUpBackAnimator;
    private ValueAnimator mUpTopAnimator;

    private final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public CircleRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        setAttrs(attrs);
        mPullHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        mHeaderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addHeaderView();
            }
        });

    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CirCleRefreshLayout);

        mHeaderBackColor = a.getColor(R.styleable.CirCleRefreshLayout_AniBackColor, mHeaderBackColor);
        mHeaderForeColor = a.getColor(R.styleable.CirCleRefreshLayout_AniForeColor, mHeaderForeColor);
        mHeaderCircleSmaller = a.getInt(R.styleable.CirCleRefreshLayout_CircleSmaller, mHeaderCircleSmaller);

        a.recycle();
    }

    private void addHeaderView() {
        mHeader = new AnimationView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);

        addViewInternal(mHeader);
        mHeader.setAniBackColor(mHeaderBackColor);
        mHeader.setAniForeColor(mHeaderForeColor);
        mHeader.setRadius(mHeaderCircleSmaller);

        //初始化头部动画mUpBackAnimator、mUpTopAnimator
        setUpChildAnimation();
    }

    private void setUpChildAnimation() {
        if (mChildView == null) {
            return;
        }
        mUpBackAnimator = ValueAnimator.ofFloat(mPullHeight, mHeaderHeight);
        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (mChildView != null) {
                    Log.d("","val:"+val);
                    mChildView.setTranslationY(val);
                }
            }
        });
        mUpBackAnimator.setDuration(REL_DRAG_DUR);
        mUpTopAnimator = ValueAnimator.ofFloat(mHeaderHeight, 0);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });
        mUpTopAnimator.setDuration(BACK_TOP_DUR);

        mHeader.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            @Override
            public void viewAniDone() {
//                Log.i(TAG, "should invoke");
                mUpTopAnimator.start();
            }
        });


    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }

        mChildView = child;
        super.addView(child);
        setUpChildAnimation();
    }

    private boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }


        return ViewCompat.canScrollVertically(mChildView, -1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //刷新过程中拦截所有点击事件
        if (mIsRefreshing) {
            return true;
        }
        switch (ev.getAction()) {
            //记下按下的Y坐标
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                mTouchCurY = mTouchStartY;
                break;
            case MotionEvent.ACTION_MOVE:
                float curY = ev.getY();
                float dy = curY - mTouchStartY;
                //如果是向下滑动并且已经滑动顶部，拦截点击事件
                if (dy > 0 && !canChildScrollUp()) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRefreshing) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurY = event.getY();
                //手指下滑的物理距离
                float dy = mTouchCurY - mTouchStartY;

                //移动mChildView和mHeader
                if (mChildView != null) {
                    //巧妙地利用减速插值器decelerateInterpolator.getInterpolation()方法，该方法input的参数是0到1，output也是0到1
                    //实现随着下拉距离的增大，下拉阻尼变大的下拉手感
                    float offsetY = decelerateInterpolator.getInterpolation(dy / DRAG_RATE / mPullHeight) * dy / DRAG_RATE;
//                    float offsetY = dy / DRAG_RATE;
                    //保证下拉距离大于0而不大于mPullHeight
                    offsetY = Math.min(mPullHeight, offsetY);
                    offsetY = Math.max(0, offsetY);
                    Log.d("TAG", "offsetY:" + offsetY + "dy:" + dy + "mPullHeight:" + mPullHeight);
                    mChildView.setTranslationY(offsetY);

                    mHeader.getLayoutParams().height = (int) offsetY;
                    mHeader.requestLayout();
                }

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    //如果下拉高度达到mHeaderHeight，触发刷新
                    if (mChildView.getTranslationY() >= mHeaderHeight) {
                        //将mChildView回滚到mHeaderHeight
                        mUpBackAnimator = ValueAnimator.ofFloat(mChildView.getTranslationY(), mHeaderHeight);
                        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                if (mChildView != null) {
                                    Log.d("","val:"+val);
                                    mChildView.setTranslationY(val);
                                }
                            }
                        });
                        mUpBackAnimator.start();
                        mHeader.releaseDrag();
                        mIsRefreshing = true;
                        if (onCircleRefreshListener != null) {
                            onCircleRefreshListener.refreshing();
                        }

                    } else {//否则，回滚
                        float height = mChildView.getTranslationY();
                        ValueAnimator backTopAni = ValueAnimator.ofFloat(height, 0);
                        backTopAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                                if (mChildView != null) {
                                    mChildView.setTranslationY(val);
                                }
                                mHeader.getLayoutParams().height = (int) val;
                                mHeader.requestLayout();
                            }
                        });
                        backTopAni.setDuration((long) (height * BACK_TOP_DUR / mHeaderHeight));
                        backTopAni.start();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void finishRefreshing() {
        if (onCircleRefreshListener != null) {
            onCircleRefreshListener.completeRefresh();
        }
        mIsRefreshing = false;
        mHeader.setRefreshing(false);
    }

    private OnCircleRefreshListener onCircleRefreshListener;

    public void setOnRefreshListener(OnCircleRefreshListener onCircleRefreshListener) {
        this.onCircleRefreshListener = onCircleRefreshListener;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public interface OnCircleRefreshListener {
        void completeRefresh();

        void refreshing();
    }
}

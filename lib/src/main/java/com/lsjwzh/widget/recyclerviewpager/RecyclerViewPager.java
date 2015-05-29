package com.lsjwzh.widget.recyclerviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * RecyclerViewPager
 * @author Green
 */
public class RecyclerViewPager extends RecyclerView {

    private RecyclerViewPagerAdapter<?> mViewPagerAdapter;
    private OnScrollListener mOnScrollListener;
    private float mTriggerOffset = 0.25f;
    private float mFlingFactor = 0.15f;

    public RecyclerViewPager(Context context) {
        super(context);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewPager, defStyle,
                0);
        mFlingFactor = a.getFloat(R.styleable.RecyclerViewPager_flingFactor, 0.15f);
        mTriggerOffset = a.getFloat(R.styleable.RecyclerViewPager_triggerOffset, 0.25f);
        a.recycle();

    }

    public void setFlingFactor(float flingFactor) {
        mFlingFactor = flingFactor;
    }

    public float getFlingFactor() {
        return mFlingFactor;
    }

    public void setTriggerOffset(float triggerOffset) {
        mTriggerOffset = triggerOffset;
    }

    public float getTriggerOffset() {
        return mTriggerOffset;
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mViewPagerAdapter = new RecyclerViewPagerAdapter(this, adapter);
        super.setAdapter(mViewPagerAdapter);
        super.setOnScrollListener(new OnScrollListener() {
            boolean mNeedAdjust;
            int mLeft;
            View mCurView;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_DRAGGING) {
                    mNeedAdjust = true;
                    mCurView = ViewUtils.getCenterXChild(recyclerView);
                    if (mCurView != null) {
                        mLeft = mCurView.getLeft();
                    }
                } else if (newState == SCROLL_STATE_SETTLING) {
                    mNeedAdjust = false;
                    mCurView = null;
                } else if (mNeedAdjust && newState == SCROLL_STATE_IDLE) {
                    int targetPosition = ViewUtils.getCenterXChildPosition(recyclerView);
                    if (mCurView != null) {
                        targetPosition = recyclerView.getChildPosition(mCurView);
                        int spanX = mCurView.getLeft() - mLeft;
                        if (spanX > mCurView.getWidth() * mTriggerOffset) {
                            targetPosition--;
                        } else if (spanX < mCurView.getWidth() * -mTriggerOffset) {
                            targetPosition++;
                        }
                    }
                    targetPosition = Math.max(targetPosition, 0);
                    targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
                    smoothScrollToPosition(targetPosition);
                }
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }
        });
    }

    @Override
    public Adapter getAdapter() {
        if (mViewPagerAdapter != null) {
            return mViewPagerAdapter.mAdapter;
        }
        return null;
    }

    public RecyclerViewPagerAdapter getWrapperAdapter() {
        return mViewPagerAdapter;
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        Log.d("@", "velocityX:" + velocityX);
        boolean flinging = super.fling(velocityX, velocityY);
        if (flinging) {
            if (getLayoutManager().canScrollHorizontally()) {
                adjustPositionX(velocityX);
            } else {
                adjustPositionY(velocityY);
            }
        }
        return flinging;
    }


    /***
     * adjust position before Touch event complete and fling action start.
     */
    protected void adjustPositionX(int velocityX) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int curPosition = ViewUtils.getCenterXChildPosition(this);
            int childWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int flingCount = (int) (velocityX * mFlingFactor / childWidth);
            int targetPosition = curPosition + flingCount;
            targetPosition = Math.max(targetPosition, 0);
            targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
            smoothScrollToPosition(targetPosition);
        }
    }

    /***
     * adjust position before Touch event complete and fling action start.
     */
    protected void adjustPositionY(int velocityY) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int curPosition = ViewUtils.getCenterXChildPosition(this);
            int childHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int flingCount = (int) (velocityY * mFlingFactor / childHeight);
            int targetPosition = curPosition + flingCount;
            targetPosition = Math.max(targetPosition, 0);
            targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
            smoothScrollToPosition(targetPosition);
        }
    }

}

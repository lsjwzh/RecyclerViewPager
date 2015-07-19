package com.lsjwzh.widget.recyclerviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerViewPager
 *
 * @author Green
 */
public class RecyclerViewPager extends RecyclerView {
    // ToDo remove this before release
    public static final boolean DEBUG = true;

    private RecyclerViewPagerAdapter<?> mViewPagerAdapter;
    private float mTriggerOffset = 0.25f;
    private float mFlingFactor = 0.15f;
    private float mTouchSpan;
    private List<OnPageChangedListener> mOnPageChangedListeners;
    private int mSmoothScrollTargetPosition = -1;
    private int mPositionBeforeScroll = -1;

    boolean mNeedAdjust;
    int mLeft;
    int mTop;
    View mCurView;

    public RecyclerViewPager(Context context) {
        this(context, null);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs, defStyle);
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
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            Field fLayoutState = state.getClass().getDeclaredField("mLayoutState");
            fLayoutState.setAccessible(true);
            Object layoutState = fLayoutState.get(state);
            Field fAnchorOffset = layoutState.getClass().getDeclaredField("mAnchorOffset");
            Field fAnchorPosition = layoutState.getClass().getDeclaredField("mAnchorPosition");
            fAnchorPosition.setAccessible(true);
            fAnchorOffset.setAccessible(true);
            if (fAnchorOffset.getInt(layoutState) > 0) {
                fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) - 1);
            } else {
                fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) + 1);
            }
            fAnchorOffset.setInt(layoutState, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mViewPagerAdapter = new RecyclerViewPagerAdapter(this, adapter);
        super.setAdapter(mViewPagerAdapter);
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
    public boolean fling(int velocityX, int velocityY) {
        boolean flinging = super.fling((int) (velocityX * mFlingFactor), (int) (velocityY * mFlingFactor));
        if (flinging) {
            if (getLayoutManager().canScrollHorizontally()) {
                adjustPositionX(velocityX);
                if (DEBUG) {
                    Log.d("@", "velocityX:" + velocityX);
                }
            } else {
                adjustPositionY(velocityY);
                if (DEBUG) {
                    Log.d("@", "velocityY:" + velocityY);
                }
            }
        }
        return flinging;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (DEBUG) {
            Log.d("@", "smoothScrollToPosition:" + position);
        }
        mSmoothScrollTargetPosition = position;
        super.smoothScrollToPosition(position);
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
            if (targetPosition == curPosition) {
                View centerXChild = ViewUtils.getCenterXChild(this);
                if (centerXChild != null) {
                    if (mTouchSpan > centerXChild.getWidth() * mTriggerOffset * mTriggerOffset && targetPosition != 0) {
                        targetPosition--;
                    } else if (mTouchSpan < centerXChild.getWidth() * -mTriggerOffset && targetPosition != getAdapter().getItemCount() - 1) {
                        targetPosition++;
                    }
                }
            }
            if (DEBUG) {
                Log.d("@", "mTouchSpan:" + mTouchSpan);
                Log.d("@", "adjustPositionX:" + targetPosition);
            }
            smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
        }
    }

    public void addOnPageChangedListener(OnPageChangedListener listener) {
        if (mOnPageChangedListeners == null) {
            mOnPageChangedListeners = new ArrayList<>();
        }
        mOnPageChangedListeners.add(listener);
    }

    public void removeOnPageChangedListener(OnPageChangedListener listener) {
        if (mOnPageChangedListeners != null) {
            mOnPageChangedListeners.remove(listener);
        }
    }

    public void clearOnPageChangedListeners() {
        if (mOnPageChangedListeners != null) {
            mOnPageChangedListeners.clear();
        }
    }

    /***
     * adjust position before Touch event complete and fling action start.
     */
    protected void adjustPositionY(int velocityY) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int curPosition = ViewUtils.getCenterYChildPosition(this);
            int childHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int flingCount = (int) (velocityY * mFlingFactor / childHeight);
            int targetPosition = curPosition + flingCount;
            targetPosition = Math.max(targetPosition, 0);
            targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
            if (targetPosition == curPosition) {
                View centerYChild = ViewUtils.getCenterYChild(this);
                if (centerYChild != null) {
                    if (mTouchSpan > centerYChild.getHeight() * mTriggerOffset && targetPosition != 0) {
                        targetPosition--;
                    } else if (mTouchSpan < centerYChild.getHeight() * -mTriggerOffset && targetPosition != getAdapter().getItemCount() - 1) {
                        targetPosition++;
                    }
                }
            }
            if (DEBUG) {
                Log.d("@", "mTouchSpan:" + mTouchSpan);
                Log.d("@", "adjustPositionY:" + targetPosition);
            }
            smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            mNeedAdjust = true;
            mCurView = getLayoutManager().canScrollHorizontally() ? ViewUtils.getCenterXChild(this) :
                    ViewUtils.getCenterYChild(this);
            if (mCurView != null) {
                mPositionBeforeScroll = getChildLayoutPosition(mCurView);
                if (DEBUG) {
                    Log.d("@", "mPositionBeforeScroll:" + mPositionBeforeScroll);
                }
                mLeft = mCurView.getLeft();
                mTop = mCurView.getTop();
            } else {
                mPositionBeforeScroll = -1;
            }
            mTouchSpan = 0;
        } else if (state == SCROLL_STATE_SETTLING) {
            mNeedAdjust = false;
            if (mCurView != null) {
                if (getLayoutManager().canScrollHorizontally()) {
                    mTouchSpan = mCurView.getLeft() - mLeft;
                } else {
                    mTouchSpan = mCurView.getTop() - mTop;
                }
            } else {
                mTouchSpan = 0;
            }
            mCurView = null;
        } else if (state == SCROLL_STATE_IDLE) {
            if (mNeedAdjust) {
                int targetPosition = getLayoutManager().canScrollHorizontally() ? ViewUtils.getCenterXChildPosition(this) :
                        ViewUtils.getCenterYChildPosition(this);
                if (mCurView != null) {
                    targetPosition = getChildAdapterPosition(mCurView);
                    if (getLayoutManager().canScrollHorizontally()) {
                        int spanX = mCurView.getLeft() - mLeft;
                        if (spanX > mCurView.getWidth() * mTriggerOffset) {
                            targetPosition--;
                        } else if (spanX < mCurView.getWidth() * -mTriggerOffset) {
                            targetPosition++;
                        }
                    } else {
                        int spanY = mCurView.getTop() - mTop;
                        if (spanY > mCurView.getHeight() * mTriggerOffset) {
                            targetPosition--;
                        } else if (spanY < mCurView.getHeight() * -mTriggerOffset) {
                            targetPosition++;
                        }
                    }
                }
                smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
                mCurView = null;
            } else if (mSmoothScrollTargetPosition != mPositionBeforeScroll) {
                if (DEBUG) {
                    Log.d("@", "onPageChanged:" + mSmoothScrollTargetPosition);
                }
                if (mOnPageChangedListeners != null) {
                    for (OnPageChangedListener onPageChangedListener : mOnPageChangedListeners) {
                        if (onPageChangedListener != null) {
                            onPageChangedListener.OnPageChanged(mPositionBeforeScroll, mSmoothScrollTargetPosition);
                        }
                    }
                }
            }
        }
    }

    private int safeTargetPosition(int position, int count) {
        if (position < 0) {
            return 0;
        }
        if (position >= count) {
            return count - 1;
        }
        return position;
    }

    public interface OnPageChangedListener {
        void OnPageChanged(int oldPosition, int newPosition);
    }

}

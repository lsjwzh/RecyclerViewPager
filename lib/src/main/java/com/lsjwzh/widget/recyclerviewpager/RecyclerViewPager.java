package com.lsjwzh.widget.recyclerviewpager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.FixPagerSnapHelper;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PowerfulPagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewUtil;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class RecyclerViewPager extends RecyclerView {
  public static final int SCROLL_TEND_NONE = -1;
  public static final int SCROLL_TEND_FORWARD = 0;
  public static final int SCROLL_TEND_DOWNWARD = 1;
  public static final int INVALID_POINTER = -1;
  final String TAG = getClass().getSimpleName();
  protected int mScrollPointerId = INVALID_POINTER;
  protected int mInitialTouchX, mInitialTouchY;
  protected int mTouchSlop;
  protected PowerfulPagerSnapHelper mSnapHelper;
  protected int mScrollTend = SCROLL_TEND_NONE;

  private List<OnPageChangedListener> mPageListeners = new ArrayList<>();
  private RecyclerViewPagerAdapter<?> mViewPagerAdapter;

  private int mCurrentPosition = -1;
  private int prevPosition = -1;
  private Integer mPendingTargetPosition;
  private Runnable mPageChangedNotifyRunnable = new Runnable() {
    @Override
    public void run() {
      if (getScrollState() == SCROLL_STATE_IDLE && getChildCount() > 0) {
        removeCallbacks(this);
        notifyPageSelected(mCurrentPosition, prevPosition);
      } else {
        postDelayed(mPageChangedNotifyRunnable, 10);
      }
    }
  };

  public RecyclerViewPager(Context context) {
    this(context, null);
  }

  public RecyclerViewPager(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RecyclerViewPager(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    mSnapHelper = new PowerfulPagerSnapHelper() {

      @Nullable
      @Override
      protected SmoothScroller createScroller(LayoutManager layoutManager) {
        removeCallbacks(mPageChangedNotifyRunnable);
        return super.createScroller(layoutManager);
      }

      @Override
      public View findSnapView(LayoutManager layoutManager) {
        View view = super.findSnapView(layoutManager);

        if (view != null) {
          int childAdapterPosition = getChildAdapterPosition(view);
          if (childAdapterPosition != mCurrentPosition) {
            prevPosition = mCurrentPosition;
            mCurrentPosition = childAdapterPosition;
          }
          Log.e(TAG, String.format("findSnapView: %s/%s/%s", prevPosition, mCurrentPosition,
              mScrollTend));
        }
        mScrollTend = SCROLL_TEND_NONE;
        return view;
      }

      @Override
      public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX,
                                        int velocityY) {
        int targetPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        Log.e(TAG, String.format("findTargetSnapPosition: current %s/ mScrollTend %s/ " +
                "targetPosition %s/ velocityX %s",
            mCurrentPosition, mScrollTend, targetPosition, velocityX));
        if (targetPosition != RecyclerView.NO_POSITION) {
          prevPosition = mCurrentPosition;
          mCurrentPosition = targetPosition;
        }

        return targetPosition;
      }

      @Override
      public void onSmoothScrollStop() {
        postDelayed(mPageChangedNotifyRunnable, 10);
      }

      @Nullable
      @Override
      public int[] calculateDistanceToFinalSnap(@NonNull LayoutManager layoutManager, @NonNull
              View targetView) {
        return super.calculateDistanceToFinalSnap(layoutManager, targetView);
      }
    };
    mSnapHelper.attachToRecyclerView(this);
    setScrollingTouchSlop(TOUCH_SLOP_PAGING);
  }

  @Override
  public void setAdapter(Adapter adapter) {
    mViewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
    super.setAdapter(mViewPagerAdapter);
  }

  @Override
  public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
    mViewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
    super.swapAdapter(mViewPagerAdapter, removeAndRecycleExistingViews);
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

  @SuppressWarnings("unchecked")
  @NonNull
  protected RecyclerViewPagerAdapter ensureRecyclerViewPagerAdapter(Adapter adapter) {
    return (adapter instanceof RecyclerViewPagerAdapter)
            ? (RecyclerViewPagerAdapter) adapter
            : new RecyclerViewPagerAdapter(this, adapter);

  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      mScrollTend = SCROLL_TEND_NONE;
      mSnapHelper.updateTouchStartView(getLayoutManager());
    } else if (ev.getAction() == MotionEvent.ACTION_UP
        || ev.getAction() == MotionEvent.ACTION_CANCEL
        || ev.getAction() == MotionEvent.ACTION_POINTER_UP) {
      final int index = ev.findPointerIndex(mScrollPointerId);
      if (index >= 0) {
        final boolean canScrollHorizontally = getLayoutManager().canScrollHorizontally();
        final int x = (int) (ev.getX(index) + 0.5f);
        final int y = (int) (ev.getY(index) + 0.5f);
        final int dx = x - mInitialTouchX;
        final int dy = y - mInitialTouchY;
        if (canScrollHorizontally) {
          mScrollTend = dx < 0 ? SCROLL_TEND_FORWARD : SCROLL_TEND_DOWNWARD;
        } else {
          mScrollTend = dy < 0 ? SCROLL_TEND_FORWARD : SCROLL_TEND_DOWNWARD;
        }
        Log.e(TAG, String.format("dispatchTouchEvent: mCurrentPosition %s/ mScrollTend %s/ dx %s",
            mCurrentPosition, mScrollTend, dx));
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public void setScrollingTouchSlop(int slopConstant) {
    super.setScrollingTouchSlop(slopConstant);

    final ViewConfiguration vc = ViewConfiguration.get(getContext());
    switch (slopConstant) {
      case TOUCH_SLOP_DEFAULT:
        mTouchSlop = vc.getScaledTouchSlop();
        mSnapHelper.allowSmoothScroll = true;
        break;
      case TOUCH_SLOP_PAGING:
        mSnapHelper.allowSmoothScroll = false;
        mTouchSlop = vc.getScaledPagingTouchSlop();
        break;
      default:
        break;
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent e) {
    final int action = e.getActionMasked();
    final int actionIndex = e.getActionIndex();

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mScrollPointerId = e.getPointerId(0);
        mInitialTouchX = (int) (e.getX() + 0.5f);
        mInitialTouchY = (int) (e.getY() + 0.5f);

        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        mScrollPointerId = e.getPointerId(actionIndex);
        mInitialTouchX = (int) (e.getX(actionIndex) + 0.5f);
        mInitialTouchY = (int) (e.getY(actionIndex) + 0.5f);
        break;

      case MotionEvent.ACTION_MOVE: {
        final int index = e.findPointerIndex(mScrollPointerId);
        if (index < 0) {
          Log.e(TAG, "Error processing scroll; pointer index for id "
              + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
          return false;
        }

        final boolean canScrollHorizontally = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertically = getLayoutManager().canScrollVertically();

        final int x = (int) (e.getX(index) + 0.5f);
        final int y = (int) (e.getY(index) + 0.5f);
        if (getScrollState() != SCROLL_STATE_DRAGGING) {
          final int dx = x - mInitialTouchX;
          final int dy = y - mInitialTouchY;
          boolean startScroll = false;

          /**
           * Notice:
           * consider the angle when decide to intercept touch event
           */
          if (canScrollHorizontally && Math.abs(dx) > mTouchSlop &&
              (Math.abs(dx) >= Math.abs(dy) || canScrollVertically)) {
            startScroll = true;
          }
          if (canScrollVertically && Math.abs(dy) > mTouchSlop &&
              (Math.abs(dy) >= Math.abs(dx) || canScrollHorizontally)) {
            startScroll = true;
          }

          return beforeInterceptTouchEvent(startScroll, dx, dy)
              || (super.onInterceptTouchEvent(e) && startScroll);
        }
      }
      break;

      case MotionEvent.ACTION_POINTER_UP: {
        onPointerUp(e);
      }
      break;
    }

    return super.onInterceptTouchEvent(e);
  }

  protected boolean beforeInterceptTouchEvent(boolean startScroll, int dx, int dy) {
    return false;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (mPendingTargetPosition != null) {
      prevPosition = mCurrentPosition;
      mCurrentPosition = mPendingTargetPosition;
      if (mCurrentPosition != prevPosition) {
        smoothScrollToPosition(mCurrentPosition);
      }
      mPendingTargetPosition = null;
    } else if (mCurrentPosition == -1 && getAdapter() != null && getAdapter().getItemCount() > 0) {
      prevPosition = mCurrentPosition;
      mCurrentPosition = 0;
      notifyPageSelected(mCurrentPosition, prevPosition);
    }
  }

  @Override
  public boolean hasFocusable() {
    // 如果不设置为false,则外围横向RecyclerView有可能在滑动时,调用recoverFocusFromState,
    // 从而把第一个Item作为focusView,并定位到此Item
    return false;
  }

  @Override
  public void scrollToPosition(int position) {
    removeCallbacks(mPageChangedNotifyRunnable);
    super.scrollToPosition(position);
    mPendingTargetPosition = position;

  }

  @Override
  public void smoothScrollToPosition(int position) {
    if (mPendingTargetPosition != null && mPendingTargetPosition == position) {
      return;
    }
    removeCallbacks(mPageChangedNotifyRunnable);
    LayoutManager layoutManager = getLayoutManager();
    if (!RecyclerViewUtil.isLayoutFrozen(this) && layoutManager != null) {
      LinearSmoothScroller linearSmoothScroller = mSnapHelper.createSnapScroller(layoutManager);
      if (linearSmoothScroller != null) {
        linearSmoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(linearSmoothScroller);
      }
    }
    mPendingTargetPosition = position;
  }

  public int getCurrentPosition() {
    return mCurrentPosition;
  }

  public void addOnPageChangedListener(OnPageChangedListener listener) {
    mPageListeners.add(listener);
  }


  public void removeOnPageChangedListener(OnPageChangedListener listener) {
    mPageListeners.remove(listener);
  }

  /**
   * Notice: position may be greater than adapter.getItemCount
   *
   * @param position
   * @param prevPosition
   */
  public void notifyPageSelected(int position, int prevPosition) {
    mPendingTargetPosition = null;
    if (position != prevPosition) {
      for (OnPageChangedListener listener : mPageListeners) {
        listener.OnPageChanged(prevPosition, position);
      }

    }
  }

  private void onPointerUp(MotionEvent e) {
    final int actionIndex = e.getActionIndex();
    if (e.getPointerId(actionIndex) == mScrollPointerId) {
      // Pick a new pointer to pick up the slack.
      final int newIndex = actionIndex == 0 ? 1 : 0;
      mScrollPointerId = e.getPointerId(newIndex);
      mInitialTouchX = (int) (e.getX(newIndex) + 0.5f);
      mInitialTouchY = (int) (e.getY(newIndex) + 0.5f);
    }
  }

  public interface OnPageChangedListener {
    void OnPageChanged(int prevPosition, int position);
  }
}

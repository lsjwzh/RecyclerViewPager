package com.lsjwzh.widget.recyclerviewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearPagerSnapHelper;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RVPUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SinglePagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.SnapScrollerCreator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewPager extends RecyclerView {
    public static final int INVALID_POINTER = -1;
    final String TAG = getClass().getSimpleName();
    private final SinglePagerSnapHelper singlePagerSnapHelper;
    private final LinearPagerSnapHelper linearPagerSnapHelper;
    protected int scrollPointerId = INVALID_POINTER;
    protected int initialTouchX, initialTouchY;
    protected int touchSlop;
    protected SnapHelper snapHelper;

    private List<OnPageChangedListener> pageListeners = new ArrayList<>();
    private RecyclerViewPagerAdapter<?> viewPagerAdapter;

    private int currentPosition = -1;
    private int prevPosition = -1;
    private Integer pendingTargetPosition;
    private boolean singlePagerEnable = true;
    private Runnable pageChangedNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            if (getScrollState() == SCROLL_STATE_IDLE && getChildCount() > 0) {
                removeCallbacks(this);
                if (pendingTargetPosition != null) {
                    prevPosition = currentPosition;
                    currentPosition = pendingTargetPosition;
                    notifyPageSelected(currentPosition, prevPosition);
                }
            } else {
                postDelayed(pageChangedNotifyRunnable, 10);
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
        singlePagerSnapHelper = new SinglePagerSnapHelper() {

            @Override
            public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX,
                                              int velocityY) {
                int targetPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                return RecyclerViewPager.this
                        .findTargetSnapPosition(targetPosition, layoutManager, velocityX, velocityY);
            }
        };
        linearPagerSnapHelper = new LinearPagerSnapHelper() {

            @Override
            public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX,
                                              int velocityY) {
                int targetPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                return RecyclerViewPager.this
                        .findTargetSnapPosition(targetPosition, layoutManager, velocityX, velocityY);
            }
        };
        snapHelper = singlePagerSnapHelper;
        snapHelper.attachToRecyclerView(this);
        setScrollingTouchSlop(TOUCH_SLOP_PAGING);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    Log.d(TAG, "notify when idle");
                    postDelayed(pageChangedNotifyRunnable, 10);
                }
            }
        });
    }

    public void setSinglePagerEnable(boolean enable) {
        singlePagerEnable = enable;
        snapHelper.attachToRecyclerView(null);
        if (singlePagerEnable) {
            snapHelper = singlePagerSnapHelper;
        } else {
            snapHelper = linearPagerSnapHelper;
        }
        snapHelper.attachToRecyclerView(this);
    }

    public boolean isSinglePagerEnable() {
        return singlePagerEnable;
    }

    protected int findTargetSnapPosition(int targetPosition, LayoutManager layoutManager, int velocityX, int velocityY) {
        Log.e(TAG, String.format("findTargetSnapPosition: current %s/ " +
                        "targetPosition %s/ velocityX %s",
                currentPosition, targetPosition, velocityX));
        if (targetPosition == currentPosition) {
            prevPosition = currentPosition;
            return RecyclerView.NO_POSITION;
        }
        if (targetPosition != RecyclerView.NO_POSITION) {
            pendingTargetPosition = targetPosition;
        }
        return targetPosition;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        viewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
        super.setAdapter(viewPagerAdapter);
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        viewPagerAdapter = ensureRecyclerViewPagerAdapter(adapter);
        super.swapAdapter(viewPagerAdapter, removeAndRecycleExistingViews);
    }

    @Override
    public Adapter getAdapter() {
        return viewPagerAdapter;
    }

    public RecyclerViewPagerAdapter getActualAdapter() {
        return viewPagerAdapter;
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
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setScrollingTouchSlop(int slopConstant) {
        super.setScrollingTouchSlop(slopConstant);

        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        switch (slopConstant) {
            case TOUCH_SLOP_DEFAULT:
                touchSlop = vc.getScaledTouchSlop();
                break;
            case TOUCH_SLOP_PAGING:
                touchSlop = vc.getScaledPagingTouchSlop();
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
                scrollPointerId = e.getPointerId(0);
                initialTouchX = (int) (e.getX() + 0.5f);
                initialTouchY = (int) (e.getY() + 0.5f);

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                scrollPointerId = e.getPointerId(actionIndex);
                initialTouchX = (int) (e.getX(actionIndex) + 0.5f);
                initialTouchY = (int) (e.getY(actionIndex) + 0.5f);
                break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(scrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + scrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final boolean canScrollHorizontally = getLayoutManager().canScrollHorizontally();
                final boolean canScrollVertically = getLayoutManager().canScrollVertically();

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                final int scrollState = getScrollState();
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    final int dx = x - initialTouchX;
                    final int dy = y - initialTouchY;
                    boolean startScroll = false;

                    /**
                     * Notice:
                     * consider the angle when decide to intercept touch event
                     */
                    if (canScrollHorizontally && Math.abs(dx) > touchSlop &&
                            (Math.abs(dx) >= Math.abs(dy) || canScrollVertically)) {
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > touchSlop &&
                            (Math.abs(dy) >= Math.abs(dx) || canScrollHorizontally)) {
                        startScroll = true;
                    }
                    super.onInterceptTouchEvent(e);
                    if (startScroll) {
                        RVPUtil.setScrollState(this, SCROLL_STATE_DRAGGING);
                    } else {
                        RVPUtil.setScrollState(this, scrollState);
                    }

                    return beforeInterceptTouchEvent(startScroll, dx, dy)
                            || getScrollState() == SCROLL_STATE_DRAGGING;
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
        if (pendingTargetPosition != null) {
            prevPosition = currentPosition;
            currentPosition = pendingTargetPosition;
            if (currentPosition != prevPosition) {
                smoothScrollToPosition(currentPosition);
            }
            pendingTargetPosition = null;
        } else if (currentPosition == -1 && getAdapter() != null && getAdapter().getItemCount() > 0) {
            prevPosition = currentPosition;
            currentPosition = 0;
            notifyPageSelected(currentPosition, prevPosition);
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
        removeCallbacks(pageChangedNotifyRunnable);
        super.scrollToPosition(position);
        pendingTargetPosition = position;

    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (pendingTargetPosition != null && pendingTargetPosition == position) {
            return;
        }
        removeCallbacks(pageChangedNotifyRunnable);
        LayoutManager layoutManager = getLayoutManager();
        if (!RVPUtil.isLayoutFrozen(this) && layoutManager != null) {
            LinearSmoothScroller linearSmoothScroller =
                    ((SnapScrollerCreator) snapHelper).createSnapScroller(layoutManager);
            if (linearSmoothScroller != null) {
                linearSmoothScroller.setTargetPosition(position);
                layoutManager.startSmoothScroll(linearSmoothScroller);
            }
        }
        pendingTargetPosition = position;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void addOnPageChangedListener(OnPageChangedListener listener) {
        pageListeners.add(listener);
    }


    public void removeOnPageChangedListener(OnPageChangedListener listener) {
        pageListeners.remove(listener);
    }

    /**
     * Notice: position may be greater than adapter.getItemCount
     *
     * @param position
     * @param prevPosition
     */
    public void notifyPageSelected(int position, int prevPosition) {
        pendingTargetPosition = null;
        if (position != prevPosition) {
            for (OnPageChangedListener listener : pageListeners) {
                listener.OnPageChanged(prevPosition, position);
            }

        }
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == scrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            scrollPointerId = e.getPointerId(newIndex);
            initialTouchX = (int) (e.getX(newIndex) + 0.5f);
            initialTouchY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    public interface OnPageChangedListener {
        void OnPageChanged(int prevPosition, int position);
    }
}

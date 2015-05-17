package android.support.v7.widget;

import java.util.List;

import android.content.Context;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import static android.support.v7.widget.RecyclerViewEx.NO_POSITION;

public class HorizontalCenterLayoutManager extends RecyclerViewEx.LayoutManager {

    private static final String TAG = "HCLayoutManager";

    private static final boolean DEBUG = false;

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    /**
     * While trying to find next view to focus, LayoutManager will not try to scroll more
     * than this factor times the total space of the list. If layout is vertical, total space is the
     * height minus padding, if layout is horizontal, total space is the width minus padding.
     */
    private static final float MAX_SCROLL_FACTOR = 0.33f;
    private static final int INVALID_OFFSET = Integer.MIN_VALUE;

    /**
     * Helper class that keeps temporary layout state.
     * It does not keep state after layout is complete but we still keep a reference to re-use
     * the same object.
     */
    private LayoutState mLayoutState;

    /**
     * Many calculations are made depending on orientation. To keep it clean, this interface
     * helps {@link LinearLayoutManagerEx} make those decisions.
     * Based on {@link #}, an implementation is lazily created in
     * {@link #ensureLayoutState} method.
     */
    OrientationHelperEx mOrientationHelper;

    /**
     * Works the same way as {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}.
     * see {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}
     */
    private boolean mSmoothScrollbarEnabled = true;

    /**
     * When LayoutManager needs to scroll to a position, it sets this variable and requests a
     * layout which will check this variable and re-layout accordingly.
     */
    int mPendingScrollPosition = NO_POSITION;

    private boolean mRecycleChildrenOnDetach;

    SavedState mPendingSavedState = null;

    /**
     * Re-used variable to keep anchor information on re-layout.
     * Anchor position and coordinate defines the reference point for LLM while doing a layout.
     */
    final AnchorInfo mAnchorInfo;

    /**
     * Creates a vertical LinearLayoutManager
     *
     * @param context Current context, will be used to access resources.
     */
    public HorizontalCenterLayoutManager(Context context) {
        mAnchorInfo = new AnchorInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerViewEx.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerViewEx.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Returns whether LayoutManager will recycle its children when it is detached from
     * RecyclerViewEx.
     *
     * @return true if LayoutManager will recycle its children when it is detached from
     * RecyclerViewEx.
     */
    public boolean getRecycleChildrenOnDetach() {
        return mRecycleChildrenOnDetach;
    }

    /**
     * Set whether LayoutManager will recycle its children when it is detached from
     * RecyclerViewEx.
     * <p/>
     * If you are using a {@link RecyclerViewEx.RecycledViewPool}, it might be a good idea to set
     * this flag to <code>true</code> so that views will be avilable to other RecyclerViewExs
     * immediately.
     * <p/>
     * Note that, setting this flag will result in a performance drop if RecyclerViewEx
     * is restored.
     *
     * @param recycleChildrenOnDetach Whether children should be recycled in detach or not.
     */
    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    @Override
    public void onDetachedFromWindow(RecyclerViewEx view, RecyclerViewEx.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            final AccessibilityRecordCompat record = AccessibilityEventCompat
                    .asRecord(event);
            record.setFromIndex(findFirstVisibleItemPosition());
            record.setToIndex(findLastVisibleItemPosition());
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        if (getChildCount() > 0) {
            ensureLayoutState();
            state.mAnchorLayoutFromEnd = false;
            final View refChild = getChildClosestToStart();
            state.mAnchorPosition = getPosition(refChild);
//            state.mAnchorOffset = //mOrientationHelper.getDecoratedStart(refChild) -
//                    mOrientationHelper.getStartAfterPadding();
        } else {
            state.invalidateAnchor();
        }
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = (SavedState) state;
            requestLayout();
            if (DEBUG) {
                Log.d(TAG, "loaded saved state");
            }
        } else if (DEBUG) {
            Log.d(TAG, "invalid saved state class");
        }
    }

    /**
     * @return true
     */
    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    /**
     * @return false
     */
    @Override
    public boolean canScrollVertically() {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public View findViewByPosition(int position) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        final int firstChild = getPosition(getChildAt(0));
        final int viewPosition = position - firstChild;
        if (viewPosition >= 0 && viewPosition < childCount) {
            return getChildAt(viewPosition);
        }
        return null;
    }

    /**
     * <p>Returns the amount of extra space that should be laid out by LayoutManager.
     * By default, {@link LinearLayoutManagerEx} lays out 1 extra page of
     * items while smooth scrolling and 0 otherwise. You can override this method to implement your
     * custom layout pre-cache logic.</p>
     * <p>Laying out invisible elements will eventually come with performance cost. On the other
     * hand, in places like smooth scrolling to an unknown location, this extra content helps
     * LayoutManager to calculate a much smoother scrolling; which improves user experience.</p>
     * <p>You can also use this if you are trying to pre-layout your upcoming views.</p>
     *
     * @return The extra space that should be laid out (in pixels).
     */
    protected int getExtraLayoutSpace(RecyclerViewEx.State state) {
        if (state.hasTargetScrollPosition()) {
            return mOrientationHelper.getTotalSpace();
        } else {
            return 0;
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerViewEx recyclerView, RecyclerViewEx.State state,
                                       int position) {
        final LinearSmoothScrollerEx scroller = new LinearSmoothScrollerEx(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if (getChildCount() == 0) {
                    return null;
                }

                final int direction = targetPosition < getFirstVisiblePosition() ? -1 : 1;
                if (DEBUG) {
                    LogEx.d(TAG, "direction:" + direction);
                }
                return new PointF(direction, 0);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }


            @Override
            protected int calculateTimeForScrolling(int dx) {
                int originVal = super.calculateTimeForScrolling(dx);
                return Math.max(originVal, 100);
            }

            @Override
            protected int calculateTimeForDeceleration(int dx) {
                // we want to cover same area with the linear interpolator for the first 10% of the
                // interpolation. After that, deceleration will take control.
                // area under curve (1-(1-x)^2) can be calculated as (1 - x/3) * x * x
                // which gives 0.100028 when x = .3356
                // this is why we divide linear scrolling time with .3356
                return (int) Math.ceil(calculateTimeForScrolling(dx) / .3356);
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0) {
            return 0;
        }

        return getPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }

        return getPosition(getChildAt(childCount - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLayoutChildren(RecyclerViewEx.Recycler recycler, RecyclerViewEx.State state) {
        // layout algorithm:
        // 1) by checking children and other variables, find an anchor coordinate and an anchor
        //  item position.
        // 2) fill towards start, stacking from bottom
        // 3) fill towards end, stacking from top
        // 4) scroll to fulfill requirements like stack from bottom.
        // create layout state
        if (DEBUG) {
            Log.d(TAG, "is pre layout:" + state.isPreLayout());
        }
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
            mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
        }


        ensureLayoutState();
        mLayoutState.mRecycle = false;

        mAnchorInfo.reset();
        // calculate anchor position and coordinate
        updateAnchorInfoForLayout(state, mAnchorInfo);
        if (DEBUG) {
            Log.d(TAG, "Anchor info:" + mAnchorInfo);
        }

        // LLM may decide to layout items for "extra" pixels to account for scrolling target,
        // caching or predictive animations.
        int extraForStart;
        int extraForEnd;
        final int extra = getExtraLayoutSpace(state);
        boolean before = state.getTargetScrollPosition() < mAnchorInfo.mPosition;
        if (before == false) {
            extraForEnd = extra;
            extraForStart = 0;
        } else {
            extraForStart = extra;
            extraForEnd = 0;
        }
        extraForStart += mOrientationHelper.getStartAfterPadding();
        extraForEnd += mOrientationHelper.getEndPadding();

        int startOffset;
        int endOffset;
        onAnchorReady(state, mAnchorInfo);
        detachAndScrapAttachedViews(recycler);
        mLayoutState.mIsPreLayout = state.isPreLayout();
        // fill towards end
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtra = extraForEnd;
        if (mPendingScrollPosition != NO_POSITION) {
            mLayoutState.mCurrentPosition = mPendingScrollPosition;
        }
        fill(recycler, mLayoutState, state, false);
        endOffset = mLayoutState.mOffset;
        if (mLayoutState.mAvailable > 0) {
            extraForStart += mLayoutState.mAvailable;
        }
        // fill towards start
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtra = extraForStart;
        if (mPendingScrollPosition != NO_POSITION) {
            mLayoutState.mCurrentPosition = mPendingScrollPosition + mLayoutState.mItemDirection;
        } else {
            mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        }
        fill(recycler, mLayoutState, state, false);
        startOffset = mLayoutState.mOffset;

        // changes may cause gaps on the UI, try to fix them.
        // TODO we can probably avoid this if neither stackFromEnd/reverseLayout/RTL values have
        // changed
        if (getChildCount() > 0) {
            // because layout from end may be changed by scroll to position
            // we re-calculate it.
            // find which side we should check for gaps.
            int fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
            startOffset += fixOffset;
            endOffset += fixOffset;
            fixOffset = fixLayoutEndGap(endOffset, recycler, state, false);
            startOffset += fixOffset;
            endOffset += fixOffset;
        }
        layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
        if (!state.isPreLayout()) {
            mPendingScrollPosition = NO_POSITION;
            mOrientationHelper.onLayoutComplete();
        }
//        mPendingSavedState = null; // we don't need this anymore
        if (DEBUG) {
            validateChildOrder();
        }
    }

    /**
     * Method called when Anchor position is decided. Extending class can setup accordingly or
     * even update anchor info if necessary.
     *
     * @param state
     * @param anchorInfo Simple data structure to keep anchor point information for the next layout
     */
    void onAnchorReady(RecyclerViewEx.State state, AnchorInfo anchorInfo) {
    }

    /**
     * If necessary, layouts new items for predictive animations
     */
    private void layoutForPredictiveAnimations(RecyclerViewEx.Recycler recycler,
                                               RecyclerViewEx.State state, int startOffset, int endOffset) {
        // If there are scrap children that we did not layout, we need to find where they did go
        // and layout them accordingly so that animations can work as expected.
        // This case may happen if new views are added or an existing view expands and pushes
        // another view out of bounds.
        if (!state.willRunPredictiveAnimations() || getChildCount() == 0 || state.isPreLayout()
                || !supportsPredictiveItemAnimations()) {
            return;
        }

        // to make the logic simpler, we calculate the size of children and call fill.
        int scrapExtraStart = 0, scrapExtraEnd = 0;
        final List<RecyclerViewEx.ViewHolder> scrapList = recycler.getScrapList();
        final int scrapSize = scrapList.size();
        final int firstChildPos = getPosition(getChildAt(0));
        for (int i = 0; i < scrapSize; i++) {
            RecyclerViewEx.ViewHolder scrap = scrapList.get(i);
            final int position = scrap.getPosition();
            final int direction = position >= firstChildPos
                    ? LayoutState.LAYOUT_START : LayoutState.LAYOUT_END;
            if (direction == LayoutState.LAYOUT_START) {
                scrapExtraStart += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
            } else {
                scrapExtraEnd += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
            }
        }

        if (DEBUG) {
            Log.d(TAG, "for unused scrap, decided to add " + scrapExtraStart
                    + " towards start and " + scrapExtraEnd + " towards end");
        }
        mLayoutState.mScrapList = scrapList;
        if (scrapExtraStart > 0) {
            View anchor = getChildClosestToStart();
            updateLayoutStateToFillStart(getPosition(anchor), startOffset);
            mLayoutState.mExtra = scrapExtraStart;
            mLayoutState.mAvailable = 0;
            mLayoutState.mCurrentPosition += -1;
            fill(recycler, mLayoutState, state, false);
        }

        if (scrapExtraEnd > 0) {
            View anchor = getChildClosestToEnd();
            updateLayoutStateToFillEnd(getPosition(anchor), endOffset);
            mLayoutState.mExtra = scrapExtraEnd;
            mLayoutState.mAvailable = 0;
            mLayoutState.mCurrentPosition += 1;
            fill(recycler, mLayoutState, state, false);
        }
        mLayoutState.mScrapList = null;
    }

    private void updateAnchorInfoForLayout(RecyclerViewEx.State state, AnchorInfo anchorInfo) {
        if (updateAnchorFromPendingData(state, anchorInfo)) {
            if (DEBUG) {
                Log.d(TAG, "updated anchor info from pending information");
            }
            return;
        }

        if (updateAnchorFromChildren(state, anchorInfo)) {
            if (DEBUG) {
                Log.d(TAG, "updated anchor info from existing children");
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "deciding anchor info for fresh state");
        }
        anchorInfo.assignCoordinateFromPadding();
        anchorInfo.mPosition = 0;
    }

    /**
     * Finds an anchor child from existing Views. Most of the time, this is the view closest to
     * start or end that has a valid position (e.g. not removed).
     * <p/>
     * If a child has focus, it is given priority.
     */
    private boolean updateAnchorFromChildren(RecyclerViewEx.State state, AnchorInfo anchorInfo) {
        if (getChildCount() == 0) {
            return false;
        }
        View focused = getFocusedChild();
        if (focused != null && anchorInfo.assignFromViewIfValid(focused, state)) {
            if (DEBUG) {
                Log.d(TAG, "decided anchor child from focused view");
            }
            return true;
        }

        View referenceChild = findReferenceChildClosestToCenter(state);
        if (referenceChild != null) {
            anchorInfo.assignFromView(referenceChild);
            // If all visible views are removed in 1 pass, reference child might be out of bounds.
            // If that is the case, offset it back to 0 so that we use these pre-layout children.
            if (!state.isPreLayout() && supportsPredictiveItemAnimations()) {
                // validate this child is at least partially visible. if not, offset it to start
                final boolean notVisible =
                        mOrientationHelper.getDecoratedStart(referenceChild) >= mOrientationHelper
                                .getEndAfterPadding()
                                || mOrientationHelper.getDecoratedEnd(referenceChild)
                                < mOrientationHelper.getStartAfterPadding();
                if (notVisible) {
                    anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * If there is a pending scroll position or saved states, updates the anchor info from that
     * data and returns true
     */
    private boolean updateAnchorFromPendingData(RecyclerViewEx.State state, AnchorInfo anchorInfo) {
        if (state.isPreLayout() || mPendingScrollPosition == NO_POSITION) {
            return false;
        }
        // validate scroll position
        if (mPendingScrollPosition < 0 || mPendingScrollPosition >= state.getItemCount()) {
            mPendingScrollPosition = NO_POSITION;
            if (DEBUG) {
                Log.e(TAG, "ignoring invalid scroll position " + mPendingScrollPosition);
            }
            return false;
        }

        // if child is visible, try to make it a reference child and ensure it is fully visible.
        // if child is not visible, align it depending on its virtual position.
        anchorInfo.mPosition = mPendingScrollPosition;
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
            // Anchor offset depends on how that child was laid out. Here, we update it
            // according to our current view bounds
            // rvp: this makes childeren layout in right position at first time
            anchorInfo.mCoordinate = -mRecyclerView.getWidth() + mRecyclerView.getPaddingRight() + mRecyclerView.getPaddingLeft() + mOrientationHelper.getStartAfterPadding();
            return true;
        }

        View child = findViewByPosition(mPendingScrollPosition);
        if (child != null) {
            final int childSize = mOrientationHelper.getDecoratedMeasurement(child);
            if (childSize > mOrientationHelper.getTotalSpace()) {
                // item does not fit. fix depending on layout direction
                anchorInfo.assignCoordinateFromPadding();
                return true;
            }
            final int startGap = mOrientationHelper.getDecoratedStart(child)
                    - mOrientationHelper.getStartAfterPadding();
            if (startGap < 0) {
                anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding();
                return true;
            }
            final int endGap = mOrientationHelper.getEndAfterPadding() -
                    mOrientationHelper.getDecoratedEnd(child);
            if (endGap < 0) {
                anchorInfo.mCoordinate = mOrientationHelper.getEndAfterPadding();
                return true;
            }
            anchorInfo.mCoordinate = mOrientationHelper.getDecoratedStart(child);
        } else { // item is not visible.
            if (getChildCount() > 0) {
                // get position of any child, does not matter
                int pos = getPosition(getChildAt(0));
            }
            anchorInfo.assignCoordinateFromPadding();
        }
        return true;
    }

    /**
     * @return The final offset amount for children
     */
    private int fixLayoutEndGap(int endOffset, RecyclerViewEx.Recycler recycler,
                                RecyclerViewEx.State state, boolean canOffsetChildren) {
        int gap = mOrientationHelper.getEndAfterPadding() - endOffset;
        int fixOffset = 0;
        if (gap > 0) {
            fixOffset = -scrollBy(-gap, recycler, state);
        } else {
            return 0; // nothing to fix
        }
        // move offset according to scroll amount
        endOffset += fixOffset;
        if (canOffsetChildren) {
            // re-calculate gap, see if we could fix it
            gap = mOrientationHelper.getEndAfterPadding() - endOffset;
            if (gap > 0) {
                mOrientationHelper.offsetChildren(gap);
                return gap + fixOffset;
            }
        }
        return fixOffset;
    }

    /**
     * @return The final offset amount for children
     */
    private int fixLayoutStartGap(int startOffset, RecyclerViewEx.Recycler recycler,
                                  RecyclerViewEx.State state, boolean canOffsetChildren) {
        int gap = startOffset - mOrientationHelper.getStartAfterPadding();
        int fixOffset = 0;
        if (gap > 0) {
            // check if we should fix this gap.
            fixOffset = -scrollBy(gap, recycler, state);
        } else {
            return 0; // nothing to fix
        }
        startOffset += fixOffset;
        if (canOffsetChildren) {
            // re-calculate gap, see if we could fix it
            gap = startOffset - mOrientationHelper.getStartAfterPadding();
            if (gap > 0) {
                mOrientationHelper.offsetChildren(-gap);
                return fixOffset - gap;
            }
        }
        return fixOffset;
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCOLLING_OFFSET_NaN;
    }

    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo) {
        updateLayoutStateToFillStart(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillStart(int itemPosition, int offset) {
        mLayoutState.mAvailable = offset - mOrientationHelper.getStartAfterPadding();
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_HEAD;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_START;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCOLLING_OFFSET_NaN;

    }

    protected boolean isLayoutRTL() {
        return getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    void ensureLayoutState() {
        if (mLayoutState == null) {
            mLayoutState = new LayoutState();
        }
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelperEx.createOrientationHelper(this, HORIZONTAL);
        }
    }

    /**
     * <p>Scroll the RecyclerViewEx to make the position visible.</p>
     * <p/>
     * <p>RecyclerViewEx will scroll the minimum amount that is necessary to make the
     * target position visible. If you are looking for a similar behavior to
     * {@link android.widget.ListView#setSelection(int)} or
     * {@link android.widget.ListView#setSelectionFromTop(int, int)}, use
     * <p/>
     * <p>Note that scroll position change will not be reflected until the next layout call.</p>
     *
     * @param position Scroll to this adapter position
     */
    @Override
    public void scrollToPosition(int position) {
        mPendingScrollPosition = position;
        if (mPendingSavedState != null) {
            mPendingSavedState.invalidateAnchor();
        }
        requestLayout();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerViewEx.Recycler recycler,
                                    RecyclerViewEx.State state) {
        return scrollBy(dx, recycler, state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerViewEx.Recycler recycler,
                                  RecyclerViewEx.State state) {
        return 0;
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerViewEx.State state) {
        return computeScrollOffset(state);
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerViewEx.State state) {
        return computeScrollOffset(state);
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerViewEx.State state) {
        return computeScrollExtent(state);
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerViewEx.State state) {
        return computeScrollExtent(state);
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerViewEx.State state) {
        return computeScrollRange(state);
    }

    @Override
    public int computeVerticalScrollRange(RecyclerViewEx.State state) {
        return computeScrollRange(state);
    }

    private int computeScrollOffset(RecyclerViewEx.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelperEx.computeScrollOffset(state, mOrientationHelper,
                getChildClosestToStart(), getChildClosestToEnd(), this,
                mSmoothScrollbarEnabled, false);
    }

    private int computeScrollExtent(RecyclerViewEx.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelperEx.computeScrollExtent(state, mOrientationHelper,
                getChildClosestToStart(), getChildClosestToEnd(), this,
                mSmoothScrollbarEnabled);
    }

    private int computeScrollRange(RecyclerViewEx.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelperEx.computeScrollRange(state, mOrientationHelper,
                getChildClosestToStart(), getChildClosestToEnd(), this,
                mSmoothScrollbarEnabled);
    }

    /**
     * When smooth scrollbar is enabled, the position and size of the scrollbar thumb is computed
     * based on the number of visible pixels in the visible items. This however assumes that all
     * list items have similar or equal widths or heights (depending on list orientation).
     * If you use a list in which items have different dimensions, the scrollbar will change
     * appearance as the user scrolls through the list. To avoid this issue,  you need to disable
     * this property.
     * <p/>
     * When smooth scrollbar is disabled, the position and size of the scrollbar thumb is based
     * solely on the number of items in the adapter and the position of the visible items inside
     * the adapter. This provides a stable scrollbar as the user navigates through a list of items
     * with varying widths / heights.
     *
     * @param enabled Whether or not to enable smooth scrollbar.
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    public void setSmoothScrollbarEnabled(boolean enabled) {
        mSmoothScrollbarEnabled = enabled;
    }

    /**
     * Returns the current state of the smooth scrollbar feature. It is enabled by default.
     *
     * @return True if smooth scrollbar is enabled, false otherwise.
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    public boolean isSmoothScrollbarEnabled() {
        return mSmoothScrollbarEnabled;
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace,
                                   boolean canUseExistingSpace, RecyclerViewEx.State state) {
        mLayoutState.mExtra = getExtraLayoutSpace(state);
        mLayoutState.mLayoutDirection = layoutDirection;
        int fastScrollSpace;
        if (layoutDirection == LayoutState.LAYOUT_END) {
            mLayoutState.mExtra += mOrientationHelper.getEndPadding();
            // get the first child in the direction we are going
            final View child = getChildClosestToEnd();
            // the direction in which we are traversing children
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child);
            // calculate how much we can scroll without adding new children (independent of layout)
            fastScrollSpace = mOrientationHelper.getDecoratedEnd(child)
                    - mOrientationHelper.getEndAfterPadding();

        } else {
            final View child = getChildClosestToStart();
            mLayoutState.mExtra += mOrientationHelper.getStartAfterPadding();
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_HEAD;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            mLayoutState.mOffset = mOrientationHelper.getDecoratedStart(child);
            fastScrollSpace = -mOrientationHelper.getDecoratedStart(child)
                    + mOrientationHelper.getStartAfterPadding();
        }
        mLayoutState.mAvailable = requiredSpace;
        if (canUseExistingSpace) {
            mLayoutState.mAvailable -= fastScrollSpace;
        }
        mLayoutState.mScrollingOffset = fastScrollSpace;
    }

    int scrollBy(int dy, RecyclerViewEx.Recycler recycler, RecyclerViewEx.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        mLayoutState.mRecycle = true;
        ensureLayoutState();
        final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDy = Math.abs(dy);
        updateLayoutState(layoutDirection, absDy, true, state);
        final int freeScroll = mLayoutState.mScrollingOffset;
        final int consumed = freeScroll + fill(recycler, mLayoutState, state, false);
        if (consumed < 0) {
            if (DEBUG) {
                Log.d(TAG, "Don't have any more elements to scroll");
            }
            return 0;
        }
        final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
        mOrientationHelper.offsetChildren(-scrolled);
        if (DEBUG) {
            Log.d(TAG, "scroll req: " + dy + " scrolled: " + scrolled);
        }
        return scrolled;
    }

    @Override
    public void assertNotInLayoutOrScroll(String message) {
        if (mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }
    }

    /**
     * Recycles children between given indices.
     *
     * @param startIndex inclusive
     * @param endIndex   exclusive
     */
    private void recycleChildren(RecyclerViewEx.Recycler recycler, int startIndex, int endIndex) {
        if (startIndex == endIndex) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "Recycling " + Math.abs(startIndex - endIndex) + " items");
        }
        if (endIndex > startIndex) {
            for (int i = endIndex - 1; i >= startIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        } else {
            for (int i = startIndex; i > endIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        }
    }

    /**
     * Recycles views that went out of bounds after scrolling towards the end of the layout.
     *
     * @param recycler Recycler instance of {@link android.support.v7.widget.RecyclerViewEx}
     * @param dt       This can be used to add additional padding to the visible area. This is used
     *                 to
     *                 detect children that will go out of bounds after scrolling, without actually
     *                 moving them.
     */
    private void recycleViewsFromStart(RecyclerViewEx.Recycler recycler, int dt) {
        if (dt < 0) {
            if (DEBUG) {
                Log.d(TAG, "Called recycle from start with a negative value. This might happen"
                        + " during layout changes but may be sign of a bug");
            }
            return;
        }
        // ignore padding, ViewGroup may not clip children.
        final int limit = dt;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedEnd(child) > limit) {// stop here
                recycleChildren(recycler, 0, i);
                return;
            }
        }
    }


    /**
     * Recycles views that went out of bounds after scrolling towards the start of the layout.
     *
     * @param recycler Recycler instance of {@link android.support.v7.widget.RecyclerViewEx}
     * @param dt       This can be used to add additional padding to the visible area. This is used
     *                 to detect children that will go out of bounds after scrolling, without
     *                 actually moving them.
     */
    private void recycleViewsFromEnd(RecyclerViewEx.Recycler recycler, int dt) {
        final int childCount = getChildCount();
        if (dt < 0) {
            if (DEBUG) {
                Log.d(TAG, "Called recycle from end with a negative value. This might happen"
                        + " during layout changes but may be sign of a bug");
            }
            return;
        }
        final int limit = mOrientationHelper.getEnd() - dt;
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedStart(child) < limit) {// stop here
                recycleChildren(recycler, childCount - 1, i);
                return;
            }
        }

    }

    /**
     * Helper method to call appropriate recycle method depending on current layout direction
     *
     * @param recycler    Current recycler that is attached to RecyclerViewEx
     * @param layoutState Current layout state. Right now, this object does not change but
     *                    we may consider moving it out of this view so passing around as a
     *                    parameter for now, rather than accessing {@link #mLayoutState}
     * @see #recycleViewsFromStart(android.support.v7.widget.RecyclerViewEx.Recycler, int)
     * @see #recycleViewsFromEnd(android.support.v7.widget.RecyclerViewEx.Recycler, int)
     * @see LinearLayoutManagerEx.LayoutState#mLayoutDirection
     */
    private void recycleByLayoutState(RecyclerViewEx.Recycler recycler, LayoutState layoutState) {
        if (!layoutState.mRecycle) {
            return;
        }
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            recycleViewsFromEnd(recycler, layoutState.mScrollingOffset);
        } else {
            recycleViewsFromStart(recycler, layoutState.mScrollingOffset);
        }
    }

    /**
     * The magic functions :). Fills the given layout, defined by the layoutState. This is fairly
     * independent from the rest of the {@link LinearLayoutManagerEx}
     * and with little change, can be made publicly available as a helper class.
     *
     * @param recycler        Current recycler that is attached to RecyclerViewEx
     * @param layoutState     Configuration on how we should fill out the available space.
     * @param state           Context passed by the RecyclerViewEx to control scroll steps.
     * @param stopOnFocusable If true, filling stops in the first focusable new child
     * @return Number of pixels that it added. Useful for scoll functions.
     */
    int fill(RecyclerViewEx.Recycler recycler, LayoutState layoutState,
             RecyclerViewEx.State state, boolean stopOnFocusable) {
        // max offset we should set is mFastScroll + available
        final int start = layoutState.mAvailable;
        if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {
            // TODO ugly bug fix. should not happen
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        int remainingSpace = layoutState.mAvailable + layoutState.mExtra;
        LayoutChunkResult layoutChunkResult = new LayoutChunkResult();
        while (remainingSpace > 0 && layoutState.hasMore(state)) {
            layoutChunkResult.resetInternal();
            layoutChunk(recycler, state, layoutState, layoutChunkResult);
            if (layoutChunkResult.mFinished) {
                break;
            }
            layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
            /**
             * Consume the available space if:
             * * layoutChunk did not request to be ignored
             * * OR we are laying out scrap children
             * * OR we are not doing pre-layout
             */
            if (!layoutChunkResult.mIgnoreConsumed || mLayoutState.mScrapList != null
                    || !state.isPreLayout()) {
                layoutState.mAvailable -= layoutChunkResult.mConsumed;
                // we keep a separate remaining space because mAvailable is important for recycling
                remainingSpace -= layoutChunkResult.mConsumed;
            }

            if (layoutState.mScrollingOffset != LayoutState.SCOLLING_OFFSET_NaN) {
                layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
                if (layoutState.mAvailable < 0) {
                    layoutState.mScrollingOffset += layoutState.mAvailable;
                }
                recycleByLayoutState(recycler, layoutState);
            }
            if (stopOnFocusable && layoutChunkResult.mFocusable) {
                break;
            }
        }
        if (DEBUG) {
            validateChildOrder();
        }
        return start - layoutState.mAvailable;
    }

    void layoutChunk(RecyclerViewEx.Recycler recycler, RecyclerViewEx.State state,
                     LayoutState layoutState, LayoutChunkResult result) {
        View view = layoutState.next(recycler);
        if (view == null) {
            if (DEBUG && layoutState.mScrapList == null) {
                throw new RuntimeException("received null view when unexpected");
            }
            // if we are laying out views in scrap, this may return null which means there is
            // no more items to layout.
            result.mFinished = true;
            return;
        }
        RecyclerViewEx.LayoutParams params = (RecyclerViewEx.LayoutParams) view.getLayoutParams();
        if (layoutState.mScrapList == null) {
            if ((layoutState.mLayoutDirection
                    != LayoutState.LAYOUT_START)) {
                addView(view);
            } else {
                addView(view, 0);
            }
        } else {
            if (layoutState.mLayoutDirection
                    != LayoutState.LAYOUT_START) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        measureChildWithMargins(view, 0, 0);
        result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
        int left, top, right, bottom;
        top = getPaddingTop();
        bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view);

        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            right = layoutState.mOffset;
            left = layoutState.mOffset - result.mConsumed;
        } else {
            left = layoutState.mOffset;
            right = layoutState.mOffset + result.mConsumed;
        }
        // We calculate everything with View's bounding box (which includes decor and margins)
        // To calculate correct layout position, we subtract margins.
        layoutDecorated(view, left + params.leftMargin, top + params.topMargin,
                right - params.rightMargin, bottom - params.bottomMargin);
        if (DEBUG) {
            Log.d(TAG, "laid out child at position " + getPosition(view) + ", with l:"
                    + (left + params.leftMargin) + ", t:" + (top + params.topMargin) + ", r:"
                    + (right - params.rightMargin) + ", b:" + (bottom - params.bottomMargin));
        }
        // Consume the available space if the view is not removed OR changed
        if (params.isItemRemoved() || params.isItemChanged()) {
            result.mIgnoreConsumed = true;
        }
        result.mFocusable = view.isFocusable();
    }

    /**
     * Converts a focusDirection to orientation.
     *
     * @param focusDirection One of {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN},
     *                       {@link View#FOCUS_LEFT}, {@link View#FOCUS_RIGHT},
     *                       {@link View#FOCUS_BACKWARD}, {@link View#FOCUS_FORWARD}
     *                       or 0 for not applicable
     * @return {@link LayoutState#LAYOUT_START} or {@link LayoutState#LAYOUT_END} if focus direction
     * is applicable to current state, {@link LayoutState#INVALID_LAYOUT} otherwise.
     */
    private int convertFocusDirectionToLayoutDirection(int focusDirection) {
        switch (focusDirection) {
            case View.FOCUS_BACKWARD:
                return LayoutState.LAYOUT_START;
            case View.FOCUS_FORWARD:
                return LayoutState.LAYOUT_END;
            case View.FOCUS_UP:
                return LayoutState.INVALID_LAYOUT;
            case View.FOCUS_DOWN:
                return LayoutState.INVALID_LAYOUT;
            case View.FOCUS_LEFT:
                return LayoutState.LAYOUT_START;
            case View.FOCUS_RIGHT:
                return LayoutState.LAYOUT_END;
            default:
                if (DEBUG) {
                    Log.d(TAG, "Unknown focus request:" + focusDirection);
                }
                return LayoutState.INVALID_LAYOUT;
        }

    }

    /**
     * Convenience method to find the child closes to start. Caller should check it has enough
     * children.
     *
     * @return The child closes to start of the layout from user's perspective.
     */
    private View getChildClosestToStart() {
        return getChildAt(0);
    }

    /**
     * Convenience method to find the child closes to end. Caller should check it has enough
     * children.
     *
     * @return The child closes to end of the layout from user's perspective.
     */
    private View getChildClosestToEnd() {
        return getChildAt(getChildCount() - 1);
    }

    /**
     * Among the children that are suitable to be considered as an anchor child, returns the one
     * closest to the start of the layout.
     * <p/>
     * Due to ambiguous adapter updates or children being removed, some children's positions may be
     * invalid. This method is a best effort to find a position within adapter bounds if possible.
     * <p/>
     * It also prioritizes children that are within the visible bounds.
     *
     * @return A View that can be used an an anchor View.
     */
    private View findReferenceChildClosestToCenter(RecyclerViewEx.State state) {
        int centerXChildPosition = mRecyclerView.getCenterXChildPosition();
        if (centerXChildPosition != NO_POSITION) {
            return getChildAt(centerXChildPosition);
        }
        return null;
    }

    private View findLastReferenceChild(int itemCount) {
        return findReferenceChild(getChildCount() - 1, -1, itemCount);
    }

    private View findReferenceChild(int start, int end, int itemCount) {
        ensureLayoutState();
        View invalidMatch = null;
        View outOfBoundsMatch = null;
        final int boundsStart = mOrientationHelper.getStartAfterPadding();
        final int boundsEnd = mOrientationHelper.getEndAfterPadding();
        final int diff = end > start ? 1 : -1;
        for (int i = start; i != end; i += diff) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                if (((RecyclerViewEx.LayoutParams) view.getLayoutParams()).isItemRemoved()) {
                    if (invalidMatch == null) {
                        invalidMatch = view; // removed item, least preferred
                    }
                } else if (mOrientationHelper.getDecoratedStart(view) >= boundsEnd ||
                        mOrientationHelper.getDecoratedEnd(view) < boundsStart) {
                    if (outOfBoundsMatch == null) {
                        outOfBoundsMatch = view; // item is not visible, less preferred
                    }
                } else {
                    return view;
                }
            }
        }
        return outOfBoundsMatch != null ? outOfBoundsMatch : invalidMatch;
    }

    /**
     * Returns the adapter position of the first visible view.
     * <p/>
     * Note that, this value is not affected by layout orientation or item order traversal.
     * ({@link #(boolean)}). Views are sorted by their positions in the adapter,
     * not in the layout.
     * <p/>
     * If RecyclerViewEx has item decorators, they will be considered in calculations as well.
     * <p/>
     * LayoutManager may pre-cache some views that are not necessarily visible. Those views
     * are ignored in this method.
     *
     * @return The adapter position of the first visible item or {@link RecyclerViewEx#NO_POSITION} if
     * there aren't any visible items.
     * @see #findFirstCompletelyVisibleItemPosition()
     * @see #findLastVisibleItemPosition()
     */
    public int findFirstVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), false);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the first fully visible view.
     * <p/>
     * Note that bounds check is only performed in the current orientation. That means, if
     * LayoutManager is horizontal, it will only check the view's left and right edges.
     *
     * @return The adapter position of the first fully visible item or
     * {@link RecyclerViewEx#NO_POSITION} if there aren't any visible items.
     * @see #findFirstVisibleItemPosition()
     * @see #findLastCompletelyVisibleItemPosition()
     */
    public int findFirstCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), true);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the last visible view.
     * <p/>
     * Note that, this value is not affected by layout orientation or item order traversal.
     * ({@link #(boolean)}). Views are sorted by their positions in the adapter,
     * not in the layout.
     * <p/>
     * If RecyclerViewEx has item decorators, they will be considered in calculations as well.
     * <p/>
     * LayoutManager may pre-cache some views that are not necessarily visible. Those views
     * are ignored in this method.
     *
     * @return The adapter position of the last visible view or {@link RecyclerViewEx#NO_POSITION} if
     * there aren't any visible items.
     * @see #findLastCompletelyVisibleItemPosition()
     * @see #findFirstVisibleItemPosition()
     */
    public int findLastVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, false);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the last fully visible view.
     * <p/>
     * Note that bounds check is only performed in the current orientation. That means, if
     * LayoutManager is horizontal, it will only check the view's left and right edges.
     *
     * @return The adapter position of the last fully visible view or
     * {@link RecyclerViewEx#NO_POSITION} if there aren't any visible items.
     * @see #findLastVisibleItemPosition()
     * @see #findFirstCompletelyVisibleItemPosition()
     */
    public int findLastCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, true);
        return child == null ? NO_POSITION : getPosition(child);
    }

    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible) {
        ensureLayoutState();
        final int start = mOrientationHelper.getStartAfterPadding();
        final int end = mOrientationHelper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = getChildAt(i);
            final int childStart = mOrientationHelper.getDecoratedStart(child);
            final int childEnd = mOrientationHelper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    }
                } else {
                    return child;
                }
            }
        }
        return null;
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection,
                                    RecyclerViewEx.Recycler recycler, RecyclerViewEx.State state) {
        if (getChildCount() == 0) {
            return null;
        }

        final int layoutDir = convertFocusDirectionToLayoutDirection(focusDirection);
        if (layoutDir == LayoutState.INVALID_LAYOUT) {
            return null;
        }
        ensureLayoutState();
        final View referenceChild;
        referenceChild = findReferenceChildClosestToCenter(state);
        if (referenceChild == null) {
            if (DEBUG) {
                Log.d(TAG,
                        "Cannot find a child with a valid position to be used for focus search.");
            }
            return null;
        }
        ensureLayoutState();
        final int maxScroll = (int) (MAX_SCROLL_FACTOR * mOrientationHelper.getTotalSpace());
        updateLayoutState(layoutDir, maxScroll, false, state);
        mLayoutState.mScrollingOffset = LayoutState.SCOLLING_OFFSET_NaN;
        mLayoutState.mRecycle = false;
        fill(recycler, mLayoutState, state, true);
        final View nextFocus;
        if (layoutDir == LayoutState.LAYOUT_START) {
            nextFocus = getChildClosestToStart();
        } else {
            nextFocus = getChildClosestToEnd();
        }
        if (nextFocus == referenceChild || !nextFocus.isFocusable()) {
            return null;
        }
        return nextFocus;
    }

    /**
     * Used for debugging.
     * Logs the internal representation of children to default logger.
     */
    private void logChildren() {
        if (DEBUG) {
            Log.d(TAG, "internal representation of views on the screen");
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (DEBUG) {
                Log.d(TAG, "item " + getPosition(child) + ", coord:"
                        + mOrientationHelper.getDecoratedStart(child));
            }
        }
        if (DEBUG) {
            Log.d(TAG, "==============");
        }
    }

    /**
     * Used for debugging.
     * Validates that child views are laid out in correct order. This is important because rest of
     * the algorithm relies on this constraint.
     * <p/>
     * In default layout, child 0 should be closest to screen position 0 and last child should be
     * closest to position WIDTH or HEIGHT.
     * In reverse layout, last child should be closes to screen position 0 and first child should
     * be closest to position WIDTH  or HEIGHT
     */
    void validateChildOrder() {
        if (DEBUG) {
            Log.d(TAG, "validating child count " + getChildCount());
        }
        if (getChildCount() < 1) {
            return;
        }
        int lastPos = getPosition(getChildAt(0));
        int lastScreenLoc = mOrientationHelper.getDecoratedStart(getChildAt(0));
        for (int i = 1; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int pos = getPosition(child);
            int screenLoc = mOrientationHelper.getDecoratedStart(child);
            if (pos < lastPos) {
                logChildren();
                throw new RuntimeException("detected invalid position. loc invalid? " +
                        (screenLoc < lastScreenLoc));
            }
            if (screenLoc < lastScreenLoc) {
                logChildren();
                throw new RuntimeException("detected invalid location");
            }
        }
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return mPendingSavedState == null;
    }

    /**
     * Helper class that keeps temporary state while {LayoutManager} is filling out the empty
     * space.
     */
    static class LayoutState {

        final static String TAG = "HCLM#LayoutState";

        final static int LAYOUT_START = -1;

        final static int LAYOUT_END = 1;

        final static int INVALID_LAYOUT = Integer.MIN_VALUE;

        final static int ITEM_DIRECTION_HEAD = -1;

        final static int ITEM_DIRECTION_TAIL = 1;

        final static int SCOLLING_OFFSET_NaN = Integer.MIN_VALUE;

        /**
         * We may not want to recycle children in some cases (e.g. layout)
         */
        boolean mRecycle = true;

        /**
         * Pixel offset where layout should start
         */
        int mOffset;

        /**
         * Number of pixels that we should fill, in the layout direction.
         */
        int mAvailable;

        /**
         * Current position on the adapter to get the next item.
         */
        int mCurrentPosition;

        /**
         * Defines the direction in which the data adapter is traversed.
         * Should be {@link #ITEM_DIRECTION_HEAD} or {@link #ITEM_DIRECTION_TAIL}
         */
        int mItemDirection;

        /**
         * Defines the direction in which the layout is filled.
         * Should be {@link #LAYOUT_START} or {@link #LAYOUT_END}
         */
        int mLayoutDirection;

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        int mScrollingOffset;

        /**
         * Used if you want to pre-layout items that are not yet visible.
         * The difference with {@link #mAvailable} is that, when recycling, distance laid out for
         * {@link #mExtra} is not considered to avoid recycling visible children.
         */
        int mExtra = 0;

        /**
         * Equal to {@link RecyclerViewEx.State#isPreLayout()}. When consuming scrap, if this value
         * is set to true, we skip removed views since they should not be laid out in post layout
         * step.
         */
        boolean mIsPreLayout = false;

        /**
         * When LLM needs to layout particular views, it sets this list in which case, LayoutState
         * will only return views from this list and return null if it cannot find an item.
         */
        List<RecyclerViewEx.ViewHolder> mScrapList = null;

        /**
         * @return true if there are more items in the data adapter
         */
        boolean hasMore(RecyclerViewEx.State state) {
            return mCurrentPosition >= 0 && mCurrentPosition < state.getItemCount();
        }

        /**
         * Gets the view for the next element that we should layout.
         * Also updates current item index to the next item, based on {@link #mItemDirection}
         *
         * @return The next element that we should layout.
         */
        View next(RecyclerViewEx.Recycler recycler) {
            if (mScrapList != null) {
                return nextFromLimitedList();
            }
            final View view = recycler.getViewForPosition(mCurrentPosition);
            mCurrentPosition += mItemDirection;
            return view;
        }

        /**
         * Returns next item from limited list.
         * <p/>
         * Upon finding a valid VH, sets current item position to VH.itemPosition + mItemDirection
         *
         * @return View if an item in the current position or direction exists if not null.
         */
        private View nextFromLimitedList() {
            int size = mScrapList.size();
            RecyclerViewEx.ViewHolder closest = null;
            int closestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                RecyclerViewEx.ViewHolder viewHolder = mScrapList.get(i);
                if (!mIsPreLayout && viewHolder.isRemoved()) {
                    continue;
                }
                final int distance = (viewHolder.getPosition() - mCurrentPosition) * mItemDirection;
                if (distance < 0) {
                    continue; // item is not in current direction
                }
                if (distance < closestDistance) {
                    closest = viewHolder;
                    closestDistance = distance;
                    if (distance == 0) {
                        break;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "layout from scrap. found view:?" + (closest != null));
            }
            if (closest != null) {
                mCurrentPosition = closest.getPosition() + mItemDirection;
                return closest.itemView;
            }
            return null;
        }

        void log() {
            if (DEBUG) {
                Log.d(TAG, "avail:" + mAvailable + ", ind:" + mCurrentPosition + ", dir:" +
                        mItemDirection + ", offset:" + mOffset + ", layoutDir:" + mLayoutDirection);
            }
        }
    }

    static class SavedState implements Parcelable {

        int mAnchorPosition;

//        int mAnchorOffset;

        boolean mAnchorLayoutFromEnd;

        public SavedState() {

        }

        SavedState(Parcel in) {
            mAnchorPosition = in.readInt();
//            mAnchorOffset = in.readInt();
            mAnchorLayoutFromEnd = in.readInt() == 1;
        }

        public SavedState(SavedState other) {
            mAnchorPosition = other.mAnchorPosition;
//            mAnchorOffset = other.mAnchorOffset;
            mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
        }

        boolean hasValidAnchor() {
            return mAnchorPosition >= 0;
        }

        void invalidateAnchor() {
            mAnchorPosition = NO_POSITION;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mAnchorPosition);
//            dest.writeInt(mAnchorOffset);
            dest.writeInt(mAnchorLayoutFromEnd ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * Simple data class to keep Anchor information
     */
    class AnchorInfo {
        int mPosition;
        int mCoordinate;

        void reset() {
            mPosition = NO_POSITION;
            mCoordinate = INVALID_OFFSET;
        }

        /**
         * assigns anchor coordinate from the RecyclerViewEx's padding depending on current
         * layoutFromEnd value
         */
        void assignCoordinateFromPadding() {
            mCoordinate = mOrientationHelper.getStartAfterPadding();
        }

        @Override
        public String toString() {
            return "AnchorInfo{" +
                    "mPosition=" + mPosition +
                    ", mCoordinate=" + mCoordinate +
                    '}';
        }

        /**
         * Assign anchor position information from the provided view if it is valid as a reference
         * child.
         */
        public boolean assignFromViewIfValid(View child, RecyclerViewEx.State state) {
            RecyclerViewEx.LayoutParams lp = (RecyclerViewEx.LayoutParams) child.getLayoutParams();
            if (!lp.isItemRemoved() && lp.getViewPosition() >= 0
                    && lp.getViewPosition() < state.getItemCount()) {
                assignFromView(child);
                return true;
            }
            return false;
        }

        public void assignFromView(View child) {
            mCoordinate = mOrientationHelper.getDecoratedStart(child);
            mPosition = getPosition(child);
        }
    }

    protected static class LayoutChunkResult {
        public int mConsumed;
        public boolean mFinished;
        public boolean mIgnoreConsumed;
        public boolean mFocusable;

        void resetInternal() {
            mConsumed = 0;
            mFinished = false;
            mIgnoreConsumed = false;
            mFocusable = false;
        }
    }
}

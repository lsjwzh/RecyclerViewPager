package com.lsjwzh.widget.recyclerviewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

public class LoopRecyclerViewPager extends RecyclerViewPager {

    public LoopRecyclerViewPager(Context context) {
        this(context, null);
    }

    public LoopRecyclerViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopRecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        super.scrollToPosition(getMiddlePosition());
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews);
        super.scrollToPosition(getMiddlePosition());
    }

    @Override
    @NonNull
    protected RecyclerViewPagerAdapter ensureRecyclerViewPagerAdapter(Adapter adapter) {
        return (adapter instanceof LoopRecyclerViewPagerAdapter)
                ? (LoopRecyclerViewPagerAdapter) adapter
                : new LoopRecyclerViewPagerAdapter(this, adapter);
    }

    /**
     * Starts a smooth scroll to an adapter position.
     * if position < adapter.getActualCount,
     * position will be transform to right position.
     *
     * @param position target position
     */
    @Override
    public void smoothScrollToPosition(int position) {
        int transformedPosition = transformInnerPositionIfNeed(position);
        super.smoothScrollToPosition(transformedPosition);
        Log.e("test", "transformedPosition:" + transformedPosition);
    }

    /**
     * Starts a scroll to an adapter position.
     * if position < adapter.getActualCount,
     * position will be transform to right position.
     *
     * @param position target position
     */
    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(transformInnerPositionIfNeed(position));
    }

    /**
     * get actual current position in actual adapter.
     */
    public int getActualCurrentPosition() {
        int position = getCurrentPosition();
        return transformToActualPosition(position);
    }

    /**
     * Transform adapter position to actual position.
     * @param position adapter position
     * @return actual position
     */
    public int transformToActualPosition(int position) {
        if(getAdapter() == null || getAdapter().getItemCount() < 0) {
            return 0;
        }
        return position % getActualItemCountFromAdapter();
    }

    private int getActualItemCountFromAdapter() {
        return ((LoopRecyclerViewPagerAdapter) getWrapperAdapter()).getActualItemCount();
    }

    private int transformInnerPositionIfNeed(int position) {
        final int actualItemCount = getActualItemCountFromAdapter();
        if (actualItemCount == 0) { return actualItemCount; }
        final int actualCurrentPosition = getCurrentPosition() % actualItemCount;
        final int actualNextPosition = position % actualItemCount;
        // position in current chunk
        int currentChunkPosition =
                getCurrentPosition() - actualCurrentPosition + actualNextPosition;
        // position in left chunk
        int leftChunkPosition =
                getCurrentPosition() - actualCurrentPosition - actualItemCount + actualNextPosition;
        // position in right chunk
        int rightChunkPosition =
                getCurrentPosition() - actualCurrentPosition + actualItemCount + actualNextPosition;
        Log.e("test",
              currentChunkPosition +
              "/" +
              leftChunkPosition +
              "/" +
              rightChunkPosition +
              "/" +
              getCurrentPosition());

        // If given actualItemCount lesser than position, considering scroll called by gesture
        final boolean isGestureScroll = actualItemCount < position;

        int toCurrentChunkPosition = Math.abs(currentChunkPosition - getCurrentPosition());
        int toLeftChunkPosition = Math.abs(leftChunkPosition - getCurrentPosition());
        int toRightChunkPosition = Math.abs(rightChunkPosition - getCurrentPosition());
        if (getCurrentPosition() > position) {
            // Left direction
            // Scroll from current actual position to next
            // actual position with gesture scrolls to next chunk
            if (actualCurrentPosition == actualNextPosition && isGestureScroll) {
                return leftChunkPosition;
            }
            if (toLeftChunkPosition <= toCurrentChunkPosition) {
                return leftChunkPosition;
            }
        } else if(getCurrentPosition() == position) {
            return currentChunkPosition;
        } else {
            // Right direction
            // Scroll from current actual position to next
            // actual position with gesture scrolls to next chunk
            if (actualCurrentPosition == actualNextPosition && isGestureScroll) {
                return rightChunkPosition;
            }
            if (toRightChunkPosition <= toCurrentChunkPosition) {
                return rightChunkPosition;
            }
        }

        return currentChunkPosition;
    }

    private int getMiddlePosition() {
        int middlePosition = Integer.MAX_VALUE / 2;
        final int actualItemCount = getActualItemCountFromAdapter();
        if (actualItemCount > 0 && middlePosition % actualItemCount != 0) {
            middlePosition = middlePosition - middlePosition % actualItemCount;
        }
        return middlePosition;
    }
}

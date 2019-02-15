package android.support.v7.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SinglePagerSnapHelper extends PagerSnapHelper implements SnapScrollerCreator {
    int lastTargetPosition;

    @Nullable
    @Override
    public LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        return super.createSnapScroller(layoutManager);
    }

    public boolean onFling(int velocityX, int velocityY) {
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        } else {
            RecyclerView.Adapter adapter = this.mRecyclerView.getAdapter();
            if (adapter == null) {
                return false;
            } else {
                int minFlingVelocity = this.mRecyclerView.getMinFlingVelocity();
                return (Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity) && this.snapFromFling(layoutManager, velocityX, velocityY);
            }
        }
    }

    private boolean snapFromFling(@NonNull RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return false;
        } else {
            // 避免滑动到边界item时，smoothScroller不触发stop导致的pageChange事件不能触发的bug
            int targetPosition = this.findTargetSnapPosition(layoutManager, velocityX, velocityY);
            if (targetPosition != -1 && lastTargetPosition == targetPosition) {
                return false;
            }
            RecyclerView.SmoothScroller smoothScroller = this.createScroller(layoutManager);
            if (smoothScroller == null) {
                return false;
            } else {
                if (targetPosition == -1) {
                    return false;
                } else {
                    lastTargetPosition = targetPosition;
                    smoothScroller.setTargetPosition(targetPosition);
                    layoutManager.startSmoothScroll(smoothScroller);
                    return true;
                }
            }
        }
    }
}

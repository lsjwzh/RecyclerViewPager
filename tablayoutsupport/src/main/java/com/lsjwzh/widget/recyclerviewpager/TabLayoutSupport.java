package com.lsjwzh.widget.recyclerviewpager;

import java.lang.ref.WeakReference;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class TabLayoutSupport {

    public static void setupWithViewPager(@NonNull TabLayout tabLayout
            , @NonNull RecyclerViewPager viewPager
            , @NonNull ViewPagerTabLayoutAdapter viewPagerTabLayoutAdapter) {
        tabLayout.removeAllTabs();
        int i = 0;

        for (int count = viewPagerTabLayoutAdapter.getItemCount(); i < count; ++i) {
            tabLayout.addTab(tabLayout.newTab().setText(viewPagerTabLayoutAdapter.getPageTitle(i)));
        }
        final TabLayoutOnPageChangeListener listener
                = new TabLayoutOnPageChangeListener(tabLayout, viewPager);
        viewPager.addOnScrollListener(listener);
        viewPager.addOnPageChangedListener(listener);
        tabLayout.setOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager));
    }

    public interface ViewPagerTabLayoutAdapter {
        String getPageTitle(int position);

        int getItemCount();
    }

    public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final RecyclerViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(RecyclerViewPager viewPager) {
            this.mViewPager = viewPager;
        }

        public void onTabSelected(TabLayout.Tab tab) {
            this.mViewPager.smoothScrollToPosition(tab.getPosition());
        }

        public void onTabUnselected(TabLayout.Tab tab) {
        }

        public void onTabReselected(TabLayout.Tab tab) {
        }
    }

    public static class TabLayoutOnPageChangeListener extends RecyclerView.OnScrollListener implements RecyclerViewPager
            .OnPageChangedListener {
        private final WeakReference<TabLayout> mTabLayoutRef;
        private final WeakReference<RecyclerViewPager> mViewPagerRef;
        private int mPositionBeforeScroll;
        private int mPagerLeftBeforeScroll;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout, RecyclerViewPager viewPager) {
            this.mTabLayoutRef = new WeakReference<>(tabLayout);
            this.mViewPagerRef = new WeakReference<>(viewPager);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                mPositionBeforeScroll = -1;
                mPagerLeftBeforeScroll = 0;
            } else if (mPositionBeforeScroll < 0) {
                mPositionBeforeScroll = ((RecyclerViewPager) recyclerView).getCurrentPosition();
                mPagerLeftBeforeScroll = recyclerView.getPaddingLeft();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            TabLayout tabLayout = this.mTabLayoutRef.get();
            final RecyclerViewPager viewPager = (RecyclerViewPager) recyclerView;
            final int pagerWidth = recyclerView.getWidth()
                    - recyclerView.getPaddingLeft()
                    - recyclerView.getPaddingRight();
            final View centerXChild = ViewUtils.getCenterXChild(viewPager);
            if (centerXChild == null) {
                return;
            }
            int centerChildPosition = viewPager.getChildAdapterPosition(centerXChild);
            float offset = mPagerLeftBeforeScroll - centerXChild.getLeft()
                    + pagerWidth * (centerChildPosition - mPositionBeforeScroll);
            final float positionOffset = offset * 1f / pagerWidth;
            if (tabLayout != null) {
                if (positionOffset < 0) {
                    try {
                        tabLayout.setScrollPosition(mPositionBeforeScroll
                                + (int) Math.floor(positionOffset),
                            positionOffset - (int) Math.floor(positionOffset),
                            false);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else {
                    tabLayout.setScrollPosition(mPositionBeforeScroll + (int) (positionOffset),
                            positionOffset - (int) (positionOffset),
                            false);
                }
            }
            Log.d("test", "dx:" + positionOffset);
        }

        @Override
        public void OnPageChanged(int oldPosition, int newPosition) {
            if (mViewPagerRef.get() == null) {
                return;
            }
            if (mViewPagerRef.get() instanceof LoopRecyclerViewPager) {
                newPosition = ((LoopRecyclerViewPager) mViewPagerRef.get())
                        .transformToActualPosition(newPosition);
            }
            TabLayout tabLayout = this.mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getTabAt(newPosition) != null) {
                tabLayout.getTabAt(newPosition).select();
            }
        }
    }
}

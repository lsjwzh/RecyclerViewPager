package com.lsjwzh.widget.recyclerviewpagerdeomo;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.lsjwzh.widget.recyclerviewpager.FragmentStatePagerAdapter;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.lsjwzh.widget.recyclerviewpager.ViewUtils;

public class MaterialDemoActivity extends AppCompatActivity {
    protected RecyclerViewPager mRecyclerView;
    private FragmentsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_material);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initViewPager();
        initTabLayout();

    }

    private void initTabLayout() {
        //给TabLayout增加Tab, 并关联ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText("item" + i));
        }
        final TabLayoutOnPageChangeListener listener = new TabLayoutOnPageChangeListener(tabLayout);
        mRecyclerView.addOnScrollListener(listener);
        mRecyclerView.addOnPageChangedListener(listener);
        tabLayout.setOnTabSelectedListener(new ViewPagerOnTabSelectedListener(mRecyclerView));
    }

    protected void initViewPager() {
        mRecyclerView = (RecyclerViewPager) findViewById(R.id.viewpager);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);
        mAdapter =new FragmentsAdapter(getSupportFragmentManager());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {
                Log.d("test", "oldPosition:" + oldPosition + " newPosition:" + newPosition);
            }
        });

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
        private int mPositionBeforeScroll;
        private int mPagerLeftBeforeScroll;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            this.mTabLayoutRef = new WeakReference<>(tabLayout);
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
            int centerChildPosition = viewPager.getChildAdapterPosition(ViewUtils.getCenterXChild
                    (viewPager));
            float offset = mPagerLeftBeforeScroll - ViewUtils.getCenterXChild(viewPager).getLeft()
                    + pagerWidth * (centerChildPosition - mPositionBeforeScroll);
            final float positionOffset = offset * 1f / pagerWidth;
            if (tabLayout != null) {
                if (positionOffset < 0) {
                    tabLayout.setScrollPosition(mPositionBeforeScroll + (int) (positionOffset - 0.99)
                            , (int) (0.99 - positionOffset) + positionOffset,
                            false);
                } else {
                    tabLayout.setScrollPosition(mPositionBeforeScroll + (int) (positionOffset)
                            , positionOffset - (int) (positionOffset),
                            false);
                }
            }
            Log.d("test", "dx:" + positionOffset);
        }

        @Override
        public void OnPageChanged(int oldPosition, int newPosition) {
            TabLayout tabLayout = this.mTabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.getTabAt(newPosition).select();
            }
        }
    }

    class FragmentsAdapter extends FragmentStatePagerAdapter {

        public FragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position, Fragment.SavedState savedState) {
            Fragment f = new CheeseListFragment();
            if (savedState == null) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", position);
                f.setArguments(bundle);
            }
            f.setInitialSavedState(savedState);
            return f;
        }

        @Override
        public void onDestroyItem(int position, Fragment fragment) {
            // onDestroyItem
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

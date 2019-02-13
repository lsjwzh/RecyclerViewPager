package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.os.Bundle;

import static android.support.v7.widget.RecyclerView.TOUCH_SLOP_DEFAULT;

public class FreeFlingPagerActivity extends SingleFlingPagerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView.setScrollingTouchSlop(TOUCH_SLOP_DEFAULT);
    }
}

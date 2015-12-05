package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.os.Bundle;

public class FreeFlingPagerActivity extends SingleFlingPagerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecyclerView.setSinglePageFling(false);
    }
}

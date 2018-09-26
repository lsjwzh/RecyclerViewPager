package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.app.Activity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lsjwzh.widget.recyclerviewpager.LoopRecyclerViewPager;

public class LoopPagerActivity extends Activity {
    protected LoopRecyclerViewPager mRecyclerViewPager;
    private EditText mTargetPosition;
    private Button mScrollToPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_loopviewpager);
        initViewPager();
    }

    protected void initViewPager() {
        mRecyclerViewPager = (LoopRecyclerViewPager) findViewById(R.id.viewpager);
        mTargetPosition = (EditText) findViewById(R.id.target_position);
        mScrollToPosition = (Button) findViewById(R.id.scroll_to_target_position);
        mScrollToPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mTargetPosition.getText() != null ? Integer.valueOf(mTargetPosition
                        .getText()
                        .toString()) : 0;
                mRecyclerViewPager.smoothScrollToPosition(position);
            }
        });

        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerViewPager.setTriggerOffset(0.15f);
        mRecyclerViewPager.setFlingFactor(0.25f);
        mRecyclerViewPager.setLayoutManager(layout);
        mRecyclerViewPager.setAdapter(new LayoutAdapter(this, mRecyclerViewPager));
        mRecyclerViewPager.setHasFixedSize(true);
        mRecyclerViewPager.setLongClickable(true);

        mRecyclerViewPager.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
//                mPositionText.setText("First: " + mRecyclerViewPager.getFirstVisiblePosition());
                int childCount = mRecyclerViewPager.getChildCount();
                int width = mRecyclerViewPager.getChildAt(0).getWidth();
                int padding = (mRecyclerViewPager.getWidth() - width) / 2;

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    //往左 从 padding 到 -(v.getWidth()-padding) 的过程中，由大到小
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                    } else {
                        //往右 从 padding 到 recyclerView.getWidth()-padding 的过程中，由大到小
                        if (v.getLeft() <= recyclerView.getWidth() - padding) {
                            rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                        }
                        v.setScaleY(0.9f + rate * 0.1f);
                    }
                }
            }
        });

        mRecyclerViewPager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mRecyclerViewPager.getChildCount() < 3) {
                    if (mRecyclerViewPager.getChildAt(1) != null) {
                        View v1 = mRecyclerViewPager.getChildAt(1);
                        v1.setScaleY(0.9f);
                    }
                } else {
                    if (mRecyclerViewPager.getChildAt(0) != null) {
                        View v0 = mRecyclerViewPager.getChildAt(0);
                        v0.setScaleY(0.9f);
                    }
                    if (mRecyclerViewPager.getChildAt(2) != null) {
                        View v2 = mRecyclerViewPager.getChildAt(2);
                        v2.setScaleY(0.9f);
                    }
                }

            }
        });
    }
}

package com.lsjwzh.widget.recyclerviewpager;

import android.support.v7.widget.RecyclerViewEx;
import android.view.View;
import android.view.ViewGroup;

/**
 * RecyclerViewPagerAdapter </br>
 * Adapter wrapper. Use to add margin at first and last itemView.
 *
 * @author Green
 * @since 2015/1/20 下午2:17
 */
public class RecyclerViewPagerAdapter<VH extends RecyclerViewEx.ViewHolder> extends RecyclerViewEx.Adapter<VH> {
    private final RecyclerViewPager mViewPager;
    RecyclerViewEx.Adapter<VH> mAdapter;
    int mPagerLeftMargin = -1;
    int mPagerRightMargin = -1;


    public RecyclerViewPagerAdapter(RecyclerViewPager viewPager, RecyclerViewEx.Adapter<VH> adapter) {
        mAdapter = adapter;
        mViewPager = viewPager;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        mAdapter.onBindViewHolder(holder, position);
        final View itemView = holder.itemView;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        if(mPagerLeftMargin<0){
            mPagerLeftMargin = lp.leftMargin;
        }
        if(mPagerRightMargin<0){
            mPagerRightMargin = lp.rightMargin;
        }
        int padding = mViewPager.getDisplayPadding();
        lp.width = mViewPager.getWidth() - 2*padding;
        if (position == 0) {
            if (itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    lp.leftMargin = mPagerLeftMargin +  padding;
            }
        } else if (position == getItemCount() - 1) {
            if (itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                lp.rightMargin = mPagerRightMargin + padding;
            }
        } else {
            if (lp.leftMargin > mPagerLeftMargin) {
                lp.leftMargin = mPagerLeftMargin;
            }
            if (lp.rightMargin > mPagerRightMargin) {
                lp.rightMargin = mPagerRightMargin;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }
}

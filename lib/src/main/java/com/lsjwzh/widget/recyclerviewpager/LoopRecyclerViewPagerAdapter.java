package com.lsjwzh.widget.recyclerviewpager;

import android.support.v7.widget.RecyclerView;

public class LoopRecyclerViewPagerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerViewPagerAdapter<VH> {

    public LoopRecyclerViewPagerAdapter(RecyclerViewPager viewPager, RecyclerView.Adapter<VH> adapter) {
        super(viewPager, adapter);
    }

    public int getActualItemCount() {
        return super.getItemCount();
    }

    public int getActualItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public long getActualItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(getActualPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(getActualPosition(position));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        super.onBindViewHolder(holder, getActualPosition(position));
    }

    public int getActualPosition(int position) {
        int actualPosition = position;
        if (position >= getActualItemCount()) {
            actualPosition = position % getActualItemCount();
        }
        return actualPosition;
    }
}

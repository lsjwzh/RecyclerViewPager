package com.lsjwzh.widget.recyclerviewpager;

import android.content.Context;
import android.support.v7.widget.RecyclerViewEx;
import android.util.AttributeSet;

/**
 * RecyclerViewPager
 * @author Green
 * @since 15/1/18$ 上午2:06
 */
public class RecyclerViewPager extends RecyclerViewEx {

    int mDisplayPadding;
    RecyclerViewPagerAdapter mViewPagerAdapter;


    public RecyclerViewPager(Context context) {
        super(context);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mViewPagerAdapter = new RecyclerViewPagerAdapter(this,adapter);
        super.setAdapter(mViewPagerAdapter);
    }

    @Override
    public Adapter getAdapter() {
        if(mViewPagerAdapter!=null){
            return mViewPagerAdapter.mAdapter;
        }
        return null;
    }

    public RecyclerViewPagerAdapter getWrapperAdapter(){
        return mViewPagerAdapter;
    }

    /**
     * get the padding from center page to RecyclerViewEx's edge
     * @return
     */
    public int getDisplayPadding(){
        return mDisplayPadding;
    }

    /**
     * set the padding from center page to RecyclerViewEx's edge
     * @param displayPadding
     */
    public void setDisplayPadding(int displayPadding){
         mDisplayPadding = displayPadding;
    }
}

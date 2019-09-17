package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int mColumnCount;
    private final int mSpace;

    public SpacesItemDecoration(int space, int columnCount) {
        this.mSpace = space;
        this.mColumnCount = columnCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mSpace;
        // Add top margin only for the first item to avoid double mSpace between items
        if (parent.getChildLayoutPosition(view) == mColumnCount - 1) {
            outRect.right = 0;
        } else {
            outRect.right = mSpace / 2;
        }
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.left = 0;
        } else {
            outRect.left = mSpace / 2;
        }
    }
}

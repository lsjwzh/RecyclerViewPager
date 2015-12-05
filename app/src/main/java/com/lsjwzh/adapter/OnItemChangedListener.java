package com.lsjwzh.adapter;

public interface OnItemChangedListener {

  boolean onItemMove(int fromPosition, int toPosition);

  void onItemDismiss(int position);
}

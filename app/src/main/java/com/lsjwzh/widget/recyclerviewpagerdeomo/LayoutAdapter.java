/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.content.Context;
import android.support.v7.widget.RecyclerViewEx;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import static com.lsjwzh.widget.recyclerviewpagerdeomo.R.*;

public class LayoutAdapter extends RecyclerViewEx.Adapter<LayoutAdapter.SimpleViewHolder> {
    private static final int COUNT = 100;

    private final Context mContext;
    private final RecyclerViewEx mRecyclerView;
    private final List<Integer> mItems;
    private final int mLayoutId;
    private int mCurrentItemId = 0;

    public static class SimpleViewHolder extends RecyclerViewEx.ViewHolder {
        public final TextView title;

        public SimpleViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(id.title);
        }
    }

    public LayoutAdapter(Context context, RecyclerViewEx recyclerView, int layoutId) {
        mContext = context;
        mItems = new ArrayList<Integer>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            addItem(i);
        }

        mRecyclerView = recyclerView;
        mLayoutId = layoutId;
    }

    public void addItem(int position) {
        final int id = mCurrentItemId++;
        mItems.add(position, id);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(layout.item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.title.setText(mItems.get(position).toString());

        final View itemView = holder.itemView;

        final int itemId = mItems.get(position);
        if(position==0){
            if(itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).leftMargin = (mRecyclerView.getWidth()-itemView.getLayoutParams().width)/2;
            }
        }else if(position==mItems.size()-1){
            if(itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
                ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).rightMargin = (mRecyclerView.getWidth()-itemView.getLayoutParams().width)/2;
            }
        }else {
            ((ViewGroup.MarginLayoutParams) itemView.getLayoutParams()).leftMargin = 0;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}

/*
 * Copyright (C) 2015 lsjwzh
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

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lsjwzh.adapter.GenericRecyclerViewAdapter;
import com.lsjwzh.adapter.OnRecyclerViewItemClickListener;


public class MainActivity extends AppCompatActivity {

    RecyclerView mDemoRecyclerView;
    private DemoListAdapter mDemoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_list);
        mDemoRecyclerView = (RecyclerView) findViewById(R.id.demo_list);
        mDemoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
        mDemoRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager
                .VERTICAL));
        mDemoListAdapter = new DemoListAdapter();
        mDemoRecyclerView.setAdapter(mDemoListAdapter);
        mDemoListAdapter.add(new DemoItem("Single Fling Pager(like official ViewPager)") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, SingleFlingPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Free Fling Pager(like ViewPager combine with Gallary)") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, FreeFlingPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Material Demo") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, MaterialDemoActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Material Demo With loop pager") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, MaterialDemoWithLoopPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Vertical ViewPager Demo") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, VerticalPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Loop ViewPager Demo") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, LoopPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Reverse Single Fling Pager(like official ViewPager)") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, ReverseSingleFlingPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("Reverse Vertical ViewPager Demo") {
            @Override
            void onClick() {
                startActivity(new Intent(MainActivity.this, ReverseVerticalPagerActivity.class));
            }
        });
        mDemoListAdapter.add(new DemoItem("3D effect Demo(TODO)") {
            @Override
            void onClick() {
                // TODO: open 3D effect Demo
            }
        });
    }


    class DemoListAdapter extends GenericRecyclerViewAdapter<DemoItem, DemoListItemViewHolder> {

        DemoListAdapter() {
            setOnItemClickListener(new OnRecyclerViewItemClickListener<DemoListItemViewHolder>() {
                @Override
                public void onItemClick(View view, int position, DemoListItemViewHolder viewHolder) {
                    getItem(position).onClick();
                }
            });
        }

        @Override
        public DemoListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.demo_list_item, parent, false);
            return new DemoListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DemoListItemViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.mTextView.setText(getItem(position).mText);
        }
    }

    class DemoItem {
        String mText;

        DemoItem(String text) {
            mText = text;
        }

        void onClick() {
        }
    }

    class DemoListItemViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public DemoListItemViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }
    }

}

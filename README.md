# RecyclerViewPager
An ViewPager implemention base on RecyclerView's code. Support fling operation like gallary.

![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/demo.gif)
## Usage

### how to import?
add this into gradle

    compile('com.lsjwzh:recyclerviewpager:0.5.3')


### xml:

```
<com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
        android:id="@+id/viewpager"
        android:layout_width="MATCH_PARENT"
        android:layout_height="MATCH_PARENT"/>
```

### java api:
```
RecyclerViewPager mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.list);
LinearLayoutManagerEx layout = new LinearLayoutManagerEx(getActivity(),OrientationHelperEx.HORIZONTAL, false);
mRecyclerView.setLayoutManager(layout);//setLayoutManager
//set the left margin of first viewpage and the right margin of last viewpage
mRecyclerView.setDisplayPadding(dip2px(getActivity(), 15));
//set adapter
mRecyclerView.setAdapter(new LayoutAdapter(activity, mRecyclerView, mLayoutId));



//set scroll listener
//this will show you how to implement a ViewPager like the demo gif
 mRecyclerView.setOnScrollListener(new RecyclerViewEx.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerViewEx recyclerView, int scrollState) {
                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerViewEx recyclerView, int i, int i2) {
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding  = (mRecyclerView.getWidth() - width)/2;
                mCountText.setText("Count: " + childCount);

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

// and because 'onScrolled' just be called after the viewpager scrolling
// we must set the initial state 
mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mRecyclerView.getChildAt(1) != null) {
                    View v2 = mRecyclerView.getChildAt(1);
                    v2.setScaleY(0.9f);
                    mRecyclerView.removeOnLayoutChangeListener(this);
                }
            }
        });

```
### release notes:
    0.5.3 fix bug: setHasStableIds not work
	0.1.0



License
-------

    Copyright 2015 lsjwzh

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

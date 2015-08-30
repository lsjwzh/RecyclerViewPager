[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RecyclerViewPager-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1622)
# RecyclerViewPager
A ViewPager implemention base on RecyclerView. Support fling operation like gallary.

## Features:
1. Extends RecyclerView.
2. Custom fling factor.
3. Custom paging trigger.
4. Support Vertical and Horizontal orientation.
5. Support FragmentViewPager (api 12+)

![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/vertical.gif)
![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/horizontal.gif)
![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/fragment.gif)

## Usage

### how to import?
add this into gradle

    compile('com.lsjwzh:recyclerviewpager:1.0.8')

### xml:

```
<com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
    android:id="@+id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="15dp"
    android:paddingRight="15dp"
    app:triggerOffset="0.1"
    app:singlePageFling="true"
    android:clipToPadding="false"/>
```

### java api:
```
RecyclerViewPager mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.list);
LinearLayoutManager layout = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
mRecyclerView.setLayoutManager(layout);//setLayoutManager
//set adapter
mRecyclerView.setAdapter(new LayoutAdapter(activity, mRecyclerView, mLayoutId));//you need to impelement LayoutAdapter by yourself like a ListAdapter.

//set scroll listener
//this will show you how to implement a ViewPager like the demo gif

mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerViewEx recyclerView, int scrollState) {
                // do something
            }

            @Override
            public void onScrolled(RecyclerViewEx recyclerView, int i, int i2) {
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding  = (mRecyclerView.getWidth() - width)/2;
                mCountText.setText("Count: " + childCount);

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                    } else {
                        if (v.getLeft() <= recyclerView.getWidth() - padding) {
                            rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                        }
                        v.setScaleY(0.9f + rate * 0.1f);
                    }
                }
            }
        });
        // registering addOnLayoutChangeListener  aim to setScale at first layout action
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mRecyclerView.getChildCount()<3){
                    if (mRecyclerView.getChildAt(1) != null) {
                        View v1 = mRecyclerView.getChildAt(1);
                        v1.setScaleY(0.9f);
                    }
                }else {
                    if (mRecyclerView.getChildAt(0) != null) {
                        View v0 = mRecyclerView.getChildAt(0);
                        v0.setScaleY(0.9f);
                    }
                    if (mRecyclerView.getChildAt(2) != null) {
                        View v2 = mRecyclerView.getChildAt(2);
                        v2.setScaleY(0.9f);
                    }
                }

            }
        });

```
### Release Notes:
    1.0.8 override swapAdapter；support singlePageFling；
    1.0.7 remove redandunt codes; support cancel action
    1.0.6 resolve potential id conflicting on FragmentViewPagerApdater
    1.0.4 fix bug : exception happens if  ItemView LayoutParam is not MarginLayoutParam
    1.0.3 add method: getCurrentPosition
    1.0.2 support FragmentViewPager, add OnPageChangedListener
    1.0.1 fix bug: smoothScrollToPosition index out of range
    1.0.0 reimplement RecyclerViewPager without coping RecyclerView's codes.

    0.5.4 add 'HorizontalCenterLayoutManager' to implement ViewPager
    0.5.3 fix bug: setHasStableIds not work
	0.1.0

### Special Thanks:
[saadfarooq](https://github.com/saadfarooq)
[taxomania](https://github.com/taxomania)

### ToDo:
~~Vertical ViewPager~~    
~~FragmentViewPager~~
~~observe OnPageChanged~~

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

Deprecated


[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RecyclerViewPager-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1622)
# RecyclerViewPager
A ViewPager implemention base on RecyclerView. Support fling operation like gallary.

android.support.v4.view.ViewPager的完美替代品

## Features:
1. Base on RecyclerView.
2. Custom fling factor.
3. Custom paging trigger.
4. Support Vertical and Horizontal orientation.
5. Support FragmentViewPager (api 12+)
6. Infinite-Loop-ViewPager
7. Support TabLayout

## 特性:
1. 扩展自RecyclerView.
2. 可自定义fling滑动速率.
3. 可自定义翻页触发条件.
4. 支持垂直ViewPager.
5. 支持Fragment (api 12+)
6. 支持无限循环
7. 支持和TabLayout配合使用

![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/vertical.gif)
![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/horizontal.gif)
![RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager/blob/master/fragment.gif)

## Usage

### how to import?
add this into gradle

```gradle
// Yes, I have switched to jitpack.io.

repositories {
    ...
    maven { url "https://jitpack.io" }
    ...
}

dependencies {
    ...
    compile 'com.github.lsjwzh.RecyclerViewPager:lib:v1.2.0@aar'
    ...
}
```
add proguard rules if need

	-keep class com.lsjwzh.widget.recyclerviewpager.**
	-dontwarn com.lsjwzh.widget.recyclerviewpager.**


### Basic Usage:

It is easy to setup like a Recycler List View.

#### xml:

```xml
<com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
    android:id="@+id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="15dp"
    android:paddingRight="15dp"
    app:rvp_triggerOffset="0.1"
    app:rvp_singlePageFling="true"
    android:clipToPadding="false"/>
```
#### code:
```java
RecyclerViewPager mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.list);

// setLayoutManager like normal RecyclerView, you do not need to change any thing.
LinearLayoutManager layout = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
mRecyclerView.setLayoutManager(layout);

//set adapter
//You just need to implement ViewPageAdapter by yourself like a normal RecyclerView.Adpater.
mRecyclerView.setAdapter(new RecyclerView.Adpater<X>());

// That is all.

```
### Support TabLayout:

#### 1.Add Dependency:
```gradle
dependencies {
    ...
    compile 'com.github.lsjwzh.RecyclerViewPager:tablayoutsupport:v1.1.2@aar'
    ...
}
```
#### 2.Call 'setupWithViewPager'
```java
TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
TabLayoutSupport.setupWithViewPager(tabLayout, mRecyclerView, mAdapter);
```
### Infinite Loop ViewPager:
```xml
<com.lsjwzh.widget.recyclerviewpager.LoopRecyclerViewPager
        android:id="@+id/viewpager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="20dp"
        android:clipToPadding="false"
        android:paddingTop="15dp"
        android:paddingBottom="15dp"/>
```
You just need to use `LoopRecyclerViewPager` replace `RecyclerViewPager`.
The usage of `LoopRecyclerViewPager` is the same as `RecyclerViewPager`.
No matter what you specify position you want to scroll, `LoopRecyclerViewPager` will transform it
 to right position.
Ex:In a `LoopRecyclerViewPager` with 10 items, `scrollToPosition(1)` will get
  same scroll behavior as `scrollToPosition(11)`、`scrollToPosition(21)` and so on.

#### How to get actual position in LoopRecyclerViewPager:
```
    int actualPosition = mLoopRecyclerViewPager.transformToActualPosition(adapterPosition);
```
If you just want to get current actual position, you can do it like this:

```
    int actualCurrentPosition = mLoopRecyclerViewPager.getActualCurrentPosition();
```


### Release Notes:
    1.2.0 update support lib to 26.0.2
    1.1.2 merge some fix.
    1.1.1 merge some fix.
    1.1.0stable fix bug:LoopViewPager position confusion;LoopViewPager non stop spinning;
    1.1.0beta5 feat: TabLayoutSupport Lib supports LoopViewPager
    1.1.0beta4 fix bug:support ItemDecorations;
    1.1.0beta3 support reverse;
    1.1.0 refactor;support TabLayout;
    1.0.11 support infinite loop
    1.0.10 make touch gesture smother；
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
~~Infinite-Loop-ViewPager~~
~~Support TabLayout	~~
Support ViewPagerIndicator
Wrap Content？
dispatchTouchEvent override demo

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

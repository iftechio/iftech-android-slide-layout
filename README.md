# SlideLayout
A layout for managing two list in a single page

### Release
`TODO`

### 实现原理
[SlideLayout 双列表页面实现](https://github.com/tuesda/blog/blob/master/android/view/20190718_slidelayout_double_list/SlideLayout%20%E5%8F%8C%E5%88%97%E8%A1%A8%E9%A1%B5%E9%9D%A2%E5%AE%9E%E7%8E%B0.md)

### Usage

使用Slide的页面布局结构如下：
```xml
<io.iftech.android.library.slide.SlideLayout>
    <!-- header -->
    <io.iftech.android.library.slide.MinVerticalMarginFrameLayout>
        <androidx.core.widget.NestedScrollView>
            <!-- header content here -->
        </androidx.core.widget.NestedScrollView>
    </io.iftech.android.library.slide.MinVerticalMarginFrameLayout>
    <!-- slider -->
    <io.iftech.android.library.slide.MinVerticalMarginFrameLayout>
        <LinearLayout>
            <io.iftech.android.library.slide.SlideBarLayout>
                <!-- slide bar content here -->
            </io.iftech.android.library.slide.SlideBarLayout>
            <androidx.recyclerview.widget.RecyclerView/>
        </LinearLayout>
    </io.iftech.android.library.slide.MinVerticalMarginFrameLayout>
    <!-- refresh -->
    <io.iftech.android.library.refresh.RefreshViewLayout/>
</io.iftech.android.library.slide.SlideLayout>
```
代码中需要指定 SlideLayout 结构中 Header 和 Slider 中各自实现 NestedScrollingChild 接口的 View。具体方法如下:

```kotlin
headerNestedScrollingChildImplView.configSlideChildTypeHeader()
silderNestedScrollingChildImplView.configSlideChildTypeSlider()
```
除了上面的还有一些配置项需要设置下:

1. Header 最小高度，用于指定当 Header 被折叠时需要显示的高度，这个值一般和 ActionBar 高度一致。
    ```kotlin
    layHeader.minimumHeight = headerMinHeight
    ```
2. Slider 根据上面设置的 Header 最小值设置最少间距，用来指定 Slider 完全展开时的高度。
    ```kotlin
    laySlider.setMinVerticalMargin(layHeader.minimumHeight)
    ```
3. SlideLayout 设置 offset，用于指定展示刷新动画的竖向起始位置。
    ```kotlin
    laySlide.setOffset(layHeader.minimumHeight)
    ```
另外 RefreshViewLayout 只是一个刷新动画容器，并不提供具体的刷新动画实现。使用者可以通过实现 RefreshView 接口创建自定义的刷新动画，并设置给 RefreshViewLayout 的 refreshInterface 来生效。本项目中实现了一个简单的文本刷新动画作为例子以供参考，代码如下:
```kotlin
layRefresh.refreshInterface = MyRefreshViewImpl(this)
``` 
最后 SlideLayout 还提供了丰富的接口用来满足各种交互功能：

* `fun setOnRefreshListener(listener: (byPull: Boolean, isSliderExpand: Boolean) -> Unit)`
    设置下拉刷新监听接口，当用户被出发下拉刷新时接口会被回调。回调的方法的第一个参数表示该下拉刷新是否是用户手动触发，为 false 时表示代码调用触发；第二个参数表示下拉刷新时 Slider 是否打开。常见的使用场景是开始一个网络请求。
* `finishRefresh()`
    结束下拉刷新动画，一般当下拉刷新触发的延迟工作处理完成时调用，常见的使用场景是当页面的刷新请求完成时调用。
* `fun refresh()`
    代码触发一个下拉刷新，一般用于非手动下拉刷新的场景。
* `fun doOnHeaderUpdate(listener: (headerTop: Int) -> Unit)`
    设置 Header 位置发生改变的监听回调，headerTop 表示 Header 当前的高度。
* `fun doOnSliderOffsetChange(listener: (sliderTop: Int) -> Unit)`
    设置 Slider 位置发生改变的监听回调，sliderTop 表示 Slider 当前的高度。
* `fun doOnSliderExpandChange(listener: (expand: Boolean) -> Unit)`
    设置 Slider 是否展开的监听回调，expand 表示 Slider 是否展开。
* `quickReturn()`
    页面的 quickReturn 方法，调用后 Slider 和 Header 中的内容会回到最开始位置并且展开 Header。
* `fun scrollAroundSlider(distance: Int, onEndListener: () -> Unit)`
    用于展示 Slider 可以盖在 Header 上面的新手引导功能函数。
* `fun expandHeader(offset: Int? = null)`
    展开 Header，offset 表示展开的偏移量，默认为 null 表示不偏移。
* `fun expandSlider(slideMode: Boolean)`
    展开 Slider，slideMode == true 表示将 Slider 盖在 Header 上面，否则表示联动滚动。
* `fun slideExpandSlider()`
    动画展开 Slider
* `fun isSliderCollapsed()`
    表示 Slider 是否折叠
* `fun doOnHeaderVisibleRangeChange(listener: (headerVisibleRange: IntRange) -> Unit)`
    设置 Header 可见区域改变监听回调，headerVisibleRange 表示可见范围，取值为竖向坐标。
* `fun setRefreshOffset(offset: Int)`
    设置刷新动画展示区域的起始位置，取值为竖向坐标。
* xml 属性
    * `widget_slide_overlay_distance` 表示 Header 和 Slider 重叠距离。
    * `widget_disable_slider_refresh` 表示是否禁用 slider 刷新，未禁用时当 Header 被折叠，不展开 Header 即可触发下拉刷新并展示刷新动画。 
# XAtrBoard

XArtBoard是一个Android画板库，可以很方便加载图片到画板上，并对画板进行涂鸦，结束后可以保存图片输出到本地

[<img title="" src="https://jitpack.io/v/shengMR/XArtBoard.svg" alt="Release" data-align="inline">] (https://jitpack.io/shengMR/XArtBoard)

## 添加库

1, 添加maven仓库

```groovy
maven { url "https://jitpack.io"  }
```

2, 添加依赖

```groovy
dependencies {
    implementation 'com.github.shengMR:XArtBoard:v1.0.0'
}
```

## 项目使用

项目中使用了viewbinding ，需要在app模块下的build.gradle启用viewbinding

```groovy
android {
    ...
    viewBinding {
        enabled = true
    }
}
```

Activity的onCreate

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)  
    }
}
```

> 注意：
> 
> 图片加载：项目中的图片加载都是原图，并没进行优化，可以使用[Glide]([GitHub - bumptech/glide: An image loading and caching library for Android focused on smooth scrolling](https://github.com/bumptech/glide))进行优化之后再加载进画板

### 基础

#### 添加布局

```xml
<cn.com.cys.xartboard.XArtBoard
    android:id="@+id/xartboard"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</cn.com.cys.xartboard.XArtBoard>
```

#### 使用图片(可选)

类似于在图片上面进行编辑，可以设置图片的位置，靠边或者居中

```kotlin
binding.xartboard.loadBitmap(bitmap, gravity = XGravity.GravityCenter)
```

#### 使用画笔进行涂鸦

```kotlin
// 设置画笔为线条模式
binding.xartboard.setPenType(XPenType.LINE)
// 设置画笔颜色
binding.xartboard.setPenColor(Color.BLACK)
// 设置画笔宽度
binding.xartboard.setPenWidth(50f)
```

#### 使用橡皮擦

```kotlin
// 设置橡皮擦模式
binding.xartboard.setPenType(XPenType.ERASER)
// 设置橡皮擦宽度
binding.xartboard.setPenEraserWidth(50f)
```

#### 撤回

撤回上一次操作

```kotlin
binding.xartboard.retract()
```

#### 恢复

恢复之前撤回的操作

```kotlin
binding.xartboard.retrieve()
```

#### 重置

清空涂鸦

```kotlin
binding.xartboard.reset()
```

#### 获取画板的Bitmap对象

获取bitmap对象后，可以对bitmap进行操作，例如保存到本地

```kotlin
binding.xartboard.getBitmap()
```

### 保存到本地

库中内置了方法方便直接保存bitmap到本地

在Activity/ViewModel中保存

```kotlin
// 保存到本地
saveXArtBoardToLocal( binding.xartboard.getBitmap(), "${filesDir}/${System.currentTimeMillis()}.png") { bool, _ ->
    if (bool) {
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
    }
}
```

### 进阶

#### 使用预置图形

```kotlin
// 设置方形模式
binding.xartboard.setPenType(XPenType.RECT)
// 设置正方形模式
binding.xartboard.setPenType(XPenType.SQUARE)
// 设置椭圆模式
binding.xartboard.setPenType(XPenType.OVAL)
// 设置圆形模式
binding.xartboard.setPenType(XPenType.CIRCLE)
```



###### 后续功能开发中...

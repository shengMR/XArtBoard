package cn.com.cys.xartboard.helper

import android.content.Context

/**
 * Author: Damon
 * Date: 2022/8/26 10:27
 * Description
 */
object XArtUtils {

    fun dp2pxFloat(context: Context, dp: Float): Float {
        return context.resources.displayMetrics.density * dp
    }

    fun dp2pxInt(context: Context, dp: Float): Int {
        return (context.resources.displayMetrics.density * dp + .5).toInt()
    }
}
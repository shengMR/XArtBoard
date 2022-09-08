package cn.com.cys.xartboard.helper

import android.util.Log

/**
 * Author: Damon
 * Date: 2022/8/18 09:42
 * Description
 */
object XLog {

    private const val TAG = "XLog"
    private var isDebug: Boolean = false

    fun debug(debug: Boolean){
        this.isDebug = debug
    }

    fun d(tag: String, msg: String){
        if(!isDebug){
            return
        }
        Log.d(tag, msg)
    }

    fun e(tag: String, msg: String){
        if(!isDebug){
            return
        }
        Log.e(tag, msg)
    }

    fun d(msg: String){
        e(TAG, msg)
    }

    fun e(msg: String){
        e(TAG, msg)
    }
}
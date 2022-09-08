package cn.com.cys.xartboard.helper

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Author: Damon
 * Date: 2022/8/29 13:30
 * Description
 */

/**
 * 在Activity中保存绘制到本地路径
 */
fun AppCompatActivity.saveXArtBoardToLocal(bitmap: Bitmap, localPath: String, listener: (Boolean, Throwable?) -> Unit){
    lifecycleScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                val file = File(localPath)
                if (!file.exists()) {
                    file.createNewFile()
                }
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.flush()
                    it.close()
                }
            }
        }.onSuccess {
            listener.invoke(true, null)
        }.onFailure {
            listener.invoke(false, it)
        }
    }
}

/**
 * 在viewModel中保存绘制到本地路径
 */
fun ViewModel.saveXArtBoardToLocal(bitmap: Bitmap, localPath: String, listener: (Boolean, Throwable?) -> Unit){
    viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                val file = File(localPath)
                if (!file.exists()) {
                    file.createNewFile()
                }
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.flush()
                    it.close()
                }
            }
        }.onSuccess {
            listener.invoke(true, null)
        }.onFailure {
            listener.invoke(false, it)
        }
    }
}
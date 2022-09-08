package cn.com.cys.xartboard

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cn.com.cys.xartboard.databinding.ActivityMainBinding
import cn.com.cys.xartboard.helper.saveXArtBoardToLocal
import cn.com.cys.xartboard.core.material.XMaterial
import cn.com.cys.xartboard.core.tool.XGravity
import cn.com.cys.xartboard.core.tool.XPenType
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class MainActivity : AppCompatActivity(), View.OnLongClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pen.setOnLongClickListener(this)
        binding.eraser.setOnLongClickListener(this)

        // 加载本地图片
        binding.xartboard.loadBitmap(R.drawable.img1, gravity = XGravity.GravityEnd)
        // 加载网络图片
        Glide.with(this)
            .asBitmap()
            .load("https://images.pexels.com/photos/7858743/pexels-photo-7858743.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1")
            .into(object : SimpleTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    resource.let { bitmap ->
                        binding.xartboard.loadBitmap(bitmap, gravity = XGravity.GravityStart)
                    }
                }
            })
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.pen -> {
                binding.xartboard.setPenType(XPenType.LINE)
                if (binding.flControl.visibility == View.VISIBLE) {
                    binding.seekbarEraser.progress = binding.xartboard.getPenWidth().toInt()
                }
            }
            R.id.eraser -> {
                if (binding.flControl.visibility == View.VISIBLE) {
                    binding.seekbarEraser.progress = binding.xartboard.getPenEraserWidth().toInt()
                }
                binding.xartboard.setPenType(XPenType.ERASER)
            }
            R.id.color -> {
                val nextInt =
                    Color.argb(255, Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
                binding.xartboard.setPenColor(nextInt)
                binding.color.setTextColor(nextInt)
            }
            R.id.retract -> {
                binding.xartboard.retract()
            }
            R.id.retrieve -> {
                binding.xartboard.retrieve()
            }
            R.id.reset -> {
                binding.xartboard.reset()
            }
            R.id.close -> {
                binding.flControl.visibility = View.GONE
            }
            R.id.save -> {
                saveXArtBoardToLocal( binding.xartboard.getBitmap(), "${filesDir}/${System.currentTimeMillis()}.png") { bool, _ ->
                    if (bool) {
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.rect -> {
                binding.xartboard.setPenType(XPenType.RECT)
            }
            R.id.square -> {
                binding.xartboard.setPenType(XPenType.SQUARE)
            }
            R.id.oval -> {
                binding.xartboard.setPenType(XPenType.OVAL)
            }
            R.id.circle -> {
                binding.xartboard.setPenType(XPenType.CIRCLE)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when(v.id){
            R.id.pen -> {
                binding.flControl.visibility = View.VISIBLE
                binding.seekbarEraser.max = 50
                binding.seekbarEraser.progress = binding.xartboard.getPenWidth().toInt()
                binding.seekbarEraser.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        binding.xartboard.setPenWidth(seekBar.progress.toFloat())
                    }

                })
            }
            R.id.eraser -> {
                binding.flControl.visibility = View.VISIBLE
                binding.seekbarEraser.max = 50
                binding.seekbarEraser.progress = binding.xartboard.getPenEraserWidth().toInt()
                binding.seekbarEraser.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        binding.xartboard.setPenEraserWidth(seekBar.progress.toFloat())
                    }

                })
            }
        }
        return true
    }
}

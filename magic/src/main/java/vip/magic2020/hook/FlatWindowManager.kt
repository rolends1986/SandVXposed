package vip.magic2020.hook

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.interfaces.OnInvokeView
import com.lzf.easyfloat.permission.PermissionUtils

class FlatWindowManager(private var content: Context) {
    companion object {
        private var defaultManager: FlatWindowManager? = null

        fun with(content: Context): FlatWindowManager {
            EasyFloat.isDebug = true
            defaultManager = FlatWindowManager(content)
            return defaultManager!!
        }

        fun default(): FlatWindowManager {
            if (defaultManager == null) throw  Exception("first step call with")
            return defaultManager!!
        }
    }


    fun show() {
        checkPermission()
    }


    /**
     * 检测浮窗权限是否开启，若没有给与申请提示框（非必须，申请依旧是EasyFloat内部内保进行）
     */
    private fun checkPermission() {
        if (PermissionUtils.checkPermission(content)) {
            Log.i("VirtualHook", "FlatWindowManager checkPermission pass")
            showFlat()
        } else {
            Log.i("VirtualHook", "FlatWindowManager checkPermission show question")
            AlertDialog.Builder(content)
                    .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                    .setPositiveButton("去开启") { _, _ ->
                        showFlat()
                    }
                    .setNegativeButton("取消") { _, _ -> }
                    .show()
        }
    }

    private fun toast(context: Context, string: String = "onClick") =
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()

    private fun showFlat() {
        EasyFloat.with(content)
                .setShowPattern(ShowPattern.FOREGROUND)
                .setGravity(Gravity.END, 0, 100)
                .setLayout(R.layout.float_custom, OnInvokeView { it ->
                    it.findViewById<TextView>(R.id.textView).setOnClickListener { toast(it.context) }
                })
                .registerCallback {
                    // 在此处设置view也可以，建议在setLayout进行view操作
                    createResult { isCreated, msg, _ -> DevTools.logI("DSL:  $isCreated   $msg") }

                    show { toast(it.context, "show") }

                    hide { toast(it.context, "hide") }

                    dismiss { toast(content, "dismiss") }

                    touchEvent { view, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            view.findViewById<TextView>(R.id.textView).apply {
                                text = "拖一下试试"
                                setBackgroundResource(R.drawable.corners_green)
                            }
                        }
                    }

                    drag { view, _ ->
                        view.findViewById<TextView>(R.id.textView).apply {
                            text = "我被拖拽..."
                            setBackgroundResource(R.drawable.corners_red)
                        }
                    }

                    dragEnd {
                        it.findViewById<TextView>(R.id.textView).apply {
                            text = "拖拽结束"
                            val location = IntArray(2)
                            getLocationOnScreen(location)
                            setBackgroundResource(if (location[0] > 0) R.drawable.corners_left else R.drawable.corners_right)
                        }
                    }
                }
                .show()
    }
}
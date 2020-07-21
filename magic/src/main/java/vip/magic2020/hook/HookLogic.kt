package vip.magic2020.hook

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnInvokeView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier
import kotlin.math.ceil

class HookLogic : IXposedHookLoadPackage {

    private fun toast(context: Context, string: String = "onClick") =
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()


    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.i("VirtualHook", "HookLogic >> current package:" + lpparam.packageName)
        if ("com.xunmeng.pinduoduo" == lpparam.packageName && lpparam.processName == "com.xunmeng.pinduoduo") {
            try {
//                Class<?> homeActivityClz = lpparam.classLoader.loadClass("android.content.ContextWrapper");
//                printMethods(homeActivityClz);
                XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.ui.activity.HomeActivity", lpparam.classLoader, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        Log.i("VirtualHook", "HomeActivity beforeHookedMethod onCreate:" + param.thisObject.toString())
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {

                        Log.i("VirtualHook", "HomeActivity afterHookedMethod onCreate:" + param.thisObject.toString())
                        val homeActivity = param.thisObject as Activity
                        EasyFloat.with(homeActivity)
                                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                                .setGravity(Gravity.END, 0, 100)
                                .setLayout(R.layout.float_custom, OnInvokeView { it ->
                                    it.findViewById<TextView>(R.id.textView).setOnClickListener { toast(it.context) }
                                })
                                .registerCallback {
                                    // 在此处设置view也可以，建议在setLayout进行view操作
                                    createResult { isCreated, msg, _ -> logI("DSL:  $isCreated   $msg") }

                                    show { toast(it.context,"show") }

                                    hide { toast(it.context,"hide") }

                                    dismiss { toast(homeActivity,"dismiss") }

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
                        homeActivity.application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                            override fun onActivityCreated(activity: Activity, bundle: Bundle) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityCreated:$activity")
                            }

                            override fun onActivityStarted(activity: Activity) {

                                //EasyFloat.with(activity).setLayout().show();
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityStarted:$activity")
                            }

                            override fun onActivityResumed(activity: Activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityResumed:$activity")
                                logI(getViewHierarchy(activity.window.decorView.findViewById<View>(R.id.content) as ViewGroup))
                            }

                            override fun onActivityPaused(activity: Activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityPaused:$activity")
                            }

                            override fun onActivityStopped(activity: Activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityStopped:$activity")
                            }

                            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivitySaveInstanceState:$activity")
                            }

                            override fun onActivityDestroyed(activity: Activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityDestroyed:$activity")
                            }
                        })
                    }
                })
            } catch (t: Throwable) {
                Log.e("VirtualHook", "findAndHookMethod 失败", t)
            }
        }
    }

    companion object {
        fun logI(msg: String) {
            val lines = msg.split("\n".toRegex()).toTypedArray()
            val pageSize = 10
            if (lines.size > pageSize) {
                val page = ceil(lines.size.toDouble() / pageSize).toInt()
                var count = 0
                for (p in 0 until page) {
                    val sb = StringBuffer()
                    for (l in 0 until pageSize) {
                        if (count == lines.size) {
                            Log.i("VirtualHook", "\n$sb")
                            return
                        }
                        sb.append(lines[count])
                        sb.append("\n")
                        count++
                    }
                    Log.i("VirtualHook", "\n$sb")
                }
            } else {
                Log.i("VirtualHook", msg)
            }
        }

        fun printMethods(c1: Class<*>) {
            val methods = c1.declaredMethods
            val sb = StringBuffer()
            for (m in methods) {
                val retType = m.returnType
                val name = m.name
                sb.append("  ")
                val modifiers = Modifier.toString(m.modifiers)
                if (modifiers.isNotEmpty()) sb.append("$modifiers ")
                sb.append(retType.name + " " + name + "(")
                val paraTypes = m.parameterTypes
                for (i in paraTypes.indices) {
                    if (i > 0) sb.append(", ")
                    sb.append(paraTypes[i].name)
                }
                sb.append(");\n")
            }
            logI(sb.toString())
        }

        fun getViewHierarchy(v: View): String {
            val desc = StringBuilder()
            getViewHierarchy(v, desc, 0)
            return desc.toString()
        }

        private fun getViewHierarchy(v: View, desc: StringBuilder, margin: Int) {
            var margin = margin
            desc.append(getViewMessage(v, margin))
            if (v is ViewGroup) {
                margin++
                val vg = v
                for (i in 0 until vg.childCount) {
                    getViewHierarchy(vg.getChildAt(i), desc, margin)
                }
            }
        }

        private fun getViewMessage(v: View, marginOffset: Int): String {
            val repeated = String(CharArray(marginOffset)).replace("\u0000", "  ")
            return try {
                @SuppressLint("ResourceType")
                val resourceId = if (v.resources != null) if (v.id > 0) v.resources.getResourceName(v.id) else "no_id" else "no_resources"
                var text = ""
                if (v is TextView) {
                    text = "(" + v.text.toString() + ")"
                }
                "$repeated[${v.javaClass.simpleName}] $text $resourceId\n"
            } catch (e: NotFoundException) {
                "$repeated[${v.javaClass.simpleName}] name_not_found\n"
            }
        }
    }
}
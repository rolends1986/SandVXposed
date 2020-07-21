package vip.magic2020.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.lang.Exception
import java.lang.reflect.Modifier
import kotlin.math.ceil

class DevTools {
    companion object {

        private fun log(msg: String, f: (msg: String) -> Unit) {
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
                    f("\n$sb")
                }
            } else {
                f(msg)
            }
        }

        fun logI(msg: String) {
            log(msg) {
                Log.i("VirtualHook", msg)
            };
        }


        fun logE(msg: String, e: Throwable?) {
            log(msg) {
                Log.e("VirtualHook", msg, e)
            };
        }

        fun printMethods(c1: Class<*>) {

            val methods = c1.declaredMethods
            val sb = StringBuffer()
            sb.append("$c1 Methods:\n")
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

        fun printActivitywHierarchy(activity: Activity) {
            logI(getViewHierarchy(activity.window.decorView.findViewById<View>(android.R.id.content) as ViewGroup))
        }

        fun printViewHierarchy(v: View) {
            logI(getViewHierarchy(v))
        }

        private fun getViewHierarchy(v: View): String {
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
            } catch (e: Resources.NotFoundException) {
                "$repeated[${v.javaClass.simpleName}] name_not_found\n"
            }
        }
    }
}
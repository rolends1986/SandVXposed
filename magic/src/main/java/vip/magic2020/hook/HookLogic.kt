package vip.magic2020.hook


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
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


    fun registerActivityLifecycleCallbacks(application: Application) {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityCreated: $activity")
            }

            override fun onActivityStarted(activity: Activity) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityStarted:$activity")
            }

            override fun onActivityResumed(activity: Activity) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityResumed:$activity")
            }

            override fun onActivityPaused(activity: Activity) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityPaused:$activity")
            }

            override fun onActivityStopped(activity: Activity) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityStopped:$activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {
                DevTools.logI("ActivityLifecycleCallbacks onActivitySaveInstanceState:$activity")
            }

            override fun onActivityDestroyed(activity: Activity) {
                DevTools.logI("ActivityLifecycleCallbacks onActivityDestroyed:$activity")
            }
        })
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        DevTools.logI("HookLogic >> current package:${lpparam.packageName}")
        if ("com.xunmeng.pinduoduo" == lpparam.packageName && lpparam.processName == "com.xunmeng.pinduoduo") {
            try {

//                var clz = lpparam.classLoader.loadClass("com.tencent.tinker.loader.app.TinkerApplication")
//                DevTools.printMethods(clz)
                XposedHelpers.findAndHookMethod("com.tencent.tinker.loader.app.TinkerApplication", lpparam.classLoader, "onCreate", object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        DevTools.logI("TinkerApplication beforeHookedMethod")
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        DevTools.logI("TinkerApplication afterHookedMethod")
                        var application = param.thisObject as Application
                        EasyFloat.init(application, true)
                    }
                })

                XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.ui.activity.HomeActivity", lpparam.classLoader, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        DevTools.logI("HomeActivity beforeHookedMethod onCreate:" + param.thisObject.toString())
                        val homeActivity = param.thisObject as Activity
                        registerActivityLifecycleCallbacks(homeActivity.application)
                    }

                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        DevTools.logI("HomeActivity afterHookedMethod onCreate:" + param.thisObject.toString())
                        val homeActivity = param.thisObject as Activity
                        delay(1000L)
                        FlatWindowManager.with(homeActivity).show()
                    }
                })
            } catch (t: Throwable) {
                DevTools.logE("findAndHookMethod 失败", t)
            }
        }
    }

    companion object {


    }
}
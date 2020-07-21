package vip.magic2020.hook;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.ViewExtractor;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.FloatCallbacks;
import com.lzf.easyfloat.interfaces.OnInvokeView;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public class HookLogic implements IXposedHookLoadPackage {


    public static void log_i(String msg) {
        String[] lines = msg.split("\n");
        int pageSize = 10;
        if (lines.length > pageSize) {
            int page = (int) Math.ceil((double) lines.length / pageSize);
            int count = 0;
            for (int p = 0; p < page; p++) {
                StringBuffer sb = new StringBuffer();
                for (int l = 0; l < pageSize; l++) {
                    if (count == lines.length) {
                        Log.i("VirtualHook", "\n" + sb.toString());
                        return;
                    }
                    sb.append(lines[count]);
                    sb.append("\n");
                    count++;
                }
                Log.i("VirtualHook", "\n" + sb.toString());
            }
        } else {
            Log.i("VirtualHook", msg);
        }


    }


    public static void printMethods(Class c1) {
        Method[] methods = c1.getDeclaredMethods();
        StringBuffer sb = new StringBuffer();
        for (Method m : methods) {
            Class retType = m.getReturnType();
            String name = m.getName();

            sb.append("  ");
            String modifiers = Modifier.toString(m.getModifiers());
            if (modifiers.length() > 0)
                sb.append(modifiers + " ");
            sb.append(retType.getName() + " " + name + "(");
            Class[] paraTypes = m.getParameterTypes();
            for (int i = 0; i < paraTypes.length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(paraTypes[i].getName());
            }
            sb.append(");\n");
        }
        log_i(sb.toString());


    }

    public static String getViewHierarchy(@NonNull View v) {
        StringBuilder desc = new StringBuilder();
        getViewHierarchy(v, desc, 0);
        return desc.toString();
    }

    private static void getViewHierarchy(View v, StringBuilder desc, int margin) {
        desc.append(getViewMessage(v, margin));
        if (v instanceof ViewGroup) {
            margin++;
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                getViewHierarchy(vg.getChildAt(i), desc, margin);
            }
        }
    }

    private static String getViewMessage(View v, int marginOffset) {
        String repeated = new String(new char[marginOffset]).replace("\0", "  ");
        try {
            @SuppressLint("ResourceType")
            String resourceId = v.getResources() != null ? (v.getId() > 0 ? v.getResources().getResourceName(v.getId()) : "no_id") : "no_resources";
            String text = "";
            if (v instanceof TextView) {
                text = "(" + ((TextView) v).getText().toString() + ")";
            }
            return repeated + "[" + v.getClass().getSimpleName() + "] " + text + "" + resourceId + "\n";
        } catch (Resources.NotFoundException e) {
            return repeated + "[" + v.getClass().getSimpleName() + "] name_not_found\n";
        }
    }

    private void toast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.i("VirtualHook", "HookLogic >> current package:" + lpparam.packageName);

        if ("com.xunmeng.pinduoduo".equals(lpparam.packageName) && lpparam.processName.equals("com.xunmeng.pinduoduo")) {
            try {
//                Class<?> homeActivityClz = lpparam.classLoader.loadClass("android.content.ContextWrapper");
//                printMethods(homeActivityClz);
                XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.ui.activity.HomeActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        Log.i("VirtualHook", "HomeActivity beforeHookedMethod onCreate:" + param.thisObject.toString());
                    }


                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
/*
   at android.content.res.Resources.loadXmlResourceParser(Resources.java:2407)
        at android.content.res.Resources.getLayout(Resources.java:1292)
        at android.view.LayoutInflater.inflate(LayoutInflater.java:534)
        at android.view.LayoutInflater.inflate(LayoutInflater.java:483)
        at android.view.View.inflate(View.java:25977)
* */
                        Log.i("VirtualHook", "HomeActivity afterHookedMethod onCreate:" + param.thisObject.toString());
                        Activity homeActivity = (Activity) param.thisObject;
//                        EasyFloat.with(homeActivity).setSidePattern(SidePattern.RESULT_HORIZONTAL)
//                                .setGravity(Gravity.END, 0, 100).setLayout(R.layout.float_custom, new OnInvokeView() {
//                            @Override
//                            public void invoke(View view) {
//                                view.findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        toast(v.getContext(), "OnClick");
//                                    }
//                                });
//                            }
//                        }).registerCallback(new Function1<FloatCallbacks.Builder, Unit>() {
//                            @Override
//                            public Unit invoke(FloatCallbacks.Builder builder) {
//                                builder.createResult(new Function3<Boolean, String, View, Unit>() {
//                                    @Override
//                                    public Unit invoke(Boolean aBoolean, String s, View view) {
//
//                                        return null;
//                                    }
//                                });
//                                builder.touchEvent(new Function2<View, MotionEvent, Unit>() {
//                                    @Override
//                                    public Unit invoke(View view, MotionEvent event) {
//                                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                                        }
//                                        return null;
//                                    }
//                                });
//                                return null;
//                            }
//                        });
                        homeActivity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                            @Override
                            public void onActivityCreated(Activity activity, Bundle bundle) {

                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityCreated:" + activity.toString());
                            }

                            @Override
                            public void onActivityStarted(Activity activity) {

                                //EasyFloat.with(activity).setLayout().show();
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityStarted:" + activity.toString());
                            }

                            @Override
                            public void onActivityResumed(Activity activity) {

                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityResumed:" + activity.toString());
                                log_i(getViewHierarchy((ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content)));
                            }

                            @Override
                            public void onActivityPaused(Activity activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityPaused:" + activity.toString());
                            }

                            @Override
                            public void onActivityStopped(Activity activity) {

                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityStopped:" + activity.toString());
                            }

                            @Override
                            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivitySaveInstanceState:" + activity.toString());
                            }

                            @Override
                            public void onActivityDestroyed(Activity activity) {
                                Log.i("VirtualHook", "ActivityLifecycleCallbacks onActivityDestroyed:" + activity.toString());
                            }
                        });
                    }
                });


            } catch (Throwable t) {
                Log.e("VirtualHook", "findAndHookMethod 失败", t);
            }
        }
    }

}
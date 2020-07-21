package com.lzf.easyfloat;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;

import vip.magic2020.hook.MainActivity;

public class ViewExtractor {
    static Object locked = new Object();
    static Context packageContext;

    private static View getView(Context context, int resId, ViewGroup root) {

        return View.inflate(context, resId, root);

    }


    public static View getViewFromOther(Context context, int resId, ViewGroup root) {

        //https://gist.github.com/Miha-x64/bb842d833d220c8f52c5e9f743b3ffd7

        //https://stackoverflow.com/questions/13218819/load-resource-layout-from-another-apk-dynamically
        try {
            synchronized (locked) {
                if (packageContext == null) {
                    packageContext = context.createPackageContext(MainActivity.class.getPackage().getName(), Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
                }
            }
            Log.i("VirtualHook", " packageContext:" + packageContext.toString());
            Class<?> viewExtractor = packageContext.getClassLoader().loadClass(ViewExtractor.class.getName());
            Log.i("VirtualHook", " viewExtractor:" + viewExtractor.toString());
            Method m = viewExtractor.getDeclaredMethod("getView", Context.class, int.class, ViewGroup.class);
            m.setAccessible(true);
            Log.i("VirtualHook", " Method:" + m.toString());
            return (View) m.invoke(null, new Object[]{packageContext, resId, root});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("VirtualHook", " AllViews invoke", e);
            return null;
        }


    }
}

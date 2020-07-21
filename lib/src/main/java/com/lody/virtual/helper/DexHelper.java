package com.lody.virtual.helper;

import android.app.Application;
import android.content.Context;

import com.lody.virtual.helper.compat.ApplicationThreadCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class DexHelper {

    private static String optimizedDirectory = "optimized";
    private static String workDirectory = "working";

    public static void loadDexFile(Application app, String dexPath) throws Exception {


        ClassLoader localClassLoader = app.getClassLoader();
        BaseDexClassLoader classLoader = new DexClassLoader(dexPath,null, null, localClassLoader);

        if (localClassLoader instanceof BaseDexClassLoader) {
            Object existing = getDexClassLoaderElements((BaseDexClassLoader) localClassLoader);
            Object incoming = getDexClassLoaderElements(classLoader);
            Object joined = joinArrays(incoming, existing);
            setDexClassLoaderElements((BaseDexClassLoader) localClassLoader, joined);
        } else {
            throw new UnsupportedOperationException("Class loader not supported");
        }
    }

    private static void setDexClassLoaderElements(BaseDexClassLoader classLoader, Object elements) throws Exception {
        Class<BaseDexClassLoader> dexClassLoaderClass = BaseDexClassLoader.class;
        Field pathListField = dexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        dexElementsField.set(pathList, elements);
    }

    private static Object getDexClassLoaderElements(BaseDexClassLoader classLoader) throws Exception {
        Class<BaseDexClassLoader> dexClassLoaderClass = BaseDexClassLoader.class;
        Field pathListField = dexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object dexElements = dexElementsField.get(pathList);
        return dexElements;
    }

    private static Object joinArrays(Object o1, Object o2) {
        Class<?> o1Type = o1.getClass().getComponentType();
        Class<?> o2Type = o2.getClass().getComponentType();

        if (o1Type != o2Type)
            throw new IllegalArgumentException();

        int o1Size = Array.getLength(o1);
        int o2Size = Array.getLength(o2);
        Object array = Array.newInstance(o1Type, o1Size + o2Size);

        int offset = 0, i;
        for (i = 0; i < o1Size; i++, offset++)
            Array.set(array, offset, Array.get(o1, i));
        for (i = 0; i < o2Size; i++, offset++)
            Array.set(array, offset, Array.get(o2, i));

        return array;
    }
}

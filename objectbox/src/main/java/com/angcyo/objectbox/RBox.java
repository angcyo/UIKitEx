package com.angcyo.objectbox;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import com.angcyo.lib.L;
import com.angcyo.uiview.less.kotlin.ExKt;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.utilcode.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;
import io.objectbox.DebugFlags;
import io.objectbox.exception.DbException;

/**
 * Entity 有变化时, 请 执行 Make Project, 否则可能不会生效
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class RBox {

    static SimpleArrayMap<String, BoxStore> boxStoreMap = new SimpleArrayMap<>();
    static final String ERROR_FILE_NAME = "error.log";

    public static void init(@NonNull Context context, @Nullable String dbName, boolean debug) {
        init(context, context.getPackageName(), dbName, debug);
    }

    /**
     * myObjectBox 此对象会在 Make Project工程之后, 自动生成
     *
     * @param context
     * @param packageName 主工程, 和子工程都可以数据库
     */
    public static void init(@NonNull final Context context, @NonNull String packageName, @Nullable String dbName, boolean debug) {
        try {
//            File initFile = new File(boxPath(context), dbName + "db.init");
//            if (initFile.exists()) {
//                //数据库初始化文件存在, 说明之前初始化失败了.
//                BoxStore.deleteAllFiles(context, dbName);
//            }

            Method builder = Class.forName(packageName + ".MyObjectBox").getDeclaredMethod("builder");

            if (dbName == null) {
                dbName = BoxStoreBuilder.DEFAULT_NAME;
            }

            dbName = checkDbError(context, dbName);

            BoxStoreBuilder storeBuilder = (BoxStoreBuilder) builder.invoke(null);
            storeBuilder.androidContext(context);
            storeBuilder.name(dbName);

            if (debug) {
                storeBuilder.debugFlags(DebugFlags.LOG_TRANSACTIONS_READ | DebugFlags.LOG_TRANSACTIONS_WRITE);
                storeBuilder.debugRelations();
            }

            BoxStore boxStore;

            File baseDirectory = new File(boxPath(context));
            File dbDirectory = new File(baseDirectory, dbName);

            //默认路径:/data/user/0/包名/files/objectbox/objectbox 文件夹下
            //storeBuilder.baseDirectory()
            ///data/user/0/com.wayto.wxbic.plugin/files/objectbox
            try {
                boxStore = buildStore(context, packageName, storeBuilder);
                boxStoreMap.put(packageName, boxStore);

                L.w("数据库路径:" + packageName + "->" + dbDirectory.getAbsolutePath());
            } catch (DbException e) {
                //e.printStackTrace();
                //io.objectbox.exception.DbException, 数据库初始化异常.一般是迁移导致的,改变了字段的数据类型
                //storeBuilder.baseDirectory()

                File errorFile = new File(dbDirectory, ERROR_FILE_NAME);
                boolean newFile = errorFile.createNewFile();
                if (newFile) {
                    ExKt.save(e, errorFile.getAbsolutePath(), true);
                }
                //重启APP
                RUtils.restart();

                //throw new IllegalArgumentException(e);

                System.exit(0);
                Process.killProcess(Process.myPid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 检查数据之前是否有迁移异常
     *
     * @return 返回有效的数据库名
     */
    private static String checkDbError(@NonNull Context context, @NonNull final String dbName) {
        String result = dbName;
        File boxFile = new File(boxPath(context));
        if (boxFile.exists()) {
            File[] files = boxFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && pathname.getName().startsWith(dbName);
                }
            });
            if (files != null && files.length > 0) {
                List<File> list = Arrays.asList(files);
                Collections.sort(list);

                File lastDbPath = list.get(list.size() - 1);
                File errorFile = new File(lastDbPath, ERROR_FILE_NAME);
                if (errorFile.exists()) {
                    //错误文件存在, 说明之前崩溃过
                    //String errorLog = FileExKt.readData(errorFile);
                    //L.v(errorLog);

                    result = dbName + (list.size() + 1);

                    result = checkDbError(context, result);
                } else {
                    result = lastDbPath.getName();
                }
            }
        }
        return result;
    }

    private static BoxStore buildStore(@NonNull Context context, @NonNull String packageName, BoxStoreBuilder storeBuilder) {
        BoxStore boxStore;
        if (TextUtils.equals(context.getPackageName(), packageName)) {
            boxStore = storeBuilder.buildDefault();
        } else {
            boxStore = storeBuilder.build();
        }
        return boxStore;
    }

    /**
     * 删除数据库文件
     */
    public static boolean deleteBoxDb(@NonNull Context context, @Nullable String dbName) {
        return FileUtils.deleteDir(boxDbPath(context, dbName));
    }

    /**
     * 数据库所在的文件夹
     */
    public static String boxDbPath(@NonNull Context context, @Nullable String dbName) {
        return boxPath(context) + File.separator + dbName;
    }

    public static String boxPath(@NonNull Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "objectbox";
    }

    /**
     * 获取BoxStore对象
     */
    public static BoxStore get() {
        return BoxStore.getDefault();
    }

    public static BoxStore get(@NonNull String packageName) {
        return boxStoreMap.get(packageName);
    }

    /**
     * 获取可操作的Box对象
     */
    public static <T> Box<T> box(Class<T> entityClass) {
        return get().boxFor(entityClass);
    }

    public static <T> Box<T> box(@NonNull String packageName, Class<T> entityClass) {
        return get(packageName).boxFor(entityClass);
    }

    public static void clear() {
        BoxStore.clearDefaultStore();
        boxStoreMap.clear();
    }
}

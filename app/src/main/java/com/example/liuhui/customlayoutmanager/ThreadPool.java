package com.example.liuhui.customlayoutmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by liuhui on 2016/10/29.
 */

public class ThreadPool {
    private ExecutorService mSercice;

    private static ThreadPool mPool;

    private ThreadPool(){
        mSercice = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static ThreadPool getInstance(){
        if (mPool == null){
            synchronized (ThreadPool.class){
                if (mPool == null){
                    mPool = new ThreadPool();
                }
            }
        }
        return mPool;
    }

    public void execute(Runnable runnable){
        mSercice.execute(runnable);
    }

    public void cancel(){
        mSercice.shutdownNow();
    }
}

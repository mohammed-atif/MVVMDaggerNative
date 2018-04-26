package com.zemosolabs.mindhive.daggermvvm.download_manager.implementations

import android.support.annotation.NonNull
import android.util.Log
import com.zemosolabs.mindhive.daggermvvm.download_manager.interfaces.FileDownloadListener
import com.zemosolabs.mindhive.daggermvvm.download_manager.interfaces.FileExecutorListener
import com.zemosolabs.mindhive.daggermvvm.download_manager.interfaces.Priority
import com.zemosolabs.mindhive.daggermvvm.download_manager.interfaces.SerialExecutor
import com.zemosolabs.mindhive.daggermvvm.service_providers.interfaces.IWebServiceProvider
import com.zemosolabs.mindhive.daggermvvm.services.interfaces.DownloadServiceCallback

/**
 * @author atif
 * Created on 24/04/18.
 */
class DownloadSerializer constructor(private val webServiceProvider: IWebServiceProvider, private val downloadTasks : PriorityQueue) : SerialExecutor, FileExecutorListener {

    override var downloadServiceCallback: DownloadServiceCallback? = null

    private val TAG = "DownloadSerializer"
    private var activeTask : DownloadRunnable? = null

    @Synchronized override fun addTask(downloadTask: List<DownloadTask>, fileDownloadListener: FileDownloadListener, priority: Priority, index : Int) {
        Log.d(TAG, "Task added with active task $activeTask")
        downloadTasks.add(index, DownloadRunnable(webServiceProvider, this, fileDownloadListener, downloadTask, priority))
        if(activeTask == null){
            scheduleNext()
        }
    }

    @Synchronized private fun scheduleNext() {
        activeTask = downloadTasks.poll() as DownloadRunnable
        if(activeTask != null){
            activeTask!!.run()
        }else{
            downloadServiceCallback?.onDownloadsCompleted()
        }
        Log.d(TAG, "Schedule next called with active task $activeTask")
    }

    override fun onDownloadCompleted(@NonNull downloadRunnable: DownloadRunnable) {
        if(downloadRunnable != activeTask){
            throw IllegalStateException("Only one active download is allowed")
        }else{
            Log.d(TAG, "Download Completed $downloadRunnable")
            scheduleNext()
        }
    }

    override fun onDownloadFailed(@NonNull downloadRunnable: DownloadRunnable, message : String?) {
        if(downloadRunnable != activeTask){
            throw IllegalStateException("Only one active download is allowed")
        }else{
            Log.e(TAG, "Download Failed $message $downloadRunnable")
        }
    }

}
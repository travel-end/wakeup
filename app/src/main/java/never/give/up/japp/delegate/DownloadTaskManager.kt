package never.give.up.japp.delegate

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.text.TextUtils
import android.util.SparseArray
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadConnectListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import never.give.up.japp.Japp
import never.give.up.japp.consts.Constants
import never.give.up.japp.event.GlobalSingle
import never.give.up.japp.event.PlaylistEvent
import never.give.up.japp.model.TasksManagerModel
import never.give.up.japp.ui.adapter.DownloadingAdapter
import never.give.up.japp.ui.download.DownloadingFragment
import never.give.up.japp.utils.DownloadLoader
import never.give.up.japp.utils.FileUtils
import never.give.up.japp.utils.execute
import java.io.File
import java.lang.ref.WeakReference

/**
 * @By Journey 2020/12/14
 * @Description
 */
object DownloadTaskManager {
    private var taskList = DownloadLoader.getDownloadingList()
    private val taskSparseArray = SparseArray<BaseDownloadTask>()
    private var connectListener:FileDownloadConnectListener?=null
    fun addTaskForViewHolder(task: BaseDownloadTask) {
        taskSparseArray.put(task.id,task)
    }

    fun removeTaskForViewHolder(id: Int) {
        taskSparseArray.remove(id)
    }
    fun updateViewHolder(id: Int, holder: DownloadingAdapter.TaskItemViewHolder) {
        if (taskSparseArray.get(id) != null) {
            taskSparseArray.get(id).tag = holder
        }
    }
    fun releaseTask() {
        taskSparseArray.clear()
    }

    private fun registerServiceConnectionListener(reference:WeakReference<DownloadingFragment>?) {
        if (connectListener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(connectListener)
        }
        connectListener = object :FileDownloadConnectListener() {
            override fun connected() {
                if (reference?.get() == null) {
                    return
                }
                reference.get()?.postNotifyDataChanged()
            }

            override fun disconnected() {
                if (reference?.get() == null) {
                    return
                }
                reference.get()?.postNotifyDataChanged()
            }
        }
        FileDownloader.getImpl().addServiceConnectListener(connectListener)
    }

    private fun unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(connectListener)
        connectListener = null
    }

    fun onCreate(reference: WeakReference<DownloadingFragment>) {
        if (!FileDownloader.getImpl().isServiceConnected) {
            FileDownloader.getImpl().bindService()
            registerServiceConnectionListener(reference)
        } else {
            registerServiceConnectionListener(reference)
        }
    }
    fun onDestroy() {
        unregisterServiceConnectionListener()
        releaseTask()
    }
    /**
     * 根据位置获取
     */
    operator fun get(position: Int): TasksManagerModel {
        return taskList[position]
    }

    /**
     * 根据model id获取对象
     */
    private fun getById(id: Int): TasksManagerModel? {
        for (model in taskList) {
            if (model.tid == id) {
                return model
            }
        }
        return null
    }
    fun isDownloaded(status: Int): Boolean {
        return status == FileDownloadStatus.completed.toInt()
    }

    fun getStatus(id: Int, path: String): Int {
        return FileDownloader.getImpl().getStatus(id, path).toInt()
    }

    fun getTotal(id: Int): Long {
        return FileDownloader.getImpl().getTotal(id)
    }

    fun getSoFar(id: Int): Long {
        return FileDownloader.getImpl().getSoFar(id)
    }


    fun getModelList(): List<TasksManagerModel> {
        return taskList
    }
    fun finishTask(tid: Int) {
        scanFileAsync(Japp.getInstance(), FileUtils.getMusicCacheDir())
        val data = execute {
            DownloadLoader.updateTask(tid)
            DownloadLoader.getDownloadingList()
        }
        taskList = data
    }

    /**
     * @param tid :下载任务唯一ID
     */
    fun addTask(tid: Int, mid: String?, name: String?, url: String?, path: String, isCached: Boolean): TasksManagerModel? {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(mid) || TextUtils.isEmpty(path)) {
            return null
        }

        val model = getById(tid)
        if (model != null) {
            return model
        }
        val newModel = DownloadLoader.addTask(tid, mid, name, url, path, isCached)
        if (newModel != null) {
            taskList.add(newModel)
        }
        return newModel
    }

    /**
     * 扫描文件夹
     */
    private fun scanFileAsync(ctx: Context, filePath: String?) {
//        LogUtil.d("NavigationHelper", "ACTION_MEDIA_SCANNER_SCAN_FILE$filePath")
        if (filePath.isNullOrEmpty()) return
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = Uri.fromFile(File(filePath))
        ctx.sendBroadcast(scanIntent)
        scanFileAsync(ctx)
    }

    fun scanFileAsync(ctx: Context) {
        //MediaScannerConnectionClient 是媒体扫描服务在MediaScannerConnection类中返回新添加文件的 uri  和 path
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = Uri.fromFile(File(FileUtils.getMusicDir()))
        ctx.sendBroadcast(scanIntent)
        MediaScannerConnection.scanFile(Japp.getInstance(), arrayOf(FileUtils.getMusicDir()), null,
            object : MediaScannerConnection.MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    // MediaScanner service 创建后回调
//                    LogUtil.d("NavigationHelper", "MediaScannerConnection onMediaScannerConnected ${FileUtils.getMusicDir()}")
                }

                override fun onScanCompleted(path: String?, uri: Uri?) {
                    // 当MediaScanner完成文件扫描后回调
//                    LogUtil.d("NavigationHelper", "MediaScannerConnection onScanCompleted $path")
//                    EventBus.getDefault().post(PlaylistEvent(Constants.PLAYLIST_LOCAL_ID))
                    GlobalSingle.playListChanged.value = PlaylistEvent(Constants.PLAYLIST_LOCAL_ID)
                }
            })
    }
}
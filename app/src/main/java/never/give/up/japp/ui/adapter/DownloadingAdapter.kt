package never.give.up.japp.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import never.give.up.japp.Japp
import never.give.up.japp.R
import never.give.up.japp.delegate.DownloadTaskManager
import never.give.up.japp.listener.FileDownloadListener
import never.give.up.japp.model.TasksManagerModel
import never.give.up.japp.utils.inflate
import java.io.File

/**
 * @By Journey 2020/12/14
 * @Description
 */
class DownloadingAdapter(var taskList: List<TasksManagerModel>) :
    RecyclerView.Adapter<DownloadingAdapter.TaskItemViewHolder>() {
    companion object {
        private val TAG = "TaskItemAdapter"
        val taskDownloadListener = FileDownloadListener()
    }

    inner class TaskItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var taskNameTv: TextView = itemView.findViewById(R.id.task_name_tv)
        var taskStatusTv: TextView = itemView.findViewById(R.id.task_status_tv)
        var taskPb: ProgressBar = itemView.findViewById(R.id.task_pb)
        var taskActionBtn: Button = itemView.findViewById(R.id.task_action_btn)

        /**
         * viewHolder position
         */
        var position1 = 0

        /**
         * download id
         */
        var id: Int = 0

//        internal var taskNameTv: TextView
//        internal var taskStatusTv: TextView
//        internal var taskPb: ProgressBar
//        internal var taskActionBtn: Button

        fun update(id: Int, position: Int) {
            this.id = id
            this.position1 = position
        }

        fun updateDownloaded() {
            taskPb.max = 1
            taskPb.progress = 1
            taskStatusTv.setText(R.string.tasks_manager_demo_status_completed)
            taskActionBtn.setText(R.string.delete)
            taskActionBtn.visibility = View.GONE
            taskPb.visibility = View.GONE
            DownloadTaskManager.finishTask(id)
        }

        fun updateNotDownloaded(status: Int, sofar: Long, total: Long) {
            if (sofar > 0 && total > 0) {
                val percent = sofar / total.toFloat()
                taskPb.max = 100
                taskPb.progress = (percent * 100).toInt()
            } else {
                taskPb.max = 1
                taskPb.progress = 0
            }

            when (status.toByte()) {
                FileDownloadStatus.error -> taskStatusTv.setText(R.string.tasks_manager_demo_status_error)
                FileDownloadStatus.paused -> taskStatusTv.setText(R.string.tasks_manager_demo_status_paused)
                else -> taskStatusTv.setText(R.string.tasks_manager_demo_status_not_downloaded)
            }
            taskActionBtn.setText(R.string.start)
        }

        fun updateDownloading(status: Int, sofar: Long, total: Long) {
            val percent = sofar / total.toFloat()
            taskPb.max = 100
            taskPb.progress = (percent * 100).toInt()

            when (status.toByte()) {
                FileDownloadStatus.pending -> taskStatusTv.setText(R.string.tasks_manager_demo_status_pending)
                FileDownloadStatus.started -> taskStatusTv.setText(R.string.start_download)
                FileDownloadStatus.connected -> taskStatusTv.setText(R.string.tasks_manager_demo_status_connected)
//                FileDownloadStatus.progress -> taskStatusTv.setText(R.string.tasks_manager_demo_status_progress)
                else -> taskStatusTv.text = Japp.getInstance().getString(
                    R.string.tasks_manager_demo_status_downloading, (percent * 100).toInt()
                )
            }

            taskActionBtn.setText(R.string.pause)
        }
    }

    private val taskActionOnClickListener = View.OnClickListener { v ->
        if (v.tag == null) {
            return@OnClickListener
        }
        val holder = v.tag as TaskItemViewHolder
        when ((v as TextView).text) {
            v.getResources().getString(R.string.pause) -> // to pause
                FileDownloader.getImpl().pause(holder.id)
            v.getResources().getString(R.string.start) -> {
                // to start
                val model = DownloadTaskManager[holder.adapterPosition]
                val task = FileDownloader.getImpl().create(model.url)
                    .setPath(model.path)
                    .setCallbackProgressTimes(100)
                    .setListener(taskDownloadListener)
                DownloadTaskManager.addTaskForViewHolder(task)
                holder.id = task.id
                DownloadTaskManager.updateViewHolder(holder.id, holder)

                task.start()
            }
            v.getResources().getString(R.string.delete) -> {
                // to delete
                File(DownloadTaskManager[holder.adapterPosition].path).delete()
                holder.taskActionBtn.isEnabled = true
                holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS.toInt(), 0, 0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        return TaskItemViewHolder(R.layout.item_downloading_music.inflate(parent))
    }

    override fun getItemCount() = taskList.size

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        val model = taskList[position]
        holder.taskActionBtn.setOnClickListener(taskActionOnClickListener)
        holder.update(model.tid, holder.adapterPosition)
        holder.taskActionBtn.tag = holder
        holder.taskNameTv.text = model.name

        DownloadTaskManager.updateViewHolder(holder.id, holder)

        holder.taskActionBtn.isEnabled = true
        if (FileDownloader.getImpl().isServiceConnected) {
            val status = DownloadTaskManager.getStatus(model.tid, model.path!!)
            if (status == FileDownloadStatus.pending.toInt() || status == FileDownloadStatus.started.toInt() ||
                status == FileDownloadStatus.connected.toInt()
            ) {
                // start task, but file not created yet
                holder.updateDownloading(
                    status,
                    DownloadTaskManager.getSoFar(model.tid),
                    DownloadTaskManager.getTotal(model.tid)
                )
            } else if (!File(model.path).exists() && !File(FileDownloadUtils.getTempPath(model.path)).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0)
            } else if (DownloadTaskManager.isDownloaded(status)) {
                // already downloaded and exist
//                LogUtil.e(TAG, "already downloaded and exist")
                holder.updateDownloaded()
            } else if (status == FileDownloadStatus.progress.toInt()) {
                // downloading
                holder.updateDownloading(
                    status,
                    DownloadTaskManager.getSoFar(model.tid),
                    DownloadTaskManager.getTotal(model.tid)
                )
            } else {
                // not start
                holder.updateNotDownloaded(
                    status,
                    DownloadTaskManager.getSoFar(model.tid),
                    DownloadTaskManager.getTotal(model.tid)
                )
            }
        } else {
            holder.taskStatusTv.setText(R.string.tasks_manager_demo_status_loading)
            holder.taskActionBtn.isEnabled = false
        }
    }
}
package never.give.up.japp.event

import never.give.up.japp.UnPeekLiveData

/**
 * @By Journey 2020/12/7
 * @Description
 */
object GlobalSingle {
    val playListChanged:UnPeekLiveData<PlaylistEvent> = UnPeekLiveData()

    val stateChanged:UnPeekLiveData<StatusChangedEvent> = UnPeekLiveData()

    val metaChanged:UnPeekLiveData<MetaChangedEvent> = UnPeekLiveData()
}
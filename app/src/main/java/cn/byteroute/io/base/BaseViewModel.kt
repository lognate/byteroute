package cn.byteroute.io.base

import androidx.lifecycle.*
import cn.byteroute.io.data.Action
import cn.byteroute.io.data.Response
import cn.byteroute.io.ext.observe
import kotlinx.coroutines.Dispatchers

class BaseViewModel : ViewModel() {
    val actionData: MutableLiveData<Action<*>> = MutableLiveData()
    var pageBehavior: PageBehavior? = null
    fun bind(pageBehavior: PageBehavior) {
        this.pageBehavior = pageBehavior
    }

    open fun onResume() {

    }

    open fun onPause() {

    }

    fun unBind() {
        pageBehavior = null
    }

    // livedata
    fun <K,V> request(
        isDialog: Boolean = true,
         change: (K) -> V,
        block: suspend () -> K
    ) =
        liveData(Dispatchers.Main.immediate) {
            try {
                if (isDialog) {
                    pageBehavior?.loading()
                }
                val data = block()
                var invoke = change.invoke(data)
                if (isDialog) {
                    pageBehavior?.close()
                }
                emit(invoke)
            } catch (exception: Exception) {
                exception.printStackTrace()
                emit(null)
                if (isDialog) {
                    pageBehavior?.close()
                }
            }

        }

    //msg livedata
    fun msgRequest(
        change: (Response<*>) -> String = { it.msg },
        isDialog: Boolean = true,
        block: suspend () -> Response<*>
    ) = request(isDialog, change, block)

    //data livedata
    fun <T> dataRequest(
        change: (Response<T>) -> T = { it.data },
        isDialog: Boolean = true,
        block: suspend () -> Response<T>
    ) = request(isDialog, change, block)

    //response 回调
    fun <T> dataResponse(
        owner: LifecycleOwner,
        block: suspend () -> Response<T>,
        callback: (T?) -> Unit,
        isDialog: Boolean = true
    ) {
        owner.observe(dataRequest(isDialog = isDialog,block = block)) {
            callback.invoke(it)
        }
    }

    fun <T> response(
        owner: LifecycleOwner,
        block: suspend () -> Response<T>,
        callback: (Response<T>?) -> Unit,
        isDialog: Boolean = true,
        change: (Response<T>) -> Response<T> = { it }
    ) {
        owner.observe(request(isDialog = isDialog,change = change, block = block)) {
            callback.invoke(it)
        }
    }

    //msg 回调
    fun msgResponse(
        owner: LifecycleOwner,
        block: suspend () -> Response<*>,
        callback: (String?) -> Unit,
        isDialog: Boolean = true
    ) {
        owner.observe(msgRequest(isDialog = isDialog,block = block)) {
            callback.invoke(it)
        }
    }
}
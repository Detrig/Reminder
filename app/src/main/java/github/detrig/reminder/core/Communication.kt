package github.detrig.corporatekanbanboard.core

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface Communication<T> {
    fun setData(data: List<T>)
    fun observe(owner: LifecycleOwner, observer: Observer<List<T>>)
    fun add(value: T)
}

open class BaseCommunication<T> : Communication<T> {
    private val liveData = SingleLiveEvent<List<T>>()

    override fun setData(data: List<T>) {
        liveData.postValue(data)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<List<T>>) {
        liveData.observe(owner, observer)
    }

    override fun add(value: T) {
        val list = ArrayList(liveData.value ?: emptyList())
        list.add(value)
        setData(list)
    }
}
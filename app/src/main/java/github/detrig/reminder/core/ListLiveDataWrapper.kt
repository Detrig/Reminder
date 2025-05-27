package github.detrig.corporatekanbanboard.core

import android.util.Log
import androidx.lifecycle.LiveData

interface ListLiveDataWrapper<T> {
    interface Read<T : Any> {
        fun liveData(): LiveData<List<T>>
    }

    interface Update<T : Any> {
        fun update(value: List<T>)
    }

    interface Add<T : Any> {
        fun add(value: T)
        fun addAll(value: List<T>)
    }

    interface Mutable<T : Any> : Read<T>, Update<T>

    interface All<T : Any> : Add<T>, Mutable<T>


    abstract class Abstract<T : Any>(
        protected val liveData: SingleLiveEvent<List<T>> = SingleLiveEvent()
    ) : All<T> {
        override fun liveData(): LiveData<List<T>> = liveData

        override fun update(value: List<T>) {
            liveData.value = value
        }

        override fun add(value: T) {
            val list = ArrayList(liveData.value ?: emptyList())
            list.add(value)
            update(list)
        }

        override fun addAll(value: List<T>) {
            val list = ArrayList(liveData.value ?: emptyList())
            list.addAll(value)
            update(list)
        }
    }

}
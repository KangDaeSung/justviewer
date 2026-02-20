package com.kds3393.just.justviewer2.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListViewModel<T> : ViewModel() {
    private var list = mutableStateListOf<T>()
    val stateList: SnapshotStateList<T>
        @Composable get() = remember { list }

    init {
        list.clear()
    }

    fun clearList() { list.clear() }
    fun add(obj:T) { list.add(obj) }
    fun set(newList:List<T>) {
        list.clear()
        list.addAll(newList)
    }
    fun remove(obj:T) { list.remove(obj) }
    fun get() : List<T> { return list }
}

class MapViewModel<K,V> : ViewModel() {
    private var list = mutableStateMapOf<K,V>()
    val stateList: SnapshotStateMap<K,V>
        @Composable get() = remember { list }

    init {
        list.clear()
    }

    fun clearList() { list.clear() }
    fun add(key:K,obj:V) { list[key] = obj }
    fun set(newList:Map<K,V>) {
        list.clear()
        list.putAll(newList)
    }
    fun remove(key:K) { list.remove(key) }
    fun get() : Map<K,V> { return list }
    fun size() : Int { return list.size}
}

data class StringData(var text: String = "")

class StringModel : ViewModel() {
    private val text = MutableStateFlow(StringData())
    val state: StateFlow<StringData> = text.asStateFlow()
}

data class BooleanData(var flag: Boolean = false)
class BooleanModel : ViewModel() {
    private val value = MutableStateFlow(BooleanData())
    val state: StateFlow<BooleanData> = value.asStateFlow()
}
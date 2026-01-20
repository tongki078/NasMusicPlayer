package com.nas.musicplayer

import kotlinx.coroutines.CoroutineScope

expect open class BaseViewModel() {
    val coroutineScope: CoroutineScope
}

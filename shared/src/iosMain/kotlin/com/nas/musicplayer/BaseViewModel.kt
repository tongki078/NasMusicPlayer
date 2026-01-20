package com.nas.musicplayer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

actual open class BaseViewModel {
    actual val coroutineScope: CoroutineScope = MainScope()
}

package com.example.camerascanmvi.scan.utils

import io.fotoapparat.preview.Frame
import io.fotoapparat.util.FrameProcessor
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

open class FrameProcessorOnSubscribe : ObservableOnSubscribe<Frame>, FrameProcessor {

    private var emitter: ObservableEmitter<Frame>? = null

    override fun subscribe(emitter: ObservableEmitter<Frame>) {
        emitter.setCancellable { this.emitter = null }
        this.emitter = emitter
    }

    override fun invoke(frame: Frame) {
        emitter?.onNext(frame)
    }
}
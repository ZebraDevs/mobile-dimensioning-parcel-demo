package com.nilac.zebra.mobiledimensioningparceldemo.models

class Event<T>(content: T?) {

    private val mContent: T?
    private var hasBeenHandled = false

    init {
        mContent = content
    }

    val contentIfNotHandled: T?
        get() = if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            mContent
        }

    fun hasBeenHandled(): Boolean {
        return hasBeenHandled
    }
}
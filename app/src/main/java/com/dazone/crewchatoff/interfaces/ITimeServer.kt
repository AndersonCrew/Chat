package com.dazone.crewchatoff.interfaces

interface ITimeServer {
    fun onGetTimeSuccess(mili: Long)
}

interface ILoadImage {
    fun onLoaded()
}
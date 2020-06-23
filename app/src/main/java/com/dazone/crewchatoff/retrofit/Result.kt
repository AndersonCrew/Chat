package com.dazone.crewchatoff.retrofit

sealed class Result<out T: Any> {
    data class Success<out T: Any>(val response: T) : Result<T>()
    data class Error(val exception: String) : Result<Nothing>()
}
package com.dazone.crewchatoff.retrofit

import com.dazone.crewchatoff.constant.Config
import com.dazone.crewchatoff.interfaces.Urls
import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DazoneService {

    @POST(Urls.URL_ROOT_2)
    fun getChatMessageList(@Body param: JsonObject): Observable<Response<JsonObject>>

    @POST(Urls.URL_ROOT_2)
    fun updateMessageUnreadCount(@Body param: JsonObject): Observable<Response<JsonObject>>

    @POST(Urls.URL_ROOT_2)
    fun getMessageUnreadCount(@Body param: JsonObject): Observable<Response<JsonObject>>

    @POST(Urls.URL_ROOT_2)
    fun sendAttachFile(@Body param: JsonObject): Observable<Response<JsonObject>>

    @POST(Urls.URL_ROOT_2)
    fun sendNormalMessage(@Body param: JsonObject): Observable<Response<JsonObject>>

    @POST(Urls.URL_ROOT_2)
    fun checkHasCallUnreadCount(@Body param: JsonObject): Observable<Response<JsonObject>>
}
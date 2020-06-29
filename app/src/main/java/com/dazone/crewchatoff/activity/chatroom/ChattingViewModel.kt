package com.dazone.crewchatoff.activity.chatroom

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import android.view.View
import com.dazone.crewchatoff.activity.base.BaseViewModel
import com.dazone.crewchatoff.constant.Statics
import com.dazone.crewchatoff.database.ChatMessageDBHelper
import com.dazone.crewchatoff.dto.ChattingDto
import com.dazone.crewchatoff.dto.ErrorDto
import com.dazone.crewchatoff.dto.MessageUnreadCountDTO
import com.dazone.crewchatoff.fragment.CurrentChatListFragment
import com.dazone.crewchatoff.fragment.RecentFavoriteFragment
import com.dazone.crewchatoff.interfaces.Urls
import com.dazone.crewchatoff.retrofit.RetrofitFactory
import com.dazone.crewchatoff.utils.CrewChatApplication
import com.dazone.crewchatoff.utils.TimeUtils
import com.dazone.crewchatoff.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.sql.Time
import java.util.*

class ChattingViewModel : BaseViewModel() {
    var listChatting: MutableLiveData<List<ChattingDto>> = MutableLiveData()
    var listChattingLoadmore: MutableLiveData<List<ChattingDto>> = MutableLiveData()
    var listMessageUnReadCount: MutableLiveData<List<MessageUnreadCountDTO>> = MutableLiveData()
    var hasLoadNewMessage = false
    private var disposables = CompositeDisposable()

    fun getChatListFirst(roomNo: Long, userID: Int) {
        val listObservable = Single.just(ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST))
        val dispo: Disposable = listObservable
                .subscribeOn(Schedulers.io())
                .subscribe { list ->
                    if (!list.isNullOrEmpty()) {
                        listChatting.postValue(list)
                    }

                    if (Utils.isNetworkAvailable()) {
                        val listChatMessage = ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST)
                        var baseDate = CrewChatApplication.getInstance().timeServer
                        var mesType = 1
                        if (listChatMessage.size > 0) {
                            baseDate = listChatMessage[listChatMessage.size - 1].strRegDate
                            mesType = ChatMessageDBHelper.AFTER
                        }

                        getChatList(baseDate, roomNo, mesType, userID)
                    } else {
                        showLoading(false)
                    }
                }

        disposables.add(dispo)
    }

    fun loadMoreLocal(roomNo: Long, messageNo: Long) {
        val listObservable = Single.just(ChatMessageDBHelper.getMsgSession(roomNo, messageNo, ChatMessageDBHelper.BEFORE))
        val dispo: Disposable = listObservable
                .subscribeOn(Schedulers.io())
                .subscribe { list ->
                    if (!list.isNullOrEmpty()) {
                        listChattingLoadmore.postValue(list)
                    }
                }

        disposables.add(dispo)
    }


    fun getChatList(regDate: String, roomNo: Long, type: Int, userID: Int) {
        val params = JsonObject()
        params.addProperty("command", Urls.URL_GET_CHAT_MSG_SECTION_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("getType", type)
        param1.addProperty("baseDate", regDate)

        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory.apiService.getChatMessageList(params)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showLoading(true) }
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessful) {
                        hasLoadNewMessage = true
                        val body = it.body()?.get("d") as JsonObject
                        val success = body.get("success").toString()
                        if (success == "true") {
                            val data = body.get("data").toString()
                            val listType = object : TypeToken<List<ChattingDto>>() {}.type
                            val list = Gson().fromJson<List<ChattingDto>>(data, listType)
                            if (type == ChatMessageDBHelper.BEFORE) {
                                listChattingLoadmore.postValue(list)
                            } else if (!list.isNullOrEmpty()) {
                                listChatting.postValue(list)

                                //Update MessageUnreadCount
                                updateMessageUnReadCount(roomNo, userID, list[list.size - 1].strRegDate)
                                CurrentChatListFragment.fragment?.updateRoomUnread(roomNo)
                                RecentFavoriteFragment.instance?.updateRoomUnread(roomNo)
                            }
                        } else {
                            showError(ErrorDto().setMessage("Cannot fetch message form Server!"))
                        }
                    }
                }, {
                    showError(ErrorDto().setMessage("Cannot fetch message form Server!"))
                }))
    }

    fun updateMessageUnReadCount(roomNo: Long, userNo: Int, baseDate: String) {
        val params = JsonObject()
        params.addProperty("command", Urls.URL_UPDATE_MESSAGE_UNREAD_COUNT_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("userNo", userNo)
        param1.addProperty("baseDate", baseDate)
        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory.apiService.updateMessageUnreadCount(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Success")
                    } else Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Fail")
                }, {
                    Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Fail")
                }))
    }

    fun getMessageUnReadCount(roomNo: Long, baseDate: String) {
        val params = JsonObject()
        params.addProperty("command", Urls.URL_GET_MESSAGE_UNREAD_COUNT_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("baseDate", baseDate)
        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory.apiService.getMessageUnreadCount(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        val body = it.body()?.get("d") as JsonObject
                        val success = body.get("success").toString()
                        if (success == "true") {
                            val data = body.get("data").toString()
                            val listType = object : TypeToken<List<MessageUnreadCountDTO>>() {}.type
                            val list = Gson().fromJson<List<MessageUnreadCountDTO>>(data, listType)
                            listMessageUnReadCount.postValue(list)
                        } else {
                            Log.d("CHATTING_ROOM", "GetMessageUnReadCount Fail")
                        }
                        Log.d("CHATTING_ROOM", "GetMessageUnReadCount Success")
                    } else Log.d("CHATTING_ROOM", "GetMessageUnReadCount Fail")
                }, {
                    Log.d("CHATTING_ROOM", "GetMessageUnReadCount Fail")
                }))
    }

    override fun onCleared() {
        disposables.clear()
    }
}
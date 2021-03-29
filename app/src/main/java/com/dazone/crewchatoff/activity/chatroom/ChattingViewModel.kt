package com.dazone.crewchatoff.activity.chatroom

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.util.Log
import com.dazone.crewchatoff.activity.base.BaseViewModel
import com.dazone.crewchatoff.constant.Constants
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
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChattingViewModel : BaseViewModel() {
    var listChatting: MutableLiveData<List<ChattingDto>> = MutableLiveData()
    var listChattingLoadmore: MutableLiveData<List<ChattingDto>> = MutableLiveData()
    var listMessageUnReadCount: MutableLiveData<List<MessageUnreadCountDTO>> = MutableLiveData()
    var attachFile: MutableLiveData<ChattingDto> = MutableLiveData()
    var dtoFailed: MutableLiveData<ChattingDto> = MutableLiveData()
    var normalMessage: MutableLiveData<ChattingDto> = MutableLiveData()
    var sendActionShare: MutableLiveData<Boolean> = MutableLiveData()
    var hasLoadNewMessage = false
    private var disposables = CompositeDisposable()

    fun getChatListFirst(roomNo: Long, userID: Int) {
        val listObservable = Single.just(ChatMessageDBHelper.getMsgSession(roomNo, 0, ChatMessageDBHelper.FIRST, ""))
        val dispo: Disposable = listObservable
                .subscribeOn(Schedulers.io())
                .subscribe { list ->
                    if (!list.isNullOrEmpty()) {
                        listChatting.postValue(list)
                    }

                    if (Utils.isNetworkAvailable()) {
                        var baseDate = CrewChatApplication.getInstance().timeServer
                        var mesType = 1
                        if (list.size > 0) {
                            baseDate = list[list.size - 1].strRegDate
                            mesType = ChatMessageDBHelper.AFTER
                        }

                        val strBaseDate = if (list.size > 0) list[list.size - 1].strRegDate else null
                        getChatList(baseDate, roomNo, mesType, userID, strBaseDate)
                    } else {
                        showLoading(false)
                    }
                }

        disposables.add(dispo)
    }

    fun loadMoreLocal(roomNo: Long, messageNo: Long, type: Int, strFilter: String) {
        val listObservable = Single.just(ChatMessageDBHelper.getMsgSession(roomNo, messageNo, type, strFilter))
        val dispo: Disposable = listObservable
                .subscribeOn(Schedulers.io())
                .subscribe { list ->
                    if (!list.isNullOrEmpty()) {
                        listChattingLoadmore.postValue(list)
                    }
                }

        disposables.add(dispo)
    }


    fun getChatList(regDate: String, roomNo: Long, type: Int, userID: Int, strRegDate: String?) {
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

        disposables.add(RetrofitFactory().apiService.getChatMessageList(params)
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
                            } else {
                                if (!list.isNullOrEmpty()) {
                                    listChatting.postValue(list)

                                    //Update MessageUnreadCount
                                    updateMessageUnReadCount(roomNo, userID, list[list.size - 1].strRegDate, false)
                                    CurrentChatListFragment.fragment?.updateRoomUnread(roomNo)
                                    RecentFavoriteFragment.instance?.updateRoomUnread(roomNo)
                                } else {
                                    val unwrappedStr = strRegDate ?: return@subscribe
                                    getMessageUnReadCount(roomNo, unwrappedStr)
                                }

                                sendActionShare.postValue(true)
                            }
                        } else {
                            Log.d("CHAT ROOM", "Cannot fetch message form Server!")
                        }
                    }
                }, {
                    Log.d("CHAT ROOM", "Cannot fetch message form Server!")
                }))
    }

    fun updateMessageUnReadCount(roomNo: Long, userNo: Int, baseDate: String, fromNotification: Boolean) {
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

        disposables.add(RetrofitFactory().apiService.updateMessageUnreadCount(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Success")
                        if (fromNotification) {
                            getMessageUnReadCount(roomNo, baseDate)
                        }
                    } else Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Fail")
                }, {
                    Log.d("CHATTING_ROOM", "UpdateMessageUnReadCount Fail")
                }))
    }

    fun getMessageUnReadCount(roomNo: Long, baseDate: String) {
        if(CrewChatApplication.getInstance().prefs.getBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, false)) {
            return
        }

        val params = JsonObject()
        params.addProperty("command", Urls.URL_GET_MESSAGE_UNREAD_COUNT_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("baseDate", baseDate)
        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory().apiService.getMessageUnreadCount(params)
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

    @SuppressLint("UseSparseArrays")
    fun sendAttachFile(attachNo: Int, roomNo: Long, chattingDto: ChattingDto) {
        val formatter = SimpleDateFormat(Statics.yyyy_MM_dd_HH_mm_ss_SSS, Locale.getDefault())
        val serverDate = formatter.parse(chattingDto.strRegDate).time - CrewChatApplication.getInstance().prefs.getLongValue(Statics.TIME_SERVER_MILI, 0)
        val params = JsonObject()
        params.addProperty("command", Urls.URL_SEND_ATTACH_FILE_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("attachNo", attachNo)
        param1.addProperty("regDate", formatter.format(serverDate))
        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory().apiService.sendAttachFile(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        val body = it.body()?.get("d") as JsonObject
                        val success = body.get("success").toString()
                        if (success == "true") {
                            val data = body.get("data").toString()
                            val dto = Gson().fromJson<ChattingDto>(data, ChattingDto::class.java)
                            dto.lastedMsgAttachType = chattingDto.lastedMsgAttachType
                            dto.lastedMsgType = chattingDto.lastedMsgType
                            dto.setmType(chattingDto.getmType())
                            dto.isSendTemp = true
                            dto.attachFileName = chattingDto.attachFileName
                            dto.attachFilePath = chattingDto.attachFilePath
                            dto.attachFileSize = chattingDto.attachFileSize
                            dto.attachFileType = chattingDto.attachFileType
                            dto.positionUploadImage = chattingDto.positionUploadImage
                            dto.attachNo = attachNo
                            dto.setmType(chattingDto.getmType())

                            attachFile.postValue(dto)

                        } else {
                            dtoFailed.postValue(chattingDto)
                        }

                        Log.d("CHATTING_ROOM", "sendAttachFile Success")
                    } else {
                        dtoFailed.postValue(chattingDto)
                    }
                }, {
                    dtoFailed.postValue(chattingDto)
                }))
    }

    fun sendNormalMessage(roomNo: Long, message: String, chattingDto: ChattingDto) {
        val formatter = SimpleDateFormat(Statics.yyyy_MM_dd_HH_mm_ss_SSS, Locale.getDefault())
        val serverDate = formatter.parse(chattingDto.strRegDate).time - CrewChatApplication.getInstance().prefs.getLongValue(Statics.TIME_SERVER_MILI, 0)
        val params = JsonObject()
        params.addProperty("command", Urls.URL_SEND_CHAT_TIME)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        val param1 = JsonObject()
        param1.addProperty("roomNo", roomNo)
        param1.addProperty("message", message)
        param1.addProperty("regDate", formatter.format(serverDate))
        params.addProperty("reqJson", Gson().toJson(param1))

        disposables.add(RetrofitFactory().apiService.sendNormalMessage(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        val body = it.body()?.get("d") as JsonObject
                        val success = body.get("success").toString()
                        if (success == "true") {
                            val data = body.get("data").toString()
                            val dto = Gson().fromJson<ChattingDto>(data, ChattingDto::class.java)
                            chattingDto.isHasSent = true
                            chattingDto.messageNo = dto.messageNo
                            chattingDto.unReadCount = dto.unReadCount
                            chattingDto.regDate = dto.regDate
                            chattingDto.isSendding = false
                            chattingDto.isHasSent = true

                            normalMessage.postValue(chattingDto)
                        } else {
                            dtoFailed.postValue(chattingDto)
                        }

                        Log.d("CHATTING_ROOM", "SendNormal Message Success")
                    } else {
                        dtoFailed.postValue(chattingDto)
                        Log.d("CHATTING_ROOM", "SendNormal Message Fail")
                    }
                }, {
                    dtoFailed.postValue(chattingDto)
                    Log.d("CHATTING_ROOM", "SendNormal Message Fail")
                }))
    }

    fun checkHasCallUnreadCount() {
        val params = JsonObject()
        params.addProperty("command", Urls.URL_CHECK_HAS_CALL_UNREAD_COUNT)
        params.addProperty("sessionId", CrewChatApplication.getInstance().prefs.getaccesstoken())
        params.addProperty("languageCode", Locale.getDefault().language.toUpperCase())
        params.addProperty("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes())

        disposables.add(RetrofitFactory().apiService.checkHasCallUnreadCount(params)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        val body = it.body()?.get("d") as JsonObject
                        val success = body.get("success").toString()
                        if (success == "true") {
                            val data = body.get("data").toString()
                            CrewChatApplication.getInstance().prefs.putBooleanValue(Constants.HAS_CALL_UNREAD_COUNT, !(data == "true"))

                        } else {
                            //dtoFailed.postValue(chattingDto)
                        }

                        Log.d("CHATTING_ROOM", "SendNormal Message Success")
                    } else {
                        //dtoFailed.postValue(chattingDto)
                        Log.d("CHATTING_ROOM", "SendNormal Message Fail")
                    }
                }, {
                    //dtoFailed.postValue(chattingDto)
                    Log.d("CHATTING_ROOM", "SendNormal Message Fail")
                }))
    }
}
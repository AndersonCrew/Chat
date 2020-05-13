package com.dazone.crewchatoff.activity.chatting

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.dazone.crewchatoff.HTTPs.HttpRequest
import com.dazone.crewchatoff.R
import com.dazone.crewchatoff.activity.MainActivity
import com.dazone.crewchatoff.database.ChatMessageDBHelper
import com.dazone.crewchatoff.dto.ChattingDto
import com.dazone.crewchatoff.dto.ErrorDto
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack
import com.dazone.crewchatoff.utils.CrewChatApplication
import com.dazone.crewchatoff.utils.Utils


class ChatttingViewModel : ViewModel() {
    var title: MutableLiveData<String>? = null
    var status: MutableLiveData<String>? = null
    var leaveGroupSuccess: MutableLiveData<Boolean>? = null
    var error: MutableLiveData<ErrorDto>? = null
    private var roomNo: Long? = null

    private val myId: Long by lazy { Utils.getCurrentId().toLong() }

    init {
        title = MutableLiveData()
        status = MutableLiveData()
        leaveGroupSuccess = MutableLiveData()
    }

    fun getHeader(chattingDto: ChattingDto) {
        var mTitle = ""

        if (!chattingDto.userNos.isNullOrEmpty()) {
            chattingDto.listTreeUser.map {
                mTitle += it.NameEN + ","
            }
        }

        title?.value = mTitle.substring(0, mTitle.length - 1)

        status?.value = if (chattingDto.userNos.size > 2) CrewChatApplication.getInstance().resources.getString(R.string.room_info_participant_count, chattingDto.userNos?.size.toString()) else ""

        roomNo = chattingDto.roomNo
    }

    fun leaveGroup() {
        val unwrappedRoomNo = roomNo ?: return
        HttpRequest.getInstance().DeleteChatRoomUser(unwrappedRoomNo, myId, object : BaseHTTPCallBack {
            override fun onHTTPSuccess() {
                // delete local db this room
                ChatMessageDBHelper.deleteMessageByLocalRoomNo(unwrappedRoomNo)
                leaveGroupSuccess?.value = true
            }

            override fun onHTTPFail(errorDto: ErrorDto) {
                error?.value = errorDto
            }
        })
    }
}
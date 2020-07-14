package com.dazone.crewchatoff.interfaces

import com.dazone.crewchatoff.dto.ChattingDto

interface ILoadImage {
    fun onLoaded(chattingDto: ChattingDto)
}
package com.innov.geotracking.utils.enum


enum class MsgTypes(val value: Int){
    SUCCESS(1),
    ERROR(2),
    WARNING(3)
}

enum class MsgDuration(val value: Int){
    SHORT(1),
    LONG(2)
}
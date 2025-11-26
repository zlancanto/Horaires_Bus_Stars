package com.example.horairebusmihanbot.exception

fun <T> requireNonNull(obj: T?, message: String) : T {
    if (obj == null) {
        throw NullPointerException(message)
    }
    return obj
}
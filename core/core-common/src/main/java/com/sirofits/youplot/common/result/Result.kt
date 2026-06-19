package com.sirofits.youplot.common.result

sealed class YouPlotResult<out T> {
    data class Success<T>(val data: T) : YouPlotResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : YouPlotResult<Nothing>()
    data object Loading : YouPlotResult<Nothing>()
}

inline fun <T> YouPlotResult<T>.onSuccess(block: (T) -> Unit): YouPlotResult<T> {
    if (this is YouPlotResult.Success) block(data)
    return this
}

inline fun <T> YouPlotResult<T>.onError(block: (String) -> Unit): YouPlotResult<T> {
    if (this is YouPlotResult.Error) block(message)
    return this
}

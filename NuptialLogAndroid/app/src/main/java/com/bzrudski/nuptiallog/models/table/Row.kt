package com.bzrudski.nuptiallog.models.table

data class Row<T, M: RowModifier> (
    val headerId:Int,
    val content: T,
    val modifier: M,
    val isStringResource: Boolean = false
)
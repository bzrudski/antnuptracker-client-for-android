package com.bzrudski.nuptiallog.models.table

data class Table<M: RowModifier>(private val rows:ArrayList<Row<Any, M>> = ArrayList()):Iterable<Row<Any, M>> {

    val size: Int get() = rows.size

    operator fun get(i:Int): Row<Any, M>{
        return rows[i]
    }

    override fun iterator(): Iterator<Row<Any, M>> {
        return rows.iterator()
    }

    fun insertRows(i: Int, vararg newRows: Row<Any, M>) {
        rows.addAll(i, newRows.asList())
    }

    fun addRow(row: Row<Any, M>){
        rows.add(row)
    }

    fun getFirstIndexOfHeader(headerId:Int): Int {
        return rows.indexOfFirst { it.headerId == headerId }
    }

    fun extend(table: Table<M>){
        rows.addAll(table.rows)
    }

}
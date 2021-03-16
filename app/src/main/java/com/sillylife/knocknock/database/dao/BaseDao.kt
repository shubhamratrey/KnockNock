package com.sillylife.knocknock.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj:T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg obj:T)

    @Delete
    fun delete(obj:T): Int

    @Update
    fun update(obj:T): Int

}
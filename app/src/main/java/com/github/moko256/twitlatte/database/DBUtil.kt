/*
 * Copyright 2015-2019 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by moko256 on 2019/03/07.
 *
 * @author moko256
 */

fun SQLiteDatabase.addColumn(tableName: String, columnName: String, defaultValue: String? = null) {
    execSQL("alter table $tableName add column $columnName")
    defaultValue?.let {
        execSQL("update $tableName set $columnName=$defaultValue")
    }
}

fun Cursor.getBoolean(index: Int) = getInt(index) == 1

inline fun SQLiteOpenHelper.write(action: SQLiteDatabase.() -> Unit) {
    writableDatabase.also {
        action(it)
    }.close()
}

inline fun SQLiteOpenHelper.transaction(action: SQLiteDatabase.() -> Unit) {
    writableDatabase.apply {
        beginTransaction()
        try {
            action()
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }.close()
}
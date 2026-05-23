package com.example.leanmasscalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "leanmass_db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USERS = "users"
        private const val USER_ID = "id"
        private const val USER_NAME = "name"
        private const val USER_EMAIL = "email"
        private const val USER_PASSWORD_HASH = "password"

        private const val TABLE_HISTORY = "history"
        private const val HISTORY_ID = "id"
        private const val HISTORY_USER_EMAIL = "user_email"
        private const val HISTORY_GENDER = "gender"
        private const val HISTORY_WEIGHT = "weight"
        private const val HISTORY_HEIGHT = "height"
        private const val HISTORY_LBM = "lbm"
        private const val HISTORY_STATUS = "status"
        private const val HISTORY_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_NAME TEXT NOT NULL,
                $USER_EMAIL TEXT UNIQUE NOT NULL,
                $USER_PASSWORD_HASH TEXT NOT NULL
            )
        """.trimIndent()

        val createHistoryTable = """
            CREATE TABLE $TABLE_HISTORY (
                $HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $HISTORY_USER_EMAIL TEXT NOT NULL,
                $HISTORY_GENDER TEXT NOT NULL,
                $HISTORY_WEIGHT REAL NOT NULL,
                $HISTORY_HEIGHT REAL NOT NULL,
                $HISTORY_LBM REAL NOT NULL,
                $HISTORY_STATUS TEXT NOT NULL,
                $HISTORY_DATE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            hashExistingUserPasswords(db)
            return
        }

        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase

        if (isEmailExists(email)) {
            return false
        }

        val values = ContentValues().apply {
            put(USER_NAME, name)
            put(USER_EMAIL, email)
            put(USER_PASSWORD_HASH, hashPassword(password))
        }

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val passwordHash = hashPassword(password)

        val cursor = db.rawQuery(
            "SELECT $USER_ID FROM $TABLE_USERS WHERE $USER_EMAIL = ? AND $USER_PASSWORD_HASH = ?",
            arrayOf(email, passwordHash)
        )

        val exists = cursor.count > 0
        cursor.close()

        return exists
    }

    private fun isEmailExists(email: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $USER_EMAIL = ?",
            arrayOf(email)
        )

        val exists = cursor.count > 0
        cursor.close()

        return exists
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    private fun hashExistingUserPasswords(db: SQLiteDatabase) {
        val cursor = db.rawQuery(
            "SELECT $USER_ID, $USER_PASSWORD_HASH FROM $TABLE_USERS",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID))
                val savedPassword = cursor.getString(
                    cursor.getColumnIndexOrThrow(USER_PASSWORD_HASH)
                )

                if (!isSha256Hash(savedPassword)) {
                    val values = ContentValues().apply {
                        put(USER_PASSWORD_HASH, hashPassword(savedPassword))
                    }
                    db.update(TABLE_USERS, values, "$USER_ID = ?", arrayOf(userId.toString()))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
    }

    private fun isSha256Hash(value: String): Boolean {
        return value.length == 64 && value.all { character ->
            character in '0'..'9' ||
                character in 'a'..'f' ||
                character in 'A'..'F'
        }
    }

    fun addHistory(
        userEmail: String,
        gender: String,
        weight: Double,
        height: Double,
        lbm: Double,
        status: String,
        date: String
    ): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(HISTORY_USER_EMAIL, userEmail)
            put(HISTORY_GENDER, gender)
            put(HISTORY_WEIGHT, weight)
            put(HISTORY_HEIGHT, height)
            put(HISTORY_LBM, lbm)
            put(HISTORY_STATUS, status)
            put(HISTORY_DATE, date)
        }

        val result = db.insert(TABLE_HISTORY, null, values)
        return result != -1L
    }

    fun getHistory(userEmail: String): ArrayList<HistoryItem> {
        val historyList = ArrayList<HistoryItem>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HISTORY WHERE $HISTORY_USER_EMAIL = ? ORDER BY $HISTORY_ID DESC",
            arrayOf(userEmail)
        )

        if (cursor.moveToFirst()) {
            do {
                val item = HistoryItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(HISTORY_ID)),
                    gender = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_GENDER)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(HISTORY_WEIGHT)),
                    height = cursor.getDouble(cursor.getColumnIndexOrThrow(HISTORY_HEIGHT)),
                    lbm = cursor.getDouble(cursor.getColumnIndexOrThrow(HISTORY_LBM)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_STATUS)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_DATE))
                )
                historyList.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return historyList
    }

    fun deleteHistory(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_HISTORY, "$HISTORY_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun clearHistory(userEmail: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_HISTORY, "$HISTORY_USER_EMAIL = ?", arrayOf(userEmail))
        return result > 0
    }
}

data class HistoryItem(
    val id: Int,
    val gender: String,
    val weight: Double,
    val height: Double,
    val lbm: Double,
    val status: String,
    val date: String
)

/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-11-17
 */

package org.ole.planet.myplanet.lite.profile

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "user_profile.db"
private const val DATABASE_VERSION = 4
private const val TABLE_PROFILE = "user_profile"
private const val COLUMN_ID = "id"
private const val COLUMN_USERNAME = "username"
private const val COLUMN_FIRST_NAME = "first_name"
private const val COLUMN_MIDDLE_NAME = "middle_name"
private const val COLUMN_LAST_NAME = "last_name"
private const val COLUMN_EMAIL = "email"
private const val COLUMN_LANGUAGE = "language"
private const val COLUMN_PHONE_NUMBER = "phone_number"
private const val COLUMN_BIRTH_DATE = "birth_date"
private const val COLUMN_GENDER = "gender"
private const val COLUMN_LEVEL = "level"
private const val COLUMN_AVATAR = "avatar"
private const val COLUMN_REVISION = "revision"
private const val COLUMN_DERIVED_KEY = "derived_key"
private const val COLUMN_RAW_DOCUMENT = "raw_document"
private const val COLUMN_IS_USER_ADMIN = "is_user_admin"
private const val PROFILE_ROW_ID = 1

class UserProfileDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PROFILE (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_FIRST_NAME TEXT,
                $COLUMN_MIDDLE_NAME TEXT,
                $COLUMN_LAST_NAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_LANGUAGE TEXT,
                $COLUMN_PHONE_NUMBER TEXT,
                $COLUMN_BIRTH_DATE TEXT,
                $COLUMN_GENDER TEXT,
                $COLUMN_LEVEL TEXT,
                $COLUMN_AVATAR BLOB,
                $COLUMN_REVISION TEXT,
                $COLUMN_DERIVED_KEY TEXT,
                $COLUMN_RAW_DOCUMENT TEXT,
                $COLUMN_IS_USER_ADMIN INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_PROFILE ADD COLUMN $COLUMN_REVISION TEXT")
            db.execSQL("ALTER TABLE $TABLE_PROFILE ADD COLUMN $COLUMN_DERIVED_KEY TEXT")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_PROFILE ADD COLUMN $COLUMN_RAW_DOCUMENT TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_PROFILE ADD COLUMN $COLUMN_IS_USER_ADMIN INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun saveProfile(profile: UserProfile) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PROFILE, null, null)
            val values = ContentValues().apply {
                put(COLUMN_ID, PROFILE_ROW_ID)
                put(COLUMN_USERNAME, profile.username)
                put(COLUMN_FIRST_NAME, profile.firstName)
                put(COLUMN_MIDDLE_NAME, profile.middleName)
                put(COLUMN_LAST_NAME, profile.lastName)
                put(COLUMN_EMAIL, profile.email)
                put(COLUMN_LANGUAGE, profile.language)
                put(COLUMN_PHONE_NUMBER, profile.phoneNumber)
                put(COLUMN_BIRTH_DATE, profile.birthDate)
                put(COLUMN_GENDER, profile.gender)
                put(COLUMN_LEVEL, profile.level)
                put(COLUMN_AVATAR, profile.avatarImage)
                put(COLUMN_REVISION, profile.revision)
                put(COLUMN_DERIVED_KEY, profile.derivedKey)
                put(COLUMN_RAW_DOCUMENT, profile.rawDocument)
                put(COLUMN_IS_USER_ADMIN, if (profile.isUserAdmin) 1 else 0)
            }
            db.insertOrThrow(TABLE_PROFILE, null, values)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getProfile(): UserProfile? {
        val db = readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_PROFILE,
                null,
                "$COLUMN_ID = ?",
                arrayOf(PROFILE_ROW_ID.toString()),
                null,
                null,
                null,
                "1"
            )
            if (cursor.moveToFirst()) {
                UserProfile(
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    firstName = cursor.getStringOrNull(COLUMN_FIRST_NAME),
                    middleName = cursor.getStringOrNull(COLUMN_MIDDLE_NAME),
                    lastName = cursor.getStringOrNull(COLUMN_LAST_NAME),
                    email = cursor.getStringOrNull(COLUMN_EMAIL),
                    language = cursor.getStringOrNull(COLUMN_LANGUAGE),
                    phoneNumber = cursor.getStringOrNull(COLUMN_PHONE_NUMBER),
                    birthDate = cursor.getStringOrNull(COLUMN_BIRTH_DATE),
                    gender = cursor.getStringOrNull(COLUMN_GENDER),
                    level = cursor.getStringOrNull(COLUMN_LEVEL),
                    avatarImage = cursor.getBlobOrNull(COLUMN_AVATAR),
                    revision = cursor.getStringOrNull(COLUMN_REVISION),
                    derivedKey = cursor.getStringOrNull(COLUMN_DERIVED_KEY),
                    rawDocument = cursor.getStringOrNull(COLUMN_RAW_DOCUMENT),
                    isUserAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_USER_ADMIN)) != 0
                )
            } else {
                null
            }
        } finally {
            cursor?.close()
        }
    }

    fun clearProfile() {
        val db = writableDatabase
        db.delete(TABLE_PROFILE, null, null)
    }

    private fun Cursor.getStringOrNull(columnName: String): String? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getString(index)
    }

    private fun Cursor.getBlobOrNull(columnName: String): ByteArray? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getBlob(index)
    }

    companion object {
        @Volatile
        private var instance: UserProfileDatabase? = null

        fun getInstance(context: Context): UserProfileDatabase {
            return instance ?: synchronized(this) {
                instance ?: UserProfileDatabase(context).also { instance = it }
            }
        }
    }
}

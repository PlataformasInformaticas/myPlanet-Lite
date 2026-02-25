/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-12-19
 */

package org.ole.planet.myplanet.lite.dashboard

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import org.ole.planet.myplanet.lite.dashboard.DashboardSurveySubmissionsRepository.SurveySubmission
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardSurveyOutboxStore(
    context: Context,
    moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build(),
) : SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val submissionAdapter = moshi.adapter(SurveySubmission::class.java)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SUBMISSIONS(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SURVEY_ID TEXT,
                $COLUMN_TEAM_ID TEXT,
                $COLUMN_TEAM_NAME TEXT,
                $COLUMN_SURVEY_NAME TEXT,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_PAYLOAD TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_outbox_team_id ON $TABLE_SUBMISSIONS($COLUMN_TEAM_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBMISSIONS")
            onCreate(db)
        }
    }

    suspend fun saveSubmission(
        submission: SurveySubmission,
        surveyId: String?,
        surveyName: String?,
        teamId: String?,
        teamName: String?,
    ): Boolean {
        val serialized = submissionAdapter.toJson(submission) ?: return false
        return withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(COLUMN_SURVEY_ID, surveyId)
                put(COLUMN_TEAM_ID, teamId)
                put(COLUMN_TEAM_NAME, teamName)
                put(COLUMN_SURVEY_NAME, surveyName)
                put(COLUMN_CREATED_AT, System.currentTimeMillis())
                put(COLUMN_PAYLOAD, serialized)
            }
            writableDatabase.insert(TABLE_SUBMISSIONS, null, values) != -1L
        }
    }

    suspend fun getPendingForTeam(teamId: String?): List<OutboxEntry> = withContext(Dispatchers.IO) {
        readableDatabase.query(
            TABLE_SUBMISSIONS,
            arrayOf(
                COLUMN_ID,
                COLUMN_SURVEY_ID,
                COLUMN_TEAM_ID,
                COLUMN_TEAM_NAME,
                COLUMN_SURVEY_NAME,
                COLUMN_CREATED_AT,
                COLUMN_PAYLOAD,
            ),
            if (teamId.isNullOrBlank()) null else "$COLUMN_TEAM_ID = ?",
            teamId?.let { arrayOf(it) },
            null,
            null,
            "$COLUMN_CREATED_AT DESC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val payload = cursor.getStringOrNull(COLUMN_PAYLOAD) ?: continue
                    val parsed = runCatching { submissionAdapter.fromJson(payload) }.getOrNull() ?: continue
                    add(
                        OutboxEntry(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            surveyId = cursor.getStringOrNull(COLUMN_SURVEY_ID),
                            teamId = cursor.getStringOrNull(COLUMN_TEAM_ID),
                            teamName = cursor.getStringOrNull(COLUMN_TEAM_NAME),
                            surveyName = cursor.getStringOrNull(COLUMN_SURVEY_NAME),
                            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                            submission = parsed,
                        ),
                    )
                }
            }
        }
    }

    suspend fun getEntry(id: Long): OutboxEntry? = withContext(Dispatchers.IO) {
        readableDatabase.query(
            TABLE_SUBMISSIONS,
            arrayOf(
                COLUMN_ID,
                COLUMN_SURVEY_ID,
                COLUMN_TEAM_ID,
                COLUMN_TEAM_NAME,
                COLUMN_SURVEY_NAME,
                COLUMN_CREATED_AT,
                COLUMN_PAYLOAD,
            ),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val payload = cursor.getStringOrNull(COLUMN_PAYLOAD) ?: return@use null
                val parsed = runCatching { submissionAdapter.fromJson(payload) }.getOrNull() ?: return@use null
                OutboxEntry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    surveyId = cursor.getStringOrNull(COLUMN_SURVEY_ID),
                    teamId = cursor.getStringOrNull(COLUMN_TEAM_ID),
                    teamName = cursor.getStringOrNull(COLUMN_TEAM_NAME),
                    surveyName = cursor.getStringOrNull(COLUMN_SURVEY_NAME),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    submission = parsed,
                )
            } else {
                null
            }
        }
    }

    suspend fun deleteEntry(id: Long): Boolean = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_SUBMISSIONS, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    data class OutboxEntry(
        val id: Long,
        val surveyId: String?,
        val teamId: String?,
        val teamName: String?,
        val surveyName: String?,
        val createdAt: Long,
        val submission: SurveySubmission,
    )

    private fun Cursor.getStringOrNull(columnName: String): String? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getString(index)
    }

    private companion object {
        private const val DATABASE_NAME = "dashboard_survey_outbox.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_SUBMISSIONS = "outbox_submissions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_SURVEY_ID = "survey_id"
        private const val COLUMN_TEAM_ID = "team_id"
        private const val COLUMN_TEAM_NAME = "team_name"
        private const val COLUMN_SURVEY_NAME = "survey_name"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_PAYLOAD = "payload"
    }
}

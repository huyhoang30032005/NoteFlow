package com.example.noteflow.database

import android.content.ContentValues
import android.content.Context
import com.example.noteflow.model.Note

class NoteDao(context: Context) {

    private val dbHelper = NoteDatabaseHelper(context)

    fun insert(note: Note): Long {
        val db = dbHelper.writableDatabase

        val values = noteToValues(note, includeId = false)

        return db.insert(
            NoteDatabaseHelper.TABLE_NOTES,
            null,
            values
        )
    }

    fun getAll(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            NoteDatabaseHelper.TABLE_NOTES,
            null,
            null,
            null,
            null,
            null,
            "${NoteDatabaseHelper.COLUMN_CREATED_AT} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                notes.add(cursorToNote(it))
            }
        }

        return notes
    }

    fun getFilteredNotes(
        keyword: String,
        category: String,
        sort: String
    ): List<Note> {
        val notes = mutableListOf<Note>()
        val db = dbHelper.readableDatabase

        val selectionParts = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (keyword.isNotBlank()) {
            selectionParts.add(
                "(" +
                        "${NoteDatabaseHelper.COLUMN_TITLE} LIKE ? " +
                        "OR ${NoteDatabaseHelper.COLUMN_CONTENT} LIKE ?" +
                        ")"
            )

            val searchValue = "%$keyword%"
            selectionArgs.add(searchValue)
            selectionArgs.add(searchValue)
        }

        if (category != "Tất cả") {
            selectionParts.add(
                "${NoteDatabaseHelper.COLUMN_CATEGORY} = ?"
            )
            selectionArgs.add(category)
        }

        val selection = selectionParts
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" AND ")

        val orderBy = when (sort) {
            "Cũ nhất" ->
                "${NoteDatabaseHelper.COLUMN_CREATED_AT} ASC"

            else ->
                "${NoteDatabaseHelper.COLUMN_CREATED_AT} DESC"
        }

        val cursor = db.query(
            NoteDatabaseHelper.TABLE_NOTES,
            null,
            selection,
            selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray(),
            null,
            null,
            orderBy
        )

        cursor.use {
            while (it.moveToNext()) {
                notes.add(cursorToNote(it))
            }
        }

        return notes
    }

    fun getById(id: Int): Note? {
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            NoteDatabaseHelper.TABLE_NOTES,
            null,
            "${NoteDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            return if (it.moveToFirst()) {
                cursorToNote(it)
            } else {
                null
            }
        }
    }

    fun update(note: Note): Int {
        val db = dbHelper.writableDatabase
        val values = noteToValues(note, includeId = false)

        return db.update(
            NoteDatabaseHelper.TABLE_NOTES,
            values,
            "${NoteDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(note.id.toString())
        )
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase

        return db.delete(
            NoteDatabaseHelper.TABLE_NOTES,
            "${NoteDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Thay thế toàn bộ ghi chú bằng dữ liệu sao lưu.
     * Nếu có lỗi, SQLite sẽ rollback giao dịch.
     */
    fun restoreAll(notes: List<Note>) {
        val db = dbHelper.writableDatabase

        db.beginTransaction()

        try {
            db.delete(
                NoteDatabaseHelper.TABLE_NOTES,
                null,
                null
            )

            for (note in notes) {
                val values = noteToValues(
                    note,
                    includeId = true
                )

                val result = db.insert(
                    NoteDatabaseHelper.TABLE_NOTES,
                    null,
                    values
                )

                check(result != -1L) {
                    "Không thể khôi phục ghi chú ID ${note.id}"
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun noteToValues(
        note: Note,
        includeId: Boolean
    ): ContentValues {
        return ContentValues().apply {
            if (includeId) {
                put(
                    NoteDatabaseHelper.COLUMN_ID,
                    note.id
                )
            }

            put(
                NoteDatabaseHelper.COLUMN_TITLE,
                note.title
            )

            put(
                NoteDatabaseHelper.COLUMN_CONTENT,
                note.content
            )

            put(
                NoteDatabaseHelper.COLUMN_CATEGORY,
                note.category
            )

            put(
                NoteDatabaseHelper.COLUMN_FAVORITE,
                if (note.isFavorite) 1 else 0
            )

            put(
                NoteDatabaseHelper.COLUMN_CREATED_AT,
                note.createdAt
            )

            put(
                NoteDatabaseHelper.COLUMN_UPDATED_AT,
                note.updatedAt
            )
        }
    }

    private fun cursorToNote(
        cursor: android.database.Cursor
    ): Note {
        return Note(
            id = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_ID
                )
            ),
            title = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_TITLE
                )
            ),
            content = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_CONTENT
                )
            ),
            category = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_CATEGORY
                )
            ),
            isFavorite = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_FAVORITE
                )
            ) == 1,
            createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_CREATED_AT
                )
            ),
            updatedAt = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    NoteDatabaseHelper.COLUMN_UPDATED_AT
                )
            )
        )
    }
}
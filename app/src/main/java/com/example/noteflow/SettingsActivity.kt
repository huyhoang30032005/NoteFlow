package com.example.noteflow

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.noteflow.database.NoteDao
import com.example.noteflow.databinding.ActivitySettingsBinding
import com.example.noteflow.model.Note
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var noteDao: NoteDao

    private val preferences by lazy {
        getSharedPreferences(
            "app_settings",
            MODE_PRIVATE
        )
    }

    // =====================================================
    // CREATE JSON FILE
    // =====================================================

    private val createJsonFile =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            if (uri != null) {
                backupJson(uri)
            }
        }

    // =====================================================
    // CREATE CSV FILE
    // =====================================================

    private val createCsvFile =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument(
                "text/csv"
            )
        ) { uri ->

            if (uri != null) {
                backupCsv(uri)
            }
        }

    // =====================================================
    // OPEN BACKUP FILE
    // =====================================================

    private val openBackupFile =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                restoreFromFile(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding =
            ActivitySettingsBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        noteDao = NoteDao(this)

        setupDarkMode()
        setupButtons()
    }

    // =====================================================
    // APPLY SAVED THEME
    // =====================================================

    private fun applySavedTheme() {

        val isDarkMode =
            preferences.getBoolean(
                "dark_mode",
                false
            )

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    // =====================================================
    // DARK / LIGHT MODE
    // =====================================================

    private fun setupDarkMode() {

        binding.switchDarkMode.isChecked =
            preferences.getBoolean(
                "dark_mode",
                false
            )

        binding.switchDarkMode.setOnCheckedChangeListener {
                _,
                isChecked ->

            preferences.edit()
                .putBoolean(
                    "dark_mode",
                    isChecked
                )
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    // =====================================================
    // BUTTONS
    // =====================================================

    private fun setupButtons() {

        binding.btnBack.setOnClickListener {

            finish()
        }

        binding.btnBackupJson.setOnClickListener {

            createJsonFile.launch(
                "noteflow_backup.json"
            )
        }

        binding.btnBackupCsv.setOnClickListener {

            createCsvFile.launch(
                "noteflow_backup.csv"
            )
        }

        binding.btnRestore.setOnClickListener {

            openBackupFile.launch(
                arrayOf(
                    "application/json",
                    "text/*",
                    "application/octet-stream"
                )
            )
        }
    }

    // =====================================================
    // BACKUP JSON
    // =====================================================

    private fun backupJson(uri: Uri) {

        try {

            val jsonArray = JSONArray()

            noteDao.getAll().forEach { note ->

                val item = JSONObject().apply {

                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("category", note.category)
                    put("isFavorite", note.isFavorite)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                }

                jsonArray.put(item)
            }

            contentResolver.openOutputStream(uri)?.use {
                    outputStream ->

                outputStream.write(
                    jsonArray
                        .toString(2)
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )

            } ?: throw IllegalStateException(
                "Không mở được file"
            )

            showMessage(
                "Đã sao lưu JSON"
            )

        } catch (e: Exception) {

            showMessage(
                "Sao lưu JSON thất bại: ${e.message}"
            )
        }
    }

    // =====================================================
    // BACKUP CSV
    // =====================================================

    private fun backupCsv(uri: Uri) {

        try {

            val csv = StringBuilder()

            csv.append(
                "id,title,content,category,isFavorite,createdAt,updatedAt\r\n"
            )

            noteDao.getAll().forEach { note ->

                val fields = listOf(
                    note.id.toString(),
                    note.title,
                    note.content,
                    note.category,
                    note.isFavorite.toString(),
                    note.createdAt.toString(),
                    note.updatedAt.toString()
                )

                csv.append(
                    fields.joinToString(",") {
                        csvEscape(it)
                    }
                )

                csv.append("\r\n")
            }

            contentResolver.openOutputStream(uri)?.use {
                    outputStream ->

                outputStream.write(
                    csv.toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )

            } ?: throw IllegalStateException(
                "Không mở được file"
            )

            showMessage(
                "Đã sao lưu CSV"
            )

        } catch (e: Exception) {

            showMessage(
                "Sao lưu CSV thất bại: ${e.message}"
            )
        }
    }

    private fun csvEscape(
        value: String
    ): String {

        return "\"" +
                value.replace(
                    "\"",
                    "\"\""
                ) +
                "\""
    }

    // =====================================================
    // RESTORE FILE
    // =====================================================

    private fun restoreFromFile(uri: Uri) {

        try {

            val content =
                contentResolver.openInputStream(uri)?.use {
                        inputStream ->

                    BufferedReader(
                        InputStreamReader(
                            inputStream,
                            Charsets.UTF_8
                        )
                    ).readText()

                } ?: throw IllegalStateException(
                    "Không đọc được file"
                )

            val trimmed =
                content.trimStart()

            when {

                trimmed.startsWith("[") -> {

                    restoreJson(content)
                }

                trimmed.startsWith("{") -> {

                    showMessage(
                        "JSON phải chứa một mảng ghi chú"
                    )
                }

                else -> {

                    restoreCsv(content)
                }
            }

        } catch (e: Exception) {

            showMessage(
                "Không thể đọc file: ${e.message}"
            )
        }
    }

    // =====================================================
    // RESTORE JSON
    // =====================================================

    private fun restoreJson(
        content: String
    ) {

        try {

            val array =
                JSONArray(content)

            val notes =
                mutableListOf<Note>()

            for (i in 0 until array.length()) {

                val item =
                    array.getJSONObject(i)

                val title =
                    item.getString("title")

                val category =
                    item.getString("category")

                require(
                    title.isNotBlank()
                ) {
                    "Tiêu đề ghi chú không được để trống"
                }

                require(
                    category in VALID_CATEGORIES
                ) {
                    "Danh mục không hợp lệ: $category"
                }

                notes.add(

                    Note(
                        id = item.getInt("id"),
                        title = title,
                        content = item.getString(
                            "content"
                        ),
                        category = category,
                        isFavorite = item.optBoolean(
                            "isFavorite",
                            false
                        ),
                        createdAt = item.getLong(
                            "createdAt"
                        ),
                        updatedAt = item.optLong(
                            "updatedAt",
                            item.getLong(
                                "createdAt"
                            )
                        )
                    )
                )
            }

            validateNotes(notes)

            confirmRestore(notes)

        } catch (e: Exception) {

            showMessage(
                "File JSON không hợp lệ: ${e.message}"
            )
        }
    }

    // =====================================================
    // RESTORE CSV
    // =====================================================

    private fun restoreCsv(
        content: String
    ) {

        try {

            val rows =
                parseCsv(content)

            if (rows.size < 2) {

                showMessage(
                    "File CSV không có dữ liệu"
                )

                return
            }

            val expectedHeader = listOf(
                "id",
                "title",
                "content",
                "category",
                "isFavorite",
                "createdAt",
                "updatedAt"
            )

            require(
                rows.first() == expectedHeader
            ) {
                "Tiêu đề cột CSV không đúng định dạng NoteFlow"
            }

            val notes =
                rows.drop(1)
                    .filter { row ->

                        row.any {
                            it.isNotBlank()
                        }
                    }
                    .map { row ->

                        require(
                            row.size == 7
                        ) {
                            "Một dòng CSV không đủ 7 cột"
                        }

                        val title =
                            row[1]

                        val category =
                            row[3]

                        require(
                            title.isNotBlank()
                        ) {
                            "Tiêu đề ghi chú không được để trống"
                        }

                        require(
                            category in VALID_CATEGORIES
                        ) {
                            "Danh mục không hợp lệ: $category"
                        }

                        Note(
                            id = row[0].toInt(),
                            title = title,
                            content = row[2],
                            category = category,
                            isFavorite = row[4].toBooleanStrict(),
                            createdAt = row[5].toLong(),
                            updatedAt = row[6].toLong()
                        )
                    }

            validateNotes(notes)

            confirmRestore(notes)

        } catch (e: Exception) {

            showMessage(
                "File CSV không hợp lệ: ${e.message}"
            )
        }
    }

    // =====================================================
    // PARSE CSV
    // =====================================================

    private fun parseCsv(
        content: String
    ): List<List<String>> {

        val rows =
            mutableListOf<List<String>>()

        var row =
            mutableListOf<String>()

        val field =
            StringBuilder()

        var inQuotes = false

        var i = 0

        while (i < content.length) {

            val c =
                content[i]

            when {

                c == '"' &&
                        inQuotes &&
                        i + 1 < content.length &&
                        content[i + 1] == '"' -> {

                    field.append('"')

                    i++
                }

                c == '"' -> {

                    inQuotes =
                        !inQuotes
                }

                c == ',' &&
                        !inQuotes -> {

                    row.add(
                        field.toString()
                    )

                    field.clear()
                }

                (c == '\n' ||
                        c == '\r') &&
                        !inQuotes -> {

                    if (
                        c == '\r' &&
                        i + 1 < content.length &&
                        content[i + 1] == '\n'
                    ) {

                        i++
                    }

                    row.add(
                        field.toString()
                    )

                    field.clear()

                    if (
                        row.any {
                            it.isNotEmpty()
                        }
                    ) {

                        rows.add(row)
                    }

                    row =
                        mutableListOf()
                }

                else -> {

                    field.append(c)
                }
            }

            i++
        }

        require(
            !inQuotes
        ) {
            "CSV có dấu ngoặc kép chưa đóng"
        }

        if (
            field.isNotEmpty() ||
            row.isNotEmpty()
        ) {

            row.add(
                field.toString()
            )

            rows.add(row)
        }

        return rows
    }

    // =====================================================
    // VALIDATE NOTES
    // =====================================================

    private fun validateNotes(
        notes: List<Note>
    ) {

        require(
            notes.isNotEmpty()
        ) {
            "Không có ghi chú để khôi phục"
        }

        require(
            notes.all {
                it.id > 0
            }
        ) {
            "ID ghi chú không hợp lệ"
        }

        require(
            notes.map {
                it.id
            }.distinct().size == notes.size
        ) {
            "File có ID ghi chú bị trùng"
        }
    }

    // =====================================================
    // CONFIRM AND RESTORE
    // =====================================================

    private fun confirmRestore(
        notes: List<Note>
    ) {

        AlertDialog.Builder(this)
            .setTitle(
                "Khôi phục ghi chú"
            )
            .setMessage(
                "File có ${notes.size} ghi chú. " +
                        "Khôi phục sẽ thay thế toàn bộ ghi chú hiện tại. " +
                        "Bạn nên sao lưu trước khi tiếp tục."
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Khôi phục"
            ) { _, _ ->

                try {

                    noteDao.restoreAll(notes)

                    showMessage(
                        "Đã khôi phục ${notes.size} ghi chú"
                    )

                } catch (e: Exception) {

                    showMessage(
                        "Khôi phục thất bại; dữ liệu cũ được giữ lại. " +
                                (e.message ?: "")
                    )
                }
            }
            .show()
    }

    // =====================================================
    // SHOW MESSAGE
    // =====================================================

    private fun showMessage(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {

        private val VALID_CATEGORIES = setOf(
            "Công việc",
            "Cá nhân",
            "Học tập",
            "Ý tưởng"
        )
    }
}

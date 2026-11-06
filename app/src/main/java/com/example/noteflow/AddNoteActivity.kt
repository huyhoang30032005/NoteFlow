package com.example.noteflow

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.noteflow.database.NoteDao
import com.example.noteflow.databinding.ActivityAddNoteBinding
import com.example.noteflow.model.Note

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteDao: NoteDao

    private var selectedCategory = "Công việc"

    private var isFavorite = false

    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNoteBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        noteDao = NoteDao(this)

        noteId = intent.getIntExtra(
            "NOTE_ID",
            -1
        )

        setupCategory()

        setupButtons()

        if (noteId != -1) {

            loadNote()

        } else {

            binding.btnDelete.visibility =
                View.GONE

            updateFavoriteIcon()
        }
    }

    // =========================================================
    // LOAD NOTE
    // =========================================================

    private fun loadNote() {

        val note = noteDao.getById(noteId)

        if (note == null) {

            Toast.makeText(
                this,
                "Không tìm thấy ghi chú",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        binding.edtTitle.setText(
            note.title
        )

        binding.edtContent.setText(
            note.content
        )

        selectedCategory =
            note.category

        isFavorite =
            note.isFavorite

        updateCategorySelection()

        updateFavoriteIcon()

        binding.tvTitle.text =
            "Chỉnh sửa ghi chú"

        binding.btnDelete.visibility =
            View.VISIBLE
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private fun setupCategory() {

        binding.chipWork.setOnClickListener {

            selectedCategory = "Công việc"

            updateCategorySelection()
        }

        binding.chipPersonal.setOnClickListener {

            selectedCategory = "Cá nhân"

            updateCategorySelection()
        }

        binding.chipStudy.setOnClickListener {

            selectedCategory = "Học tập"

            updateCategorySelection()
        }

        binding.chipIdea.setOnClickListener {

            selectedCategory = "Ý tưởng"

            updateCategorySelection()
        }

        updateCategorySelection()
    }

    private fun updateCategorySelection() {

        // -----------------------------------------------------
        // CÔNG VIỆC
        // -----------------------------------------------------

        val workSelected =
            selectedCategory == "Công việc"

        binding.chipWork.setChipBackgroundColorResource(
            if (workSelected)
                R.color.category_work
            else
                R.color.category_work_bg
        )

        binding.chipWork.setTextColor(
            ContextCompat.getColor(
                this,
                if (workSelected)
                    android.R.color.white
                else
                    R.color.category_work
            )
        )

        // -----------------------------------------------------
        // CÁ NHÂN
        // -----------------------------------------------------

        val personalSelected =
            selectedCategory == "Cá nhân"

        binding.chipPersonal.setChipBackgroundColorResource(
            if (personalSelected)
                R.color.category_personal
            else
                R.color.category_personal_bg
        )

        binding.chipPersonal.setTextColor(
            ContextCompat.getColor(
                this,
                if (personalSelected)
                    android.R.color.white
                else
                    R.color.category_personal
            )
        )

        // -----------------------------------------------------
        // HỌC TẬP
        // -----------------------------------------------------

        val studySelected =
            selectedCategory == "Học tập"

        binding.chipStudy.setChipBackgroundColorResource(
            if (studySelected)
                R.color.category_study
            else
                R.color.category_study_bg
        )

        binding.chipStudy.setTextColor(
            ContextCompat.getColor(
                this,
                if (studySelected)
                    android.R.color.white
                else
                    R.color.category_study
            )
        )

        // -----------------------------------------------------
        // Ý TƯỞNG
        // -----------------------------------------------------

        val ideaSelected =
            selectedCategory == "Ý tưởng"

        binding.chipIdea.setChipBackgroundColorResource(
            if (ideaSelected)
                R.color.category_idea
            else
                R.color.category_idea_bg
        )

        binding.chipIdea.setTextColor(
            ContextCompat.getColor(
                this,
                if (ideaSelected)
                    android.R.color.white
                else
                    R.color.category_idea
            )
        )
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private fun setupButtons() {

        binding.btnBack.setOnClickListener {

            finish()
        }

        binding.btnFavorite.setOnClickListener {

            isFavorite = !isFavorite

            updateFavoriteIcon()
        }

        binding.btnSave.setOnClickListener {

            saveNote()
        }

        binding.btnDelete.setOnClickListener {

            confirmDelete()
        }
    }

    // =========================================================
    // FAVORITE
    // =========================================================

    private fun updateFavoriteIcon() {

        binding.btnFavorite.setImageResource(
            R.drawable.favourite
        )

        binding.btnFavorite.imageTintList =
            if (isFavorite) {

                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.error
                    )
                )

            } else {

                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.light_text_secondary
                    )
                )
            }
    }

    // =========================================================
    // SAVE
    // =========================================================

    private fun saveNote() {

        val title =
            binding.edtTitle.text
                .toString()
                .trim()

        val content =
            binding.edtContent.text
                .toString()
                .trim()

        if (title.isEmpty()) {

            binding.titleInputLayout.error =
                "Vui lòng nhập tiêu đề"

            return
        }

        binding.titleInputLayout.error = null

        val currentTime =
            System.currentTimeMillis()

        // =====================================================
        // ADD
        // =====================================================

        if (noteId == -1) {

            val note = Note(

                title = title,

                content = content,

                category = selectedCategory,

                isFavorite = isFavorite,

                createdAt = currentTime,

                updatedAt = currentTime
            )

            val result =
                noteDao.insert(note)

            if (result != -1L) {

                Toast.makeText(
                    this,
                    "Đã lưu ghi chú",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } else {

                Toast.makeText(
                    this,
                    "Lưu ghi chú thất bại",
                    Toast.LENGTH_SHORT
                ).show()
            }

            return
        }

        // =====================================================
        // UPDATE
        // =====================================================

        val oldNote =
            noteDao.getById(noteId)

        if (oldNote == null) {

            Toast.makeText(
                this,
                "Không tìm thấy ghi chú",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val updatedNote = Note(

            id = oldNote.id,

            title = title,

            content = content,

            category = selectedCategory,

            isFavorite = isFavorite,

            createdAt = oldNote.createdAt,

            updatedAt = currentTime
        )

        val result =
            noteDao.update(updatedNote)

        if (result > 0) {

            Toast.makeText(
                this,
                "Đã cập nhật ghi chú",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Cập nhật thất bại",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // DELETE CONFIRM
    // =========================================================

    private fun confirmDelete() {

        if (noteId == -1) {
            return
        }

        AlertDialog.Builder(this)

            .setTitle("Xóa ghi chú")

            .setMessage(
                "Bạn có chắc muốn xóa ghi chú này không?"
            )

            .setNegativeButton(
                "Hủy",
                null
            )

            .setPositiveButton(
                "Xóa"
            ) { _, _ ->

                deleteNote()
            }

            .show()
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun deleteNote() {

        val result =
            noteDao.delete(noteId)

        if (result > 0) {

            Toast.makeText(
                this,
                "Đã xóa ghi chú",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Xóa ghi chú thất bại",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
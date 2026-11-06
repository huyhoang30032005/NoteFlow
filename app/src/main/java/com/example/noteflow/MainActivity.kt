package com.example.noteflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteflow.adapter.NoteAdapter
import com.example.noteflow.database.NoteDao
import com.example.noteflow.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDao: NoteDao
    private lateinit var noteAdapter: NoteAdapter

    private var searchKeyword = ""
    private var selectedCategory = "Tất cả"
    private var selectedSort = "Mới nhất"

    override fun onCreate(savedInstanceState: Bundle?) {

        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        noteDao = NoteDao(this)

        setupRecyclerView()
        setupSearch()
        setupCategoryFilter()
        setupSort()
        setupButtons()

        loadNotes()
    }

    // =================================================
    // APPLY SAVED THEME
    // =================================================

    private fun applySavedTheme() {

        val preferences = getSharedPreferences(
            "app_settings",
            MODE_PRIVATE
        )

        val isDarkMode = preferences.getBoolean(
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

    // =================================================
    // RECYCLER VIEW
    // =================================================

    private fun setupRecyclerView() {

        noteAdapter = NoteAdapter(
            notes = emptyList(),

            onNoteClick = { note ->

                val intent = Intent(
                    this,
                    AddNoteActivity::class.java
                )

                intent.putExtra(
                    "NOTE_ID",
                    note.id
                )

                startActivity(intent)
            },

            onFavoriteClick = { note ->

                val updatedNote = note.copy(
                    isFavorite = !note.isFavorite
                )

                noteDao.update(
                    updatedNote
                )

                loadNotes()
            }
        )

        binding.recyclerNotes.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MainActivity
                )

            adapter = noteAdapter
        }
    }

    // =================================================
    // SEARCH
    // =================================================

    private fun setupSearch() {

        binding.searchView.setupWithSearchBar(
            binding.searchBar
        )

        binding.searchView.editText.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    searchKeyword =
                        s?.toString()?.trim() ?: ""

                    loadNotes()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    // =================================================
    // CATEGORY FILTER
    // =================================================

    private fun setupCategoryFilter() {

        binding.chipAll.setOnClickListener {
            selectCategory("Tất cả")
        }

        binding.chipWork.setOnClickListener {
            selectCategory("Công việc")
        }

        binding.chipPersonal.setOnClickListener {
            selectCategory("Cá nhân")
        }

        binding.chipStudy.setOnClickListener {
            selectCategory("Học tập")
        }

        binding.chipIdeas.setOnClickListener {
            selectCategory("Ý tưởng")
        }

        updateCategoryStyle()
    }

    private fun selectCategory(
        category: String
    ) {

        selectedCategory = category

        updateCategoryStyle()

        loadNotes()
    }

    private fun updateCategoryStyle() {

        val allSelected =
            selectedCategory == "Tất cả"

        binding.chipAll.isChecked =
            allSelected

        binding.chipAll.setChipBackgroundColorResource(
            if (allSelected) {
                R.color.primary
            } else {
                R.color.chip_bgr
            }
        )

        binding.chipAll.setTextColor(
            getColor(
                if (allSelected) {
                    android.R.color.white
                } else {
                    R.color.text_chip
                }
            )
        )

        val categories = listOf(

            Triple(
                "Công việc",
                binding.chipWork,
                Pair(
                    R.color.category_work,
                    R.color.category_work_bg
                )
            ),

            Triple(
                "Cá nhân",
                binding.chipPersonal,
                Pair(
                    R.color.category_personal,
                    R.color.category_personal_bg
                )
            ),

            Triple(
                "Học tập",
                binding.chipStudy,
                Pair(
                    R.color.category_study,
                    R.color.category_study_bg
                )
            ),

            Triple(
                "Ý tưởng",
                binding.chipIdeas,
                Pair(
                    R.color.category_idea,
                    R.color.category_idea_bg
                )
            )
        )

        categories.forEach {
                (category, chip, colors) ->

            val selected =
                selectedCategory == category

            chip.isChecked =
                selected

            chip.setChipBackgroundColorResource(
                if (selected) {
                    colors.first
                } else {
                    colors.second
                }
            )

            chip.setTextColor(
                getColor(
                    if (selected) {
                        android.R.color.white
                    } else {
                        colors.first
                    }
                )
            )

            chip.setChipStrokeColorResource(
                colors.first
            )
        }
    }

    // =================================================
    // SORT
    // =================================================

    private fun setupSort() {

        val sortOptions = arrayOf(
            "Mới nhất",
            "Cũ nhất"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            sortOptions
        )

        binding.sortDropdown.setAdapter(
            adapter
        )

        binding.sortDropdown.setText(
            selectedSort,
            false
        )

        binding.sortDropdown.setOnClickListener {

            binding.sortDropdown.showDropDown()
        }

        binding.sortDropdown.setOnItemClickListener {
                _, _, position, _ ->

            selectedSort =
                sortOptions[position]

            binding.sortDropdown.setText(
                selectedSort,
                false
            )

            loadNotes()
        }
    }

    // =================================================
    // BUTTONS
    // =================================================

    private fun setupButtons() {

        binding.fabAddNote.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AddNoteActivity::class.java
                )
            )
        }

        binding.accountIcon.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
    }

    // =================================================
    // LOAD NOTES
    // =================================================

    private fun loadNotes() {

        val notes =
            noteDao.getFilteredNotes(
                keyword = searchKeyword,
                category = selectedCategory,
                sort = selectedSort
            )

        noteAdapter.updateData(
            notes
        )
    }

    override fun onResume() {

        super.onResume()

        loadNotes()
    }
}
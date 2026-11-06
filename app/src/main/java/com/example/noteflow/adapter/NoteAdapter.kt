package com.example.noteflow.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.noteflow.R
import com.example.noteflow.databinding.ItemNoteBinding
import com.example.noteflow.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onFavoriteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {

            // =================================================
            // TEXT
            // =================================================

            binding.txtTitle.text = note.title
            binding.txtContent.text = note.content
            binding.txtCategory.text = note.category

            val dateFormat = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            binding.txtDate.text =
                dateFormat.format(
                    Date(note.createdAt)
                )

            // =================================================
            // FAVORITE
            // =================================================

            if (note.isFavorite) {

                // Đã yêu thích: trái tim đặc màu đỏ
                binding.btnFavorite.setImageResource(
                    R.drawable.ic_favorite
                )

                binding.btnFavorite.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.error
                        )
                    )

            } else {

                // Chưa yêu thích: giữ nguyên icon hiện tại
                binding.btnFavorite.setImageResource(
                    R.drawable.favourite
                )

                binding.btnFavorite.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.light_text_secondary
                        )
                    )
            }

            binding.btnFavorite.setOnClickListener {

                onFavoriteClick(note)
            }

            // =================================================
            // SHARE
            // =================================================

            binding.btnShare.setOnClickListener {

                val shareText =
                    "${note.title}\n\n${note.content}"

                val shareIntent = Intent(
                    Intent.ACTION_SEND
                ).apply {

                    type = "text/plain"

                    putExtra(
                        Intent.EXTRA_TEXT,
                        shareText
                    )
                }

                val chooserIntent =
                    Intent.createChooser(
                        shareIntent,
                        "Chia sẻ ghi chú"
                    )

                binding.root.context.startActivity(
                    chooserIntent
                )
            }

            // =================================================
            // NOTE CLICK
            // =================================================

            binding.root.setOnClickListener {

                onNoteClick(note)
            }

            // =================================================
            // CATEGORY COLOR
            // =================================================

            when (note.category) {

                "Công việc" -> {

                    binding.categoryIndicator.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_work
                        )
                    )

                    binding.categoryBadge.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_work_bg
                        )
                    )

                    binding.txtCategory.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_work
                        )
                    )
                }

                "Cá nhân" -> {

                    binding.categoryIndicator.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_personal
                        )
                    )

                    binding.categoryBadge.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_personal_bg
                        )
                    )

                    binding.txtCategory.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_personal
                        )
                    )
                }

                "Học tập" -> {

                    binding.categoryIndicator.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_study
                        )
                    )

                    binding.categoryBadge.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_study_bg
                        )
                    )

                    binding.txtCategory.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_study
                        )
                    )
                }

                "Ý tưởng" -> {

                    binding.categoryIndicator.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_idea
                        )
                    )

                    binding.categoryBadge.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_idea_bg
                        )
                    )

                    binding.txtCategory.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.category_idea
                        )
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {

        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {

        holder.bind(
            notes[position]
        )
    }

    override fun getItemCount(): Int {

        return notes.size
    }

    fun updateData(
        newNotes: List<Note>
    ) {

        notes = newNotes

        notifyDataSetChanged()
    }
}

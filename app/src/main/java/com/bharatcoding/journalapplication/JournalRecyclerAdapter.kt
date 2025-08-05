package com.bharatcoding.journalapplication;

import android.content.Context;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bharatcoding.journalapplication.databinding.JournalRowBinding
import com.bumptech.glide.Glide
import android.content.Intent
import android.widget.Toast

class JournalRecyclerAdapter(val context:Context, var journalList:List<Journal>) :
    RecyclerView.Adapter<JournalRecyclerAdapter.MyViewholder>()
{
    lateinit var binding: JournalRowBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewholder {
        //val view:View = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false)
        binding = JournalRowBinding.inflate(
            LayoutInflater.from(context), // Use context from constructor
            parent,
            false)
        return MyViewholder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewholder,
        position: Int
    ) {
        val journal = journalList[position]
        holder.bind(journal)
    }

    override fun getItemCount(): Int = journalList.size

    public class MyViewholder(var binding: JournalRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(journal: Journal)
        {
            binding.journal = journal
            
            // Load image from Firebase Storage if available
            if (journal.hasImage) {
                binding.journalRowImage.visibility = View.VISIBLE
                binding.journalRowPlaceholderImage.visibility = View.GONE
                // Set content container to be below the real image
                val params = binding.contentContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.topToBottom = binding.journalRowImage.id
                binding.contentContainer.layoutParams = params
                
                Glide.with(binding.root.context)
                    .load(journal.imageUrl)
                    .centerCrop()
                    .into(binding.journalRowImage)
            } else {
                binding.journalRowImage.visibility = View.GONE
                binding.journalRowPlaceholderImage.visibility = View.VISIBLE
                // Set content container to be below the placeholder image
                val params = binding.contentContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.topToBottom = binding.journalRowPlaceholderImage.id
                binding.contentContainer.layoutParams = params
            }
            
            // Set up click listeners
            setupClickListeners(journal)
        }
        
        private fun setupClickListeners(journal: Journal) {
            // Edit button click listener
            binding.journalRowEditButton.setOnClickListener {
                // TODO: Implement edit functionality
                Toast.makeText(binding.root.context, "Edit functionality coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            // Share button click listener
            binding.journalRowShareButton.setOnClickListener {
                shareJournalEntry(journal)
            }
        }
        
        private fun shareJournalEntry(journal: Journal) {
            val shareText = buildString {
                append("Journal Entry: ${journal.title}\n\n")
                append(journal.thoughts)
                if (journal.hasImage) {
                    append("\n\n[Image attached]")
                }
                append("\n\nShared from My Journal App")
            }
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            binding.root.context.startActivity(Intent.createChooser(shareIntent, "Share Journal Entry"))
        }

    }
}

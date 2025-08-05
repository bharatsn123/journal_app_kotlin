package com.bharatcoding.journalapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bharatcoding.journalapplication.databinding.ActivityJournalListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference

class JournalList : AppCompatActivity() {
    lateinit var binding: ActivityJournalListBinding

    // Firebase References
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    var db = FirebaseFirestore.getInstance()
    lateinit var storageReference: StorageReference
    var collectionReference: CollectionReference = db.collection("Journal")

    lateinit var journalList: MutableList<Journal>
    lateinit var adapter: JournalRecyclerAdapter

    lateinit var noPostTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_list)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)

        //Firebase Auth
        firebaseAuth = Firebase.auth
        
        // Check if user is authenticated
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // User is not authenticated, redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        user = currentUser

        // RecyclerView
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        //Posts arraylist
        journalList = arrayListOf<Journal>()

        // Set up floating action button
        binding.fabAddEntry.setOnClickListener {
            if(user != null && firebaseAuth != null) {
                val intent = Intent(this, AddJournalActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onStart(){
        super.onStart()
        // Clear the list before loading new data
        journalList.clear()
        
        Log.d("JournalList", "Loading journals for user: ${user.uid}")
        
        collectionReference.whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("JournalList", "Successfully retrieved ${documents.size()} documents")
                if(!documents.isEmpty) {
                    documents.forEach { document ->
                        try {
                            // Convert snapshots to journal objects
                            val journal = document.toObject(Journal::class.java)
                            journal?.let {
                                journalList.add(it)
                                Log.d("JournalList", "Added journal: ${it.title}")
                            } ?: run {
                                Log.w("JournalList", "Failed to convert document ${document.id} to Journal object")
                            }
                        } catch (e: Exception) {
                            Log.e("JournalList", "Error converting document ${document.id}", e)
                        }
                    }
                    // Set up Recycler View
                    adapter = JournalRecyclerAdapter(this, journalList)
                    binding.recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                    
                    // Hide empty state
                    binding.listNoPosts.visibility = View.GONE
                } else {
                    // Show empty state
                    binding.listNoPosts.visibility = View.VISIBLE
                    Log.d("JournalList", "No journals found for user")
                }
            }.addOnFailureListener { exception ->
                Log.e("JournalList", "Error getting documents", exception)
                val errorMessage = when {
                    exception.message?.contains("NOT_FOUND") == true -> 
                        "Firestore database not found. Please check your Firebase configuration."
                    exception.message?.contains("PERMISSION_DENIED") == true -> 
                        "Permission denied. Please check Firestore security rules."
                    exception.message?.contains("UNAVAILABLE") == true -> 
                        "Firestore is currently unavailable. Please try again."
                    else -> "Error loading journals: ${exception.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.journal_list_menu, menu)
        Log.d("JournalList", "Menu inflated successfully")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("JournalList", "Menu item clicked: ${item.title} (ID: ${item.itemId})")
        
        when(item.itemId){
            R.id.action_add -> {
                Log.d("JournalList", "Add action clicked")
                if(user != null && firebaseAuth != null) {
                    val intent = Intent(this, AddJournalActivity::class.java)
                    startActivity(intent)
                }
                return true
            }
            R.id.action_signout -> {
                Log.d("JournalList", "Sign out action clicked")
                Toast.makeText(this, "Sign out clicked", Toast.LENGTH_SHORT).show()
                signOut()
                return true
            }
            R.id.action_search -> {
                Log.d("JournalList", "Search action clicked")
                // TODO: Implement search functionality
                Toast.makeText(this, "Search functionality coming soon!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_sort -> {
                Log.d("JournalList", "Sort action clicked")
                // TODO: Implement sort functionality
                Toast.makeText(this, "Sort functionality coming soon!", Toast.LENGTH_SHORT).show()
                return true
            }
            android.R.id.home -> {
                // Handle home button click
                Toast.makeText(this, "Home button clicked", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        try {
            // Sign out from Firebase
            firebaseAuth.signOut()
            
            // Show success message
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            
            // Navigate back to MainActivity and clear the activity stack
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error signing out: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
package com.bharatcoding.journalapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bharatcoding.journalapplication.databinding.ActivityAddJournalBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class AddJournalActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddJournalBinding
    
    // Firebase References
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    
    // Image handling
    private var selectedImageUri: Uri? = null
    private var imageUploaded = false
    
    // Activity result launcher for image selection
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.postImageView.setImageURI(it)
            binding.cameraIconOverlay.visibility = View.GONE
            binding.addImageText.text = "Image selected ✓"
            imageUploaded = true
        }
    }
    
    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("AddJournal", "Permission granted, opening image picker")
            openImagePicker()
        } else {
            Log.w("AddJournal", "Permission denied")
            Toast.makeText(
                this, 
                "Permission required to select images. You can enable it in Settings > Apps > Journal Application > Permissions", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_journal)
        
        // Initialize Firebase
        initializeFirebase()
        
        // Set up UI
        setupUI()
        
        // Set up click listeners
        setupClickListeners()
    }
    
    private fun initializeFirebase() {
        auth = Firebase.auth
        user = auth.currentUser!!
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        
        // Test Firebase connection
        testFirebaseConnection()
    }
    
    private fun testFirebaseConnection() {
        Log.d("AddJournal", "Testing Firebase connection...")
        db.collection("test")
            .document("connection_test")
            .get()
            .addOnSuccessListener {
                Log.d("AddJournal", "Firebase connection successful")
            }
            .addOnFailureListener { e ->
                Log.e("AddJournal", "Firebase connection failed", e)
                Toast.makeText(this, "Firebase connection issue: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun setupUI() {
        // Set current user info
        binding.postUsernameTextview.text = user.email ?: "User"
        
        // Set current date
        val currentDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        binding.postDateTextView.text = currentDate
        
        // Initially hide progress bar
        binding.postProgressbar.visibility = View.GONE
    }
    
    private fun setupClickListeners() {
        // Image selection
        binding.postImageView.setOnClickListener {
            checkPermissionAndPickImage()
        }
        
        binding.cameraIconOverlay.setOnClickListener {
            checkPermissionAndPickImage()
        }
        
        // Save button
        binding.saveButton.setOnClickListener {
            saveJournalEntry()
        }
    }
    
    private fun checkPermissionAndPickImage() {
        when {
            // For Android 13+ (API 33+), use READ_MEDIA_IMAGES
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                        showPermissionRationaleDialog(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            // For Android 12 and below, use READ_EXTERNAL_STORAGE
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        showPermissionRationaleDialog(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }
    
    private fun showPermissionRationaleDialog(permission: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs access to your photos to allow you to add images to your journal entries.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
            .setNegativeButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun openImagePicker() {
        Log.d("AddJournal", "Opening image picker")
        getContent.launch("image/*")
    }
    
    private fun hasImagePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun openAppSettings() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    
    private fun saveJournalEntry() {
        val title = binding.postTitleEt.text.toString().trim()
        val description = binding.postDescriptionEt.text.toString().trim()
        
        // Validate input
        if (title.isEmpty()) {
            binding.postTitleLayout.error = "Title is required"
            return
        }
        
        if (description.isEmpty()) {
            binding.postDescriptionLayout.error = "Description is required"
            return
        }
        
        // Clear errors
        binding.postTitleLayout.error = null
        binding.postDescriptionLayout.error = null
        
        // Show progress and disable save button
        showProgress(true)
        
        // Create journal entry
        val journalEntry = Journal(
            title = title,
            thoughts = description,
            imageUrl = "", // Will be updated after image upload
            userId = user.uid,
            timeAdded = (System.currentTimeMillis() / 1000).toInt(), // Convert to seconds for better compatibility
            username = user.email ?: "User"
        )
        
        if (selectedImageUri != null && imageUploaded) {
            // Upload image first, then save journal entry
            uploadImageAndSaveJournal(journalEntry)
        } else {
            // Save journal entry without image
            saveJournalToFirestore(journalEntry)
        }
    }
    
    private fun uploadImageAndSaveJournal(journalEntry: Journal) {
        val imageFileName = "journal_images/${user.uid}_${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child(imageFileName)
        
        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get download URL
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update journal entry with image URL
                        val updatedJournal = journalEntry.copy(
                            imageUrl = downloadUri.toString()
                        )
                        saveJournalToFirestore(updatedJournal)
                    }.addOnFailureListener { e ->
                        Log.e("AddJournal", "Error getting download URL", e)
                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                        showProgress(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddJournal", "Error uploading image", e)
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                    showProgress(false)
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    binding.postProgressbar.progress = progress
                }
        }
    }
    
    private fun saveJournalToFirestore(journalEntry: Journal) {
        Log.d("AddJournal", "Attempting to save journal entry: $journalEntry")
        
        db.collection("Journal")
            .add(journalEntry)
            .addOnSuccessListener { documentReference ->
                Log.d("AddJournal", "Journal entry saved with ID: ${documentReference.id}")
                Toast.makeText(this, "Journal entry saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddJournal", "Error saving journal entry", e)
                val errorMessage = when {
                    e.message?.contains("NOT_FOUND") == true -> 
                        "Firestore database not found. Please check your Firebase configuration."
                    e.message?.contains("PERMISSION_DENIED") == true -> 
                        "Permission denied. Please check Firestore security rules."
                    e.message?.contains("UNAVAILABLE") == true -> 
                        "Firestore is currently unavailable. Please try again."
                    else -> "Error saving journal entry: ${e.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                showProgress(false)
            }
    }
    
    private fun showProgress(show: Boolean) {
        binding.postProgressbar.visibility = if (show) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !show
        binding.saveButton.text = if (show) "Saving..." else "Save Journal Entry"
    }
}
package com.example.myapplicationmyday

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.User
import com.example.myapplicationmyday.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivProfilePhoto.setImageURI(uri)
                uploadProfilePhoto(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            imageBitmap?.let { bitmap ->
                binding.ivProfilePhoto.setImageBitmap(bitmap)
                // Convert bitmap to URI and upload
                val uri = getImageUriFromBitmap(bitmap)
                uploadProfilePhoto(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        loadUserProfile()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabEditPhoto.setOnClickListener {
            showPhotoSourceDialog()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    displayUserData(currentUser)
                } else {
                    // Create new user profile
                    createUserProfile()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        val newUser = User(
            uid = userId,
            email = email,
            displayName = "",
            username = "",
            photoUrl = "",
            bio = ""
        )

        firestore.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                currentUser = newUser
                displayUserData(newUser)
            }
    }

    private fun displayUserData(user: User?) {
        user?.let {
            binding.etDisplayName.setText(it.displayName)
            binding.etUsername.setText(it.username)
            binding.etBio.setText(it.bio)
            binding.tvEmail.text = it.email

            // Load profile photo
            if (it.photoUrl.isNotEmpty()) {
                // TODO: Load image with Glide or Coil
                // For now, keep default
            }
        }
    }

    private fun saveProfile() {
        val displayName = binding.etDisplayName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (displayName.isEmpty()) {
            binding.tilDisplayName.error = "El nombre es requerido"
            return
        }

        if (username.isEmpty()) {
            binding.tilUsername.error = "El nombre de usuario es requerido"
            return
        }

        showLoading(true)
        val userId = auth.currentUser?.uid ?: return

        val updatedUser = hashMapOf(
            "displayName" to displayName,
            "username" to username,
            "bio" to bio
        )

        firestore.collection("users").document(userId)
            .update(updatedUser as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.profile_updated),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.error_updating_profile),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showPhotoSourceDialog() {
        val options = arrayOf(
            getString(R.string.camera),
            getString(R.string.gallery)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_photo_source))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun uploadProfilePhoto(uri: Uri) {
        showLoading(true)
        val userId = auth.currentUser?.uid ?: return
        val filename = "profile_${userId}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("profile_photos/$filename")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updatePhotoUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.error_uploading_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updatePhotoUrl(photoUrl: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.photo_uploaded),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                showLoading(false)
            }
    }

    private fun getImageUriFromBitmap(bitmap: android.graphics.Bitmap): Uri {
        val bytes = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Profile_${System.currentTimeMillis()}",
            null
        )
        return Uri.parse(path)
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out))
            .setMessage(getString(R.string.sign_out_confirm))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                signOut()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, getString(R.string.sign_out_success), Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.fabEditPhoto.isEnabled = !isLoading
    }
}

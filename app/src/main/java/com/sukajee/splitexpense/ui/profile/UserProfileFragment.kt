package com.sukajee.splitexpense.ui.profile

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sukajee.splitexpense.R


class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private lateinit var imageViewUserImage: ImageView
    private lateinit var firstLastName: TextView
    private lateinit var imageButtonEdit: ImageButton
    private lateinit var imageUri: Uri
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var fireStoreDb: FirebaseStorage
    private lateinit var currentUser: FirebaseUser
    private lateinit var userImageRef: StorageReference
    private lateinit var firebaseAuth: FirebaseAuth
    //private lateinit var progressUploadImage: ProgressBar

    companion object {
        private const val PICK_IMAGE = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewUserImage = view.findViewById(R.id.userImage)
        firstLastName = view.findViewById(R.id.textViewFirstLastName)
        imageButtonEdit = view.findViewById(R.id.imageButtonEdit)
        //progressUploadImage = view.findViewById(R.id.progressUploadImage)

        fireStore = FirebaseFirestore.getInstance()
        fireStoreDb = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        userImageRef = fireStoreDb.getReference("images")


        imageViewUserImage.setOnClickListener {
            openFileChooser()
        }
        imageButtonEdit.setOnClickListener {
            openFileChooser()
        }
    }

    fun openFileChooser() {
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intentGallery, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null && data.data != null) {
            imageUri = data.data!!
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(imageViewUserImage)
            //progressUploadImage.visibility = View.VISIBLE
            uploadImage()
        }
    }

    fun uploadImage() {
        if(imageUri != null) {
            val fileReference = userImageRef.child(currentUser.uid + "." + getFileExtension(imageUri))
            fileReference.putFile(imageUri)
                    .continueWithTask { task ->
                        if(!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        fileReference.downloadUrl
                    }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_LONG).show();
                            val userRef = fireStore.collection("users").document(currentUser.uid)
                            userRef.update("imageUrl", downloadUri.toString())
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Photo Url Upload Unsuccessful ${it.message}", Toast.LENGTH_SHORT).show();
                                    }
                        } else {
                            Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
        } else {
            Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
        //progressUploadImage.visibility = View.INVISIBLE
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver!!.getType(uri))!!
    }
}

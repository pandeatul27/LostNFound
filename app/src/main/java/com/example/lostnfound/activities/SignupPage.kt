package com.example.lostnfound.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lostnfound.R
import com.example.lostnfound.firebase.firestoreclass
import com.example.lostnfound.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_signup_page.*


class SignupPage : BaseActivity() {
    companion  object{
        const val  READ_STORAGE_PERMISSION_CODE=1
        const val PICK_IMAGE_REQUEST_CODE=2
    }
    private val auth=FirebaseAuth.getInstance()
    var selectedImageFileUri: Uri?=null
    var imageUrl:String="https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.britannica.com%2Ftopic%2FIron-Man-comic-book-character&psig=AOvVaw0_YiG1-nVaLvJs7iaeoweJ&ust=1668330857672000&source=images&cd=vfe&ved=0CBAQjRxqFwoTCKiIhPamqPsCFQAAAAAdAAAAABAD"
    private lateinit var name: String
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)




        var mask=1;
        view_password.setOnClickListener{

            if(mask==1){
                register_password.inputType=InputType.TYPE_CLASS_TEXT
                register_password.transformationMethod = null
            }
            else{
                register_password.inputType=InputType.TYPE_MASK_CLASS
                register_password.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            mask=-1*mask;
        }


        var mmask=1;
        view_confirm_password.setOnClickListener{

            if(mmask==1){
                register_confirm_password.inputType=InputType.TYPE_CLASS_TEXT
                register_confirm_password.transformationMethod = null
            }
            else{
                register_confirm_password.inputType=InputType.TYPE_MASK_CLASS
                register_confirm_password.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            mmask=-1*mmask;
        }



        register_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 6) {
                    Signup_button.isEnabled = false
//                    register_password.error = "Password must be at least 6 characters"
                    valid_pass_text.visibility=View.VISIBLE
                } else {
                    Signup_button.isEnabled = true
//                    register_password.error = null
                    valid_pass_text.visibility=View.INVISIBLE
                }
            }
        })


        Signup_button.setOnClickListener{
            if(register_password.text.length<6){
                Toast.makeText(this,"Password must contain atleast 6 characters",Toast.LENGTH_SHORT).show()
            }
            else if(selectedImageFileUri!=null){
                uploadUserImage()
            }
            else registerUser("")
        }
        Upload_profile_pic_button.setOnClickListener{
            if(ContextCompat.checkSelfPermission(
                    this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
                showImageChosser()

            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                 showImageChosser()
            }
        }else{
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
    }
    private fun showImageChosser(){
        val gallery=Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK
            && requestCode== PICK_IMAGE_REQUEST_CODE
            && data!!.data!=null){
            selectedImageFileUri=data.data
            Toast.makeText(this,"Image selected successfully",Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(image:String){
        name=register_name.text.toString()
        val roll=register_roll.text.toString().trim{it <=' '}
        val email=register_insti_email.text.toString().trim{it <=' '}
        val password=register_password.text.toString().trim{it <=' '}
        val confirm=register_confirm_password.text.toString().trim{it <=' '}
        val whatsapp=register_whatsapp.text.toString().trim{it <=' '}
        val mobile=register_mobile.text.toString().trim{it <=' '}

        if(validateForm(name,roll,email,password,confirm,whatsapp,mobile)){
           showProgressDialog("Please Wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,OnCompleteListener() {

                hideProgressDialog()
                if (it.isSuccessful) {
                    val user= Users(name,roll, email, whatsapp, mobile,image)
                    firestoreclass().registerUser(this,user)
                }else{
//                    TODO("handle various exceptions")
                    Toast.makeText(this,
                        it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            })

        }
    }

    private fun validateForm(name:String,roll:String,email:String,password:String,confirm:String,whatsapp:String,mobile:String):Boolean {
        return when {//checking if the field are empty
            TextUtils.isEmpty(name.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Name.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(roll.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Roll Number.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(password.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Password.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(confirm.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Confirm Password.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            (mobile.length!=10)->{
                Toast.makeText(
                    this,
                    "Please Enter valid Mobile Number.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            (whatsapp.length!=10)->{
                Toast.makeText(
                    this,
                    "Please Enter valid Whatsapp Number.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(email.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Email.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(whatsapp.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Whatsapp Number.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            TextUtils.isEmpty(mobile.trim { it <= ' ' }) -> {
                Toast.makeText(
                    this@SignupPage,
                    "Please Enter Your Mobile Number.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

           password.trim { it <= ' ' } != confirm.trim { it <= ' ' } -> {
                Toast.makeText(
                    this@SignupPage,
                    "Password and Confirm Password should be same",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            !email.endsWith("@iitp.ac.in") ->{
                Toast.makeText(
                    this@SignupPage,
                    "Invalid Institute Email",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> {
                true
            }
        }
    }
        fun userRegisteredSuccess(){
            Toast.makeText(this@SignupPage,
            "You have been registered successfully",Toast.LENGTH_SHORT).show()

            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(this,"Please Verify your email to Login. A mail has been sent to your email",Toast.LENGTH_LONG).show()
            }
            FirebaseAuth.getInstance().signOut()
            finish()
        }

    private fun uploadUserImage(){
//        showProgressDialog("Please wait")
        if(selectedImageFileUri!=null){
            val sRef: StorageReference=FirebaseStorage.getInstance().reference
                .child("User_Image"+System.currentTimeMillis()+"."+getFileExt(selectedImageFileUri))
            sRef.putFile(selectedImageFileUri!!).addOnSuccessListener {

                sRef.downloadUrl
                    .addOnCompleteListener {
                                imageUrl = it.result!!.toString()
                        registerUser(imageUrl)
                            }

            }.addOnFailureListener{
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getFileExt(uri: Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }
    }



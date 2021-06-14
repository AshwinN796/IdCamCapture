package com.kknirmale.idcamcapture

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kknirmale.idcamcapture.camera.KYCCamera

class MainActivity : AppCompatActivity() {

    private lateinit var adharFrontImage: ImageView
    private lateinit var frontFaceImage: ImageView
    private lateinit var frontPanImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adharFrontImage = findViewById(R.id.acf)
        frontFaceImage = findViewById(R.id.frontFaceImage)
        frontPanImage = findViewById(R.id.frontPanImage)
    }

    fun captureAadharFront(view: View) {
        KYCCamera.create(this).openCamera(KYCCamera.TYPE_AADHAARCARD_FRONT)
    }

    fun captureFrontFace(view: View) {
        if (KYCCamera.hasFrontCamera(this)) {
            KYCCamera.create(this).openCamera(KYCCamera.TYPE_CAPTURE_FACE_WO_CROP)
        }else {
            Toast.makeText(this,"Front camera not found",Toast.LENGTH_LONG).show()
        }
    }

    fun captureIDWOCrop(view: View) {
        KYCCamera.create(this).openCamera(KYCCamera.TYPE_CAPTURE_ID_WO_CROP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == KYCCamera.RESULT_CODE) {
            //Get image path，display image
            val path = KYCCamera.getImagePath(data)
            if (!TextUtils.isEmpty(path)) {
                if (requestCode == KYCCamera.TYPE_AADHAARCARD_FRONT) { //Front of AADHAAR card
                    adharFrontImage.setImageBitmap(BitmapFactory.decodeFile(path))
                } else if (requestCode == KYCCamera.TYPE_CAPTURE_FACE_WO_CROP) {  //Reverse side of AADHAAR card
                    frontFaceImage.setImageBitmap(BitmapFactory.decodeFile(path))
                } else if (requestCode == KYCCamera.TYPE_CAPTURE_ID_WO_CROP) {  //Front of PAN card
                    frontPanImage.setImageBitmap(BitmapFactory.decodeFile(path))
                }
            }
        }
    }
}
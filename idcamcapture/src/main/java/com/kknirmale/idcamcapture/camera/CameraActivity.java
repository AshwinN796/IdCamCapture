package com.kknirmale.idcamcapture.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.kknirmale.idcamcapture.R;
import com.kknirmale.idcamcapture.cropper.CropImageView;
import com.kknirmale.idcamcapture.cropper.CropListener;
import com.kknirmale.idcamcapture.utils.CommonUtils;
import com.kknirmale.idcamcapture.utils.FileUtils;
import com.kknirmale.idcamcapture.utils.ImageUtils;
import com.kknirmale.idcamcapture.utils.PermissionUtils;
import com.kknirmale.idcamcapture.utils.ScreenUtils;

import java.io.File;

public class CameraActivity extends Activity implements View.OnClickListener {
    private CropImageView mCropImageView;
    private Bitmap mCropBitmap;
    private CameraPreview mCameraPreview;
    private View          mLlCameraCropContainer;
    private ImageView mIvCameraCrop;
    private ImageView     mIvCameraFlash;
    private View          mLlCameraOption;
    private View          mLlCameraResult;
    private TextView mViewCameraCropBottom;
    private FrameLayout mFlCameraOption;
    private View          mViewCameraCropLeft;

    private int     mType;
    private boolean isToast = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(this, KYCCamera.PERMISSION_CODE_FIRST,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        if (checkPermissionFirst) {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    if (isToast) {
                        Toast.makeText(this, "Please manually open the required permissions for the app", Toast.LENGTH_SHORT).show();
                        isToast = false;
                    }
                }
            }
        }
        isToast = true;
        if (isPermissions) {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "Allow all permissions");
            init();
        } else {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "Permission not allowed\n");
            finish();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void init() {
        setContentView(R.layout.activity_camera);
        mType = getIntent().getIntExtra(KYCCamera.TAKE_TYPE, 0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        if (mType == KYCCamera.TYPE_CAPTURE_FACE_FRONT) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }else  {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        initView();
        initListener();
    }

    private void initView() {
        mCameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mLlCameraCropContainer = findViewById(R.id.ll_camera_crop_container);
        mIvCameraCrop = (ImageView) findViewById(R.id.iv_camera_crop);
        mIvCameraFlash = (ImageView) findViewById(R.id.iv_camera_flash);
        mLlCameraOption = findViewById(R.id.ll_camera_option);
        mLlCameraResult = findViewById(R.id.ll_camera_result);
        mCropImageView = findViewById(R.id.crop_image_view);
        mViewCameraCropBottom = (TextView) findViewById(R.id.view_camera_crop_bottom);
        mFlCameraOption = (FrameLayout) findViewById(R.id.fl_camera_option);
        mViewCameraCropLeft = findViewById(R.id.view_camera_crop_left);

        float screenMinSize = Math.min(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        float screenMaxSize = Math.max(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        float height = (int) (screenMinSize * 0.90);
        float width = (int) (height * 75.0f / 47.0f);

        if (mType == KYCCamera.TYPE_CAPTURE_FACE_WO_CROP
            ||mType == KYCCamera.TYPE_CAPTURE_FACE_FRONT) {
            mCameraPreview.setFrontCamera(true);
        }else  {
            mCameraPreview.setFrontCamera(false);
        }

        float flCameraOptionWidth = (screenMaxSize - height) / 6;
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
        LinearLayout.LayoutParams cameraOptionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)flCameraOptionWidth);
        mLlCameraCropContainer.setLayoutParams(containerParams);
        mIvCameraCrop.setLayoutParams(cropParams);
        //Get the width of the "camera crop area" to dynamically set the width of the bottom "operation area" to center the "camera crop area"
        mFlCameraOption.setLayoutParams(cameraOptionParams);

        switch (mType) {
            case KYCCamera.TYPE_AADHAARCARD_FRONT:
                mIvCameraCrop.setImageResource(R.mipmap.af);
                break;
            case KYCCamera.TYPE_AADHAARCARD_BACK:
                mIvCameraCrop.setImageResource(R.mipmap.ab);
                break;
            case KYCCamera.TYPE_PANCARD_FRONT:
                mIvCameraCrop.setImageResource(R.mipmap.pf);
                break;
            case KYCCamera.TYPE_CAPTURE_FACE_FRONT:
                mIvCameraCrop.setImageResource(R.mipmap.try1);
                break;
            case KYCCamera.TYPE_CAPTURE_ID_WO_CROP:
                mIvCameraCrop.setImageResource(R.mipmap.try1);
                break;
            case KYCCamera.TYPE_CAPTURE_FACE_WO_CROP:
                mIvCameraCrop.setImageResource(R.mipmap.try1);
                break;
        }

        /*Added 0.5 second transition interface to solve the problem of slow startup of preview interface caused by individual mobile phone's first permission application*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraPreview.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }

    private void initListener() {
        mCameraPreview.setOnClickListener(this);
        mIvCameraFlash.setOnClickListener(this);
        findViewById(R.id.iv_camera_close).setOnClickListener(this);
        findViewById(R.id.iv_camera_take).setOnClickListener(this);
        findViewById(R.id.iv_camera_result_ok).setOnClickListener(this);
        findViewById(R.id.iv_camera_result_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_preview) {
            mCameraPreview.focus();
        } else if (id == R.id.iv_camera_close) {
            finish();
        } else if (id == R.id.iv_camera_take) {
            if (!CommonUtils.isFastClick()) {
                takePhoto();
            }
        } else if (id == R.id.iv_camera_flash) {
            if (CameraUtils.hasFlash(this)) {
                boolean isFlashOn = mCameraPreview.switchFlashLight();
                mIvCameraFlash.setImageResource(isFlashOn ? R.mipmap.camera_flash_on : R.mipmap.camera_flash_off);
            } else {
                Toast.makeText(this, R.string.no_flash, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.iv_camera_result_ok) {
            confirm();
        } else if (id == R.id.iv_camera_result_cancel) {
            mCameraPreview.setEnabled(true);
            mCameraPreview.addCallback();
            mCameraPreview.startPreview();
            mIvCameraFlash.setImageResource(R.mipmap.camera_flash_off);
            setTakePhotoLayout();
        }
    }

    /**
     * Take pictures
     */
    private void takePhoto() {
        mCameraPreview.setEnabled(false);
        CameraUtils.getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] bytes, Camera camera) {
                final Camera.Size size = camera.getParameters().getPreviewSize(); //Get preview size
                camera.stopPreview();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int w = size.width;
                        final int h = size.height;
                        Bitmap bitmap = ImageUtils.getBitmapFromByte(bytes, w, h);
                        if (mType == KYCCamera.TYPE_CAPTURE_ID_WO_CROP
                            || mType == KYCCamera.TYPE_CAPTURE_FACE_WO_CROP) {
                            confirmWithOutCrop(bitmap);
                        }else {
                            cropImage(bitmap);
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * Crop the picture
     */
    private void cropImage(Bitmap bitmap) {
        /*Calculate the coordinate points of the scan frame*/
        float left = mIvCameraCrop.getLeft();
        float top = mIvCameraCrop.getTop();
        float right = mIvCameraCrop.getRight();
        float bottom = mIvCameraCrop.getBottom();

        /*Calculate the ratio of the coordinate points of the scan frame to the coordinate points of the original image*/
        float leftProportion = left / mCameraPreview.getWidth();
        float topProportion = top / mCameraPreview.getHeight();
        float rightProportion = right / mCameraPreview.getWidth();
        float bottomProportion = bottom / mCameraPreview.getBottom();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        mCropBitmap = Bitmap.createBitmap(bitmap,
                (int) (leftProportion * (float) bitmap.getWidth()),
                (int) (topProportion * (float) bitmap.getHeight()),
                (int) (rightProportion * (float) bitmap.getWidth()),
                (int) (bottomProportion * (float) bitmap.getHeight()),matrix,true);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mCropImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mIvCameraCrop.getHeight()));
                setCropLayout();
                mCropImageView.setImageBitmap(mCropBitmap);
            }
        });
    }


    private void setCropLayout() {
        mIvCameraCrop.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.GONE);
        mLlCameraOption.setVisibility(View.GONE);
        mCropImageView.setVisibility(View.VISIBLE);
        mLlCameraResult.setVisibility(View.VISIBLE);
        mViewCameraCropBottom.setText("");
    }


    private void setTakePhotoLayout() {
        mIvCameraCrop.setVisibility(View.VISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mLlCameraOption.setVisibility(View.VISIBLE);
        mCropImageView.setVisibility(View.GONE);
        mLlCameraResult.setVisibility(View.GONE);
        mViewCameraCropBottom.setText(getString(R.string.touch_to_focus));

        mCameraPreview.focus();
    }


    private void confirm() {

        mCropImageView.crop(new CropListener() {
            @Override
            public void onFinish(Bitmap bitmap) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.crop_fail), Toast.LENGTH_SHORT).show();
                    finish();
                }

                String imagePath = new StringBuffer().append(FileUtils.getImageCacheDir(CameraActivity.this)).append(File.separator)
                        .append(System.currentTimeMillis()).append(".jpg").toString();
                if (ImageUtils.save(bitmap, imagePath, Bitmap.CompressFormat.JPEG)) {
                    Intent intent = new Intent();
                    intent.putExtra(KYCCamera.IMAGE_PATH, imagePath);
                    setResult(KYCCamera.RESULT_CODE, intent);
                    finish();
                }
            }
        }, true);
    }

    private void confirmWithOutCrop(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.crop_fail), Toast.LENGTH_SHORT).show();
            finish();
        }

        Matrix matrix = new Matrix();
        if (mType == KYCCamera.TYPE_CAPTURE_FACE_FRONT
            || mType == KYCCamera.TYPE_CAPTURE_FACE_WO_CROP) {
            matrix.postRotate(270);
        }else  {
            matrix.postRotate(90);
        }

        Bitmap fullSizeBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

        String imagePath = new StringBuffer().append(FileUtils.getImageCacheDir(CameraActivity.this)).append(File.separator)
                .append(System.currentTimeMillis()).append(".jpg").toString();
        if (ImageUtils.save(fullSizeBitmap, imagePath, Bitmap.CompressFormat.JPEG)) {
            Intent intent = new Intent();
            intent.putExtra(KYCCamera.IMAGE_PATH, imagePath);
            setResult(KYCCamera.RESULT_CODE, intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraPreview != null) {
            mCameraPreview.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraPreview != null) {
            mCameraPreview.onStop();
        }
    }
}
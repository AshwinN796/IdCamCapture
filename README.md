# IdCamCapture
Android IdCamCapture

**Note：** Camera only open in PORTRAIT ORIENTATION.

## Features
- Open Front Camera to capture face
- Get Captured result with or without cropping
- Support to turn on/off flash
- Support touch screen for focusing
- Support automatic clipping of images
- Support manual irregular clipping of images

## How to Use:
### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2. Add a gradle dependency
```java
dependencies {
	        implementation 'com.github.AshwinN796:IdCamCapture:v1.0.0'
	}
```

### Step 3. Open photographic interface
- Front facing camera without crop
```java
KYCCamera.create(this).openCamera(KYCCamera.TYPE_CAPTURE_FACE_WO_CROP);
```

- ID Capture without crop
```java
KYCCamera.create(this).openCamera(KYCCamera.TYPE_CAPTURE_FACE_WO_CROP);
```

- Front side of Pan Card
```java
KYCCamera.create(this).openCamera(KYCCamera.TYPE_PANCARD_FRONT);
```
**notice：** The parameters of the create() method are passed the context, activity.this is passed in Activity, and fragment.this is passed in Fragment.

### Step 4. Get a picture
```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == KYCCamera.RESULT_CODE) {
            //Get image path，display image
            final String path = KYCCamera.getImagePath(data);
            if (!TextUtils.isEmpty(path)) {
                if (requestCode == KYCCamera.TYPE_AADHAARCARD_FRONT) { //Front of AADHAAR card

                } else if (requestCode == KYCCamera.TYPE_CAPTURE_FACE_WO_CROP) {  //Front capture without crop

                } else if (requestCode == KYCCamera.TYPE_CAPTURE_FACE_FRONT) {  //Front photo with cropped result

                }

            }
        }
    }
```

### Clear Cache
```java
FileUtils.clearCache(this);
```

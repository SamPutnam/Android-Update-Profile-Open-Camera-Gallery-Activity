package com.samuelputnam.one;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.app.AlertDialog;
import android.widget.Button;
import android.content.DialogInterface;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.provider.MediaStore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import android.net.Uri;
import java.io.File;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

/** help from android: http://developer.android.com/training/camera/photobasics.html */

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
    private String mCurrentPhotoPath;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";
    private Uri mImageCaptureUri;
    private static final String IMAGE_UNSPECIFIED = "image/*";


    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView_Profile_Photo);
        mImageBitmap = null;
        if (savedInstanceState != null) {
            mImageCaptureUri = savedInstanceState
                    .getParcelable(URI_INSTANCE_STATE_KEY);
        }
        createImageFile();
        loadSnap();


        final Button button = (Button) findViewById(R.id.button_Change);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(R.string.pick_profile_picture);

                alertDialog.setItems(R.array.change_button_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            // Open Camera
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            startActivity(intent);
                            //Specify the uri of the image
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                            //startActivityForResult(intent, PICK_FROM_CAMERA);
                        }
                        if (which == 1){
                            //Select from Gallery
                            Intent intent2 = new Intent();
                            intent2.setType("image/*");
                            intent2.putExtra("crop", "true");
                            intent2.setAction(Intent.ACTION_GET_CONTENT);
                            intent2.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                            startActivityForResult(Intent.createChooser(intent2, "Complete action using"), PICK_FROM_FILE);
                        }

                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });
    }

   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode == RESULT_OK) {
           if (requestCode == PICK_FROM_CAMERA) {
               cropImage();
               Bundle extras = data.getExtras();
               // Set the picture image in UI
               mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
               File f = new File(mImageCaptureUri.getPath());
               if (f.exists())
                   f.delete();
           }
           if (requestCode == PICK_FROM_FILE) {
               cropImage();
               Bundle extras2 = data.getExtras();
               //mImageView.setImageBitmap((Bitmap) extras2.getParcelable("data"));
           }
       }
   }

// ****************** private helper functions ***************************//

    private void loadSnap() {
        // Load profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(mCurrentPhotoPath);
                Bitmap bmap = BitmapFactory.decodeStream(fis);
                mImageView.setImageBitmap(bmap);
                fis.close();
            }
        catch (IOException e) {
            // Default profile photo if no photo saved before.
            mImageView.setImageResource(R.drawable.dartmouth);
        }
    }
    private void saveSnap() {
        // Commit all the changes into preference file
        // Save profile image into internal storage.
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(mCurrentPhotoPath, MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     *
     */
    // A method that returns a unique file name for a new photo using a date-time stamp:
    private File createImageFile() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        } catch (IOException e) {
            mImageView.setImageResource(R.drawable.dartmouth);
            e.printStackTrace();
        }
        return image;
    }

    //With the createImageFile() method available to create a file for the photo,
    // you can now create and invoke the Intent like this:

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */
		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }
    /**
     * Invoke the system's media scanner to add your photo to the Media Provider's database,
     * making it available in the Android Gallery application and to other apps.
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    /**Some lifecycle callbacks so that the image can survive orientation change*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }
    /** Settings overflow menu option */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow, menu);
        return true;
    }

    // Crop and resize the image for profile
    private void cropImage() {
        // Use existing crop activity.
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);

        // Specify aspect ratio, 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
    }
}



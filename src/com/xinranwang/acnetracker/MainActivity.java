package com.xinranwang.acnetracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	//final static int CAMERA_RESULT = 0;
	static final int REQUEST_IMAGE_CAPTURE = 1;
	
	private AlbumDirFactory mAlbumStorageDirFactory;
	
	private String mCurrentPhotoPath;
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	ImageView lastPhoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		lastPhoto = (ImageView) this.findViewById(R.id.imageView1);
		
		mAlbumStorageDirFactory = new AlbumDirFactory();
		
		File[] existingFiles = getAlbumDir().listFiles();
		for (int i = 0; i < existingFiles.length; i++) {
			Log.v("MainActivity", existingFiles[i].getAbsolutePath());
			setPic(existingFiles[i].getAbsolutePath());
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_camera) {
			
			dispatchTakePictureIntent();
			
//			File imageFile = new File(imageFilePath);
//			Uri imageFileUri = Uri.fromFile(imageFile);
//			
//			Intent cameraintent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//			cameraintent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
//			startActivityForResult(cameraintent, CAMERA_RESULT);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	    	File f = null;
			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
	        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mCurrentPhotoPath != null) {
			setPic(mCurrentPhotoPath);
			galleryAddPic();
			mCurrentPhotoPath = null;
		}
	}
	
	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CREATE DIRECTORY", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
	
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}
	
	/* Add the Photo to a Gallery */
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}
	
	private void setPic(String photoPath) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = lastPhoto.getWidth();
		int targetH = lastPhoto.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(photoPath, bmOptions);
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
		Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		lastPhoto.setImageBitmap(bitmap);
//		mVideoUri = null;
//		lastPhoto.setVisibility(View.VISIBLE);
//		mVideoView.setVisibility(View.INVISIBLE);
	}

}

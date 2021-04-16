package com.jiangpeng.android.antrace;

import java.io.File;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import androidx.core.app.ActivityCompat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.StrictMode;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.internal.Internal;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements SideActivity {

	private static int CAMERA_STATUS_CODE = 111;
	private static int EDIT_IMAGE_CODE = 122;
	private static int SELECT_PHOTO = 100;
	private static int REQUEST_PERMISSION = 133;
	public static String PHOTO_FILE_TEMP_ = "__antrace.jpg";



	// on below line we are creating variables for
	// our array list, recycler view and adapter class.
	private static final int PERMISSION_REQUEST_CODE = 200;
	private ArrayList<String> imagePaths = new ArrayList<>();
	private RecyclerView imagesRV;
	private RecyclerViewAdapter imageRVAdapter;

	Button m_takePicture = null;
	Button m_selectPicture = null;
	Button m_about = null;
	protected String m_photoFile = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {



		requestPermission();


		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_takePicture = (Button)findViewById(R.id.take_picture);
		m_selectPicture = (Button)findViewById(R.id.select_picture);
		m_about = (Button)findViewById(R.id.about);

		OnClickListener takeListener = new TakePictureListener();
		m_takePicture.setOnClickListener(takeListener);

		OnClickListener selectListener = new SelectPictureListener();
		m_selectPicture.setOnClickListener(selectListener);

		OnClickListener aboutListener = new AboutListener();
		m_about.setOnClickListener(aboutListener);

		m_photoFile = FileUtils.getCacheDir(this) + FileUtils.sep + PHOTO_FILE_TEMP_;
		FileUtils.checkAndCreateFolder(FileUtils.getCacheDir(this));

		String svgFile = FileUtils.tempSvgFile(this);
		File file = new File(svgFile);
		file.delete();


		//super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);

		// we are calling a method to request
		// the permissions to read external storage.
		requestPermission();

		// creating a new array list and
		// initializing our recycler view.
		imagePaths = new ArrayList<>();
		imagesRV = findViewById(R.id.idRVImages);

		// calling a method to
		// prepare our recycler view.
		prepareRecyclerView();



		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
			{
				showMessageDialog(R.string.permission_warning,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityCompat.requestPermissions(MainActivity.this,
										new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
										REQUEST_PERMISSION);
							}
						});
				return;
			}
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_PERMISSION);
			return;
		}


	}

	private boolean checkPermission() {
		// in this method we are checking if the permissions are granted or not and returning the result.
		int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
		return result == PackageManager.PERMISSION_GRANTED;
	}

	/*private void requestPermissions() {
		if (checkPermission()) {
			// if the permissions are already granted we are calling
			// a method to get all images from our external storage.
			Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
			getImagePath();
		} else {
			// if the permissions are not granted we are
			// calling a method to request permissions.
			requestPermission();
		}
	}*/

	private void requestPermission() {
		//on below line we are requesting the rea external storage permissions.
		ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
	}

	private void prepareRecyclerView() {

		// in this method we are preparing our recycler view.
		// on below line we are initializing our adapter class.
		imageRVAdapter = new RecyclerViewAdapter(MainActivity.this, imagePaths);

		// on below line we are creating a new grid layout manager.
		GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);

		// on below line we are setting layout
		// manager and adapter to our recycler view.
		imagesRV.setLayoutManager(manager);
		imagesRV.setAdapter(imageRVAdapter);
	}

	private void getImagePath() {
		// in this method we are adding all our image paths
		// in our arraylist which we have created.
		// on below line we are checking if the device is having an sd card or not.
		boolean isSDPresent = true; //android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

		if (isSDPresent) {

			// if the sd card is present we are creating a new list in
			// which we are getting our images data with their ids.
			final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

			// on below line we are creating a new
			// string to order our images by string.
			final String orderBy = MediaStore.Images.Media._ID;

			// this method will stores all the images
			// from the gallery in Cursor
			Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

			// below line is to get total number of images
			int count = cursor.getCount();

			// on below line we are running a loop to add
			// the image file path in our array list.
			for (int i = 0; i < count; i++) {

				// on below line we are moving our cursor position
				cursor.moveToPosition(i);

				// on below line we are getting image file path
				int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

				// after that we are getting the image file path
				// and adding that path in our array list.
				imagePaths.add(cursor.getString(dataColumnIndex));
			}
			imageRVAdapter.notifyDataSetChanged();
			// after adding the data to our
			// array list we are closing our cursor.
			cursor.close();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		// this method is called after permissions has been granted.
		switch (requestCode) {
			// we are checking the permission code.
			case PERMISSION_REQUEST_CODE:
				// in this case we are checking if the permissions are accepted or not.
				if (grantResults.length > 0) {
					boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (storageAccepted) {
						// if the permissions are accepted we are displaying a toast message
						// and calling a method to get image path.
						Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
						getImagePath();
					} else {
						// if permissions are denied we are closing the app and displaying the toast message.
						Toast.makeText(this, "Permissions denined, Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
					}
				}
				break;
		}
	}

	private void showMessageDialog(int str, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage(str)
				.setPositiveButton("OK", okListener)
				.create()
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class TakePictureListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(m_photoFile)));

			try
			{
				startActivityForResult(i, CAMERA_STATUS_CODE);
			}
			catch(ActivityNotFoundException err)
			{
				Toast t = Toast.makeText(MainActivity.this, err.getLocalizedMessage(), Toast.LENGTH_SHORT);
				t.show();
			}
		}
	}

	class SelectPictureListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}
	}

	class AboutListener implements OnClickListener
	{
		@Override
		public void onClick(View view)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			View v = getLayoutInflater().inflate(R.layout.about_dialog, (ViewGroup) findViewById(R.id.about_layout));
			//TextView tv = (TextView) v.findViewById(R.id.appVersion);

			String ver = getResources().getString(R.string.app_version, getResources().getString(R.string.app_name),
					Utils.getCurrentVersionName(MainActivity.this));

			builder.setTitle(ver);

			builder.setView(v);

			builder.setIcon(R.drawable.ic_launcher);

			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int whichButton)
						{

						}
					}
			);

			AlertDialog dialog = builder.create();

			dialog.show();
		}
	}

	static {
		System.loadLibrary("antrace");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if(requestCode == CAMERA_STATUS_CODE && resultCode == RESULT_OK)
		{
			launchPreviewActivity(m_photoFile);
			super.onActivityResult(requestCode, resultCode, intent);
			return;
		}

		if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK)
		{
			Uri selectedImage = intent.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			if(cursor == null)
			{
				super.onActivityResult(requestCode, resultCode, intent);
				return;
			}
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();

			launchPreviewActivity(filePath);
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	protected void launchPreviewActivity(String filename) {
		Intent i = new Intent();

		i.setClass(MainActivity.this, PreviewActivity.class);

		i.putExtra(PreviewActivity.FILENAME, filename);

		startActivityForResult(i, EDIT_IMAGE_CODE);

	}

}

package com.nxr.tpad.bitmapexample;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

public class BitmapExampleActivity extends Activity {

	// Used to initialize our screenview object for
	// drawing on to

	private static BitmapExampleView myBitmapView;

	private static CheckBox ShowButton;
	private static Button PictureButton;

	private static final int REQ_CODE_PICK_IMAGE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// set the content view to our layout .xml
		setContentView(R.layout.bitmap_example_view);

		// POSSIBLE BATTERY DRAIN!
		// Set the screen to be always on. Delete if you want screen to turn off
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// initialize screenview class object
		myBitmapView = (BitmapExampleView) findViewById(R.id.bitmapView);
		myBitmapView.setShowing(true);

		// Setup checkbox button and set it to be true by default
		ShowButton = (CheckBox) findViewById(R.id.checkBox1);
		ShowButton.setChecked(true);

		// Setup picture selection button
		PictureButton = (Button) findViewById(R.id.photoButton);

		// Action to take when show button is clicked.
		ShowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Here we just toggle the value of isShowing
				myBitmapView.setShowing(!myBitmapView.getShowing());

			}
		});

		// Action to take when picture selection button is clicked
		PictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// We must start a new intent to request data from the system's
				// image picker
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
			}
		});
	}

	// Following code modified from
	// http://stackoverflow.com/questions/9564644/null-pointer-exception-while-loading-images-from-gallery
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		// Parse the activity result
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri bitmapUri = imageReturnedIntent.getData();

				try {

					// set the display bitmap based on the bitmap we just got
					// back from our intent
					Bitmap b = Media.getBitmap(getContentResolver(), bitmapUri);
					myBitmapView.setBitmap(b);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			}
		}
	}

	@Override
	protected void onPause() {
		// Pauses our surfaceview thread
		super.onPause();
		// Stop our drawing thread (which runs in the screenview object) when
		// the screen is paused
		myBitmapView.pause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// stop our communication server when activity is exited from menu
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// resume the drawing thread (which runs in the screenview object) when
		// screen is resumed.
		myBitmapView.resume();

	}

}

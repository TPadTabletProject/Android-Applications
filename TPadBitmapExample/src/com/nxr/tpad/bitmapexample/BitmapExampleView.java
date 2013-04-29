package com.nxr.tpad.bitmapexample;

import com.nxr.tpad.lib.TPad;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;

public class BitmapExampleView extends SurfaceView implements Runnable {

	// Creates thread for drawing to run on
	Thread myThread = null;

	// Sets up boolean to check if thread is running
	private boolean isRunning = false;

	// Controls if the image is visible on the screen or not
	private boolean isShowing;

	// Output string used for debugging
	String displayString;

	// TPad Object for this activity
	private TPad myTpad;

	// Keeps track of touching on screen
	private boolean isTouching = false;

	// Controls timout timer
	private long touchTimer;
	private static final double TIMEOUT = 3000000000f;

	// Friction level variable
	private float friction;

	// Finger position variables
	private float px, py;

	// Bitmap position variables (these have a velocity component added on)
	private int bx, by;

	// Sets up Holder to manipulate the surface view for us
	private SurfaceHolder holder;

	// Set up bitmap variables for general use by the class
	private final Bitmap logobmp;
	private Bitmap displaybmp;

	/*
	 * Holds the HSV data for each pixel hsv[0] is hue 0-360 hsv[1] is
	 * saturation 0-1 hsv[2] is value 0-1
	 */
	public float[] hsv = new float[3];

	private VelocityTracker vTracker;
	private float vy, vx;

	// define canvas once so we are not constantly re-allocating memory in
	// our draw thread
	private Canvas c;

	/*
	 * Objects we will draw onto the canvas. Initialize everything you intend to
	 * use as a draw variables here
	 */
	private Paint black = new Paint();
	private Paint white = new Paint();
	private Paint blue = new Paint();
	private Paint bluetext = new Paint();

	// SurfaceView Constructor
	public BitmapExampleView(Context context, AttributeSet attrs) {

		super(context, attrs);
		// Passes surface view to our holder;
		holder = getHolder();

		/*
		 * Setup the paint variables here to certain colors/attributes
		 */

		black.setColor(Color.BLACK);
		black.setAlpha(255);
		black.setAntiAlias(true);

		white.setColor(Color.WHITE);

		bluetext.setColor(Color.BLUE);
		bluetext.setTextAlign(Paint.Align.LEFT);
		bluetext.setTextSize(24);
		bluetext.setAntiAlias(true);

		blue.setColor(Color.BLUE);
		blue.setDither(true);
		blue.setAlpha(200);

		// Import defauly background and set as initial display bmp
		logobmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.logoblackfinal);
		displaybmp = logobmp;

		// Instantiate new TPad and start communication with it
		myTpad = new TPad();
		myTpad.startTPad();

		isFocusable();
		isFocusableInTouchMode();

	}

	public boolean getTouching() {

		return isTouching;
	}

	public boolean getShowing() {

		return isShowing;
	}

	public void setBitmap(Bitmap bmp) {

		displaybmp = bmp;

	}

	public void setShowing(boolean show) {

		isShowing = show;

	}

	// This method takes in a pixel value and maps it to a corresponding
	// friction
	// See the hsv declaration for info on what the array contains
	public float pixelToFriction(int pixel) {

		Color.colorToHSV(pixel, hsv);

		return hsv[2];
	}

	private void bitmapTasks() {

		// Here we make sure that the position coordinate are within the
		// displaying bitmap
		if (px >= displaybmp.getWidth() - 1)
			bx = displaybmp.getWidth() - 1;
		else if (px <= 0)
			bx = 0;
		else
			bx = (int) px;
		if (py >= displaybmp.getHeight() - 1)
			by = displaybmp.getHeight() - 1;
		else if (py <= 0)
			by = 0;
		else
			by = (int) py;

		// Read in the bitmap pixel value off and convert it to a fiction value
		friction = pixelToFriction(displaybmp.getPixel(bx, by));

		// Send the friction value to the TPad
		myTpad.send(friction);

	}

	// Handling touch events
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Update cursor positions
			px = event.getX();
			py = event.getY();

			// Start a new velocity tracker
			if (vTracker == null) {
				vTracker = VelocityTracker.obtain();
			} else {
				vTracker.clear();
			}
			vTracker.addMovement(event);

			// Call the timeout timer
			touchTimer = System.nanoTime();

			// Set touching to true
			isTouching = true;
			bitmapTasks();
			break;

		case MotionEvent.ACTION_MOVE:
			// Update old positions

			vTracker.addMovement(event);
			vTracker.computeCurrentVelocity(1000);
			vx = vTracker.getXVelocity();
			vy = vTracker.getYVelocity();

			px = event.getX() + vx / 30f;
			py = event.getY() + vy / 30f;

			touchTimer = System.nanoTime();
			bitmapTasks();
			break;

		case MotionEvent.ACTION_UP:

			isTouching = false;
			touchTimer = System.nanoTime();

			break;

		case MotionEvent.ACTION_CANCEL:
			vTracker.recycle();
			break;
		}

		return true;
	}

	// Below is the main background thread for performing other tasks
	// On this thread we do the drawing to the screen, and updating of state
	// variables except the finger position and friction.
	public void run() {

		// Make sure thread is running
		while (isRunning) {
			// checks to make sure the holder has a valid surfaceview in it,
			// if not then skip
			if (!holder.getSurface().isValid()) {
				continue;
			}

			displayString = String.valueOf(friction);

			if (System.nanoTime() > touchTimer + TIMEOUT)
				myTpad.send(0);

			/*
			 * The following code is where we do all of the drawing to the
			 * screen First we lock the canvas, then draw a white background.
			 * Then we draw text and our two circles
			 */

			// Lock canvas so we can manpipulate it
			c = holder.lockCanvas();

			c.drawARGB(255, 0, 0, 0);
			// Do all of our drawing to the canvas

			if (isShowing)
				c.drawBitmap(displaybmp, 0, 0, black);
			// c.drawCircle(px, py, 10, black);
			// c.drawText(displayString, 5, 25, white);

			// Unlock the canvas and draw it to the screen
			holder.unlockCanvasAndPost(c);

		}

	}

	// Takes care of stopping our drawing thread when the system is paused
	public void pause() {
		isRunning = false;
		while (true) {
			try {
				myThread.join();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		myThread = null;
	}

	// Takes care of resuming our thread when the system is resumed
	public void resume() {
		isRunning = true;
		myThread = new Thread(this);
		myThread.setPriority(Thread.MAX_PRIORITY - 4);
		myThread.start();
	}

}

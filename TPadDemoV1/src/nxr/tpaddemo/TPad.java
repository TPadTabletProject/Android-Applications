package nxr.tpaddemo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nxr.tpaddemo.AbstractServerListener;
import nxr.tpaddemo.Client;
import nxr.tpaddemo.Server;

import android.app.Activity;
import android.util.Log;

public class TPad extends Activity implements Runnable {

	public Server server;

	private AbstractServerListener serverListener;
	public boolean isConnected;
	private boolean isRunning;

	private long delay;
	private int pulses;
	final int MAXSAMPLES = 10;
	private float[] squareSamples;
	private float[] linearSamples = { 0f, .2f, .4f, .6f, .8f, 1f, .8f, .6f,
			.4f, .2f };

	private ByteBuffer b = ByteBuffer.allocate(4);
	final String TAG = null;

	private static final byte[] MAGCOMMAND = { 0x0a };
	private static final byte[] FREQCOMMAND = { 0x0b };
	
	public static final int LINEAR = 1;
	public static final int SQUARE = 2;

	public TPad() {
	}

	public void startTPad() {
		isConnected = false;
		isRunning = false;
		// starting the communication server
		try {
			server = new Server();
			server.start();
		} catch (IOException e) {
			Log.e(TAG, "Unable to start TCP server", e);
			System.exit(-1);
		}

		serverListener = initializeServerListener();
		server.addListener(serverListener);

		// Init square sample array
		squareSamples = new float[MAXSAMPLES];
		float temp = 0;
		for (int i = 0; i < MAXSAMPLES; i++) {
			squareSamples[i] = temp;
			if (temp == 0)
				temp = 1;
			else if (temp == 1)
				temp = 0;

		}

	}

	public void stopTPad() {
		server.stop();
	}

	public AbstractServerListener initializeServerListener() {

		return new AbstractServerListener() {
			@Override
			public void onClientConnect(Server server, Client client) {
				isConnected = true;
			};

			public void onClientDisconnect(Server server, Client client) {
				isConnected = false;
			}

		};
	}

	// Send data method for TPad: float 1.0 = Tpad turned on (low fiction) float
	// 0.0 = Tpad turned off (high friction)
	public void send(float friction) {

		try {
			server.send(concatAll(MAGCOMMAND,
					intToBytes(Math.round(friction * 1024f))));
		} catch (IOException e) {
			Log.e(TAG, "Error transmitting data to the PIC32", e);
		}

	}

	// Send data method for TPad: float 1.0 = Tpad turned on (low fiction) float
	// 0.0 = Tpad turned off (high friction)
	public void freq(int freq) {

		try {
			server.send(concatAll(FREQCOMMAND, intToBytes(freq)));
		} catch (IOException e) {
			Log.e(TAG, "Error transmitting data to the PIC32", e);
		}

	}

	// Convert an int to an array of bytes. The Tpad accepts an array of bytes,
	// and we send 4 bytes at a time, represening one int
	private byte[] intToBytes(int i) {
		b.clear();
		return b.order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
	}

	public void pulse(int pul, float mag, int del, int type) {

		pulses = pul;

		int count = 0;
		while (count < pulses) {

			for (int i = 0; i < MAXSAMPLES; i++) {
				if (type == LINEAR) {
					send(linearSamples[i] * mag);
					// Log.e(TAG, "Sample # " + String.valueOf(i));
					try {
						Thread.sleep(del);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else if (type == SQUARE ){
					send(squareSamples[i] * mag);
					// Log.e(TAG, "Sample # " + String.valueOf(i));
					try {
						Thread.sleep(del);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			count++;
		}

	}

	public static final byte[] concatAll(byte[]... arrays) {
		int totalLength = 0;
		for (byte[] array : arrays) {
			totalLength += array.length;
		}
		byte[] result = new byte[totalLength];
		int offset = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}

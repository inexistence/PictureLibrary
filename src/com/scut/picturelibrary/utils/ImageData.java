package com.scut.picturelibrary.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageData {
	int[] mPixel;
	int mWidth;
	int mHeight;

	public ImageData(Bitmap bmp) {
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();
		int length = mWidth * mHeight;
		mPixel = new int[length];
		bmp.getPixels(mPixel, 0, mWidth, 0, 0, mWidth, mHeight);
	}
	
	public ImageData(int[] pts, int width, int height) {
		mPixel = pts;
		mWidth = width;
		mHeight = height;
	}
	
	public ImageData clone() {
		int[] newPts = mPixel.clone(); 
		return new ImageData(newPts, mWidth, mHeight);
	}

	public int getR(int x, int y) {
		return Color.red(mPixel[x + y * mWidth]);
	}

	public int getG(int x, int y) {
		return Color.green(mPixel[x + y * mWidth]);
	}

	public int getB(int x, int y) {
		return Color.blue(mPixel[x + y * mWidth]);
	}

	public int getAlpha(int x, int y) {
		return Color.alpha(mPixel[x + y * mWidth]);
	}

	public void setARGB(int x, int y, int alpha, int r, int g, int b) {
		mPixel[x + y * mWidth] = Color.argb(alpha, r, g, b);
	}

	public void setRGB(int x, int y, int r, int g, int b) {
		mPixel[x + y * mWidth] = Color.rgb(r, g, b);
	}

	public void setPixelColor(int x, int y, int c) {
		mPixel[x + y * mWidth] = c;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int[] getPts() {
		return mPixel;
	}
}

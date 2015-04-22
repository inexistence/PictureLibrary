package com.scut.picturelibrary.utils;

import android.graphics.Bitmap;

public class ImageFilter {

	/**
	 * ����Ƭ
	 * 
	 * @param imageData
	 * @return
	 */
	public static ImageData oldPhotoFilter(ImageData imageData) {
		int width = imageData.getWidth();
		int height = imageData.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int oldR = imageData.getR(x, y);
				int oldG = imageData.getG(x, y);
				int oldB = imageData.getB(x, y);

				int newR = (int) (0.393f * oldR + 0.769f * oldG + 0.189 * oldB);
				int newG = (int) (0.349f * oldR + 0.686f * oldG + 0.168 * oldB);
				int newB = (int) (0.272f * oldR + 0.534f * oldG + 0.131 * oldB);

				newR = checkRGB(newR);
				newG = checkRGB(newG);
				newB = checkRGB(newB);

				imageData.setRGB(x, y, newR, newG, newB);
			}
		}
		return imageData;
	}

	/**
	 * �Ҷ�
	 * 
	 * @param imageData
	 * @return
	 */
	public static ImageData grayFilter(ImageData imageData) {
		int width = imageData.getWidth();
		int height = imageData.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int oldR = imageData.getR(x, y);
				int oldG = imageData.getG(x, y);
				int oldB = imageData.getB(x, y);
				// �Ҷ�ֵ�㷨
				// f(i,j)=0.30R(i,j)+0.59G(i,j)+0.11B(i,j))
				int newR = (int) (0.30f * oldR + 0.59f * oldG + 0.11f * oldB);
				newR = checkRGB(newR);

				imageData.setRGB(x, y, newR, newR, newR);
			}
		}
		return imageData;
	}

	/**
	 * ����
	 * 
	 * @param imageData
	 * @return
	 */
	public static ImageData comicFilter(ImageData imageData) {
		int width = imageData.getWidth();
		int height = imageData.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int oldR = imageData.getR(x, y);
				int oldG = imageData.getG(x, y);
				int oldB = imageData.getB(x, y);
				// R = |g �C b + g + r| * r / 256;
				int newR = (int) (Math.abs(oldG - oldB + oldG + oldR) * oldR / 256);
				// G = |b �C g + b + r| * r / 256;
				int newG = (int) (Math.abs(oldB - oldG + oldB + oldR) * oldR / 256);
				// B = |b �C g + b + r| * g / 256;
				int newB = (int) (Math.abs(oldB - oldG + oldB + oldR) * oldG / 256);

				newR = checkRGB(newR);
				newG = checkRGB(newG);
				newB = checkRGB(newB);

				// �Ҷ�ֵ�㷨
				// f(i,j)=0.30R(i,j)+0.59G(i,j)+0.11B(i,j))
				newR = newG = newB = (int) (0.30f * newR + 0.59f * newG + 0.11f * newB);

				imageData.setRGB(x, y, newR, newG, newB);
			}
		}
		return imageData;
	}

	/**
	 * �����Աȶ�
	 */
	public static ImageData brightContrastFilter(ImageData imageData) {
		float BrightnessFactor = 0.25f;
		float ContrastFactor = 0f; // should be [-1,1]
		int width = imageData.getWidth();
		int height = imageData.getHeight();

		int bfi = (int) (BrightnessFactor * 255);
		float cf = 1f + ContrastFactor;
		cf *= cf;
		int cfi = (int) (cf * 32768) + 1;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int oldR = imageData.getR(x, y);
				int oldG = imageData.getG(x, y);
				int oldB = imageData.getB(x, y);

				int r = 0, g = 0, b = 0;

				// Modify brightness (addition)
				if (bfi != 0) {
					// Add brightness
					int ri = oldR + bfi;
					int gi = oldG + bfi;
					int bi = oldB + bfi;
					// Clamp to byte boundaries
					r = checkRGB(ri);
					g = checkRGB(gi);
					b = checkRGB(bi);
				}
				// Modifiy contrast (multiplication)
				if (cfi != 32769) {
					// Transform to range [-128, 127]
					int ri = r - 128;
					int gi = g - 128;
					int bi = b - 128;

					// Multiply contrast factor
					ri = (ri * cfi) >> 15;
					gi = (gi * cfi) >> 15;
					bi = (bi * cfi) >> 15;

					// Transform back to range [0, 255]
					ri = ri + 128;
					gi = gi + 128;
					bi = bi + 128;

					// Clamp to byte boundaries
					r = checkRGB(ri);
					g = checkRGB(gi);
					b = checkRGB(bi);
				}

				imageData.setRGB(x, y, r, g, b);
			}
		}
		return imageData;
	}

	/**
	 * �ڰ�
	 * 
	 * @param imageData
	 * @return
	 */
	public static ImageData whiteBlackFilter(ImageData imageData) {
		int width = imageData.getWidth();
		int height = imageData.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int oldR = imageData.getR(x, y);
				int oldG = imageData.getG(x, y);
				int oldB = imageData.getB(x, y);

				int newP = (oldR + oldG + oldB) / 3;
				newP = (newP >= 127 ? 255 : 0);

				imageData.setRGB(x, y, newP, newP, newP);
			}
		}
		return imageData;
	}

	/**
	 * ��
	 * 
	 * @param image
	 * @return
	 */
	public static ImageData featherFilter(ImageData image) {
		float Size = 0.5f;
		int width = image.getWidth();
		int height = image.getHeight();
		int ratio = width > height ? height * 32768 / width : width * 32768
				/ height;

		int cx = width >> 1;
		int cy = height >> 1;
		int max = cx * cx + cy * cy;
		int min = (int) (max * (1 - Size));
		int diff = max - min;

		int R, G, B;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				R = image.getR(x, y); // ��ȡRGB��ԭɫ
				G = image.getG(x, y);
				B = image.getB(x, y);

				// Calculate distance to center and adapt aspect ratio
				int dx = cx - x;
				int dy = cy - y;
				if (width > height) {
					dx = (dx * ratio) >> 15;
				} else {
					dy = (dy * ratio) >> 15;
				}
				int distSq = dx * dx + dy * dy;
				float v = ((float) distSq / diff) * 255;
				R = (int) (R + (v));
				G = (int) (G + (v));
				B = (int) (B + (v));
				R = checkRGB(R);
				G = checkRGB(G);
				B = checkRGB(B);
				image.setRGB(x, y, R, G, B);
			}
		}
		return image;
	}

	public static int checkRGB(int value) {
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}

	public static Bitmap createBitmap(ImageData data) {
		Bitmap bitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(),
				Bitmap.Config.RGB_565);
		bitmap.setPixels(data.getPts(), 0, data.getWidth(), 0, 0,
				data.getWidth(), data.getHeight());
		return bitmap;
	}

	public static Bitmap redrawBitmap(Bitmap bmp, ImageData imageData) {

		return bmp;
	}
}

package com.scut.picturelibrary.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

public class FileUtil {

	public static final int MEDIA_TYPE_IMAGE = 1;

	public static final int MEDIA_TYPE_RECORDER = 3;

	public static File getOutPutMediaFile(int type) {
		// 文件保存路径
		File mediaStorageDir;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaStorageDir = new File(
					Environment.getExternalStorageDirectory(), "PictureLibrary"
							+ File.separator + "Pictures");
		} else if (type == MEDIA_TYPE_RECORDER) {
			mediaStorageDir = new File(
					Environment.getExternalStorageDirectory(), "PictureLibrary"
							+ File.separator + "Videos");
		} else {
			return null;
		}
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// 获取当前时间作为文件名
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_RECORDER) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".3gp");
		} else {

			return null;
		}

		return mediaFile;
	}

	/**
	 * 根据文件路径扫描文件
	 * 
	 * @param context
	 * @param filePath
	 */
	public static void scanFiles(Context context, String filePath) {
		if (context == null || filePath == null)
			return;
		MediaScannerConnection.scanFile(context, new String[] { filePath },
				null, null);
	}
}

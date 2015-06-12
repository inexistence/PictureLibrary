package com.scut.picturelibrary;

import android.provider.MediaStore;

/**
 * 存放全局常量
 * 
 * @author 黄建斌
 */
public class Constants {
	public final static String SORT_BY_NAME = MediaStore.Images.Media.DISPLAY_NAME;
	public final static String SORT_BY_BUCKET_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
	public final static String SORT_BY_DATE = MediaStore.Images.Media.DATE_MODIFIED;

	// 文件夹默认排序方式
	public final static String BUCKET_SORT_DEFAULT = SORT_BY_DATE;
	// 文件默认排序方式
	public final static String FILE_SORT_DEFAULT = SORT_BY_DATE;

	public static String IMAGE_URLS = "com.scut.picturelibrary.IMAGE_URLS";
	public static String IMAGE_POSITION = "com.scut.picturelibrary.IMAGE_POSITION";

	public static float[] EDIT_IMAGE_SCALES = { 1.0f, 4.0f / 3.0f, 3.0f / 4.0f };
	public static String[] FONTS = { "fonts/fzfys.TTF", "fonts/fzjh.otf",
			"fonts/fzltxh.TTF", "fonts/fzqkbys.TTF", "fonts/fzygytk.ttf",
			"fonts/fzzdh.ttf", "fonts/txjzh.ttf", "fonts/xdhbbdz.ttf" };
	public static int[] FONTS_DRAWABLE_ID = { R.drawable.bg_font1,
			R.drawable.bg_font2, R.drawable.bg_font3, R.drawable.bg_font4,
			R.drawable.bg_font5, R.drawable.bg_font6, R.drawable.bg_font7,
			R.drawable.bg_font8 };
}

package com.scut.picturelibrary.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Image功能类
 * 
 * @author 黄建斌
 * 
 */
public class ImageUtil {

	/**
	 * 计算保持原图比例需要的缩放比例，使得图片高宽小于等于指定的高宽
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 保持原图片比例的略缩图
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromPath(String pathName,
			int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(String videoPath, int width,
			int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获得指定ImageView的高宽
	 * 
	 * @param view
	 * @return
	 */
	public static ImageSize getImageViewSize(View view) {

		ImageSize imageSize = new ImageSize();
		DisplayMetrics displayMetrics = view.getContext().getResources()
				.getDisplayMetrics();

		LayoutParams lp = view.getLayoutParams();

		int width = view.getWidth();// 获取imageview的实际宽度
		if (width <= 0) {
			width = lp.width;// 获取imageview在layout中声明的宽度
		}
		// if (width <= 0) {
		// width = imageView.getMaxWidth(); // 检查最大值
		// }
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}

		int height = view.getHeight();// 获取imageview的实际高度
		if (height <= 0) {
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		// if (height <= 0) {
		// height = imageView.getMaxHeight();// 检查最大值
		// }
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}
		imageSize.width = width;
		imageSize.height = height;

		return imageSize;
	}
	public static Bitmap decodeInSampleFromPath(String pathName, int reqWidth,
			int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
	public static Bitmap decodeInSampleBitmapFromUri(
			ContentResolver contextResolver, Uri uri, int reqWidth,
			int reqHeight) {
		Cursor cursor = contextResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Images.Media.DATA));
		return decodeInSampleFromPath(path, reqWidth, reqHeight);
	}
	/**
	 * 获取View的bitmap
	 * @param v
	 * @return
	 */
	public static Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);
		
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);

		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);

		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);

		return bitmap;
	}
	
	public static class ImageSize {
		public int width;
		public int height;
	}
}

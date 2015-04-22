package com.scut.picturelibrary.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.scut.picturelibrary.R;

/**
 * 使用外部ImageLoader库的Adapter 同时显示图片和视频的略缩图
 * 
 * @author 黄建斌
 * 
 */
public class MediaFilesAdapter extends CursorAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "MediaFilesAdapter";
	private int mImageHoverResource = 0;
	private int mVideoHoverResource = R.drawable.ic_play;

	public MediaFilesAdapter(Context context, Cursor c) {
		super(context, c, true);
	}

	public void setImageHoverResource(int id) {
		mImageHoverResource = id;
	}

	public void setVideoHoverResource(int id) {
		mVideoHoverResource = id;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		if (cursor == null)
			return;
		ViewHolder viewHolder = (ViewHolder) v.getTag();
		// 使用外部库ImageLoader进行图片缓存和异步加载显示
		int typeIndex = cursor.getColumnIndex("type");
		String id = cursor.getString(cursor
				.getColumnIndex(MediaStore.Images.Media._ID));
		if (typeIndex >= 0 && cursor.getString(typeIndex).equals("video")) {
			// 视频格式的略缩图
			ImageLoader.getInstance().displayImage(
					"content://media/external/video/media/" + id,
					viewHolder.imgThumbnail);
			viewHolder.imgHover.setImageResource(mVideoHoverResource);
		} else {
			// 图片略缩图
			ImageLoader.getInstance().displayImage(
					"content://media/external/images/media/" + id,
					viewHolder.imgThumbnail);
			viewHolder.imgHover.setImageResource(mImageHoverResource);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.grid_files_item, parent, false);
		ImageView photo = (ImageView) v
				.findViewById(R.id.img_grid_files_item_photo);
		ImageView hover = (ImageView) v
				.findViewById(R.id.img_grid_files_item_hover);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.imgThumbnail = photo;
		viewHolder.imgHover = hover;
		// 设置Tag
		v.setTag(viewHolder);
		return v;
	}

	/**
	 * 获取要显示的图片的路径
	 * 
	 * @param index
	 * @return
	 */
	public String getPath(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
	}

	@SuppressWarnings("deprecation")
	public String getTime(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		Calendar cal = Calendar.getInstance();
		// DATE_MODIFIED获取的为秒数要转成秒
		cal.setTimeInMillis(Long.valueOf(c.getString(c
				.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) + "000"));
		return cal.getTime().toLocaleString();
	}

	public String getFileSize(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		String filesize = "11";
		int Filesize;
		DecimalFormat df = new DecimalFormat("#.00");
		String path = c.getString(c
				.getColumnIndex(MediaStore.Images.Media.DATA));
		File file = new File(path);
		try {
			FileInputStream fi = new FileInputStream(file);
			// 可获取的剩余字节数
			Filesize = fi.available();
			if (Filesize < 1024) {
				filesize = Filesize + "B";
			} else if (Filesize < 1024 * 1024) {
				filesize = (double) (Filesize / 1024) + "KB";
			} else if (Filesize < 1024 * 1024 * 1024) {
				filesize = df.format((double) (Filesize)
						/ (double) (1024 * 1024))
						+ "MB";
			} else {
				filesize = df.format((double) (Filesize)
						/ (double) (1024 * 1024 * 1024))
						+ "GB";
			}
			fi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return filesize;
	}

	public String getImageSize(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		String path = c.getString(c
				.getColumnIndex(MediaStore.Images.Media.DATA));
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		// 只获取图片尺寸不占用内存
		BitmapFactory.decodeFile(path, opts);
		int x = opts.outHeight;
		int y = opts.outWidth;
		if (x > y)
			return x + "*" + y;
		else
			return y + "*" + x;
	}

	public String getType(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c.getColumnIndex("type"));
	}

	public String getTitle(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c
				.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
	}

	public String getVideoTime(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		String videotime = "";
		// 获取电影时长
		int VideoTime = c.getInt(c
				.getColumnIndex(MediaStore.Video.Media.DURATION)) / 1000;
		if (VideoTime < 60) {
			videotime = VideoTime < 10 ? "00:0" + VideoTime : "00:" + VideoTime;
		} else if (VideoTime < 3600) {
			videotime = VideoTime - VideoTime / 60 * 60 < 10 ? (VideoTime / 60 < 10 ? "0"
					+ VideoTime / 60 + ":0" + (VideoTime - VideoTime / 60 * 60)
					: "0" + VideoTime / 60 + ":"
							+ (VideoTime - VideoTime / 60 * 60))
					: VideoTime / 60 + ":" + (VideoTime - VideoTime / 60 * 60);
		} else {
			String mintime = "";
			int hour = VideoTime / 3600;
			int MinTime = VideoTime - VideoTime / 3600 * 3600;
			if (MinTime < 60) {
				mintime = MinTime < 10 ? "00:0" + MinTime : "00:" + MinTime;
			} else {
				mintime = MinTime - MinTime / 60 * 60 < 10 ? (MinTime / 60 < 10 ? "0"
						+ MinTime / 60 + ":0" + (MinTime - MinTime / 60 * 60)
						: "0" + MinTime / 60 + ":"
								+ (MinTime - MinTime / 60 * 60))
						: MinTime / 60 + ":" + (MinTime - MinTime / 60 * 60);
			}
			videotime = hour < 10 ? "0" + hour + ":" + mintime : hour + ":"
					+ mintime;
		}
		return videotime;

	}

	@SuppressLint("NewApi")
	public String getVideoSize(int index) {
		Cursor c = getCursor();
		String path = c
				.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(path);
		Bitmap bm = retriever.getFrameAtTime(1);
		int x = bm.getWidth();
		int y = bm.getHeight();
		if (x > y)
			return x + "*" + y;
		else
			return y + "*" + x;

	}

	/**
	 * 静态类 防止内存泄漏
	 * 
	 * @author 黄建斌
	 * 
	 */
	static class ViewHolder {
		ImageView imgThumbnail;
		ImageView imgHover;
	}
}

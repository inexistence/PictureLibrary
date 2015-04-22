package com.scut.picturelibrary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.scut.picturelibrary.R;

/**
 * 显示文件夹
 * 
 * @author 黄建斌
 * 
 */
public class MediaFoldersAdapter extends CursorAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = "MediaFoldersAdapter";

	public MediaFoldersAdapter(Context context, Cursor c) {
		super(context, c, true);
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

		} else {
			// 图片略缩图
			ImageLoader.getInstance().displayImage(
					"content://media/external/images/media/" + id,
					viewHolder.imgThumbnail);
		}
		viewHolder.txtName.setText(cursor.getString(cursor
				.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)));
		// 设置文件夹内文件数量
		viewHolder.txtNum.setText("("
				+ cursor.getString(cursor.getColumnIndex("num")) + ")");
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.grid_folders_item, parent, false);
		ImageView photo = (ImageView) v
				.findViewById(R.id.img_grid_folders_item_photo);
		TextView name = (TextView) v
				.findViewById(R.id.txt_grid_folders_item_name);
		TextView num = (TextView) v
				.findViewById(R.id.txt_grid_folders_item_num);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.imgThumbnail = photo;
		viewHolder.txtName = name;
		viewHolder.txtNum = num;
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

	public String getFolderPath(int index) {
		String path = getPath(index);
		int pivot = path.lastIndexOf("//");
		return path.substring(0, pivot);
	}

	public String getBucketId(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
	}

	public String getBuckName(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c
				.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
	}

	/**
	 * 静态类 防止内存泄漏
	 * 
	 * @author 黄建斌
	 * 
	 */
	static class ViewHolder {
		ImageView imgThumbnail;
		TextView txtName;
		TextView txtNum;
	}
}

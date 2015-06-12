package com.scut.picturelibrary.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.activity.EditActivity;
import com.scut.picturelibrary.activity.RecognizeImageActivity;
import com.scut.picturelibrary.utils.ShareUtil;

/**
 * 对话框管理
 * 
 * @author 黄建斌
 * 
 */
@SuppressLint("InflateParams")
public class DialogManager {
	private static Dialog mDialog;
	private static Dialog nDialog;
	private static ProgressDialog mProgressDialog;

	// 显示图片长按菜单
	public static void showImageItemMenuDialog(final Context context,
			String title, final String filename, final String path,
			final String filesize, final String size, final String time) {
		dismissDialog();
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				context);
		// 设置对话框的标题
		builder.setTitle(title);
		builder.setItems(new String[] { "识图", "分享", "属性", "编辑" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:// 识图
							Intent intent = new Intent();
							intent.setClass(context,
									RecognizeImageActivity.class);
							intent.putExtra("path", path);
							intent.putExtra("filename", filename);
							context.startActivity(intent);
							break;
						case 1:// 分享
							ShareUtil.showShare(context, path);
							break;
						case 2:// 属性
							DialogManager.showImagePropertyDialog(context,
									filename, path, filesize, size, time);
							break;
						case 3:// 编辑
							Intent it = new Intent();
							it.setClass(context, EditActivity.class);
							it.putExtra("path", path);
//							it.putExtra("uri", "file:///" + path);
							context.startActivity(it);
							break;
						default:
							break;
						}

					}
				});
		mDialog = builder.create();
		mDialog.show();
	}

	public static void showImageItemMenuDialog(Context context, String title,
			DialogInterface.OnClickListener listener) {
		dismissDialog();
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				context);
		// 设置对话框的标题
		builder.setTitle(title);
		builder.setItems(new String[] { "识图", "分享", "属性", "编辑" }, listener);
		// 创建一个列表对话框
		mDialog = builder.create();
		mDialog.show();
	}

	// 显示视频长按菜单
	public static void showVideoItemMenuDialog(final Context context,
			String title, final String filename, final String path,
			final String filesize, final String size, final String videotime,
			final String time, final int videosecond) {
		dismissDialog();
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				context);
		// 设置对话框的标题
		builder.setTitle(title);
		builder.setItems(new String[] { "预览", "属性" }, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {// 预览
				case 0:
					DialogManager.showVideoPreview(context, path, videosecond);
					break;
				// 属性
				case 1:
					DialogManager.showVideoPropertyDialog(context, filename,
							path, filesize, size, videotime, time);
					break;
				default:
					break;
				}
			}
		});
		// 创建一个列表对话框
		mDialog = builder.create();
		mDialog.show();
	}

	// 显示图片属性对话框
	public static void showImagePropertyDialog(Context context, String title,
			String path, String filesize, String size, String time) {// 因为layout没被载入所以要获取布局文件对象
		LayoutInflater inflater = LayoutInflater.from(context);

		View layout = inflater.inflate(R.layout.dialog_property, null);
		android.app.AlertDialog.Builder builder_Property = new android.app.AlertDialog.Builder(
				context);
		TextView path_textview = (TextView) layout.findViewById(R.id.path);
		path_textview.setText(path);

		TextView time_textview = (TextView) layout.findViewById(R.id.time);
		time_textview.setText(time);

		TextView filesize_textview = (TextView) layout
				.findViewById(R.id.filesize);
		filesize_textview.setText(filesize);

		TextView size_textview = (TextView) layout.findViewById(R.id.size);
		size_textview.setText(size);

		builder_Property.setTitle(title).setView(layout)
				.setPositiveButton("确定", null);
		nDialog = builder_Property.create();
		nDialog.show();

	}

	// 显示视频属性对话框
	public static void showVideoPropertyDialog(Context context, String title,
			String path, String filesize, String size, String videotime,
			String time) {// 因为layout没被载入所以要获取布局文件对象
		LayoutInflater inflater = LayoutInflater.from(context);
		// 不能导入android.R,ctrl+shift+o
		View layout = inflater.inflate(R.layout.dialog_property, null);
		android.app.AlertDialog.Builder builder_Property = new android.app.AlertDialog.Builder(
				context);
		TextView path_textview = (TextView) layout.findViewById(R.id.path);
		TextView size_textview = (TextView) layout.findViewById(R.id.size);
		TextView time_textview = (TextView) layout.findViewById(R.id.time);
		TextView filesize_textview = (TextView) layout
				.findViewById(R.id.filesize);
		TextView time_name_textview = (TextView) layout
				.findViewById(R.id.time_name);
		TextView video_textview = (TextView) layout.findViewById(R.id.video);
		TextView video_name_textview = (TextView) layout
				.findViewById(R.id.video_name);

		path_textview.setText(path);
		time_textview.setText(videotime);
		video_textview.setVisibility(View.VISIBLE);
		video_name_textview.setVisibility(View.VISIBLE);
		video_textview.setText(time);
		filesize_textview.setText(filesize);
		time_name_textview.setText("时长");
		size_textview.setText(size);

		builder_Property.setTitle(title).setView(layout)
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}

				});
		nDialog = builder_Property.create();
		nDialog.show();

	}

	// 影片预览
	public static void showVideoPreview(Context context, String path, int time) {// 初始化对话框布局
		LayoutInflater l = LayoutInflater.from(context);
		View v = l.inflate(R.layout.video_preview, null);
		GridView gw = (GridView) v.findViewById(R.id.gw);
		MediaMetadataRetriever md = new MediaMetadataRetriever();
		md.setDataSource(path);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (time < 9) {
			for (int i = 1; i < time + 1; i++) {
				// 影片截图
				Bitmap bmp = md.getFrameAtTime(i * 1000 * 1000,
						MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("img", bmp);
				list.add(map);
			}
		} else {
			for (int i = time / 9; i < time + 1; i = i + time / 9) {
				Bitmap bmp = md.getFrameAtTime(i * 1000 * 1000,
						MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("img", bmp);
				list.add(map);
			}
		}
		SimpleAdapter ada = new SimpleAdapter(context, list,
				R.layout.grid_preview_item, new String[] { "img" },
				new int[] { R.id.img_grid_preview });
		ada.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				// simpleadapter中放bitmap
				if ((view instanceof ImageView) & (data instanceof Bitmap)) {
					ImageView iv = (ImageView) view;
					Bitmap bmp = (Bitmap) data;
					iv.setImageBitmap(bmp);
					return true;
				}
				return false;
			}
		});
		gw.setAdapter(ada);
		android.app.AlertDialog.Builder builder_Preview = new android.app.AlertDialog.Builder(
				context);
		builder_Preview.setTitle("预览").setView(v)
				.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		builder_Preview.create().show();
	}

	// 显示进度条
	public static void showProgressDialog(Context context,
			ProgressDialog.OnClickListener cancelListener) {
		dismissDialog();
		// 构建一个下载进度条
		// 创建ProgressDialog对象
		mProgressDialog = new ProgressDialog(context);
		// 设置进度条风格，风格为长形
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle("上传图片中");
		mProgressDialog.setProgress(0);
		// 设置ProgressDialog 的进度条是否不明确
		mProgressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		mProgressDialog.setCancelable(false);
		// 设置ProgressDialog 的一个Button
		if (cancelListener != null) {
			mProgressDialog.setCancelable(true);
			mProgressDialog.setButton(0, "取消", cancelListener);
		}
		// 让ProgressDialog显示
		mProgressDialog.show();
	}

	public static ProgressDialog getProgressDialog() {
		return mProgressDialog;
	}

	public static void showSimpleDialog(Context ctx, String title,
			String message, DialogInterface.OnCancelListener onCancelListener) {
		dismissDialog();
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				ctx);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(false);
		if (onCancelListener != null) {
			builder.setCancelable(true);
			builder.setOnCancelListener(onCancelListener);
		}
		// 创建一个列表对话框
		mDialog = builder.create();
		mDialog.show();
	}

	public static void dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

}

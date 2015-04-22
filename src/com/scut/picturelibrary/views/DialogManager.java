package com.scut.picturelibrary.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.scut.picturelibrary.R;

/**
 * 对话框管理
 * 
 * @author 黄建斌
 * 
 */
public class DialogManager {
	private static Dialog mDialog;
	private static Dialog nDialog;
	private static ProgressDialog mProgressDialog;

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

	public static void showVideoItemMenuDialog(Context context, String title,
			DialogInterface.OnClickListener listener) {
		dismissDialog();
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				context);

		// 设置对话框的标题
		builder.setTitle(title);
		builder.setItems(new String[] { "属性" }, listener);
		// 创建一个列表对话框
		mDialog = builder.create();
		mDialog.show();
	}

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
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}

				});
		nDialog = builder_Property.create();
		nDialog.show();

	}

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
		TextView filesize_textview = (TextView) layout.findViewById(R.id.filesize);
		TextView time_name_textview = (TextView) layout.findViewById(R.id.time_name);
		TextView video_textview = (TextView) layout.findViewById(R.id.video);
		TextView video_name_textview = (TextView) layout.findViewById(R.id.video_name);
		
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

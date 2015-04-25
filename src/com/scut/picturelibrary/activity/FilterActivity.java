package com.scut.picturelibrary.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.manager.CameraManager;
import com.scut.picturelibrary.utils.FileUtil;
import com.scut.picturelibrary.utils.ImageData;
import com.scut.picturelibrary.utils.ImageFilter;
import com.scut.picturelibrary.views.DialogManager;

public class FilterActivity extends Activity {
	ImageView mReviewImageView;
	Bitmap mLoadedBitmap;
	ImageData originData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		String uri = getIntent().getStringExtra("uri");

		mReviewImageView = (ImageView) findViewById(R.id.img_filter_review);

		ImageLoader.getInstance().displayImage(uri, mReviewImageView, null,
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						mLoadedBitmap = loadedImage;
						originData = new ImageData(loadedImage);
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

					}
				});

		ViewGroup vp = (ViewGroup) findViewById(R.id.hscrollview_filter_btns);
		for (int i = 0; i < vp.getChildCount(); i++) {
			vp.getChildAt(i).setOnClickListener(mOnClickListener);
		}

		Button saveBtn = (Button) findViewById(R.id.btn_filter_save);
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogManager.dismissDialog();
				DialogManager.showSimpleDialog(FilterActivity.this, "保存中",
						"正在保存，请稍候...", null);
				savePictureTask task = new savePictureTask();
				task.execute(mLoadedBitmap);
			}
		});
	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mLoadedBitmap == null || originData == null) {
				Toast.makeText(FilterActivity.this, "图片正在载入中",
						Toast.LENGTH_SHORT).show();
				return;
			}
			DialogManager.dismissDialog();
			DialogManager.showSimpleDialog(FilterActivity.this, "编辑中",
					"正在编辑，请稍候...", null);
			new editPictureTask().execute(v.getId());
		}
	};

	private class editPictureTask extends
			AsyncTask<Integer, ImageData, ImageData> {
		@Override
		protected ImageData doInBackground(Integer... ids) {
			ImageData imageData = originData.clone();
			switch (ids[0]) {
			case R.id.btn_filter_originPhoto:// 原图
				// imageData = originData;
				break;
			case R.id.btn_filter_black_white:// 黑白
				imageData = ImageFilter.whiteBlackFilter(imageData);
				break;
			case R.id.btn_filter_brightContrast:// 高亮对比度
				imageData = ImageFilter.brightContrastFilter(imageData);
				break;
			case R.id.btn_filter_comic:// 动漫
				imageData = ImageFilter.comicFilter(imageData);
				break;
			case R.id.btn_filter_feather:// 羽化
				imageData = ImageFilter.featherFilter(imageData);
				break;
			case R.id.btn_filter_oldPhoto:// 老照片
				imageData = ImageFilter.oldPhotoFilter(imageData);
				break;
			default:
				break;
			}
			return imageData;
		}

		@Override
		protected void onPostExecute(ImageData imageData) {
			super.onPostExecute(imageData);
			mLoadedBitmap.recycle();
			mLoadedBitmap = Bitmap.createBitmap(imageData.getPts(), 0,
					imageData.getWidth(), imageData.getWidth(),
					imageData.getHeight(), Config.RGB_565);
			mReviewImageView.setImageBitmap(mLoadedBitmap);
			DialogManager.dismissDialog();
		}
	}

	// 异步保存照片文件
	private class savePictureTask extends AsyncTask<Bitmap, String, String> {

		@Override
		protected String doInBackground(Bitmap... params) {
			Bitmap bmp = params[0];
			String filePath = null;
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File pictureFile = FileUtil
						.getOutPutMediaFile(CameraManager.MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					return null;
				}
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					Toast.makeText(FilterActivity.this,
							"FileNotFoundException " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {

				}
				filePath = pictureFile.getAbsolutePath();
			} else {
				Toast.makeText(FilterActivity.this, "SD卡不存在",
						Toast.LENGTH_SHORT).show();
			}
			return filePath;
		}

		// doInBackground执行完后调用，filePath是上面执行完后的返回值
		@Override
		protected void onPostExecute(final String filePath) {
			super.onPostExecute(filePath);
			scanFile(filePath);
			DialogManager.dismissDialog();
			Toast.makeText(FilterActivity.this, "保存完毕,保存在Pictures文件夹下",
					Toast.LENGTH_SHORT).show();
		}
	}

	// 根据文件路径扫描照片文件
	private void scanFile(String path) {
		if (path == null)
			return;
		MediaScannerConnection.scanFile(FilterActivity.this,
				new String[] { path }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
					}
				});
	}
}

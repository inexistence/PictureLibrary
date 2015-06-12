package com.scut.picturelibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.utils.FormatTools;
import com.scut.picturelibrary.utils.ImageData;
import com.scut.picturelibrary.utils.ImageFilter;
import com.scut.picturelibrary.views.DialogManager;

/**
 * 滤镜Activity
 * 
 * @author 黄建斌
 * 
 */
public class FilterActivity extends Activity {
	ImageView mSquareImageView;
	Bitmap mLoadedBitmap;
	ImageButton mNextButton;
	ImageData originData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);

		Intent intent = getIntent();
		byte[] bitmapBytes = intent.getByteArrayExtra("bitmapBytes");

		mSquareImageView = (ImageView) findViewById(R.id.sqimg_filter_tartget_image);

		new DecodeTask(bitmapBytes).execute();

		Toast.makeText(this, "TIP:为图片添加滤镜", Toast.LENGTH_LONG).show();

		mNextButton = (ImageButton) findViewById(R.id.btn_filter_next);
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mLoadedBitmap != null)
					new ChangePictureTask().execute(mLoadedBitmap);
			}
		});

		findViewById(R.id.btn_filter_cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						FilterActivity.this.finish();
					}
				});

		ViewGroup vp = (ViewGroup) findViewById(R.id.ll_filter_btns);
		for (int i = 0; i < vp.getChildCount(); i++) {
			vp.getChildAt(i).setOnClickListener(mFilterOnClickListener);
		}
	}

	OnClickListener mFilterOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			new FilterPictureTask().execute(v.getId());
		}
	};

	private class FilterPictureTask extends
			AsyncTask<Integer, ImageData, ImageData> {
		@Override
		protected void onPreExecute() {
			if (mLoadedBitmap == null || originData == null) {
				Toast.makeText(FilterActivity.this, "图片正在载入中",
						Toast.LENGTH_SHORT).show();
				return;
			}
			DialogManager.dismissDialog();
			DialogManager.showSimpleDialog(FilterActivity.this, "编辑中",
					"正在编辑，请稍候...", null);
		}

		@Override
		protected ImageData doInBackground(Integer... ids) {
			if (originData == null)
				return null;
			ImageData imageData = originData.clone();
			switch (ids[0]) {
			case R.id.btn_filter_origin:// 原图
				imageData = originData;
				break;
			case R.id.btn_filter_blackWhite:// 黑白
				imageData = ImageFilter.comicFilter(imageData);
				break;
			case R.id.btn_filter_brightContrast:// 高亮对比度
				imageData = ImageFilter.brightContrastFilter(imageData);
				break;
			case R.id.btn_filter_feather:// 羽化
				imageData = ImageFilter.featherFilter(imageData);
				break;
			case R.id.btn_fliter_oldPhoto:// 老照片
				imageData = ImageFilter.oldPhotoFilter(imageData);
				break;
			default:
				break;
			}
			return imageData;
		}

		@Override
		protected void onPostExecute(ImageData imageData) {
			DialogManager.dismissDialog();
			if (imageData == null)
				return;
			if (mLoadedBitmap != null)
				mLoadedBitmap.recycle();
			mLoadedBitmap = Bitmap.createBitmap(imageData.getPts(), 0,
					imageData.getWidth(), imageData.getWidth(),
					imageData.getHeight(), Config.RGB_565);
			mSquareImageView.setImageBitmap(mLoadedBitmap);
		}
	}

	/**
	 * 异步压缩图片
	 */
	private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
		private byte[] bitmapBytes;

		// ImageSize mSize;

		public DecodeTask(byte[] bitmapBytes) {
			this.bitmapBytes = bitmapBytes;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bmp = FormatTools.getInstance().bytes2Bitmap(
					this.bitmapBytes);
			// mSize = ImageUtil.getImageViewSize(mSquareImageView);
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mLoadedBitmap = result;
			originData = new ImageData(result);
			mSquareImageView.setImageBitmap(result);
		}
	}

	private class ChangePictureTask extends AsyncTask<Bitmap, String, byte[]> {
		@Override
		protected void onPreExecute() {
			mNextButton.setClickable(false);
		}

		@Override
		protected byte[] doInBackground(Bitmap... params) {
			Bitmap bmp = params[0];
			return FormatTools.getInstance().bitmap2Bytes(bmp);
		}

		@Override
		protected void onPostExecute(byte[] bitmapBytes) {
			mNextButton.setClickable(true);
			Intent intent = new Intent();
			intent.setClass(FilterActivity.this, TextActivity.class);
			intent.putExtra("bitmapBytes", bitmapBytes);
			FilterActivity.this.startActivity(intent);
			FilterActivity.this.finish();
		}
	}

}

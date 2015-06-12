package com.scut.picturelibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.scut.picturelibrary.Constants;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.utils.FormatTools;
import com.scut.picturelibrary.utils.ImageUtil;
import com.scut.picturelibrary.utils.ImageUtil.ImageSize;
import com.scut.picturelibrary.views.MoveableImageView;

/**
 * 调整图片大小 布局 背景色的Activity
 * 
 * @author 黄建斌
 * 
 */
public class EditActivity extends Activity {
	int curScaleIndex = 0;
	float mCurrentRotation = 0.0f;
	ImageButton mNextButton;
	Button mScaleSizeBtn;
	Button mBackgroundColorBtn;

	MoveableImageView mSquareImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		String path = getIntent().getStringExtra("path");

		mSquareImageView = (MoveableImageView) findViewById(R.id.sqimg_edit_tartget_image);

		new DecodeTask(path).execute();
		
		Toast.makeText(this, "TIP:移动或缩放图片以设置布局", Toast.LENGTH_LONG).show();

		mNextButton = (ImageButton) findViewById(R.id.btn_edit_next);
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Bitmap bmp = ImageUtil.getViewBitmap(mSquareImageView);
				new ChangePictureTask().execute(bmp);
			}
		});

		findViewById(R.id.btn_edit_choose_rotation).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						mSquareImageView
								.setRotation(mCurrentRotation = (mCurrentRotation + 90.0f) % 360);
					}
				});

		mBackgroundColorBtn = (Button) findViewById(R.id.btn_edit_choose_background);
		mBackgroundColorBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSquareImageView.switchBackgroundColor();
				mBackgroundColorBtn.setText(mSquareImageView
						.getCurrentBackgroundColor() == Color.BLACK ? "黑" : "白");
			}
		});

		findViewById(R.id.btn_edit_choose_incenter).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mSquareImageView.setInCenter();
					}
				});
		findViewById(R.id.btn_edit_choose_in_vertical_center)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mSquareImageView.setInVerticleCenter();
					}
				});
		findViewById(R.id.btn_edit_choose_in_horizontal_center)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mSquareImageView.setInHorizontalCenter();
					}
				});

		mScaleSizeBtn = (Button) findViewById(R.id.btn_edit_choose_in_size);
		mScaleSizeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int length = Constants.EDIT_IMAGE_SCALES.length;
				curScaleIndex = (curScaleIndex + 1) % length;
				mSquareImageView
						.setSize(Constants.EDIT_IMAGE_SCALES[curScaleIndex]);
				String btnText = null;
				switch (curScaleIndex) {
				case 0:
					btnText = "1:1";
					break;
				case 1:
					btnText = "4:3";
					break;
				case 2:
					btnText = "3:4";
					break;
				default:
					break;
				}
				mScaleSizeBtn.setText(btnText);
			}
		});
		findViewById(R.id.btn_edit_cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditActivity.this.finish();
					}
				});
	}

	private class ChangePictureTask extends AsyncTask<Bitmap, String, byte[]> {
		@Override
		protected void onPreExecute() {
			mNextButton.setClickable(false);
		}

		@Override
		protected byte[] doInBackground(Bitmap... params) {
			Bitmap bmp = params[0];
			// 旋转角度
			Matrix matrix = new Matrix();
			matrix.reset();
			matrix.postRotate(mCurrentRotation);
			matrix.setRotate(mCurrentRotation, (float) bmp.getWidth() / 2,
					(float) bmp.getHeight() / 2);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
			return FormatTools.getInstance().bitmap2Bytes(bmp);
		}

		@Override
		protected void onPostExecute(byte[] bitmapBytes) {
			mNextButton.setClickable(true);
			Intent intent = new Intent();
			intent.setClass(EditActivity.this, FilterActivity.class);
			intent.putExtra("bitmapBytes", bitmapBytes);
			EditActivity.this.startActivity(intent);
			EditActivity.this.finish();
		}
	}

	/**
	 * 异步压缩图片
	 */
	private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
		private String mPath;
		ImageSize mSize;

		public DecodeTask(String path) {
			mPath = path;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			mSize = ImageUtil.getImageViewSize(mSquareImageView);
			return ImageUtil.decodeInSampleFromPath(mPath, mSize.width,
					mSize.height);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mSquareImageView.setImageBitmap(result);
		}
	}
}

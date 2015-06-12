package com.scut.picturelibrary.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.fragment.TextFontFragment;
import com.scut.picturelibrary.fragment.TextSettingFragment;
import com.scut.picturelibrary.utils.FileUtil;
import com.scut.picturelibrary.utils.FormatTools;
import com.scut.picturelibrary.utils.ImageUtil;
import com.scut.picturelibrary.views.DialogManager;
import com.scut.picturelibrary.views.EdtImageViewLayout;

/**
 * 添加文案Activity
 * 
 * @author 黄建斌
 * 
 */
public class TextActivity extends FragmentActivity {

	EdtImageViewLayout mEdtImageViewLayout;

	ViewPager mPager;
	List<Fragment> mContent;

	ImageView mDot1;
	ImageView mDot2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);
		mEdtImageViewLayout = (EdtImageViewLayout) findViewById(R.id.edtimg_text_tartget_image);
		mPager = (ViewPager) findViewById(R.id.viewpager_text_seeting);
		mDot1 = (ImageView) findViewById(R.id.img_text_dot_page1);
		mDot2 = (ImageView) findViewById(R.id.img_text_dot_page2);

		mDot1.setSelected(true);
		mDot2.setSelected(false);

		Intent intent = getIntent();
		byte[] bitmapBytes = intent.getByteArrayExtra("bitmapBytes");

		new DecodeTask(bitmapBytes).execute();

		Toast.makeText(this, "TIP:点击加号为图片添加文案", Toast.LENGTH_LONG).show();

		mContent = new ArrayList<Fragment>();
		mContent.add(new TextSettingFragment(mEdtImageViewLayout));
		mContent.add(new TextFontFragment(mEdtImageViewLayout));

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					mDot1.setSelected(true);
					mDot2.setSelected(false);
				} else {
					mDot1.setSelected(false);
					mDot2.setSelected(true);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		mPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mContent.size();
			}

			@Override
			public Fragment getItem(int position) {
				return mContent.get(position);
			}
		});

		findViewById(R.id.btn_text_add).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mEdtImageViewLayout.addEditText();
					}
				});

		findViewById(R.id.btn_text_finish).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mEdtImageViewLayout.setAllFocuse(false);
						new SavePictureTask().execute(ImageUtil
								.getViewBitmap(mEdtImageViewLayout));
					}
				});
		findViewById(R.id.btn_text_cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						TextActivity.this.finish();
					}
				});
	}

	/**
	 * 异步解析图片
	 */
	private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
		private byte[] bitmapBytes;

		public DecodeTask(byte[] bitmapBytes) {
			this.bitmapBytes = bitmapBytes;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bmp = FormatTools.getInstance().bytes2Bitmap(
					this.bitmapBytes);
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mEdtImageViewLayout.setImageBitmap(result);
		}
	}

	// 异步保存照片文件
	private class SavePictureTask extends AsyncTask<Bitmap, String, String> {
		@Override
		protected void onPreExecute() {
			DialogManager.showSimpleDialog(TextActivity.this, "保存文件", "正在保存",
					null);
		}

		@Override
		protected String doInBackground(Bitmap... params) {
			Bitmap bmp = params[0];
			String filePath = null;
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File pictureFile = FileUtil
						.getOutPutMediaFile(FileUtil.MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					return null;
				}
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
					fos.flush();
					fos.close();
				} catch (final FileNotFoundException e) {
					TextActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(TextActivity.this,
									"FileNotFoundException " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});

				} catch (IOException e) {

				}
				filePath = pictureFile.getAbsolutePath();
			} else {
				TextActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(TextActivity.this, "SD卡不存在",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			return filePath;
		}

		// doInBackground执行完后调用，filePath是上面执行完后的返回值
		@Override
		protected void onPostExecute(final String filePath) {
			DialogManager.dismissDialog();
			FileUtil.scanFiles(TextActivity.this, filePath);
			Toast.makeText(TextActivity.this, "保存完毕,保存在" + filePath + "下",
					Toast.LENGTH_SHORT).show();

		}
	}
}

package com.scut.picturelibrary;

import android.app.Application;
import android.graphics.Bitmap;

import cn.bmob.v3.Bmob;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class PicLibApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoader();
		initBmob();
	}

	private void initBmob() {
		Bmob.initialize(getApplicationContext(),
				"ee96600c38da5fe2c41328c00b90e2a1");
	}

	private void initImageLoader() {
		// 设置图片显示选项
		DisplayImageOptions displayOp = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_loading)// 图片正在加载时显示的背景
				.cacheInMemory(true)// 缓存在内存中
				.cacheOnDisk(true)// 缓存在磁盘中
				.displayer(new FadeInBitmapDisplayer(400))// 显示渐变动画
				.bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
				.considerExifParams(true)// 考虑旋转角
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(displayOp)
				.denyCacheImageMultipleSizesInMemory().build();

		ImageLoader.getInstance().init(config);
	}

}

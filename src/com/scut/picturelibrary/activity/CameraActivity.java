package com.scut.picturelibrary.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.manager.SurfaceViewManager;

/**
 * 拍照界面
 * 
 * @author cyc
 * 
 */
public class CameraActivity extends Activity implements OnClickListener {

	// 返回
	private ImageButton mCameraBack;
	// 拍照
	private ImageButton mCameraTake;
	// 拍照预览
	private FrameLayout mCameraPreview;
	// 拍照按钮旋转动画
	private Animation takePhotoAnimation;

	private SurfaceViewManager mSurfaceViewManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		mCameraBack = (ImageButton) findViewById(R.id.ibtn_camera_back);
		mCameraBack.setOnClickListener(this);
		mCameraTake = (ImageButton) findViewById(R.id.ibtn_camera_take);
		mCameraTake.setOnClickListener(this);

		takePhotoAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		// 传入第第二个参数为媒体类型，第三个参数为控件动画
		mSurfaceViewManager = new SurfaceViewManager(this,
				SurfaceViewManager.MEDIA_TYPE_CAMERA);
		mCameraPreview = (FrameLayout) findViewById(R.id.fl_camera_preview);
		mCameraPreview.addView(mSurfaceViewManager);

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.ibtn_camera_back:
			finish();
			break;
		case R.id.ibtn_camera_take:
			mSurfaceViewManager.takePhoto();
			mCameraTake.startAnimation(takePhotoAnimation);
			break;
		}
	}
}

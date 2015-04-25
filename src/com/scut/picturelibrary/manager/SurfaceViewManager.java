package com.scut.picturelibrary.manager;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.scut.picturelibrary.animation.MyCameraButtonAnimation;
import com.scut.picturelibrary.animation.MyRecorderButtonAnimation;

/**
 * SurfaceView管理
 * 
 * @author cyc
 * 
 */
public class SurfaceViewManager extends SurfaceView implements
		SurfaceHolder.Callback {

	// 媒体类型，相机或者视频播放器
	private int mediaType;

	private SurfaceHolder mHolder;

	private Context context;

	private Camera mCamera;

	private MediaPlayer mediaPlayer;

	private MediaRecorder recorder;

	// 播放的文件路径
	private String strFilePath;
	// 视频流的最大播放时长
	public int max;

	private CameraManager mCameraManager;

	private MediaRecorderManager mRecorderManager;

	private VideoManager mVideoManager;
	// 设置相机对焦监听
	private OnCameraStatusListener listener;

	private OnPreparedListener mOnPreparedListener;

	private OnCompletionListener mOnCompletionListener;

	// 横竖屏切换时，控件跟着切换的动画
	private MyCameraButtonAnimation cameraButtonAnimation;

	private MyRecorderButtonAnimation recorderButtonAnimation;

	private MyOrientationEventListener orientationEventListener;
	// 最终方向
	private int lastBtOrientation = 0;

	// 相机的构造函数
	public SurfaceViewManager(Context context, int type,
			MyCameraButtonAnimation btAnimation) {
		super(context);
		this.context = context;
		this.mediaType = type;
		this.orientationEventListener = new MyOrientationEventListener(context);
		this.cameraButtonAnimation = btAnimation;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mCameraManager = new CameraManager(context);
	}

	// 录像的构造函数
	public SurfaceViewManager(Context context, int type,
			MyRecorderButtonAnimation btAnimation) {
		super(context);
		this.context = context;
		this.mediaType = type;
		this.orientationEventListener = new MyOrientationEventListener(context);
		this.recorderButtonAnimation = btAnimation;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mRecorderManager = new MediaRecorderManager(context);
		recorder = mRecorderManager.getMyMediaRecorder();
	}

	// 视频播放器的构造函数
	public SurfaceViewManager(Context context, int type, String filePath) {
		super(context);
		this.context = context;
		this.mediaType = type;
		this.strFilePath = filePath;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mVideoManager = new VideoManager(context);
		mediaPlayer = mVideoManager.getMyMediaPlayer();
	}

	// mediaType为1时设置相机，为2时设置视频播放器， 为3时设置录像
	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		switch (mediaType) {
		case 1:
			openCamera(holder);
			// 设置预览的角度
			mCamera.setDisplayOrientation(lastBtOrientation + 90);
			break;

		case 2:
			mVideoManager.play(strFilePath, holder, mOnPreparedListener,
					mOnCompletionListener);
			break;

		case 3:
			openCamera(holder);
			break;
		}
	}

	private void openCamera(SurfaceHolder holder) {
		if (mCamera == null) {
			switch (mediaType) {
			case 1:
				mCamera = mCameraManager.getMyCamera(context);
				mCameraManager.setCameraParameters(mCamera);
				break;
			case 3:
				mCamera = mRecorderManager.getMyCamera(context);
				break;
			}
		}
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			}
		} catch (IOException e) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		switch (mediaType) {
		case 1:
			releaseCamera();
			break;

		case 2:

			break;
		case 3:

			break;
		}
	}

	// 横竖屏切换使，监听传感器方向的改变
	public class MyOrientationEventListener extends OrientationEventListener {

		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation == ORIENTATION_UNKNOWN)
				return;

			int phoneRotation = 0;
			if (orientation > 315 && orientation <= 45) {
				phoneRotation = 0;
			} else if (orientation > 45 && orientation <= 135) {
				phoneRotation = 90;
			} else if (orientation > 135 && orientation <= 225) {
				phoneRotation = 180;
			} else if (orientation > 225 && orientation <= 315) {
				phoneRotation = 270;
			}

			if (phoneRotation == 0 && lastBtOrientation == 360) {
				lastBtOrientation = 0;
			}

			if ((phoneRotation == 0 || lastBtOrientation == 0)
					&& (Math.abs(phoneRotation - lastBtOrientation) > 180)) {
				phoneRotation = phoneRotation == 0 ? 360 : phoneRotation;
				lastBtOrientation = lastBtOrientation == 0 ? 360
						: lastBtOrientation;
			}

			if (phoneRotation != lastBtOrientation) {
				int fromDegress = 360 - lastBtOrientation;
				int toDegrees = 360 - phoneRotation;

				RotateAnimation animation = new RotateAnimation(fromDegress,
						toDegrees, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setDuration(1000);
				animation.setFillAfter(true);
				switch (mediaType) {
				case 1:
					cameraButtonAnimation.executeAnimation(animation);
					break;
				case 3:
					recorderButtonAnimation.executeAnimation(animation);
					break;
				}
				lastBtOrientation = phoneRotation;
				if (mCamera != null) {
					// 随着横竖屏切换，设置预览的角度
					mCamera.setDisplayOrientation(lastBtOrientation + 90);
				}
			}
		}
	}

	public void takePhoto() {
		if (mCamera != null) {
			mCamera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if (null != listener) {
						listener.onAutoFocus(success);
					}
					if (success) {
						mCameraManager.takePicture();
					}
				}
			});
		}
	}

	public void OnCameraStatusListener(OnCameraStatusListener listener) {
		this.listener = listener;
	}

	// 相机拍照监听接口
	public interface OnCameraStatusListener {
		// 对焦事件
		void onAutoFocus(boolean success);
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	public MediaPlayer getMyMediaPlayer() {
		return mediaPlayer;
	}

	public MediaRecorder getMyMediaRecorder() {
		return recorder;
	}

	// 播放视频进度条时间
	public String ShowTime(int currentTime) {
		return mVideoManager.ShowTime(currentTime);
	}

	// 开始录像
	public void startRecord() {
		mRecorderManager.startRecord(mCamera, mHolder);
	}

	// 释放相机
	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	// 释放录像机
	public void releaseMediaRecorder() {
		mRecorderManager.releaseMediaRecorder(mCamera);
	}

	public void scanFile() {
		mRecorderManager.scanFile();
	}

}

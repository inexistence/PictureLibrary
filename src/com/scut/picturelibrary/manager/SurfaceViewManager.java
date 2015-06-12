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

/**
 * SurfaceView管理
 * 
 * @author cyc
 * 
 */
public class SurfaceViewManager extends SurfaceView implements
		SurfaceHolder.Callback {
	public final static int MEDIA_TYPE_CAMERA = 1;
	public final static int MEDIA_TYPE_VIDEO = 2;
	public final static int MEDIA_TYPE_RECORDER = 3;

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

	private MyOrientationEventListener orientationEventListener;
	// 最终方向
	private int lastBtOrientation = 0;

	/**
	 * 录像机和照相机的构造函数
	 * 
	 * @param context
	 * @param type
	 */
	public SurfaceViewManager(Context context, int type) {
		super(context);
		init(context, type);
		if (type == MEDIA_TYPE_CAMERA) {// 照相机
			initCamera();
			enableOrientationEventListener();
		} else if (type == MEDIA_TYPE_RECORDER) {// 录像机
			initRecorder();
			enableOrientationEventListener();
		} else if (type == MEDIA_TYPE_VIDEO) {// 播放器
			initVideoPlayer();
		}
	}

	/**
	 * 设置横竖屏幕事件监听
	 */
	public void enableOrientationEventListener() {
		if (orientationEventListener == null) {
			orientationEventListener = new MyOrientationEventListener(context);
		}
		if (orientationEventListener.canDetectOrientation() == true) {
			orientationEventListener.enable();
		} else {
			orientationEventListener.disable();
		}
	}

	/**
	 * 视频播放器的构造函数
	 * 
	 * @param context
	 * @param type
	 * @param filePath
	 */
	public SurfaceViewManager(Context context, int type, String filePath) {
		super(context);
		init(context, type);
		this.strFilePath = filePath;
		initVideoPlayer();
	}

	private void init(Context context, int type) {
		this.context = context;
		this.mediaType = type;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void initRecorder() {
		mRecorderManager = new MediaRecorderManager(context);
		recorder = mRecorderManager.getMyMediaRecorder();
	}

	private void initCamera() {
		mCameraManager = new CameraManager(context);
	}

	private void initVideoPlayer() {
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
		case MEDIA_TYPE_CAMERA:
			openCamera(holder);
			mCamera.setDisplayOrientation(90);
			mCameraManager.setCameraParameters(mCamera, 0);
			break;

		case MEDIA_TYPE_VIDEO:
			mVideoManager.play(strFilePath, holder, mOnPreparedListener,
					mOnCompletionListener);
			break;

		case MEDIA_TYPE_RECORDER:
			openCamera(holder);
			break;
		}
	}

	private void openCamera(SurfaceHolder holder) {
		if (mCamera == null) {
			switch (mediaType) {
			case MEDIA_TYPE_CAMERA:
				mCamera = mCameraManager.getMyCamera(context);
				mCameraManager.setCameraParameters(mCamera, 90);
				break;
			case MEDIA_TYPE_RECORDER:
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
		case MEDIA_TYPE_CAMERA:
			releaseCamera();
			break;

		case MEDIA_TYPE_VIDEO:

			break;
		case MEDIA_TYPE_RECORDER:

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
			if (orientation < 10 || orientation > 350) {
				// 手机上边向上
				phoneRotation = 90;
			} else if (orientation < 100 && orientation > 80) {
				// 手机右边向上
				phoneRotation = 180;
			} else if (orientation < 190 && orientation > 170) {
				// 手机下边向上
				phoneRotation = 90;
			} else if (orientation < 280 && orientation > 260) {
				// 手机左边向上
				phoneRotation = 0;
			}
			lastBtOrientation = phoneRotation;
			if (mCamera != null) {
				// 随着横竖屏切换，设置拍照结果的角度
				if (mCameraManager != null) {
					Camera.Parameters parameters = mCamera.getParameters();
					parameters.setRotation(lastBtOrientation + 90);
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
	public String startRecord() {
		return mRecorderManager
				.startRecord(mCamera, mHolder, lastBtOrientation);
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
	public void releaseMediaRecorder(boolean isRecording, boolean useAgain) {
		mRecorderManager.releaseMediaRecorder(mCamera, isRecording, useAgain);
	}
}

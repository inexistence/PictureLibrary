package com.scut.picturelibrary.manager;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.scut.picturelibrary.utils.CameraCheck;
import com.scut.picturelibrary.utils.FileUtil;

/**
 * 录像管理
 * 
 * @author cyc
 * 
 */
public class MediaRecorderManager {

	public static final int MEDIA_TYPE_RECORDER = 3;

	private Context context;

	private Camera mCamera;

	private MediaRecorder mediaRecorder;

	private String filePath;

	public MediaRecorderManager(Context context) {
		super();
		this.context = context;
		mediaRecorder = new MediaRecorder();
	}

	public Camera getMyCamera(Context context) {
		mCamera = CameraCheck.getCameraInstance(context);
		mCamera.setDisplayOrientation(90);
		return mCamera;
	}

	public MediaRecorder getMyMediaRecorder() {
		return mediaRecorder;
	}

	@SuppressLint("InlinedApi")
	public void startRecord(Camera camera, SurfaceHolder holder) {
		mediaRecorder.reset();
		camera.unlock();
		mediaRecorder.setCamera(camera);
		// 设置录音源
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置视频源
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 设置视频和声音的编码
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// 设置视频编码率，使拍摄质量更高
		mediaRecorder.setVideoEncodingBitRate(15 * 1024 * 1024);
//		mediaRecorder.setProfile(CamcorderProfile
//				.get(CamcorderProfile.QUALITY_HIGH));
		filePath = FileUtil.getOutPutMediaFile(MEDIA_TYPE_RECORDER).toString();
		// 设置视频输出文件
		mediaRecorder.setOutputFile(filePath);
		mediaRecorder.setPreviewDisplay(holder.getSurface());
		// 设置视频最长录制时间为10分钟
		mediaRecorder.setMaxDuration(10 * 60 * 1000);
		try {
			mediaRecorder.prepare();
			mediaRecorder.start();
		} catch (IllegalStateException e) {

		} catch (IOException e) {

		}
	}

	public void releaseMediaRecorder(Camera camera) {
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			camera.lock();
		}
	}

	// 根据文件路径扫描照片文件
	public void scanFile() {

		MediaScannerConnection.scanFile(context, new String[] { filePath },
				null, new MediaScannerConnection.OnScanCompletedListener() {

					public void onScanCompleted(String path, Uri uri) {
					}
				});
	}

}

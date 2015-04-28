package com.scut.picturelibrary.manager;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
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
	// 手机支持的分辨率
	private List<Size> supportedVideoSizes;

	private Size size;

	int setFixVideoWidth = 0;

	int setFixVideoHeight = 0;

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

	public void startRecord(Camera camera, SurfaceHolder holder) {
		mediaRecorder.reset();
		List<Camera.Size> videoSize = camera.getParameters().getSupportedVideoSizes();
		camera.unlock();
		mediaRecorder.setCamera(camera);
		// 设置录音源
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置视频源
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 设置输出格式，视频和声音的编码格式
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		int setFixPictureWidth = 0,setFixPictureHeight = 0;
		Iterator<Camera.Size> itos = videoSize.iterator();
		while (itos.hasNext()) {
			Camera.Size curSize = itos.next();
			int curSupporSize = curSize.width * curSize.height;
			int fixPictrueSize = setFixPictureWidth * setFixPictureHeight;
			if (curSupporSize > fixPictrueSize) {
				setFixPictureWidth = curSize.width;
				setFixPictureHeight = curSize.height;
			}
		}
		mediaRecorder.setVideoSize(setFixPictureWidth, setFixPictureHeight);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// 设置视频采样率，每秒30帧
		mediaRecorder.setVideoFrameRate(30);

		// 设置视频分辨率
		mediaRecorder.setVideoSize(setFixVideoWidth, setFixVideoHeight);

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

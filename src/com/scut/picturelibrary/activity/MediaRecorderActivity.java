package com.scut.picturelibrary.activity;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.manager.SurfaceViewManager;
import com.scut.picturelibrary.utils.FileUtil;

/**
 * 录像
 * 
 * @author cyc
 * 
 */
public class MediaRecorderActivity extends Activity implements OnClickListener {

	private ImageButton mRecorderBack;

	private ImageButton mRecorderRecord;
	// 摄像预览
	private FrameLayout mRecorderPreview;

	private SurfaceViewManager mSurfaceViewManager;

	private MediaRecorder recorder;
	// 是否正在录制
	private boolean isRecording = false;
	
	private String filePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorder);

		mRecorderBack = (ImageButton) findViewById(R.id.ibtn_recorder_back);
		mRecorderBack.setOnClickListener(this);
		mRecorderRecord = (ImageButton) findViewById(R.id.ibtn_recorder_record);
		mRecorderRecord.setOnClickListener(this);

		mSurfaceViewManager = new SurfaceViewManager(this, SurfaceViewManager.MEDIA_TYPE_RECORDER);
		recorder = mSurfaceViewManager.getMyMediaRecorder();
		mRecorderPreview = (FrameLayout) findViewById(R.id.fl_recorder_preview);
		mRecorderPreview.addView(mSurfaceViewManager);

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.ibtn_recorder_record:

			if (isRecording && recorder != null) {
				mSurfaceViewManager.releaseMediaRecorder(isRecording,true);
				isRecording = false;
				Toast.makeText(MediaRecorderActivity.this, "录制完成",
						Toast.LENGTH_SHORT).show();
				// 录像完毕后扫描文件
				FileUtil.scanFiles(getApplicationContext(), filePath);
				mRecorderRecord.setImageDrawable(getResources().getDrawable(
						R.drawable.img_recorder_record));
			} else {
				filePath = mSurfaceViewManager.startRecord();
				isRecording = true;
				Toast.makeText(MediaRecorderActivity.this, "视频录制中。。。",
						Toast.LENGTH_SHORT).show();
				mRecorderRecord.setImageDrawable(getResources().getDrawable(
						R.drawable.img_recorder_recording));
			}
			break;

		case R.id.ibtn_recorder_back:
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSurfaceViewManager.releaseMediaRecorder(isRecording,false);
		mSurfaceViewManager.releaseCamera();
	}

}

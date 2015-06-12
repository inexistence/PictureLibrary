package com.scut.picturelibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.fragment.ImagePagerFragment;

/**
 * 显示大图Activity Fragment
 * 
 * @author 黄建斌
 */
public class SimpleImageActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String tag = ImagePagerFragment.class.getSimpleName();
		Fragment fr = getSupportFragmentManager().findFragmentByTag(tag);
		if (fr == null) {
			fr = new ImagePagerFragment();
			fr.setArguments(getIntent().getExtras());
		}
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, fr, tag).commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intentMedia = new Intent();
		switch (id) {// 根据选项进行排序
		// 开始拍照或录像
		case R.id.action_activity_camera:
			intentMedia
					.setClass(SimpleImageActivity.this, CameraActivity.class);
			startActivity(intentMedia);
			break;
		case R.id.action_activity_recorder:
			intentMedia.setClass(SimpleImageActivity.this,
					MediaRecorderActivity.class);
			startActivity(intentMedia);
			break;
		case R.id.action_search:
			Intent intent = new Intent();
			intent.setClass(SimpleImageActivity.this, SearchImageActivity.class);
			SimpleImageActivity.this.startActivity(intent);
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
package com.scut.picturelibrary.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.scut.picturelibrary.Constants;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.adapter.MediaFoldersAdapter;
import com.scut.picturelibrary.loader.MediaFoldersCursorLoader;

/**
 * 入口Activity，显示所有图片文件夹 使用Loader进行Cursor的异步查询和管理
 * 
 * @author 黄建斌
 */
public class MediaFoldersActivity extends ActionBarActivity implements
		LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused")
	private static final String TAG = "FoldersActivity";
	/**
	 * 用于展示文件夹的GridView
	 */
	private GridView mGridView;
	private final int LOAD_ID = 0x20150330;

	/**
	 * GridView的适配器
	 */
	private MediaFoldersAdapter mAdapter;

	private String mSort = Constants.BUCKET_SORT_DEFAULT;

	private long mKeyTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folders);
		// 初始化视图
		initView();
		// 设置监听器
		initListener();
		// 进行cursorloader初始化
		getSupportLoaderManager().initLoader(LOAD_ID, null, this);
	}

	private void initView() {
		mGridView = (GridView) findViewById(R.id.grid_folders);
		mAdapter = new MediaFoldersAdapter(this, null);
		mGridView.setAdapter(mAdapter);
	}

	private void initListener() {
		// 设置滚动时图片是否暂停加载的监听
		PauseOnScrollListener listener = new PauseOnScrollListener(
				ImageLoader.getInstance(), false, true);
		mGridView.setOnScrollListener(listener);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String bucketId = mAdapter.getBucketId(position);
				String bucketName = mAdapter.getBuckName(position);
				Intent intent = new Intent();
				intent.putExtra("bucketId", bucketId);
				intent.putExtra("bucketName", bucketName);
				intent.setClass(MediaFoldersActivity.this,
						MediaFilesActivity.class);
				MediaFoldersActivity.this.startActivity(intent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intentMedia = new Intent();
		switch (id) {
		// 根据选项进行排序
		case R.id.action_sort_name:
			return resort(Constants.SORT_BY_BUCKET_NAME);
		case R.id.action_sort_date:
			return resort(Constants.SORT_BY_DATE);
			// 开始拍照或录像
		case R.id.action_activity_camera:
			intentMedia.setClass(MediaFoldersActivity.this,
					CameraActivity.class);
			startActivity(intentMedia);
			break;
		case R.id.action_activity_recorder:
			intentMedia.setClass(MediaFoldersActivity.this,
					MediaRecorderActivity.class);
			startActivity(intentMedia);
			break;
		case R.id.action_search:
			Intent intent = new Intent();
			intent.setClass(MediaFoldersActivity.this,
					SearchImageActivity.class);
			MediaFoldersActivity.this.startActivity(intent);
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 对cursor进行重新排序
	 * 
	 * @param sort
	 *            排序规则
	 * @return
	 */
	public boolean resort(String sort) {
		if (!mSort.equals(sort)) {// 转换排序规则
			// 设置当前规则
			mSort = sort;
			// 重定cursor
			getSupportLoaderManager().restartLoader(LOAD_ID, null, this);
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		return new MediaFoldersCursorLoader(this, mSort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// 将新的cursor传入
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// 取消cursor
		mAdapter.swapCursor(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mKeyTime) > 2000) {
				mKeyTime = System.currentTimeMillis();
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_LONG).show();
			} else {
				finish();
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}

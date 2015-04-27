package com.scut.picturelibrary.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.adapter.MediaFilesAdapter;
import com.scut.picturelibrary.loader.MediaFilesCursorLoader;
import com.scut.picturelibrary.views.DialogManager;

/**
 * 显示某文件夹下所有图片视频文件
 * 
 * @author 黄建斌
 * 
 */
public class MediaFilesActivity extends ActionBarActivity implements
		LoaderCallbacks<Cursor> {
	/**
	 * 用于展示文件夹的GridView
	 */
	private GridView mGridView;
	private final int LOAD_ID = 0x20150405;

	/**
	 * GridView的适配器
	 */
	private MediaFilesAdapter mAdapter;

	private final String SORT_BY_NAME = MediaStore.Images.Media.DISPLAY_NAME;
	private final String SORT_BY_DATE = MediaStore.Images.Media.DATE_MODIFIED;

	private String mSort = SORT_BY_NAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);
		// 进行cursorloader初始化
		getSupportLoaderManager().initLoader(LOAD_ID, null, this);
		// 初始化视图
		initView();
		// 设置监听器
		initListener();
	}

	private void initView() {
		mGridView = (GridView) findViewById(R.id.grid_files);
		mAdapter = new MediaFilesAdapter(this, null);
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
				// TODO 点击显示完整图片or播放视频
				// 目前是调用外部程序
				String path = mAdapter.getPath(position);
				if (mAdapter.getType(position).equals("video")) {// 视频
					Intent intent = new Intent();
					intent.setClass(MediaFilesActivity.this,
							VideoActivity.class);
					intent.putExtra("filePath", path);
					startActivity(intent);
				} else { // 图片
					Intent it = new Intent();
					Uri uri = Uri.parse("file:///" + path);
					int count = mAdapter.getCount();
					String[] path_base = new String[count];
					for (int i = 0; i < mAdapter.getCount(); i++) {
						path_base[i] = mAdapter.getPath(i);
					}

					it.putExtra("path", path);
					it.putExtra("uri", uri);
					it.putExtra("position", position);
					it.putExtra("count", count);
					it.putExtra("path_all", path_base);
					it.setClass(MediaFilesActivity.this,
							ImageViewActivity.class);
					startActivity(it);
				}

			}
		});
		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mAdapter.getType(position).equals("video")) {// 视频
				} else { // 图片
					final String path = mAdapter.getPath(position);
					final String time = mAdapter.getTime(position);
					final String filesize = mAdapter.getFileSize(position);
					final String filename = mAdapter.getTitle(position);
					final String size = mAdapter.getImageSize(position);
					DialogManager.showImageItemMenuDialog(
							MediaFilesActivity.this, filename,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										Intent intent = new Intent();
										intent.setClass(
												MediaFilesActivity.this,
												RecognizeImageActivity.class);
										intent.putExtra("path", path);
										intent.putExtra("filename", filename);
										MediaFilesActivity.this
												.startActivity(intent);
										break;
									case 1:
										showShare(path);
										break;
									case 2:
										DialogManager.showImagePropertyDialog(
												MediaFilesActivity.this,
												filename, path, filesize, size,
												time);
										break;
									default:
										break;
									}

								}
							});
				}
				return false;
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
		switch (id) {// 根据选项进行排序
		case R.id.action_sort_name:
			return resort(SORT_BY_NAME);
		case R.id.action_sort_date:
			return resort(SORT_BY_DATE);
		case R.id.action_search:
			Intent intent = new Intent();
			intent.setClass(MediaFilesActivity.this, SearchImageActivity.class);
			MediaFilesActivity.this.startActivity(intent);
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
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String bucketId = getIntent().getStringExtra("bucketId");
		// 创建目标cursor，显示文件夹下图片视频文件
		return new MediaFilesCursorLoader(this, new String[] {
				MediaStore.Images.Media._ID,// 文件ID
				MediaStore.Images.Media.BUCKET_ID, // 文件夹ID
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
				MediaStore.Images.Media.DATE_MODIFIED,// 修改日期
				MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
				MediaStore.Images.Media.DATA // 图片绝对路径
				}, "bucket_id = " + bucketId, null, mSort);
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

	private void showShare(String path) {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("www.baidu.com");
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(path);// 确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://sharesdk.cn");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("www.baidu.com");
		// 启动分享GUI
		oks.show(this);
	}
}

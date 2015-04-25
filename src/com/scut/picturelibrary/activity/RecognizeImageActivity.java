package com.scut.picturelibrary.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UploadFileListener;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.LocalThumbnailListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.adapter.InSameListViewAdapter;
import com.scut.picturelibrary.adapter.InSimiGridViewAdapter;
import com.scut.picturelibrary.database.UploadedFileTableUtil;
import com.scut.picturelibrary.views.DialogManager;
import com.scut.picturelibrary.views.FlowTagsLayout;
import com.scut.picturelibrary.views.NoScrollGridView;
import com.scut.picturelibrary.views.NoScrollListView;

/**
 * 识图Activity
 * 
 * @author 黄建斌
 * 
 */
public class RecognizeImageActivity extends ActionBarActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "RecognizeImageActivity";
	String mFileName;
	String mSourcePath;
	String mUploadFileUrl;
	String mInsimiUrl;
	String mInsameUrl;
	long mSize;

	Set<GetWorkerTask> taskCollection;
	UploadedFileTableUtil mUploadedTable;
	InSimiGridViewAdapter mSimiAdapter;
	InSameListViewAdapter mSameAdapter;
	ArrayAdapter<String> mGuessKeyAdapter;

	NoScrollGridView mSimiGridView;
	NoScrollListView mSameListView;

	// LinearLayout mSameImagesLayout;
	ImageView mSourceImage;
	TextView mSameImagesTitleTextView;
	TextView mSimiImagesTitleTextView;
	TextView mGuessTextView;
	FlowTagsLayout mFlowTagsLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recognize);
		Intent intent = getIntent();
		mSourcePath = intent.getStringExtra("path");
		mFileName = intent.getStringExtra("filename");

		setTitle(mFileName);
		
		taskCollection = new HashSet<GetWorkerTask>();
		mUploadedTable = new UploadedFileTableUtil(this);

		findViews();

		mSameAdapter = new InSameListViewAdapter(this,
				R.layout.listview_insame_item, R.id.img_lisview_insame_same,
				R.id.txt_lisview_insame_description,
				new ArrayList<Map<String, String>>());
		mSimiAdapter = new InSimiGridViewAdapter(this,
				R.layout.grid_files_item, R.id.img_grid_files_item_photo,
				new ArrayList<Map<String, String>>());
		mGuessKeyAdapter = new ArrayAdapter<String>(this, R.layout.tags_item,
				R.id.btn_tags_item_text);

		mSameListView.setAdapter(mSameAdapter);
		mFlowTagsLayout.setAdapter(mGuessKeyAdapter);
		mSimiGridView.setAdapter(mSimiAdapter);

		mSameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// 点击使用浏览器浏览来源网页
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				Uri content_url = Uri.parse(mSameAdapter.getFromURL(position));
				intent.setData(content_url);
				startActivity(intent);
			}
		});
		mSimiGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO 点击显示大图，画廊浏览
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				Uri content_url = Uri.parse(mSimiAdapter.getFromURL(position));
				intent.setData(content_url);
				startActivity(intent);
			}
		});

		ImageLoader.getInstance().displayImage("file://" + mSourcePath,
				mSourceImage);

		uploadImage(mSourcePath, mFileName);
	}

	private void findViews() {
		mSameImagesTitleTextView = (TextView) findViewById(R.id.txt_recognize_insame_title);
		mSimiImagesTitleTextView = (TextView) findViewById(R.id.txt_recognize_insimi_title);
		mGuessTextView = (TextView) findViewById(R.id.txt_recognize_guess);
		mSourceImage = (ImageView) findViewById(R.id.img_recognize_source);
		mSimiGridView = (NoScrollGridView) findViewById(R.id.grid_recognize_insimi);
		mSameListView = (NoScrollListView) findViewById(R.id.lv_recognize_insame);
		mFlowTagsLayout = (FlowTagsLayout) findViewById(R.id.ftl_recognize_guess);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DialogManager.dismissDialog();
		cancelAllTasks();
	}

	/**
	 * 载入获得的相同图信息，包括图片和描述
	 * 
	 * @param response
	 */
	private void displayDownloadedSameImages(JSONObject response) {
		JSONArray jsonarray;
		try {
			jsonarray = response.getJSONArray("data");
			if (jsonarray.toString().equals("[{}]")) {
				mSameImagesTitleTextView.setText("未找到相同图片");
				return;
			}
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				if (obj == null)
					continue;
				String thumbURL = obj.get("thumbURL").toString();
				String description = obj.get("textHost").toString();
				String title = obj.get("fromPageTitleEnc").toString();
				String fromURL = obj.get("fromURL").toString();
				if (i < 3) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(InSameListViewAdapter.FROM_URL, fromURL);
					map.put(InSameListViewAdapter.FROM_PAGE_DESCRIPTION, title
							+ "\n" + description);
					map.put(InSameListViewAdapter.IMAGE_URL, thumbURL);
					mSameAdapter.add(map);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 载入获得的相似图片信息
	 * 
	 * @param response
	 */
	private void displayDownloadedSimiImages(JSONObject response) {
		JSONArray jsonarray;
		try {
			jsonarray = response.getJSONArray("data");
			if (jsonarray.toString().equals("[{}]")) {
				mSimiImagesTitleTextView.setText("未找到相似图片");
			}
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject obj = (JSONObject) jsonarray.get(i);
				if (obj == null)
					continue;
				String thumbURL = obj.getString("thumbURL");
				String fromURL = obj.getString("fromURL");
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(InSimiGridViewAdapter.FROM_URL, fromURL);
				map.put(InSimiGridViewAdapter.IMAGE_URL, thumbURL);
				mSimiAdapter.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 上传图片到服务器
	 * 
	 * @param path
	 * @param filename
	 */
	private void uploadImage(String path, String filename) {
		File file = new File(path);
		String url = null;
		// 获取文件大小
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			mSize = fis.available();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 尝试从数据库中获取上传后的图片url
		url = mUploadedTable.hasUploaded(filename, mSize);
		DialogManager.showProgressDialog(RecognizeImageActivity.this, null);
		if (url != null) {// 已经上传过了
			mUploadFileUrl = url;
			getSimiSameImage(mUploadFileUrl);
			return;
		}
		// 压缩图片后上传 200x200
		BmobProFile bmobProFile = BmobProFile
				.getInstance(RecognizeImageActivity.this);
		if (bmobProFile == null) {
			DialogManager.dismissDialog();
			Toast.makeText(RecognizeImageActivity.this, "压缩文件失败!",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (bmobProFile != null && !path.endsWith(".gif")) {
			bmobProFile.getLocalThumbnail(path, 1, 200, 200, 80,
					new LocalThumbnailListener() {

						@Override
						public void onError(int statuscode, String errormsg) {
							DialogManager.dismissDialog();
							Toast.makeText(RecognizeImageActivity.this,
									"压缩文件失败: " + errormsg, Toast.LENGTH_SHORT)
									.show();
						}

						@Override
						public void onSuccess(String thumbnailPath) {
							BmobFile bmobFile = new BmobFile(new File(
									thumbnailPath));
							uploadBmobFile(bmobFile);
						}
					});
		} else {
			BmobFile bmobFile = new BmobFile(new File(path));
			uploadBmobFile(bmobFile);
		}
	}

	public void uploadBmobFile(final BmobFile bmobFile) {
		// 未上传过，进行上传
		bmobFile.uploadblock(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				Toast.makeText(RecognizeImageActivity.this, "上传成功",
						Toast.LENGTH_LONG).show();
				mUploadFileUrl = bmobFile
						.getFileUrl(RecognizeImageActivity.this);
				// 上传结束，存入数据库
				mUploadedTable.insert(mFileName, mSize, mUploadFileUrl);
				getSimiSameImage(mUploadFileUrl);
			}

			@Override
			public void onProgress(Integer value) {
				if (DialogManager.getProgressDialog() != null) {
					DialogManager.getProgressDialog().setProgress(value);
				}
			}

			@Override
			public void onFailure(int code, String msg) {
				DialogManager.dismissDialog();
				Toast.makeText(RecognizeImageActivity.this, "上传失败",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	private void getSimiSameImage(String uploadFileUrl) {
		// 返回最多9张类似图片
		mInsimiUrl = "http://stu.baidu.com/i?filename=&fm=15&rt=0&pn=0&rn=9&pn=0&ct=1&stt=1&tn=insimijson&ie=utf-8&objurl="
				+ uploadFileUrl;
		// 返回最多3个相同图片
		mInsameUrl = "http://stu.baidu.com/i?filename=&fm=15&rt=0&pn=0&rn=3&pn=0&ct=1&stt=1&tn=insamejson&ie=utf-8&objurl="
				+ uploadFileUrl;
		DialogManager.dismissDialog();
		DialogManager.showSimpleDialog(RecognizeImageActivity.this, "识别中",
				"努力识图中，请稍侯", new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						Toast.makeText(RecognizeImageActivity.this, "取消识别",
								Toast.LENGTH_SHORT).show();
						// 取消识别任务
						cancelAllTasks();
					}
				});
		GetWorkerTask simitask = new GetWorkerTask();
		taskCollection.add(simitask);
		simitask.execute(INSIMI, mInsimiUrl);
		GetWorkerTask sametask = new GetWorkerTask();
		taskCollection.add(sametask);
		sametask.execute(INSAME, mInsameUrl);
	}

	public void cancelAllTasks() {
		if (taskCollection != null) {
			for (GetWorkerTask task : taskCollection) {
				task.cancel(false);
			}
		}
	}

	private static final String INSIMI = "insimi";
	private static final String INSAME = "insame";
	private String mLocationQuery;
	private String mKeyword;

	public URI getLocationURI(HttpResponse response) {
		Header[] headers = response.getAllHeaders();
		String locationName = "Location";
		URI locationUrl = null;
		String location = null;
		for (int i = 0; i < headers.length; i++) {
			String name = headers[i].getName();
			if (name.equals(locationName)) {
				location = headers[i].getValue();
				break;
			}
		}
		if (location != null) {
			try {
				locationUrl = new URI(location);
				mLocationQuery = locationUrl.getQuery();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return locationUrl;
	}

	public String getQueryString(String url, String name) {
		String[] params = url.split("&");
		for (int i = 0; i < params.length; i++) {
			if (params[i].startsWith(name)) {
				String[] values = params[i].split("=");
				if (values.length > 1)
					return params[i].split("=")[1];
			}
		}
		return null;
	}

	private boolean sameTaskOver = false;
	private boolean simiTaskOver = false;

	class GetWorkerTask extends AsyncTask<String, Void, JSONObject> {

		// params what url
		@Override
		protected JSONObject doInBackground(String... params) {
			HttpGet request = new HttpGet(params[1]);
			DefaultHttpClient client = new DefaultHttpClient();
			client.setRedirectHandler(new RedirectHandler() {
				@Override
				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return RecognizeImageActivity.this.getLocationURI(response) != null;
				}

				@Override
				public URI getLocationURI(HttpResponse response,
						HttpContext context) throws ProtocolException {
					return RecognizeImageActivity.this.getLocationURI(response);
				}
			});
			org.apache.http.HttpResponse response;
			JSONObject json = null;
			try {
				response = client.execute(request);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity, "GBK");
				if (params[0].equals(INSAME)) {
					mKeyword = getQueryString(mLocationQuery, "keyword");
				}
				json = new JSONObject(result);
				json.put("what", params[0]);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
			} finally {
				request.abort();
			}
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				try {
					String what = result.get("what").toString();
					if (what.equals(INSIMI)) {
						displayDownloadedSimiImages(result);
						simiTaskOver = true;
					}
					if (what.equals(INSAME)) {
						displayDownloadedSameImages(result);
						sameTaskOver = true;
						if (mKeyword != null && mKeyword.length() > 0) {
							mGuessTextView.setText("您要找的可能是:");
							String[] keywords = mKeyword.split("\\*");
							// 显示最多4个关键字
							for (int i = 0; i < keywords.length && i < 4; i++) {
								mGuessKeyAdapter.add(keywords[i]);
							}
						} else {
							mGuessTextView.setText("找不到关键字");
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (sameTaskOver && simiTaskOver) {// 相似和相同图片信息获取完毕
				DialogManager.dismissDialog();
			}
			taskCollection.remove(this);
		}
	}
}

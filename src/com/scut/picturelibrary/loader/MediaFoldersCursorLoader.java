package com.scut.picturelibrary.loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.scut.picturelibrary.Constants;
import com.scut.picturelibrary.utils.SortCursor;

/**
 * 合并图片和视频文件夹的CursorLoader
 * 
 * @author 黄建斌
 * 
 */
public class MediaFoldersCursorLoader extends AsyncTaskLoader<Cursor> {

	final ForceLoadContentObserver mObserver;

	@SuppressWarnings("unused")
	private final static String TAG = "MediaFoldersCursorLoader";

	// Uri mUri;
	String[] mImageProjection = new String[] {
			MediaStore.Images.Media._ID,// 文件ID
			MediaStore.Images.Media.BUCKET_ID, // 文件夹ID
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
			MediaStore.Images.Media.DATE_MODIFIED,// 修改日期
			MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
			MediaStore.Images.Media.DATA, // 图片绝对路径
			"count(" + MediaStore.Images.Media._ID + ") as num",
			"'image' as type" };
	String[] mVideoProjection = new String[] {
			MediaStore.Images.Media._ID,// 文件ID
			MediaStore.Images.Media.BUCKET_ID, // 文件夹ID
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
			MediaStore.Images.Media.DATE_MODIFIED,// 修改日期
			MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
			MediaStore.Images.Media.DATA, // 图片绝对路径
			"count(" + MediaStore.Images.Media._ID + ") as num",
			"'video' as type" };
	String mSelection = " 0==0) group by bucket_id --(";
	String mSortOrder;

	Cursor mCursor;

	static enum Col {
		_ID, // 文件ID
		BUCKET_ID, // 文件夹ID
		BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
		DATE_MODIFIED, // 修改日期
		DISPLAY_NAME, // 图片文件名
		DATA, // 图片绝对路径
		NUM, TYPE
	}

	static class FolderObj {

		Object[] colValue = new Object[8];

		public Object[] getObjects() {
			return colValue;
		}

		@Override
		public int hashCode() {
			return colValue[Col.BUCKET_ID.ordinal()].hashCode();
		}

		@Override
		public boolean equals(Object o) {
			boolean equal = false;
			if (o instanceof FolderObj) {
				equal = colValue[Col.BUCKET_ID.ordinal()]
						.equals(((FolderObj) o).getObjects()[Col.BUCKET_ID
								.ordinal()]);
			}
			return equal;
		}
	}

	List<FolderObj> mFoldersSet = new LinkedList<FolderObj>();

	/* Runs on a worker thread */
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = null;
		Cursor imageCursor = getContext().getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImageProjection,
				mSelection, null, mSortOrder);
		Cursor videoCursor = getContext().getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoProjection,
				mSelection, null, mSortOrder);
		if (imageCursor.getCount() == 0 || videoCursor.getCount() == 0) {
			if (imageCursor.getCount() == 0 && videoCursor.getCount() != 0) {
				cursor = videoCursor;
				imageCursor.close();
			} else if (imageCursor.getCount() != 0
					&& videoCursor.getCount() == 0) {
				cursor = imageCursor;
				videoCursor.close();
			}
			if (cursor != null) {
				// 排序
				if (mSortOrder == Constants.SORT_BY_DATE)
					cursor = new SortCursor(cursor, mSortOrder, SortCursor.DESC);
				else
					cursor = new SortCursor(cursor, mSortOrder);
				// Ensure the cursor window is filled
				cursor.getCount();
				cursor.registerContentObserver(mObserver);
			}
			return cursor;
		}
		cursor = new MergeCursor(new Cursor[] { imageCursor, videoCursor });
		// 清空set集合
		mFoldersSet.clear();
		// 合并重复的视频和图片文件夹
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			FolderObj obj = new FolderObj();
			obj.colValue[Col._ID.ordinal()] = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Images.Media._ID));
			obj.colValue[Col.BUCKET_ID.ordinal()] = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
			obj.colValue[Col.BUCKET_DISPLAY_NAME.ordinal()] = cursor
					.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
			obj.colValue[Col.DATE_MODIFIED.ordinal()] = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
			obj.colValue[Col.DISPLAY_NAME.ordinal()] = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
			obj.colValue[Col.DATA.ordinal()] = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			obj.colValue[Col.TYPE.ordinal()] = cursor.getString(cursor
					.getColumnIndex("type"));
			obj.colValue[Col.NUM.ordinal()] = cursor.getInt(cursor
					.getColumnIndex("num"));
			int eqIndex = mFoldersSet.indexOf(obj);
			if (eqIndex != -1) {// 重复，合并且设置正确的文件数量
				int oldNUm = (Integer) (mFoldersSet.get(eqIndex).colValue[Col.NUM
						.ordinal()]);
				int newNum = oldNUm
						+ ((Integer) (obj.colValue[Col.NUM.ordinal()]));
				mFoldersSet.set(eqIndex, obj);
				mFoldersSet.get(eqIndex).colValue[Col.NUM.ordinal()] = newNum;
			} else {
				mFoldersSet.add(obj);
			}
		}
		// 关闭游标
		cursor.close();
		String[] projection = new String[] { MediaStore.Images.Media._ID,// 文件ID
				MediaStore.Images.Media.BUCKET_ID, // 文件夹ID
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
				MediaStore.Images.Media.DATE_MODIFIED,// 修改日期
				MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
				MediaStore.Images.Media.DATA, // 图片绝对路径
				"num", "type" };
		// 将合并后的文件夹List转换为cursor
		MatrixCursor mc = new MatrixCursor(projection);
		Iterator<FolderObj> iterator = mFoldersSet.iterator();
		while (iterator.hasNext()) {
			mc.addRow(iterator.next().getObjects());
		}
		cursor = mc;
		// 排序
		if (mSortOrder == Constants.SORT_BY_DATE)
			cursor = new SortCursor(cursor, mSortOrder, SortCursor.DESC);
		else
			cursor = new SortCursor(cursor, mSortOrder);
		if (cursor != null) {
			// Ensure the cursor window is filled
			cursor.getCount();
			cursor.registerContentObserver(mObserver);
		}
		return cursor;
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			// An async query came in while the loader is stopped
			if (cursor != null) {
				cursor.close();
			}
			return;
		}
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	/**
	 * Creates an empty unspecified CursorLoader. You must follow this with
	 * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc to
	 * specify the query to perform.
	 */
	public MediaFoldersCursorLoader(Context context) {
		super(context);
		registerDb(context);
		mObserver = new ForceLoadContentObserver();
	}

	/**
	 * Creates a fully-specified CursorLoader. See
	 * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)
	 * ContentResolver.query()} for documentation on the meaning of the
	 * parameters. These will be passed as-is to that call.
	 */
	public MediaFoldersCursorLoader(Context context, String sortOrder) {
		super(context);
		registerDb(context);
		mObserver = new ForceLoadContentObserver();
		mSortOrder = sortOrder;
	}

	/**
	 * 绑定图片视频系统数据库监听
	 * 
	 * @param context
	 */
	private void registerDb(Context context) {
		context.getContentResolver().registerContentObserver(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
				new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						// 数据发生了变化 重新载入cursor
						MediaFoldersCursorLoader.this.reset();
					}
				});
		context.getContentResolver().registerContentObserver(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
				new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						// 数据发生了变化 重新载入cursor
						MediaFoldersCursorLoader.this.reset();
					}
				});
	}

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is
	 * ready the callbacks will be called on the UI thread. If a previous load
	 * has been completed and is still valid the result may be passed to the
	 * callbacks immediately.
	 * 
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
	}

	public String getSelection() {
		return mSelection;
	}

	public String getSortOrder() {
		return mSortOrder;
	}

	public void setSortOrder(String sortOrder) {
		mSortOrder = sortOrder;
	}

	@Override
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer,
			String[] args) {
		super.dump(prefix, fd, writer, args);
		writer.print(prefix);
		writer.print("mImageProjection=");
		writer.println(Arrays.toString(mImageProjection));
		writer.print("mVideoProjection=");
		writer.println(Arrays.toString(mVideoProjection));
		writer.print(prefix);
		writer.print("mSelection=");
		writer.println(mSelection);
		writer.print(prefix);
		writer.print("mSortOrder=");
		writer.println(mSortOrder);
		writer.print(prefix);
		writer.print("mCursor=");
		writer.println(mCursor);
	}
}

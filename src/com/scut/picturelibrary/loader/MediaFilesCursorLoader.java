package com.scut.picturelibrary.loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.scut.picturelibrary.Constants;
import com.scut.picturelibrary.utils.SortCursor;

/**
 * 同时包括图片和视频文件的CursorLoader
 * 
 * @author 黄建斌
 * 
 */
public class MediaFilesCursorLoader extends AsyncTaskLoader<Cursor> {

	final ForceLoadContentObserver mObserver;

	@SuppressWarnings("unused")
	private final static String TAG = "MediaFilesCursorLoader";

	// Uri mUri;
	String[] mProjection;
	String[] mImageProjection;
	String[] mVideoProjection;
	String mSelection;
	String[] mSelectionArgs;
	String mSortOrder;

	Cursor mCursor;

	/* Runs on a worker thread */
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = null;
		Cursor imageCursor = getContext().getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mImageProjection,
				mSelection, mSelectionArgs, mSortOrder);
		Cursor videoCursor = getContext().getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoProjection,
				mSelection, mSelectionArgs, mSortOrder);
		if (imageCursor.getCount() == 0 || videoCursor.getCount() == 0) {// 无需合并排序
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
		cursor = new MergeCursor(new Cursor[] { videoCursor, imageCursor });
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
	public MediaFilesCursorLoader(Context context) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		registerDb(context);
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
						MediaFilesCursorLoader.this.reset();
					}
				});
		context.getContentResolver().registerContentObserver(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
				new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						// 数据发生了变化 重新载入cursor
						MediaFilesCursorLoader.this.reset();
					}
				});
	}

	/**
	 * Creates a fully-specified CursorLoader. See
	 * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)
	 * ContentResolver.query()} for documentation on the meaning of the
	 * parameters. These will be passed as-is to that call.
	 */
	public MediaFilesCursorLoader(Context context, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context);
		registerDb(context);
		mObserver = new ForceLoadContentObserver();
		// mUri = uri;
		mProjection = projection;
		mImageProjection = new String[projection.length + 1];
		mVideoProjection = new String[projection.length + 2];
		for (int i = 0; i < projection.length; i++) {
			mImageProjection[i] = projection[i];
			mVideoProjection[i] = projection[i];
		}
		mVideoProjection[projection.length + 1] = "duration";
		mImageProjection[projection.length] = "'image' as type";
		mVideoProjection[projection.length] = "'video' as type";
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mSortOrder = sortOrder;
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

	// public Uri getUri() {
	// return mUri;
	// }

	// public void setUri(Uri uri) {
	// mUri = uri;
	// }

	public String[] getProjection() {
		return mProjection;
	}

	public void setProjection(String[] projection) {
		mProjection = projection;
	}

	public String getSelection() {
		return mSelection;
	}

	public void setSelection(String selection) {
		mSelection = selection;
	}

	public String[] getSelectionArgs() {
		return mSelectionArgs;
	}

	public void setSelectionArgs(String[] selectionArgs) {
		mSelectionArgs = selectionArgs;
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
		// writer.print(prefix); writer.print("mUri="); writer.println(mUri);
		writer.print(prefix);
		writer.print("mProjection=");
		writer.println(Arrays.toString(mProjection));
		writer.print(prefix);
		writer.print("mSelection=");
		writer.println(mSelection);
		writer.print(prefix);
		writer.print("mSelectionArgs=");
		writer.println(Arrays.toString(mSelectionArgs));
		writer.print(prefix);
		writer.print("mSortOrder=");
		writer.println(mSortOrder);
		writer.print(prefix);
		writer.print("mCursor=");
		writer.println(mCursor);
		// writer.print(prefix); writer.print("mContentChanged=");
		// writer.println(mContentChanged);
	}
}

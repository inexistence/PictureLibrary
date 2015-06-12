package com.scut.picturelibrary.views;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.Adapter;
import android.widget.AdapterView;

/**
 * 自定义流式标签布局
 * 
 * @author 黄建斌
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class FlowTagsLayout extends AdapterView<Adapter> implements
		OnTouchListener {

	// save all tags
	private List<List<View>> mAllTags = new ArrayList<List<View>>();
	private List<Integer> mLineHeight = new ArrayList<Integer>();

	private Adapter mAdapter;
	private DataSetObserver mDataSetObserver;

	// click listener
	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;

	// click position
	private int mSelectedPosition = -1;

	// about touch event
	private int mTouchMode = MotionEvent.ACTION_UP;
	private float mTouchStartX = -1;
	private float mTouchStartY = -1;
	private static boolean afterLongClick = false;

	public FlowTagsLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FlowTagsLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlowTagsLayout(Context context) {
		this(context, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		// wrap_content
		int width = 0;
		int height = 0;

		int lineWidth = 0;
		int lineHeight = 0;

		int cCount = getChildCount();

		for (int i = 0; i < cCount; i++) {
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			MarginLayoutParams lp = (MarginLayoutParams) child
					.getLayoutParams();

			int childWidth = child.getMeasuredWidth() + lp.leftMargin
					+ lp.rightMargin;
			int childHeight = child.getMeasuredHeight() + lp.topMargin
					+ lp.bottomMargin;

			if (lineWidth + childWidth > sizeWidth) {// change line
				width = Math.max(width, lineWidth);
				lineWidth = childWidth;// reset line width
				// record height
				height += lineHeight;
				lineHeight = childHeight;
			} else {// not change line
				lineWidth += childWidth;
				lineHeight = Math.max(lineHeight, childHeight);
			}
			// last tag
			if (i == cCount - 1) {
				width = Math.max(lineWidth, width);
				height += lineHeight;
			}
		}
		setMeasuredDimension(modeWidth == MeasureSpec.AT_MOST ? width
				: sizeWidth, modeHeight == MeasureSpec.AT_MOST ? height
				: sizeHeight);
	}

	List<View> lineViews = new ArrayList<View>();

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mAllTags.clear();
		mLineHeight.clear();

		int width = getWidth();
		int lineWidth = 0;
		int lineHeight = 0;

		lineViews.clear();

		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			View child = getChildAt(i);
			MarginLayoutParams lp = (MarginLayoutParams) child
					.getLayoutParams();

			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();

			if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width) {
				// record line height
				mLineHeight.add(lineHeight);
				mAllTags.add(lineViews);

				lineWidth = 0;
				lineHeight = childHeight + lp.topMargin + lp.bottomMargin;

				lineViews = new ArrayList<View>();
			}

			lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
			lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
					+ lp.bottomMargin);

			lineViews.add(child);
		}

		// last line
		mLineHeight.add(lineHeight);
		mAllTags.add(lineViews);

		// set position of child view
		int left = 0;
		int top = 0;
		// num of lines
		int lineNum = mAllTags.size();

		for (int i = 0; i < lineNum; i++) {
			lineViews = mAllTags.get(i);
			lineHeight = mLineHeight.get(i);

			for (int j = 0; j < lineViews.size(); j++) {
				View child = lineViews.get(j);
				if (child.getVisibility() == View.GONE) {
					continue;
				}
				MarginLayoutParams lp = (MarginLayoutParams) child
						.getLayoutParams();
				int lc = left + lp.leftMargin;
				int tc = top + lp.topMargin;
				int rc = lc + child.getMeasuredWidth();
				int bc = tc + child.getMeasuredHeight();

				child.layout(lc, tc, rc, bc);
				left += child.getMeasuredWidth() + lp.leftMargin
						+ lp.rightMargin;
			}
			left = 0;
			top += lineHeight;
		}
	}

	/**
	 * LayoutParams for this layout
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	private List<View> mConvertViewCache = new ArrayList<View>();

	protected void resetChildView() {
		removeAllViewsInLayout();
		if (mAdapter != null) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				View child = null;
				if (i < mConvertViewCache.size())
					child = mConvertViewCache.get(i);// reuse old view
				if (child == null) {// no view to reuse
					child = mAdapter.getView(i, null, this);// create new view
					mConvertViewCache.add(child);// save view
				}
				if (child.getLayoutParams() != null
						&& child.getLayoutParams() instanceof MarginLayoutParams)
					addViewInLayout(child, i, child.getLayoutParams());
				else
					addViewInLayout(child, i, new MarginLayoutParams(
							MarginLayoutParams.WRAP_CONTENT,
							MarginLayoutParams.WRAP_CONTENT));
			}
		}
		// Invalidate all views
		invalidate();
		// layout again
		requestLayout();
	}

	@Override
	public void setAdapter(Adapter adapter) {
		if (mAdapter != null && mDataSetObserver != null) {
			// listen data set changed
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;
		resetChildView();

		if (mAdapter != null) {
			mDataSetObserver = new DataSetObserver() {
				@Override
				public void onChanged() {
					// if changed, reset child view
					resetChildView();
				}

				@Override
				public void onInvalidated() {
					super.onInvalidated();
				}
			};
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}
	}

	@Override
	public View getSelectedView() {
		return getChildAt(mSelectedPosition);
	}

	@Override
	public void setSelection(int position) {
		mSelectedPosition = position;
	}

	@Override
	public void setOnItemClickListener(
			android.widget.AdapterView.OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	@Override
	public void setOnItemLongClickListener(
			android.widget.AdapterView.OnItemLongClickListener listener) {
		mOnItemLongClickListener = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		onTouch(this, event);
		return super.dispatchTouchEvent(event);
	}

	Runnable mLongPressRunnable;

	/**
	 * start long press check after target time
	 */
	private void startLongPressCheck() {
		// create runnable
		if (mLongPressRunnable == null) {
			mLongPressRunnable = new Runnable() {

				@Override
				public void run() {
					// still pressed
					if (mTouchMode == MotionEvent.ACTION_MOVE) {
						mSelectedPosition = getClickChildPosition(mTouchStartX,
								mTouchStartY);
						if (mSelectedPosition != INVALID_POSITION) {
							if (mOnItemLongClickListener != null
									&& mSelectedPosition < mConvertViewCache
											.size()) {
								View v = mConvertViewCache
										.get(mSelectedPosition);
								mOnItemLongClickListener.onItemLongClick(
										FlowTagsLayout.this, v,
										mSelectedPosition, v.getId());
								// set short click listener unable
								afterLongClick = true;
							}
						}
					}
				}
			};
		}
		// start long click check after target time
		postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
	}

	public int getClickChildPosition(float x, float y) {
		int lineNum = mLineHeight.size();
		float height = 0;
		int touchLine = -1;
		for (int i = 0; i < lineNum; i++) {
			if (y >= height) {
				height += mLineHeight.get(i);
				if (y <= height) {
					touchLine = i;
					break;
				}
			}
		}
		if (touchLine == -1 || touchLine >= lineNum)
			return INVALID_POSITION;
		List<View> lineViews = mAllTags.get(touchLine);
		int touchCol = -1;
		int col = 0;
		int colNum = lineViews.size();
		for (int i = 0; i < colNum; i++) {
			if (x > col) {
				col += lineViews.get(i).getWidth();
				if (x <= col) {
					touchCol = i;
					break;
				}
			}
		}
		if (touchCol == -1 || touchCol >= colNum)
			return INVALID_POSITION;
		int positon = 0;
		for (int i = 0; i < touchLine; i++) {
			positon += mAllTags.get(i).size();
		}
		positon += touchCol;
		return positon;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mTouchStartX = event.getX();
			mTouchStartY = event.getY();
			if (null != mOnItemLongClickListener)
				startLongPressCheck();
		}
		if (event.getAction() == MotionEvent.ACTION_UP && !afterLongClick) {
			// could start short click event
			float x = event.getX();
			float y = event.getY();
			mSelectedPosition = getClickChildPosition(x, y);
			if (mSelectedPosition != INVALID_POSITION
					&& mOnItemClickListener != null
					&& mSelectedPosition < mConvertViewCache.size()) {
				View clickView = mConvertViewCache.get(mSelectedPosition);
				mOnItemClickListener.onItemClick(this, clickView,
						mSelectedPosition, clickView.getId());
			}
			// cancel long click check
			getHandler().removeCallbacks(mLongPressRunnable);
		} else if (event.getAction() == MotionEvent.ACTION_UP && afterLongClick) {
			// could not start short click event
			// set short click able
			afterLongClick = false;
		}
		mTouchMode = event.getAction();
		return true;
	}
}

package com.scut.picturelibrary.views;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.scut.picturelibrary.views.CustomTextView.OnCancelBtnListener;

/**
 * 可添加文案的图片布局视图
 * 
 * @author 黄建斌
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class EdtImageViewLayout extends FrameLayout {

	ImageView mImageView;
	List<CustomTextView> mEditTextList;

	public EdtImageViewLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public EdtImageViewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EdtImageViewLayout(Context context) {
		super(context);
		init();
	}

	public void setImageBitmap(Bitmap bm) {
		mImageView.setImageBitmap(bm);
	}

	private void init() {
		mEditTextList = new ArrayList<CustomTextView>();
		mImageView = new ImageView(getContext());
		addView(mImageView);
	}

	public interface DoForSelectedTextView {
		public void doForSelectedTextView(CustomTextView v, int index);
	}

	public void doForEachSelectedTextView(DoForSelectedTextView d) {
		for (int i = 0; i < mEditTextList.size(); i++) {
			if (mEditTextList.get(i).getFocus() == true) {
				d.doForSelectedTextView(mEditTextList.get(i), i);
			}
		}
	}

	public void removeTextView(CustomTextView v) {
		mEditTextList.remove(v);
		removeView(v);
		postInvalidate();
	}

	public void addEditText() {
		CustomTextView edt = new CustomTextView(getContext());
		edt.setOnCancelBtnListener(new OnCancelBtnListener() {

			@Override
			public boolean onCancel(CustomTextView v, MotionEvent event) {
				EdtImageViewLayout.this.removeTextView(v);
				return true;
			}
		});
		addView(edt);
		mEditTextList.add(edt);
		postInvalidate();
	}

	public List<CustomTextView> getTextViewList() {
		return mEditTextList;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		setAllFocuse(false);
		return super.onTouchEvent(event);
	}

	public void setAllFocuse(boolean focus) {
		for (int i = 0; i < mEditTextList.size(); i++) {
			mEditTextList.get(i).setFocus(focus);
		}
	}

}

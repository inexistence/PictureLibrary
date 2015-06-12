package com.scut.picturelibrary.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scut.picturelibrary.R;

/**
 * 双击编辑的文本
 * 
 * @author 黄建斌
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class DoubleClickEditText extends FrameLayout {
	EditText mEditText;
	TextView mTextView;

	public DoubleClickEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DoubleClickEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DoubleClickEditText(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		// 解析反射资源文件，然后将布局附加到当前的控件
		LayoutInflater.from(context).inflate(
				R.layout.view_double_click_edit_text, this, true);
		// 根据id获取子控件
		mEditText = (EditText) findViewById(R.id.edt_double_click);
		mTextView = (TextView) findViewById(R.id.tv_double_click);

		mTextView.setHint("双击编辑");
		mEditText.setHint("双击编辑");

		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					mTextView.setText(mEditText.getText());
					mTextView.setVisibility(View.VISIBLE);
					mEditText.setVisibility(View.GONE);
				}
			}
		});
		setTextSize(20.0f);
		setTextColor(Color.BLACK);
	}

	OnTouchListener mOnTouchListener;

	//
	// @Override
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	// return super.onTouchEvent(ev);
	// }

	public void setOnTouchListener(OnTouchListener l) {
		mOnTouchListener = l;
	}

	int mTextColor;

	public void setTextColor(int color) {
		mTextColor = color;
		// mEditText.setTextColor(color);
		mTextView.setTextColor(color);
	}

	public void setTypeface(Typeface tf) {
		mEditText.setTypeface(tf);
		mTextView.setTypeface(tf);
	}

	float mTextSize;

	public void setTextSize(float size) {
		mTextSize = size;
		mEditText.setTextSize(size);
		mTextView.setTextSize(size);
	}

	public float getTextSize() {
		return mTextSize;
	}

	public void setTextAlpha(float alpha) {
		mTextView.setAlpha(alpha);
	}

	public float getTextAlpha() {
		return mTextView.getAlpha();
	}

	public EditText getEditText() {
		return mEditText;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (isDoubleClick()) {
				mTextView.setVisibility(View.GONE);
				mEditText.setVisibility(View.VISIBLE);
				mEditText.setFocusable(true);
				mEditText.setFocusableInTouchMode(true);
				mEditText.requestFocus();
				mEditText.requestFocusFromTouch();
				return true;
			}
		}
		if (mOnTouchListener != null) {
			return mOnTouchListener.onTouch(this, event);
		}
		return false;
	}

	long mKeyTime;

	protected boolean isDoubleClick() {
		if ((System.currentTimeMillis() - mKeyTime) > 500) {
			mKeyTime = System.currentTimeMillis();
			return false;
		}
		return true;
	}
}

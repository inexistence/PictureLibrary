package com.scut.picturelibrary.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.scut.picturelibrary.R;

/**
 * 在双击编辑文本的基础上添加删除按钮 以及只有在点击到该控件时才显示删除按钮
 * 
 * @author 黄建斌
 * 
 */
@SuppressLint("ClickableViewAccessibility")
public class CustomTextView extends RelativeLayout {
	DoubleClickEditText mDoubleClickEditText;
	View mCancelBtn;

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		iniLayout(context);
		initTouchEvent();
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		iniLayout(context);
		initTouchEvent();
	}

	public CustomTextView(Context context) {
		super(context);
		iniLayout(context);
		initTouchEvent();
	}

	private void iniLayout(Context context) {
		// 解析反射资源文件，然后将布局附加到当前的控件
		LayoutInflater.from(context).inflate(R.layout.view_my_textview, this,
				true);
		// 根据id获取子控件
		mDoubleClickEditText = (DoubleClickEditText) findViewById(R.id.edt_my_textView);
		mCancelBtn = findViewById(R.id.btn_my_textView_cancel);
		CustomTextView.this.setFocus(true);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	private static final int EDIT_BTN_MOVING = 0x283;
	private static final int NO_EVENT = 0x0;
	int state = NO_EVENT;

	float downX, downY;
	float lastX, lastY;

	long mKeyTime;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean onTouch = super.onTouchEvent(event);
		mDoubleClickEditText.getEditText().setFocusable(false);
		return onTouch;
	}

	private void initTouchEvent() {
		mDoubleClickEditText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent ev) {
				CustomTextView.this.setFocus(true);
				switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downX = ev.getX();
					downY = ev.getY();
					state = EDIT_BTN_MOVING;
					break;
				case MotionEvent.ACTION_MOVE:
					float distX = ev.getX() - downX;
					float distY = ev.getY() - downY;
					if (state == EDIT_BTN_MOVING) {
						newL = (int) (distX + getLeft());
						newR = (int) (distX + getRight());
						newT = (int) (distY + getTop());
						newB = (int) (distY + getBottom());
						CustomTextView.this.layout(newL, newT, newR, newB);
					}
					break;
				}

				return true;
			}
		});

		mCancelBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mOnCancelBtnListener == null ? false
						: mOnCancelBtnListener.onCancel(CustomTextView.this,
								event);
			}
		});
	}

	private OnCancelBtnListener mOnCancelBtnListener;

	public void setOnCancelBtnListener(OnCancelBtnListener l) {
		mOnCancelBtnListener = l;
	}

	public interface OnCancelBtnListener {
		public boolean onCancel(CustomTextView v, MotionEvent event);
	}

	int newL, newR, newT, newB;
	boolean first = true;
	int mLeftPaddingToParent;
	int mTopPaddingToParent;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this
				.getLayoutParams();

		if (first) {
			newL = l;
			newT = t;
			newR = r;
			newB = b;
			mLeftPaddingToParent = getLeft() - lp.leftMargin;
			mTopPaddingToParent = getTop() - lp.topMargin;
			first = false;
		}

		lp.leftMargin = l - mLeftPaddingToParent;
		lp.topMargin = t - mTopPaddingToParent;
		super.onLayout(changed, l, t, r, b);
	}

	boolean isFocuse;

	public void setFocus(boolean focus) {
		isFocuse = focus;
		mDoubleClickEditText.getEditText().setFocusable(focus);
		if (focus) {
			mCancelBtn.setVisibility(View.VISIBLE);
			mDoubleClickEditText.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.bg_stroke_rec));
		} else {
			mCancelBtn.setVisibility(View.GONE);
			mDoubleClickEditText.setBackgroundDrawable(null);
		}
	}

	public boolean getFocus() {
		return isFocuse;
	}

	public void setTextColor(int color) {
		mDoubleClickEditText.setTextColor(color);
	}

	public void setTypeface(Typeface tf) {
		mDoubleClickEditText.setTypeface(tf);
	}

	public void setTextSize(float size) {
		size = size <= 0 ? 1 : size;
		mDoubleClickEditText.setTextSize(size);
	}

	public float getTextSize() {
		return mDoubleClickEditText.getTextSize();
	}

	public void setTextAlpha(float alpha) {
		if (alpha > 1.0f)
			alpha = 1.0f;
		if (alpha < 0.0f)
			alpha = 0.0f;
		mDoubleClickEditText.setAlpha(alpha);
	}

	public float getTextAlpha() {
		return mDoubleClickEditText.getAlpha();
	}
}

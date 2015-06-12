package com.scut.picturelibrary.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

/**
 * 可自由移动，放大缩小的ImageView
 * 
 * @author 黄建斌
 * 
 */
public class MoveableImageView extends ImageView implements
		OnGlobalLayoutListener, OnScaleGestureListener, OnTouchListener {

	private boolean mOnce;

	// 图片的初始、双击放大、最大的缩放值
	private float mInitScale;
	// private float mMidScale;
	private float mMaxScale;

	private Matrix mScaleMatrix;

	// 得到手指缩放值
	private ScaleGestureDetector mScaleGestureDetector;

	// 触屏点的数量,移动
	private int mPointerCount;
	private float mX0;
	private float mY0;
	private int mTouchSlop;
	private boolean isCanDrag;

	@SuppressLint("ClickableViewAccessibility")
	public MoveableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mScaleMatrix = new Matrix();
		setScaleType(ScaleType.MATRIX);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		setOnTouchListener(this);
		setCurrentBackgroundColor(mCurrentColor);
	}

	public MoveableImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MoveableImageView(Context context) {
		this(context, null);

	}

	public static int WHITE = Color.WHITE;
	public static int BLACK = Color.BLACK;
	int mCurrentColor = BLACK;

	public int getCurrentBackgroundColor() {
		return mCurrentColor;
	}

	public void setCurrentBackgroundColor(int c) {
		mCurrentColor = c;
		setBackgroundColor(mCurrentColor);
	}

	public void switchBackgroundColor() {
		setCurrentBackgroundColor(mCurrentColor = (mCurrentColor == BLACK ? WHITE
				: BLACK));
	}

	float mSize = 1.0f;

	public void setSize(float size) {
		mSize = size;
		int width = MeasureSpec.getSize(mWidthMeasureSpec);
		int newWidth = width;
		int newHeight = (int) (mSize * newWidth);

		mWidthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth,
				MeasureSpec.EXACTLY);
		mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight,
				MeasureSpec.EXACTLY);
		measure(mWidthMeasureSpec, mHeightMeasureSpec);
		invalidate();
		requestLayout();
	}

	/**
	 * 正中间
	 */
	public void setInCenter() {
		// 图片宽高
		Drawable d = getDrawable();
		if (d == null)
			return;
		int dw = d.getIntrinsicWidth();
		int dh = d.getIntrinsicHeight();

		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		dw *= values[Matrix.MSCALE_X];
		dh *= values[Matrix.MSCALE_Y];
		// 使图片居中显示
		int dx = getWidth() / 2 - dw / 2;
		int dy = getHeight() / 2 - dh / 2;

		values[Matrix.MTRANS_X] = dx;
		values[Matrix.MTRANS_Y] = dy;
		mScaleMatrix.setValues(values);
		setImageMatrix(mScaleMatrix);
	}

	/**
	 * 垂直中间
	 */
	public void setInVerticleCenter() {
		Drawable d = getDrawable();
		if (d == null)
			return;
		int dh = d.getIntrinsicHeight();

		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		dh *= values[Matrix.MSCALE_Y];
		int dy = getHeight() / 2 - dh / 2;

		values[Matrix.MTRANS_Y] = dy;
		mScaleMatrix.setValues(values);
		setImageMatrix(mScaleMatrix);
	}

	/**
	 * 水平中间
	 */
	public void setInHorizontalCenter() {
		// 图片宽高
		Drawable d = getDrawable();
		if (d == null)
			return;
		int dw = d.getIntrinsicWidth();

		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		dw *= values[Matrix.MSCALE_X];
		// 使图片居中显示
		int dx = getWidth() / 2 - dw / 2;

		values[Matrix.MTRANS_X] = dx;
		mScaleMatrix.setValues(values);
		setImageMatrix(mScaleMatrix);
	}

	int mWidthMeasureSpec, mHeightMeasureSpec;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidthMeasureSpec = widthMeasureSpec;
		mHeightMeasureSpec = heightMeasureSpec;
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int newWidth = width;
		int newHeight = (int) (mSize * newWidth);

		mWidthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth,
				MeasureSpec.EXACTLY);
		mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight,
				MeasureSpec.EXACTLY);

		super.onMeasure(mWidthMeasureSpec, mHeightMeasureSpec);
	}

	// 注册移除接口
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * 获取imageview加载的图片
	 */

	@Override
	public void onGlobalLayout() {

		if (!mOnce) {
			// 屏幕显示图片区域的宽和高
			int width = getWidth();
			int height = getHeight();

			// 图片宽高
			Drawable d = getDrawable();
			if (d == null)
				return;
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();

			float scale = 1.0f;
			// 改图片初始显示大小
			if (dw > width && dh <= height) {
				scale = width * 1.0f / dw;
			}

			if (dh > height && dw <= width) {
				scale = height * 1.0f / dh;
			}

			if ((dh > height && dw > width) || (dh <= height && dw <= width)) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			}

			mInitScale = scale;
			mMaxScale = mInitScale * 5;
			// mMidScale = mInitScale * 2;

			// 使图片居中显示
			int dx = getWidth() / 2 - dw / 2;
			int dy = getHeight() / 2 - dh / 2;

			mScaleMatrix.postTranslate(dx, dy);
			mScaleMatrix.postScale(mInitScale, mInitScale, width / 2,
					height / 2);
			setImageMatrix(mScaleMatrix);

			mOnce = true;
		}

	}

	// 得到图片缩放值
	public float getScale() {
		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {

		float currentScale = getScale();
		float scaleFactor = detector.getScaleFactor();

		if (getDrawable() == null)
			return true;
		float minScale = mInitScale / 4;
		// 范围内允许缩放
		if ((currentScale < mMaxScale && scaleFactor > 1.0f)
				|| (currentScale > minScale && scaleFactor < 1.0f)) {
			// 缩放范围边界控制
			if (currentScale * scaleFactor > mMaxScale) {
				scaleFactor = mMaxScale / currentScale;
			}
			if (currentScale * scaleFactor < minScale) {
				scaleFactor = minScale / currentScale;
			}

			mScaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());

			setImageMatrix(mScaleMatrix);

		}

		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {

		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		mScaleGestureDetector.onTouchEvent(event);

		// 触控中心点
		float x = 0;
		float y = 0;
		int count = event.getPointerCount();
		for (int i = 0; i < count; i++) {
			x = x + event.getX(i);
			y = y + event.getY(i);
		}
		x = x / count;
		y = y / count;

		// 触控点改变
		if (mPointerCount != count) {
			mX0 = x;
			mY0 = y;
			isCanDrag = false;
		}
		mPointerCount = count;

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:

			break;

		case MotionEvent.ACTION_MOVE:

			float dx = x - mX0;
			float dy = y - mY0;

			// 未移动
			if (!isCanDrag) {
				isCanDrag = isMove(dx, dy);
			}
			// 可移动
			if (isCanDrag) {

				if (getDrawable() != null) {
					mScaleMatrix.postTranslate(dx, dy);
					setImageMatrix(mScaleMatrix);
				}
			}
			mX0 = x;
			mY0 = y;

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			// 无触控
			mPointerCount = 0;
			break;

		default:
			break;
		}

		return true;
	}

	private boolean isMove(float dx, float dy) {
		if (Math.sqrt(dx * dx + dy * dy) > mTouchSlop)
			return true;
		return false;
	}

}

package com.scut.picturelibrary.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.scut.picturelibrary.R;
import com.scut.picturelibrary.views.CustomTextView;
import com.scut.picturelibrary.views.EdtImageViewLayout;
import com.scut.picturelibrary.views.EdtImageViewLayout.DoForSelectedTextView;

/**
 * 添加文案界面下的选项界面 字体颜色 字体大小 透明度
 * 
 * @author 黄建斌
 * 
 */
public class TextSettingFragment extends Fragment {
	EdtImageViewLayout mEdtImageViewLayout;

	View mWhiteBtn;
	View mBlackBtn;

	View mAddSizeBtn;
	View mSubSizeBtn;

	View mAddAlphaBtn;
	View mSubAlphaBtn;

	public TextSettingFragment(EdtImageViewLayout edtImageViewLayout) {
		super();
		mEdtImageViewLayout = edtImageViewLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fg_text_setting, null);
		mBlackBtn = v.findViewById(R.id.btn_text_setting_font_color_black);
		mWhiteBtn = v.findViewById(R.id.btn_text_setting_font_color_white);

		mAddSizeBtn = v.findViewById(R.id.btn_text_setting_add_size);
		mSubSizeBtn = v.findViewById(R.id.btn_text_setting_sub_size);

		mSubAlphaBtn = v.findViewById(R.id.btn_text_setting_sub_alpha);
		mAddAlphaBtn = v.findViewById(R.id.btn_text_setting_add_alpha);

		mBlackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextColor(Color.BLACK);
							}
						});
			}
		});
		mWhiteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextColor(Color.WHITE);
							}
						});
			}
		});

		mAddSizeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextSize(v.getTextSize() + 1);
							}
						});
			}
		});
		mSubSizeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextSize(v.getTextSize() - 1);
							}
						});
			}
		});

		mSubAlphaBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextAlpha(v.getTextAlpha() - 0.1f);
							}
						});
			}
		});

		mAddAlphaBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEdtImageViewLayout
						.doForEachSelectedTextView(new DoForSelectedTextView() {

							@Override
							public void doForSelectedTextView(CustomTextView v,
									int index) {
								v.setTextAlpha(v.getTextAlpha() + 0.1f);
							}
						});
			}
		});

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
}

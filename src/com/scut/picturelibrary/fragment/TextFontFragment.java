package com.scut.picturelibrary.fragment;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.scut.picturelibrary.Constants;
import com.scut.picturelibrary.R;
import com.scut.picturelibrary.adapter.TextFontsShowAdapter;
import com.scut.picturelibrary.views.CustomTextView;
import com.scut.picturelibrary.views.EdtImageViewLayout;
import com.scut.picturelibrary.views.EdtImageViewLayout.DoForSelectedTextView;
import com.scut.picturelibrary.views.FlowTagsLayout;

/**
 * 添加文案界面下的选项界面 选择字体
 * 
 * @author 黄建斌
 * 
 */
public class TextFontFragment extends Fragment {
	ArrayAdapter<Integer> mAdapter;
	EdtImageViewLayout mEdtImageViewLayout;

	public TextFontFragment(EdtImageViewLayout edtImageViewLayout) {
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
		View v = inflater.inflate(R.layout.fg_text_font, null);
		FlowTagsLayout mTextFonts = (FlowTagsLayout) v
				.findViewById(R.id.ftl_text_fonts);
		// mAdapter = new ArrayAdapter<String>(getActivity(),
		// R.layout.item_text_font, R.id.btn_tags_item_text,
		// new String[] { "字体1", "字体2", "字体3", "字体4", "字体5", "字体6", "字体7",
		// "字体8", });
		mAdapter = new TextFontsShowAdapter(getActivity(),
				R.layout.item_text_font, R.id.img_text_font_item_text);
		mTextFonts.setAdapter(mAdapter);
		mTextFonts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if (mEdtImageViewLayout != null) {
					mEdtImageViewLayout
							.doForEachSelectedTextView(new DoForSelectedTextView() {
								@Override
								public void doForSelectedTextView(
										CustomTextView v, int index) {
									if (position >= Constants.FONTS.length)
										return;
									AssetManager mgr = getActivity()
											.getAssets();// 得到AssetManager
									Typeface tf = Typeface.createFromAsset(mgr,
											Constants.FONTS[position]);
									v.setTypeface(tf);
								}
							});
				}
			}
		});
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
}

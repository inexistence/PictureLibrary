package com.scut.picturelibrary.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.scut.picturelibrary.Constants;

/**
 * 用于显示都有哪些字体样式可选择的Adapter
 * 
 * @author 黄建斌
 * 
 */
public class TextFontsShowAdapter extends ArrayAdapter<Integer> {
	int mResId;
	int mTextViewResourceId;
	Context mContext;
	ArrayList<Integer> mContent;

	public TextFontsShowAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		mContext = context;
		mResId = resource;
		mContent = new ArrayList<Integer>();
		for (int i = 0; i < Constants.FONTS_DRAWABLE_ID.length; i++) {
			mContent.add(Constants.FONTS_DRAWABLE_ID[i]);
		}
		addAll(mContent);
		mTextViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = View.inflate(mContext, mResId, null);
		Button img = (Button) v.findViewById(mTextViewResourceId);
		int id = getItem(position);
		img.setBackgroundDrawable(getContext().getResources().getDrawable(id));
		return v;
	}
}

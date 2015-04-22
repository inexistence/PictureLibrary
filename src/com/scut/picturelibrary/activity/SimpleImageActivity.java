package com.scut.picturelibrary.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.scut.picturelibrary.fragment.ImagePagerFragment;

/**
 * 显示大图Activity Fragment
 * 
 * @author 黄建斌
 */
public class SimpleImageActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String tag = ImagePagerFragment.class.getSimpleName();
		Fragment fr = getSupportFragmentManager().findFragmentByTag(tag);
		if (fr == null) {
			fr = new ImagePagerFragment();
			fr.setArguments(getIntent().getExtras());
		}
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, fr, tag).commit();
	}
}
package com.vonchenchen.mybledemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vonchenchen.mybledemo.R;

public class SimpleTitleBar extends RelativeLayout {
	private TextView mTitleText;
	private TextView mLeftText;
	private TextView mRightText;
	private ImageView mLeftImage;
	private ImageView mRightImage;
	private View mLeftLayout;
	private View mRightLayout;

	/** 字体样式：方正大黑简体 */
	//protected Typeface mFZFont;

	public SimpleTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();

		mTitleText = (TextView) findViewById(R.id.title_mid);
		mLeftText = (TextView) findViewById(R.id.title_left_text);
		mRightText = (TextView) findViewById(R.id.title_right_text);
		mLeftImage = (ImageView) findViewById(R.id.title_left);
		mRightImage = (ImageView) findViewById(R.id.title_right);
		mLeftLayout = findViewById(R.id.leftLayout);
		mRightLayout = findViewById(R.id.rightLayout);
	}

	public void setTitle(int resId) {
		mTitleText.setText(resId);
	}

	public void setTitle(String text) {
		mTitleText.setText(text);
	}

	public void setTitleTextSize(int size) {
		mTitleText.setTextSize(size);
	}

	public void setLeftText(int resId) {
		mLeftText.setText(resId);
	}

	public void setLeftText(String text) {
		mLeftText.setText(text);
	}

	public void setRightText(int resId) {
		mRightText.setText(resId);
	}

	public void setRightText(String text) {
		mRightText.setText(text);
	}
	
	public void setRightTextSize(int size) {
		mRightText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
	}

	public void setLeftImage(int resId) {
		mLeftImage.setImageResource(resId);
	}

	public void setRightImage(int resId) {
		mRightImage.setImageResource(resId);
	}

	public void setLeftOnClickListener(OnClickListener listener) {
		mLeftLayout.setOnClickListener(listener);
	}

	public void setRightOnClickListener(OnClickListener listener) {
		mRightLayout.setOnClickListener(listener);
	}

	public void setRightBackgroud(int resId) {
		mRightLayout.setBackgroundResource(resId);
	}
	
	public TextView getLeftText() {
		return mLeftText;
	}
}

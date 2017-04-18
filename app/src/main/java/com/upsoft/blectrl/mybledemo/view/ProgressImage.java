/**  
 * @Title: FeiHuaProgressImage.java
 * @Package cn.com.fh21.iask.view
 * @Description: TODO(用一句话描述该文件做什么)
 * @author 张鹏飞   QQ：83659757  
 * @date 2015-1-22 下午5:30:19
 * @version V1.2  
 */
package com.vonchenchen.mybledemo.view;

import android.content.Context;
import android.graphics.drawable.RotateDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.vonchenchen.mybledemo.utils.UIUtils;

public class ProgressImage {
	private Context context;
	public ProgressImage(Context context) {
		this.context = context;
	}

	//获取ProgressBar
	public ProgressBar getProgress() {
		RotateDrawable rotateDrawable = new RotateDrawable();
		//rotateDrawable.invalidateDrawable(context.getResources().getDrawable(R.anim.progress_anim));
		ProgressBar pb = new ProgressBar(context);
			//pb.setBackgroundResource(R.drawable.progress_dialog_bg);
			//pb.setIndeterminateDrawable(context.getResources().getDrawable(R.anim.progress_anim));
//		    pb.setPaddingRelative(UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10));
//			pb.setPadding(UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10));
		//设置进度圆圈的大小
//		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT); 
		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(UIUtils.dip2px(80),UIUtils.dip2px(80));
//		lp.setMargins(UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10), UIUtils.dip2px(10));
		//设置圆圈居中对齐方式
		lp.gravity = Gravity.CENTER;
		//设置圈圈的属性
		pb.setLayoutParams(lp);
		return pb;
	}
	


}

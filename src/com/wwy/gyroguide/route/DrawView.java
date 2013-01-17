package com.wwy.gyroguide.route;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.wwy.wwyandroid.R;

public class DrawView extends View {
	Paint paint = new Paint();
	Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.pijl2);
	private Matrix matrix;

	public DrawView(Context context) {
		super(context);
		paint.setColor(Color.BLACK);
	}
	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(matrix != null) {
			canvas.drawBitmap(bmp, matrix, paint);
		}
	}
	public void updateArrow(int angle) {
		if(getHeight() > 0 && getWidth() > 0) {
			bmp = Bitmap.createScaledBitmap(bmp, getHeight() - 10, getWidth() - 10, false);
		}
		matrix = new Matrix();
		matrix.postRotate(angle-90,bmp.getWidth()/2,bmp.getHeight()/2); 
		invalidate();
	}

}

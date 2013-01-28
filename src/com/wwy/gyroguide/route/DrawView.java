package com.wwy.gyroguide.route;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class DrawView extends View {
	Paint paint = new Paint();
	Bitmap bitmap; 

	private Matrix matrix;

	public DrawView(Context context) {
		super(context);
		paint.setColor(Color.BLACK);
	}
	
	public DrawView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
	}
	
	public DrawView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	private void setBitmap() {
		String svgArrow = getSvgImage();
		InputStream inputStream = new ByteArrayInputStream(svgArrow.getBytes());
		SVG svg = SVGParser.getSVGFromInputStream(inputStream);
		bitmap = drawableToBitmap(svg.createPictureDrawable());
	}
	
	private String getSvgImage() {
		return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + 
				getWidth() + "\" height=\"" + getWidth() + "\"><path id=\"arrow\" " +
				"fill=\"#090BAB\" " + "d=\"M " + getWidth()/2 + ",0 L "	+ getWidth()/5*4 + 
				"," + getWidth()/10*7 + "" + " L " + getWidth()/2 + "," + getWidth()/2*1 + 
				" L " + getWidth()/5*1 + "," + getWidth()/10*7 + "\" /></svg>";
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(matrix != null && bitmap != null) {
			canvas.drawBitmap(bitmap, matrix, paint);
		} else {
			setBitmap();
		}
	}
	
	public void updateArrow(int angle) {
		if (matrix == null) {
			matrix = new Matrix();
		}
		matrix.setRotate(angle, getWidth()/2, getWidth()/2); 
		invalidate();
	}
	
	private Bitmap drawableToBitmap (Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
}

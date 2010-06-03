package org.droidraw;
import android.content.Context;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.util.Log;

public class DrawView extends View {
	private Bitmap bmp;
	private Bitmap brush;
	private Canvas canv;
	private Paint paint;
	private Rect srcRect;
	private RectF dstRect;
	private Rect invRect;
	int r;

	public Bitmap getBitmap() {
		return bmp;
	}
	
	public DrawView(Context c) {
		super(c);
		paint = new Paint();
		paint.setAntiAlias(true);
		setBrush(0x3FFFFFFF, 4);
	}

	public void setBrush(int c, int r) {
		paint.setColor(c);

		// generate brush bitmap
		brush = Bitmap.createBitmap(2 * r, 2 * r, Bitmap.Config.ARGB_8888);
		Canvas temp = new Canvas();
		temp.setBitmap(brush);
		temp.drawCircle(r, r, r, paint);

		// pre-setup rectangles (avoid stressing the gc)
		dstRect = new RectF();
		invRect = new Rect();
		srcRect = new Rect();
		srcRect.set(0, 0, 2 * r, 2 * r);
		this.r = r;
	}

	public void clear() {
		canv.drawPaint(paint);
		invalidate();
	}

	@Override protected void onDraw(Canvas canvas) {
		if (bmp != null)
			canvas.drawBitmap(bmp, 0, 0, null);
	}

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.v("DroiDraw", "resize w=" + w + ", h=" + h);
		int cw = bmp != null ? bmp.getWidth() : 0;
		int ch = bmp != null ? bmp.getHeight() : 0;
		if (cw >= w && ch >= h)
			return;

		if (cw < w)
			cw = w;
		if (ch < h)
			ch = h;

		Bitmap nbmp = Bitmap.createBitmap(cw, ch, Bitmap.Config.ARGB_8888);
		Canvas ncanv = new Canvas();
		ncanv.setBitmap(nbmp);

		if (bmp != null)
			ncanv.drawBitmap(bmp, 0, 0, null);

		bmp = nbmp;
		canv = ncanv;
	}

	private float cx;
	private float cy;

	@Override public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.v("DroiDraw", "down");
			cx = event.getX();
			cy = event.getY();
			drawPoint(cx, cy);
			return true;

		case MotionEvent.ACTION_MOVE:
			Log.v("DroiDraw", "move");
			int n = event.getHistorySize();
			for (int i = 0; i < n; i++)
				lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
			lineTo(event.getX(), event.getY());
			return true;

		default:
			return true;
		}
	}

	private void lineTo(float x, float y) {
		float dx = x - cx;
		float dy = y - cy;

		if (dx * dx < 0.1 && dy * dy < 0.1)
			return;

		float len = (float)Math.sqrt(dx * dx + dy * dy);
		float s = 1.0f / len;
		float ddx = dx * s;
		float ddy = dy * s;

		for (float f = 0.0f; f < len; f++) {
			cx += ddx;
			cy += ddy;
			drawPoint(cx, cy);
		}
	}

	private void drawPoint(float x, float y) {
		dstRect.set(x - r, y - r,
				x + r, y + r);

		if (bmp != null)
			canv.drawBitmap(brush, srcRect, dstRect, paint);

		dstRect.roundOut(invRect);
		invalidate(invRect);
	}
}
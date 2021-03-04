package com.osiris;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class OverlayShowingService extends Service implements View.OnTouchListener, View.OnClickListener {

	private View topLeftView;
	private ImageView overlayView;

	private Button overlayedButton;
	private float offsetX;
	private float offsetY;
	private int originalXPos;
	private int originalYPos;
	private boolean moving;
	private WindowManager wm;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		overlayedButton = new Button(this);
		overlayedButton.setText("Overlay button");
		overlayedButton.setOnTouchListener(this);
		overlayedButton.setAlpha(0.5f);
		overlayedButton.setBackgroundColor(0x55fe4444);
		overlayedButton.setOnClickListener(this);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.OPAQUE);
		params.gravity = Gravity.START | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		params.alpha = 255;
		//wm.addView(overlayedButton, params);

		overlayView = new ImageView(this);
		overlayView.setAlpha(1.0f);
		WindowManager.LayoutParams overlayViewParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.TRANSPARENT);
		overlayViewParams.gravity = Gravity.LEFT | Gravity.TOP;
		overlayViewParams.x = 0;
		overlayViewParams.y = 0;
		overlayViewParams.alpha = 0;
		Bitmap overlayBitmap = Bitmap.createBitmap(800,
				600, Bitmap.Config.ARGB_8888);
		overlayBitmap.eraseColor(Color.TRANSPARENT);
		overlayView.setImageBitmap(drawTextToBitmap(overlayBitmap, "OSiris Overlay", 0, 0));
		wm.addView(overlayView, params);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		int Measuredwidth = 0;
		int Measuredheight = 0;
		Point size = new Point();
		WindowManager w = wm;
		w.getDefaultDisplay().getSize(size);
		Measuredwidth = size.x;
		Measuredheight = size.y;

		super.onConfigurationChanged(newConfig);
		wm.removeView(overlayView);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.OPAQUE);
		params.gravity = Gravity.START | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		params.alpha = 255;
		overlayView = new ImageView(this);
		overlayView.setAlpha(1.0f);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Bitmap overlayBitmap = Bitmap.createBitmap(Measuredwidth,
					Measuredheight, Bitmap.Config.ARGB_8888);
			overlayBitmap.eraseColor(Color.TRANSPARENT);
			wm.addView(overlayView, params);

			overlayView.setImageBitmap(drawTextToBitmap(overlayBitmap, "OSiris Overlay", 0, 0));
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Bitmap overlayBitmap = Bitmap.createBitmap(Measuredwidth,
					Measuredheight, Bitmap.Config.ARGB_8888);
			overlayBitmap.eraseColor(Color.TRANSPARENT);
			wm.addView(overlayView, params);

			overlayView.setImageBitmap(drawTextToBitmap(overlayBitmap, "OSiris Overlay", 0, 0));
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (overlayedButton != null) {
			wm.removeView(overlayedButton);
			wm.removeView(topLeftView);
			overlayedButton = null;
			topLeftView = null;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			float x = event.getRawX();
			float y = event.getRawY();

			moving = false;

			int[] location = new int[2];
			overlayedButton.getLocationOnScreen(location);

			originalXPos = location[0];
			originalYPos = location[1];

			offsetX = originalXPos - x;
			offsetY = originalYPos - y;

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			int[] topLeftLocationOnScreen = new int[2];
			topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

			System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
			System.out.println("originalY="+originalYPos);

			float x = event.getRawX();
			float y = event.getRawY();

			WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayedButton.getLayoutParams();

			int newX = (int) (offsetX + x);
			int newY = (int) (offsetY + y);

			if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
				return false;
			}

			params.x = newX - (topLeftLocationOnScreen[0]);
			params.y = newY - (topLeftLocationOnScreen[1]);

			wm.updateViewLayout(overlayedButton, params);
			moving = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (moving) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();
	}

	public Bitmap drawTextToBitmap(Bitmap bitmap, String text, int x, int y) {
		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color - #3D3D3D
		paint.setColor(Color.rgb(61, 61, 61));
		// text size in pixels
		paint.setTextSize((int) (14 * 2));
		// text shadow
		paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

		// draw text to the Canvas center
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		canvas.drawText(text, -bounds.left + x, -bounds.top + y, paint);

		return bitmap;
	}
}
package com.example.and0701.drawsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	Thread _thread = null;
	SurfaceHolder _holder;
	Bitmap _bitmapDroid;

	float _currentX, _currentY;
	float _targetX, _targetY;
	final float SPEED = 30.0f;
	float _velocity;
	float direction;
	ArrayList<Point> droidPoints;

	public MySurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
		initialize();
	}

	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		initialize();
	}

	private void initialize() {
		_currentX = 0;
		_currentY = 0;
		_targetX = 0;
		_targetY = 0;
		_velocity = SPEED;
		direction = 1.0f;
		droidPoints = new ArrayList<>();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_bitmapDroid = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.droid_side_01);

		_thread = new Thread(this);
		this._holder = holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		_thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		_thread = null;
	}

	private float getDistance(float x, float y, float x2, float y2) {
		return (float) Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
	}

	private double getAngle(float x, float y, float x2, float y2) {
		double xDistance = x2 - x;
		double yDistance = y2 - y;
		double result = Math.atan2(yDistance, xDistance) * 180 / Math.PI;
		return (result + 180);
	}

	@Override
	public void run() {
		Canvas canvas;
		Paint paint = new Paint();

		while (_thread != null) {
			canvas = _holder.lockCanvas();
			if (canvas == null) return;
			canvas.drawColor(Color.BLACK);

			moveDroid();
//			canvas.rotate((float)angle, _currentX + _bitmapDroid.getWidth() / 2, _currentY + _bitmapDroid.getHeight() / 2);
			// 残像
			droidPoints.add(new Point((int)_currentX, (int)_currentY));
			if(droidPoints.size() > 20)
				droidPoints.remove(0);
			for(Point point : droidPoints) {
				canvas.drawBitmap(_bitmapDroid, point.x, point.y, paint);
			}

			drawDroid(canvas, paint);

			_holder.unlockCanvasAndPost(canvas);
			loopWait();
		}
	}

	private void moveDroid() {
		// Move
		if (getDistance(_currentX, _currentY, _targetX, _targetY) < _velocity) {
			_currentX = _targetX;
			_currentY = _targetY;
		} else {
			double angle = getAngle(_currentX, _currentY, _targetX, _targetY);
			_currentX += -(float) 50 * Math.cos(angle * Math.PI / 180.0);
			_currentY += -(float) 50 * Math.sin(angle * Math.PI / 180.0);
		}
	}

	private void drawDroid(Canvas canvas, Paint paint) {
		// Draw
		canvas.save();
		canvas.scale(direction, 1.0f, _currentX + _bitmapDroid.getWidth() / 2, _currentY + _bitmapDroid.getHeight() / 2);
		canvas.drawBitmap(_bitmapDroid, _currentX, _currentY, paint);
		canvas.restore();
	}

	private void loopWait() {
		try {
			Thread.sleep(1000 / 60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		_targetX = event.getX() - _bitmapDroid.getWidth() / 2;
		_targetY = event.getY() - _bitmapDroid.getHeight() / 2;
		direction = (_currentX < _targetX) ? -1.0f : 1.0f;
		return super.onTouchEvent(event);
	}
}

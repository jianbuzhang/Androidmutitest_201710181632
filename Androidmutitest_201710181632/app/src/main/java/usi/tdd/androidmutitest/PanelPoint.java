package usi.tdd.androidmutitest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * 20160919 Lynette: modify the position of central rectangle
 */
//public class PanelPoint {}
public class PanelPoint extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private GestureDetector mDetector;
    private Context mCtx;
    public int mFailCountSetting;
    public int mFailCount = 0;

    float width = 80;
    float fDistance = 10;
    float p1_x;
    float p1_y;
    boolean IsP1Clicked = false;

    float p2_x;
    float p2_y;
    boolean IsP2Clicked = false;

    float p3_x;
    float p3_y;
    boolean IsP3Clicked = false;

    float p4_x;
    float p4_y;
    boolean IsP4Clicked = false;

    float p5_x;
    float p5_y;
    boolean IsP5Clicked = false;

    public PanelPoint(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mCtx = context;
        init();
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);
        mDetector = new GestureDetector(this);
    }

    protected void init() {
        try {
            mFailCountSetting = GetDefine.getValueInt("touch-panel", "fail_condition", "fail_count");
        } catch (Exception e) {
            Log.d("MFG_TEST", e.toString());
            Log.d("MFG_TEST", "Initial Parameter Fail!!");
            ((MainActivity) mCtx).finish();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        //super.onDraw(canvas);
        if (mCanvas == null) {
            mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            Log.d("MFG_TEST", "Screen Width:" + Integer.toString(canvas.getWidth()));
            Log.d("MFG_TEST", "Screen Height:" + Integer.toString(canvas.getHeight()));
        }
        canvas.drawColor(0xFFAAAAAA);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        // Left up
        p1_x = fDistance;
        p1_y = fDistance;
        canvas.drawRect(p1_x, p1_y, p1_x + width, p1_y + width, paint);

        // Right up
        p2_x = canvas.getWidth() - fDistance - width;
        p2_y = fDistance;
        canvas.drawRect(p2_x, p2_y, p2_x + width, p2_y + width, paint);

        // Left down
        p3_x = fDistance;
        p3_y = canvas.getHeight() - fDistance - width;
        canvas.drawRect(p3_x, p3_y, p3_x + width, p3_y + width, paint);

        // Left down
        p4_x = canvas.getWidth() - fDistance - width;
        p4_y = canvas.getHeight() - fDistance - width;
        canvas.drawRect(p4_x, p4_y, p4_x + width, p4_y + width, paint);

        // central
        //p5_x = canvas.getWidth()/2 - fDistance;
        //p5_y = canvas.getHeight()/2 - fDistance;
        p5_x = (canvas.getWidth() - width) / 2;
        p5_y = (canvas.getHeight() - width) / 2;
        canvas.drawRect(p5_x, p5_y, p5_x + width, p5_y + width, paint);
        mCanvas.drawPath(mPath, mPaint);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        float x = e.getX();
        float y = e.getY();
        if ((x > p1_x && x < p1_x + width) && (y > p1_y && y < p1_y + width)) {
            IsP1Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p1_x, p1_y, p1_x + width, p1_y + width, paint);
        } else if ((x > p1_x && x < p1_x + width) && (y > p1_y && y < p1_y + width)) {
            IsP1Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p1_x, p1_y, p1_x + width, p1_y + width, paint);
        } else if ((x > p2_x && x < p2_x + width) && (y > p2_y && y < p2_y + width)) {
            IsP2Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p2_x, p2_y, p2_x + width, p2_y + width, paint);
        } else if ((x > p3_x && x < p3_x + width) && (y > p3_y && y < p3_y + width)) {
            IsP3Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p3_x, p3_y, p3_x + width, p3_y + width, paint);
        } else if ((x > p4_x && x < p4_x + width) && (y > p4_y && y < p4_y + width)) {
            IsP4Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p4_x, p4_y, p4_x + width, p4_y + width, paint);
        } else if ((x > p5_x && x < p5_x + width) && (y > p5_y && y < p5_y + width)) {
            IsP5Clicked = true;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            mCanvas.drawRect(p5_x, p5_y, p5_x + width, p5_y + width, paint);
        } else
            mFailCount++;

        if (IsP1Clicked && IsP2Clicked && IsP3Clicked && IsP4Clicked && IsP5Clicked) {
            Log.d("MFG_TEST", "SUCCESSFUL TEST");
            ((MainActivity) mCtx).finish();
            //finish();
        }
        if (mFailCount > mFailCountSetting) {
            Log.d("MFG_TEST", "Click to many times!!\r\nUUT-FAIL\r\n");
            ((MainActivity) mCtx).finish();
        }
        return false;
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        this.mDetector.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);

                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                touch_up();
                invalidate();
                break;
        }
        return true;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        return false;
    }
}

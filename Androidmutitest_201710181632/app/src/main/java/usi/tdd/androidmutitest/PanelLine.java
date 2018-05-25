package usi.tdd.androidmutitest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 201600920 Lynette: modify draw Line & Rec. use SCREEN_WIDTH & SCREEN_HEIGHT, delete top_x, top_y,(from MFGDefine.xml )
 * <p>
 * }
 */

public class PanelLine extends View {

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private String szSaveFile = "/sdcard/screen.png";

    int SCREEN_WIDTH;
    int SCREEN_HEIGHT;//800;

    //Horizontal
    //int Top_Y=10; // 線的位置
    int Top_Tolerance = 20; // 可以畫線的範圍 (y-tolerance ~ y+tolerance)
    int Top_StartX = 100; // 第一個點的位置必須小於
    int Top_EndX = 100; // 最後一個點的位置必須大於
    int Top_MinPointNumber = 10; // 畫每條線的點不可小於 point number
    boolean Top_Passed = false;
    boolean Top_CheckSeq = true; //是否檢查點順序

    //int Bottom_Y=600;
    int Bottom_Tolerance = 20;
    int Bottom_StartX = 100;
    int Bottom_EndX = 100;
    int Bottom_MinPointNumber = 10;
    boolean Bottom_Passed = false;
    boolean Bottom_CheckSeq = false;

    //int Left_X=30;
    int Left_Tolerance = 20;
    int Left_StartY = 100;
    int Left_EndY = 100;
    int Left_MinPointNumber = 10;
    boolean Left_Passed = false;
    boolean Left_CheckSeq = true;

    //int Right_X=450;
    int Right_Tolerance = 20;
    int Right_StartY = 100;
    int Right_EndY = 100;
    int Right_MinPointNumber = 10;
    boolean Right_Passed = false;
    boolean Right_CheckSeq = true;

    boolean IsX1Passed = false;
    boolean x1CheckSeq = false;
    int x1Width = 30;
    int x1PointNumber = 10;
    float x1StartX = 100;
    float x1StartY = 100;
    float x1EndX = 100;
    float x1EndY = 100;

    boolean IsX2Passed = false;
    boolean x2CheckSeq = false;
    int x2Width = 30;
    int x2PointNumber = 10;
    float x2StartX = 100;
    float x2StartY = 100;
    float x2EndX = 100;
    float x2EndY = 100;

    int failCountSetting;
    int failCount = 0;
    int timeOutSetting;
    int timeOut;
    Context mCtx;

    enum TestGruop {
        top, left, right, bottom, x1, x2, finished
    }

    TestGruop currentItem;
    List<Float[]> pointsList = new ArrayList<Float[]>();

    public PanelLine(Context context) {
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
        mPaint.setStrokeWidth(6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        if (mCanvas == null) {
            SCREEN_WIDTH = canvas.getWidth();
            SCREEN_HEIGHT = canvas.getHeight();//800;

            Log.d("MFG_TEST", "Screen Width:" + SCREEN_WIDTH);
            Log.d("MFG_TEST", "Screen Height:" + SCREEN_HEIGHT);

            mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            Paint paint1 = new Paint();
            paint1.setStrokeWidth(2);
            paint1.setColor(Color.DKGRAY);
            paint1.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
            //draw top range line
            //mCanvas.drawLine(0, Top_Y-Top_Tolerance,SCREEN_WIDTH ,Top_Y-Top_Tolerance, paint1);
            //mCanvas.drawLine(0, Top_Y+Top_Tolerance,SCREEN_WIDTH ,Top_Y+Top_Tolerance, paint1);
            mCanvas.drawLine(0, Top_Tolerance, SCREEN_WIDTH, Top_Tolerance, paint1);

            //draw bottom range line
            //mCanvas.drawLine(0, Bottom_Y-Bottom_Tolerance,SCREEN_WIDTH ,Bottom_Y-Bottom_Tolerance, paint1);
            //mCanvas.drawLine(0, Bottom_Y+Bottom_Tolerance,SCREEN_WIDTH ,Bottom_Y+Bottom_Tolerance, paint1);
            mCanvas.drawLine(0, SCREEN_HEIGHT - Bottom_Tolerance, SCREEN_WIDTH, SCREEN_HEIGHT - Bottom_Tolerance, paint1);

            //draw left range line
            //mCanvas.drawLine(Left_X-Left_Tolerance, 0, Left_X-Left_Tolerance,SCREEN_HEIGHT, paint1);
            //mCanvas.drawLine(Left_X+Left_Tolerance, 0, Left_X+Left_Tolerance,SCREEN_HEIGHT, paint1);
            mCanvas.drawLine(Left_Tolerance, 0, Left_Tolerance, SCREEN_HEIGHT, paint1);

            //draw right range line
            //mCanvas.drawLine(Right_X-Right_Tolerance, 0, Right_X-Right_Tolerance,SCREEN_HEIGHT, paint1);
            //mCanvas.drawLine(Right_X+Right_Tolerance, 0, Right_X+Right_Tolerance,SCREEN_HEIGHT, paint1);
            mCanvas.drawLine(SCREEN_WIDTH - Right_Tolerance, 0, SCREEN_WIDTH - Right_Tolerance, SCREEN_HEIGHT, paint1);


            float a = (float) canvas.getHeight() / (float) canvas.getWidth();
            mCanvas.drawLine(0, a * x1Width, canvas.getWidth() - x1Width, canvas.getHeight(), paint1);
            mCanvas.drawLine(x1Width, 0, canvas.getWidth(), canvas.getHeight() - a * x1Width, paint1);

            mCanvas.drawLine(0, canvas.getHeight() - a * x2Width, canvas.getWidth() - x2Width, 0, paint1);
            mCanvas.drawLine(x2Width, canvas.getHeight(), canvas.getWidth(), a * x1Width, paint1);
        }
        canvas.drawColor(0xFFAAAAAA);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        if (!Top_Passed) {
            //canvas.drawRect(0, Top_Y-Top_Tolerance, SCREEN_WIDTH, Top_Y+Top_Tolerance, paint);
            canvas.drawRect(0, 0, SCREEN_WIDTH, Top_Tolerance, paint);
            currentItem = TestGruop.top;
        } else if (!Left_Passed) {
            //canvas.drawRect(Left_X-Left_Tolerance, 0, Left_X+Left_Tolerance,SCREEN_HEIGHT, paint);
            canvas.drawRect(0, 0, Left_Tolerance, SCREEN_HEIGHT, paint);
            currentItem = TestGruop.left;
        } else if (!Right_Passed) {
            //canvas.drawRect(Right_X-Right_Tolerance, 0, Right_X+Right_Tolerance,SCREEN_HEIGHT, paint);
            canvas.drawRect(SCREEN_WIDTH - Right_Tolerance, 0, SCREEN_WIDTH, SCREEN_HEIGHT, paint);
            currentItem = TestGruop.right;
        } else if (!Bottom_Passed) {
            //canvas.drawRect(0, Bottom_Y-Bottom_Tolerance, SCREEN_WIDTH, Bottom_Y+Bottom_Tolerance, paint);
            canvas.drawRect(0, SCREEN_HEIGHT - Bottom_Tolerance, SCREEN_WIDTH, SCREEN_HEIGHT, paint);
            currentItem = TestGruop.bottom;
        } else if (!IsX1Passed) {
            float a = (float) canvas.getHeight() / (float) canvas.getWidth();
            Path path1 = new Path();
            path1.moveTo(0, 0);
            path1.lineTo(0, x1Width * a);
            path1.lineTo(canvas.getWidth() - (x1Width), canvas.getHeight());
            path1.lineTo(canvas.getWidth(), canvas.getHeight());
            path1.lineTo(canvas.getWidth(), canvas.getHeight() - (x1Width * a));
            path1.lineTo(x1Width, 0);
            path1.close();
            canvas.drawPath(path1, paint);

            Paint paint1 = new Paint();
            paint1.setStrokeWidth(2);
            paint1.setColor(Color.RED);
            paint1.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
            canvas.drawLine(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, paint1);
            currentItem = TestGruop.x1;
        } else if (!IsX2Passed) {
            float a = (float) canvas.getHeight() / (float) canvas.getWidth();
            Path path1 = new Path();
            path1.moveTo(canvas.getWidth(), 0);
            path1.lineTo(canvas.getWidth() - x2Width, 0);
            path1.lineTo(0, canvas.getHeight() - (x2Width * a));
            path1.lineTo(0, canvas.getHeight());
            path1.lineTo(x2Width, canvas.getHeight());
            path1.lineTo(canvas.getWidth(), x2Width * a);
            path1.close();
            canvas.drawPath(path1, paint);

            Paint paint1 = new Paint();
            paint1.setStrokeWidth(2);
            paint1.setColor(Color.RED);
            canvas.drawLine(SCREEN_WIDTH, 0, 0, SCREEN_HEIGHT, paint1);
            currentItem = TestGruop.x2;
        } else {
            currentItem = TestGruop.finished;
            Log.d("MFG_TEST", "1");
            GetDefine.saveToFile(mBitmap, szSaveFile);
            Log.d("MFG_TEST", "SUCCESSFUL TEST");
            ((MainActivity) mCtx).finish();
        }
        canvas.drawPath(mPath, mPaint);
        mCanvas.drawPath(mPath, mPaint);
        Log.d("myTag", "onDraw");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointsList.add(new Float[]{event.getX(), event.getY()});

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointsList.clear();
                touch_start(x, y);
                // invalidate() �|Ĳ�o onDraw() event
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                for (int nIdx = 0; nIdx < pointsList.size(); nIdx++) {
                    Log.d("myTag", Double.toString(pointsList.get(nIdx)[0]));
                }
                checkDrawLine(pointsList);
                touch_up();
                invalidate();

                if (failCount > failCountSetting) {
                    Log.d("MFG_TEST", "2");
                    GetDefine.saveToFile(mBitmap, "/sdcard/screen.png");
                    Log.d("MFG_TEST", "Draw Fail Too Many Times!!");
                    Log.d("MFG_TEST", "UUT-FAIL\r\n");
                    ((MainActivity) mCtx).finish();
                }
                Log.d("MFG_TEST", "FailCount: " + failCount);
                break;
        }
        return true;
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

    public void checkDrawLine(List<Float[]> points) {
        switch (currentItem) {
            case top:
                if (checkTopLine(points))
                    Top_Passed = true;
                else
                    failCount++;
                break;
            case left:
                if (checkLeftLine(points))
                    Left_Passed = true;
                else
                    failCount++;
                break;
            case right:
                if (checkRightLine(points))
                    Right_Passed = true;
                else
                    failCount++;
                break;
            case bottom:
                if (checkBottomLine(points))
                    Bottom_Passed = true;
                else
                    failCount++;
                break;
            case x1:
                if (checkX1Line(points))
                    IsX1Passed = true;
                else
                    failCount++;
                break;
            case x2:
                if (checkX2Line(points))
                    IsX2Passed = true;
                else
                    failCount++;
                break;
        }
    }


    public boolean checkBottomLine(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check Bottom Line");
        //check points number
        boolean bResult = true;
        int bCount = points.size();
        Log.d("MFG_TEST", "point number " + bCount);
        if (bCount < Bottom_MinPointNumber) {
            Log.d("MFG_TEST", "Expected " + Bottom_MinPointNumber);
            Log.d("MFG_TEST", "Check bottom line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start X: " + points.get(0)[0]);
        if (points.get(0)[0] > Bottom_StartX) {
            Log.d("MFG_TEST", "Expected start X <" + Bottom_StartX);
            Log.d("MFG_TEST", "Check bottom line start point failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End X: " + points.get(bCount - 1)[0]);
        if (points.get(bCount - 1)[0] < SCREEN_WIDTH - Bottom_EndX) {
            Log.d("MFG_TEST", "Expected end X >" + (SCREEN_WIDTH - Bottom_EndX));
            Log.d("MFG_TEST", "Check bottom line end point failed!!");
            bResult = false;
        }

        //check point sequence , range
        for (int nIdx = 0; nIdx < bCount; nIdx++) {
            if (Bottom_CheckSeq && nIdx > 0) {
                if (points.get(nIdx)[0] < points.get(nIdx - 1)[0]) {
                    Log.d("MFG_TEST", "Check point sequencet failed!!");
                    bResult = false;
                }
            }
            //((TActivity)mCtx).appendLog("x/y="+points.get(i)[0]+"/"+points.get(i)[1]);
            //if(!(points.get(nIdx)[1]>Bottom_Y-Bottom_Tolerance && points.get(nIdx)[1]<Bottom_Y+Bottom_Tolerance))
            if (!(points.get(nIdx)[1] > SCREEN_HEIGHT - Bottom_Tolerance && points.get(nIdx)[1] < SCREEN_HEIGHT + Bottom_Tolerance)) {
                Log.d("MFG_TEST", points.get(nIdx)[1] + " Check Point Range Failed!!");
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean checkLeftLine(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check Left Line");
        //check points number
        boolean bResult = true;
        int nCount = points.size();
        Log.d("MFG_TEST", "point number " + nCount);
        if (nCount < Left_MinPointNumber) {
            Log.d("MFG_TEST", "Expected " + Left_MinPointNumber);
            Log.d("MFG_TEST", "Check left line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start Y: " + points.get(0)[1]);
        if (points.get(0)[1] > Left_StartY) {
            //Log.d("MFG_TEST", ("Expected start Y < " + Left_StartY );
            Log.d("MFG_TEST", "Check left line start point failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End Y: " + points.get(nCount - 1)[1]);
        if (points.get(nCount - 1)[1] < SCREEN_HEIGHT - Left_EndY) {
            Log.d("MFG_TEST", "Expected end Y >" + (SCREEN_HEIGHT - Left_EndY));
            Log.d("MFG_TEST", "Check left line end point failed!!");
            bResult = false;
        }

        //check point sequence , range
        for (int nIdx = 0; nIdx < nCount; nIdx++) {
            if (Left_CheckSeq && nIdx > 0) {
                if (points.get(nIdx)[1] < points.get(nIdx - 1)[1]) {
                    Log.d("MFG_TEST", "Check point sequence failed!!");
                    bResult = false;
                }
            }
            //if(!(points.get(nIdx)[0]>Left_X-Left_Tolerance && points.get(nIdx)[0]<Left_X+Left_Tolerance))
            if (!(points.get(nIdx)[0] > 0 && points.get(nIdx)[0] < Left_Tolerance)) {
                Log.d("MFG_TEST", "Check point range failed!!");
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean checkTopLine(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check Top Line");
        //check points number
        boolean bResult = true;
        int bCount = points.size();
        Log.d("MFG_TEST", "point number " + bCount);
        if (bCount < Top_MinPointNumber) {
            Log.d("MFG_TEST", "Expected " + Top_MinPointNumber);
            Log.d("MFG_TEST", "Check top line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start X: " + points.get(0)[0]);
        if (points.get(0)[0] > Top_StartX) {
            Log.d("MFG_TEST", "Expected start X <" + Top_StartX);
            Log.d("MFG_TEST", "Check top line start point failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End X: " + points.get(bCount - 1)[0]);
        if (points.get(bCount - 1)[0] < SCREEN_WIDTH - Top_EndX) {
            Log.d("MFG_TEST", "Expected end X >" + (SCREEN_WIDTH - Top_EndX));
            Log.d("MFG_TEST", "Check top line end point failed!!");
            bResult = false;
        }

        //check point sequence , range
        for (int nIdx = 0; nIdx < bCount; nIdx++) {
            if (Top_CheckSeq && nIdx > 0) {
                if (points.get(nIdx)[0] < points.get(nIdx - 1)[0]) {
                    Log.d("MFG_TEST", "Check point sequence failed!!");
                    bResult = false;
                }
            }
            //if(!(points.get(nIdx)[1]>Top_Y-Top_Tolerance && points.get(nIdx)[1]<Top_Y+Top_Tolerance))
            if (!(points.get(nIdx)[1] > 0 && points.get(nIdx)[1] < Top_Tolerance)) {
                Log.d("MFG_TEST", points.get(nIdx)[1] + " Check point range failed!!");
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean checkRightLine(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check Right Line");
        //check points number
        boolean bResult = true;
        int bCount = points.size();
        Log.d("MFG_TEST", "point number " + bCount);
        if (bCount < Right_MinPointNumber) {
            Log.d("MFG_TEST", "Expected " + Right_MinPointNumber);
            Log.d("MFG_TEST", "Check right line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start Y: " + points.get(0)[1]);
        if (points.get(0)[1] > Right_StartY) {
            Log.d("MFG_TEST", "Expected start Y <" + Right_StartY);
            Log.d("MFG_TEST", "Check right line start point failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End Y: " + points.get(bCount - 1)[1]);
        if (points.get(bCount - 1)[1] < mCanvas.getHeight() - Right_EndY) {
            Log.d("MFG_TEST", "Expected end Y >" + (mCanvas.getHeight() - Right_EndY));
            Log.d("MFG_TEST", "Check right line end point failed!!");
            bResult = false;
        }

        //check point sequence , range
        for (int nIdx = 0; nIdx < bCount; nIdx++) {
            if (Right_CheckSeq && nIdx > 0) {
                if (points.get(nIdx)[1] < points.get(nIdx - 1)[1]) {
                    Log.d("MFG_TEST", "Check point sequencet failed!!");
                    bResult = false;
                }
            }

            //if(!(points.get(nIdx)[0]>Right_X-Right_Tolerance && points.get(nIdx)[0] < Right_X+Right_Tolerance))
            if (!(points.get(nIdx)[0] > SCREEN_WIDTH - Right_Tolerance && points.get(nIdx)[0] < SCREEN_WIDTH + Right_Tolerance)) {
                Log.d("MFG_TEST", "Check point range failed!!");
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean checkX1Line(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check X1 Line");
        //check points number
        boolean bResult = true;
        int bCount = points.size();
        Log.d("MFG_TEST", "point number " + bCount);
        if (bCount < x1PointNumber) {
            Log.d("MFG_TEST", "Expected " + x1PointNumber);
            Log.d("MFG_TEST", "Check X1 line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start X/Y: " + points.get(0)[0] + "/" + points.get(0)[1]);
        if (points.get(0)[0] > x1StartX) {
            Log.d("MFG_TEST", "Expected start X <" + x1StartX);
            Log.d("MFG_TEST", "Check X1 line start point X failed!!");
            bResult = false;
        }
        if (points.get(0)[1] > x1StartY) {
            Log.d("MFG_TEST", "Expected start Y <" + x1StartY);
            Log.d("MFG_TEST", "Check X1 line start point Y failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End X/Y: " + points.get(bCount - 1)[0] + "/" + points.get(bCount - 1)[1]);
        if (points.get(bCount - 1)[0] < mCanvas.getWidth() - x1EndX) {
            Log.d("MFG_TEST", "Expected end X >" + (mCanvas.getWidth() - x1EndX));
            Log.d("MFG_TEST", "Check X1 line end point X failed!!");
            bResult = false;
        }
        if (points.get(bCount - 1)[1] < mCanvas.getHeight() - x1EndY) {
            Log.d("MFG_TEST", "Expected end Y >" + (mCanvas.getHeight() - x1EndY));
            Log.d("MFG_TEST", "Check X1 line end point Y failed!!");
            bResult = false;
        }

        for (int nIdx = 0; nIdx < bCount; nIdx++) {
            //check point sequence
            if (x1CheckSeq && nIdx > 0) {
                if (!(points.get(nIdx)[0] >= points.get(nIdx - 1)[0])) {
                    Log.d("MFG_TEST", "Check point X sequencet failed!!");
                    bResult = false;
                }

                if (!(points.get(nIdx)[1] >= points.get(nIdx - 1)[1])) {
                    Log.d("MFG_TEST", "Check point Y sequencet failed!!");
                    bResult = false;
                }
            }

            //check point  range
            float a = (float) mCanvas.getHeight() / (float) mCanvas.getWidth();
            float b = x1Width * a;
            float Y = a * points.get(nIdx)[0];
            float start_y = Y - b;
            float end_y = Y + b;

            if (!(points.get(nIdx)[1] < end_y && points.get(nIdx)[1] > start_y)) {
                Log.d("MFG_TEST", "Check Point FAIL");
                bResult = false;
            }
        }
        return bResult;
    }

    public boolean checkX2Line(List<Float[]> points) {
        Log.d("MFG_TEST", "---------------------------------");
        Log.d("MFG_TEST", "Check X2 Line");
        //check points number
        boolean bResult = true;
        int bCount = points.size();
        Log.d("MFG_TEST", "Point Number " + bCount);
        if (bCount < x2PointNumber) {
            Log.d("MFG_TEST", "Expected " + x2PointNumber);
            Log.d("MFG_TEST", "Check X2 line points failed!!");
            bResult = false;
        }

        //check start point
        Log.d("MFG_TEST", "Start X/Y: " + points.get(0)[0] + "/" + points.get(0)[1]);
        if (points.get(0)[0] < (mCanvas.getWidth() - x2StartX)) {
            Log.d("MFG_TEST", "Expected start X <" + x2StartX);
            Log.d("MFG_TEST", "Check X2 line start point X failed!!");
            bResult = false;
        }
        if (points.get(0)[1] > x2StartY) {
            Log.d("MFG_TEST", "Expected start Y <" + x2StartY);
            Log.d("MFG_TEST", "Check X2 line start point Y failed!!");
            bResult = false;
        }

        //check end point
        Log.d("MFG_TEST", "End X/Y: " + points.get(bCount - 1)[0] + "/" + points.get(bCount - 1)[1]);
        if (points.get(bCount - 1)[0] > x2EndX) {
            Log.d("MFG_TEST", "Expected end X >" + (mCanvas.getWidth() - x2EndX));
            Log.d("MFG_TEST", "Check X2 line end point X failed!!");
            bResult = false;
        }
        if (points.get(bCount - 1)[1] < mCanvas.getHeight() - x2EndY) {
            Log.d("MFG_TEST", "Expected end Y >" + (mCanvas.getHeight() - x2EndY));
            Log.d("MFG_TEST", "Check X2 line end point Y failed!!");
            bResult = false;
        }

        //check point sequence , range
        for (int nIdx = 0; nIdx < bCount; nIdx++) {
            //check point sequence
            if (x2CheckSeq && nIdx > 0) {
                if (!(points.get(nIdx)[0] <= points.get(nIdx - 1)[0])) {
                    Log.d("MFG_TEST", "Check point X sequencet failed!!");
                    bResult = false;
                }
                if (!(points.get(nIdx)[1] >= points.get(nIdx - 1)[1])) {
                    Log.d("MFG_TEST", "Check point Y sequencet failed!!");
                    bResult = false;
                }
            }

            //check point  range
            float a = (float) mCanvas.getHeight() / (float) mCanvas.getWidth();
            float b = x1Width * a;
            float Y = a * ((float) mCanvas.getWidth() - points.get(nIdx)[0]);
            float start_y = Y - b;
            float end_y = Y + b;
            if (!(points.get(nIdx)[1] < end_y && points.get(nIdx)[1] > start_y)) {
                Log.d("MFG_TEST", "Check Point FAIL");
                bResult = false;
            }
        }
        return bResult;
    }

    protected void init() {
        try {
            failCountSetting = GetDefine.getValueInt("touch-panel", "fail_condition", "fail_count");
            timeOutSetting = GetDefine.getValueInt("touch-panel", "fail_condition", "time_out");

            //Top_Y 					= GetDefine.getValueInt("touch-panel", "top", "y");
            Top_Tolerance = GetDefine.getValueInt("touch-panel", "top", "tolerance");
            Top_StartX = GetDefine.getValueInt("touch-panel", "top", "start_x");
            Top_EndX = GetDefine.getValueInt("touch-panel", "top", "end_x");
            Top_MinPointNumber = GetDefine.getValueInt("touch-panel", "top", "point_number");
            Top_CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "top", "check_seq"));

            //Bottom_Y 				= GetDefine.getValueInt("touch-panel", "bottom", "y");
            Bottom_Tolerance = GetDefine.getValueInt("touch-panel", "bottom", "tolerance");
            Bottom_StartX = GetDefine.getValueInt("touch-panel", "bottom", "start_x");
            Bottom_EndX = GetDefine.getValueInt("touch-panel", "bottom", "end_x");
            Bottom_MinPointNumber = GetDefine.getValueInt("touch-panel", "bottom", "point_number");
            Bottom_CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "bottom", "check_seq"));

            //Left_X 					= GetDefine.getValueInt("touch-panel", "left", "x");
            Left_Tolerance = GetDefine.getValueInt("touch-panel", "left", "tolerance");
            Left_StartY = GetDefine.getValueInt("touch-panel", "left", "start_y");
            Left_EndY = GetDefine.getValueInt("touch-panel", "left", "end_y");
            Left_MinPointNumber = GetDefine.getValueInt("touch-panel", "left", "point_number");
            Left_CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "left", "check_seq"));

            //Right_X 				= GetDefine.getValueInt("touch-panel", "right", "x");
            Right_Tolerance = GetDefine.getValueInt("touch-panel", "right", "tolerance");
            Right_StartY = GetDefine.getValueInt("touch-panel", "right", "start_y");
            Right_EndY = GetDefine.getValueInt("touch-panel", "right", "end_y");
            Right_MinPointNumber = GetDefine.getValueInt("touch-panel", "right", "point_number");
            Right_CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "right", "check_seq"));

            x1Width = GetDefine.getValueInt("touch-panel", "x1", "width");
            x1StartX = GetDefine.getValueInt("touch-panel", "x1", "start_x");
            x1StartY = GetDefine.getValueInt("touch-panel", "x1", "start_y");
            x1EndX = GetDefine.getValueInt("touch-panel", "x1", "end_x");
            x1EndY = GetDefine.getValueInt("touch-panel", "x1", "end_y");
            x1PointNumber = GetDefine.getValueInt("touch-panel", "x1", "point_number");
            x1CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "x1", "check_seq"));

            x2Width = GetDefine.getValueInt("touch-panel", "x2", "width");
            x2StartX = GetDefine.getValueInt("touch-panel", "x2", "start_x");
            x2StartY = GetDefine.getValueInt("touch-panel", "x2", "start_y");
            x2EndX = GetDefine.getValueInt("touch-panel", "x2", "end_x");
            x2EndY = GetDefine.getValueInt("touch-panel", "x2", "end_y");
            x2PointNumber = GetDefine.getValueInt("touch-panel", "x2", "point_number");
            x2CheckSeq = Boolean.parseBoolean(GetDefine.getValue("touch-panel", "x2", "check_seq"));
        } catch (Exception e) {
            Log.d("MFG_TEST", e.toString());
            Log.d("MFG_TEST", "Initial Parameter Fail!!");
            ((MainActivity) mCtx).finish();
        }
    }
}
package com.rj.handpaint.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class PaintView extends View {

    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint ballPaint;
    private Bitmap mBitmap;
    private Bitmap agoBitmap;
    private Paint mPaint;

    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    private DrawPath dp;

    private float mX, mY;
    private float ballX, ballY;
    private float radius = 20;
    private static final float STROKE_WIDTH = 3f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private static final float STROKE_WIDTH1 = 50f;
    private static final float HALF_STROKE_WIDTH1 = STROKE_WIDTH1 / 2;
    private static final float TOUCH_TOLERANCE = 4;

    private int bitmapWidth;
    private int bitmapHeight;

    private int currentStyle;

    private final RectF dirtyRect = new RectF();


    public PaintView(Context c) {
        super(c);
        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;
        currentStyle = 1;

        initCanvas();
        ballPaint = new Paint();
        ballPaint.setColor(Color.GRAY);
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();

    }

    public PaintView(Context c, AttributeSet attrs) {
        super(c, attrs);
        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;
        currentStyle = 1;

        initCanvas();
        ballPaint = new Paint();
        ballPaint.setColor(Color.GRAY);
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    //设置画笔样式
    public void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setDither(true);//抖动
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 让画的线圆滑
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        if (currentStyle == 1) {
            mPaint.setColor(0xFF00FF00);
            mPaint.setStrokeWidth(STROKE_WIDTH);
        }else{
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(STROKE_WIDTH1);
        }

    }

    //设置画笔样式
    public void selectPaintStyle() {

        //当原先是笔（1）的时候，变为橡皮（0），设置颜色为白色
        if (currentStyle == 1) {
            this.currentStyle = 0;
            mPaint.setStrokeWidth(STROKE_WIDTH1);
            mPaint.setColor(Color.WHITE);
        } else {
            this.currentStyle = 1;
            mPaint.setStrokeWidth(STROKE_WIDTH);
            mPaint.setColor(0xFF00FF00);
        }
    }

    //初始化画布
    public void initCanvas() {

        setPaintStyle();
        mBitmapPaint = new Paint();
        //画布大小
        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                Bitmap.Config.ARGB_8888);//根据参数创建新位图
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mCanvas.drawColor(Color.WHITE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);     //显示旧的画布
        if (mPath != null) {
            // 实时的显示,最新的path
            canvas.drawPath(mPath, mPaint);
        }

        if (currentStyle == 0) {
            canvas.drawCircle(ballX, ballY, radius, ballPaint);
        }

    }

    //路径对象
    class DrawPath {
        Path path;
        Paint paint;
    }

    /**
     * 上一次痕迹的核心思想就是将画布清空
     * 读取sd卡文件，重画
     */
    public Boolean ago() {
        String path = Environment.getExternalStorageDirectory() + "/handpaint/aa.png";
        Log.i("aa", path);
        File mFile = new File(path);
        if (mFile.exists()) {
            initCanvas();
            agoBitmap = BitmapFactory.decodeFile(path);
            mCanvas.drawBitmap(agoBitmap, 0, 0, mBitmapPaint);
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {
        System.out.println(savePath.size() + "--------------");
        if (savePath != null && savePath.size() > 0) {
            //调用初始化画布函数以清空画布
            initCanvas();

            //将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);

            //将路径保存列表中的路径重绘在画布上
            Iterator<DrawPath> iter = savePath.iterator();        //重复保存
            while (iter.hasNext()) {
                DrawPath dp = iter.next();
                mCanvas.drawPath(dp.path, dp.paint);
            }
            invalidate();// 刷新
        }
    }

    /**
     * 恢复的核心思想就是将撤销的路径保存到另外一个列表里面(栈)，
     * 然后从redo的列表里面取出最顶端对象，
     * 画在画布上面即可
     */
    public void redo() {
        if (deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            //将取出的路径重绘在画布上
            mCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    /*
     * 清空的主要思想就是初始化画布
     * 将保存路径的两个List清空
     * */
    public void removeAllPaint() {
        //调用初始化画布函数以清空画布
        initCanvas();
        invalidate();//刷新
        savePath.clear();
        deletePath.clear();
    }

    /*
     * 保存所绘图形
     * 返回绘图文件的存储路径
     * */
    public Boolean saveBitmap() {
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        String paintPath = "";
        str = str + "paint.png";
        File dir = new File("/sdcard/handpaint/");
//        File file = new File("/sdcard/notes/", str);
        File file = new File("/sdcard/handpaint/", "aa.png");
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            if (file.exists()) {
                file.delete();
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            //保存绘图文件路径
            paintPath = "/sdcard/handpaint/" + str;
            return true;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
//        return paintPath;
    }

    private void touch_start(float x, float y) {
        if (currentStyle == 0) {
            radius = 20;
            ballX = x;
            ballY = y;
        }
        mPath.reset();//清空path
        mPath.moveTo(x, y);
    }

    private void touch_move(float x, float y, MotionEvent e) {
        resetDirtyRect(x, y);

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
//        int historySize = e.getHistorySize();
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
       /*     for (int i = 0; i < historySize; i++) {
                float historicalX = e.getHistoricalX(i);
                float historicalY = e.getHistoricalY(i);
                expandDirtyRect(historicalX, historicalY);
                mPath.quadTo(mX, mY, historicalX, historicalY);
            }*/
            mPath.quadTo(mX, mY, x, y);
        }


/*        int historySize = e.getHistorySize();
        for (int i = 0; i < historySize; i++) {
            float historicalX = e.getHistoricalX(i);
            float historicalY = e.getHistoricalY(i);
            expandDirtyRect(historicalX, historicalY);
            mPath.quadTo(mX, mY, historicalX, historicalY);
        }*/
//        mPath.quadTo(mX, mY, x, y);

    }

    private void touch_up(MotionEvent e) {
        mCanvas.drawPath(mPath, mPaint);
        savePath.add(dp);
        mPath = null;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentStyle == 0) {
                    ballX = x;
                    ballY = y;
                }
                touch_move(x, y, event);
                break;
            case MotionEvent.ACTION_UP:
                if (currentStyle == 0) {
                    radius = 0;
                }
                touch_up(event);
                break;
        }
        mX = x;
        mY = y;
        if (currentStyle == 1) {
            invalidate(
                    (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));
        } else {
            invalidate(
                    (int) (dirtyRect.left - HALF_STROKE_WIDTH1),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH1),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH1),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH1));
        }
        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     */
    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(mX, eventX);
        dirtyRect.right = Math.max(mX, eventX);
        dirtyRect.top = Math.min(mY, eventY);
        dirtyRect.bottom = Math.max(mY, eventY);
    }

}

package com.example.liuhui.customlayoutmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by liuhui on 2016/10/28.
 */

public class HiveDrawable extends Drawable {

    // 用于记录边界信息的Rect
    private Rect mRect;
    private Paint mPaint;
    private Path mPath ;
    private Bitmap mBitmap ;

    public HiveDrawable(Bitmap bitmap) {
        mBitmap = bitmap;
        mRect = new Rect();
        mPath = new Path();
        initPaint() ;
    }

    private void initPaint() {
        mPaint = new Paint() ;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3f);
        BitmapShader shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(shader) ;
    }

    // 初始化Path
    private void createPath() {
        float hiveLength = mRect.height() / 2f;
        float offsetX = (float) (Math.sqrt(3)*hiveLength/2);
        mPath.reset();
        mPath.moveTo(mRect.width() / 2f, mRect.top);
        mPath.rLineTo(offsetX,hiveLength/2);
        mPath.rLineTo(0,hiveLength);
        mPath.rLineTo(-offsetX,hiveLength/2);
        mPath.rLineTo(-offsetX,-hiveLength/2);
        mPath.rLineTo(0,-hiveLength);
        mPath.rLineTo(offsetX,-hiveLength/2);
        mPath.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(mPath,mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        if (mPaint != null) {
            mPaint.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mPaint != null) {
            mPaint.setColorFilter(colorFilter) ;
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT ;
    }

    // 设置边界信息
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mRect.set(left, top, right, bottom);
        createPath();
    }

    @Override
    public int getIntrinsicWidth() {
        if (mBitmap != null) {
            return mBitmap.getWidth();
        } else {
            return super.getIntrinsicWidth() ;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        if (mBitmap != null) {
            return mBitmap.getHeight() ;
        }
        return super.getIntrinsicHeight();
    }
}

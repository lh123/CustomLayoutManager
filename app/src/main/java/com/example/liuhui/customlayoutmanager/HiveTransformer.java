package com.example.liuhui.customlayoutmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by liuhui on 2016/10/29.
 */

public class HiveTransformer extends BitmapTransformation {

    public HiveTransformer(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap mResult = pool.get(outWidth,outHeight, Bitmap.Config.ARGB_8888);
        if (mResult == null){
            mResult = Bitmap.createBitmap(outWidth,outHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(mResult);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        float hiveLength = outWidth / 2f;
        float offsetX = (float) (Math.sqrt(3)*hiveLength/2);
        path.reset();
        path.moveTo(outWidth / 2f, 0);
        path.rLineTo(offsetX,hiveLength/2);
        path.rLineTo(0,hiveLength);
        path.rLineTo(-offsetX,hiveLength/2);
        path.rLineTo(-offsetX,-hiveLength/2);
        path.rLineTo(0,-hiveLength);
        path.rLineTo(offsetX,-hiveLength/2);
        path.close();
        canvas.drawPath(path,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(toTransform,0,0,paint);
        return mResult;
    }

    @Override
    public String getId() {
        return "HiveTransformer";
    }
}

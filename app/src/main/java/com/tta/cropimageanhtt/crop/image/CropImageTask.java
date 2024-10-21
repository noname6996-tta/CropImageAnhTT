package com.tta.cropimageanhtt.crop.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.tta.cropimageanhtt.crop.config.CropIwaSaveConfig;
import com.tta.cropimageanhtt.crop.shape.CropIwaShapeMask;
import com.tta.cropimageanhtt.crop.util.CropIwaUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Yaroslav Polyakov on 22.03.2017.
 * https://github.com/polyak01
 */

@Deprecated
class CropImageTask extends AsyncTask<Void, Void, Throwable> {

    private WeakReference<Context> context;
    private CropArea cropArea;
    private CropIwaShapeMask mask;
    private Uri srcUri;
    private CropIwaSaveConfig saveConfig;

    public CropImageTask(
            Context context, CropArea cropArea, CropIwaShapeMask mask,
            Uri srcUri, CropIwaSaveConfig saveConfig) {
        this.context = new WeakReference<>(context);
        this.cropArea = cropArea;
        this.mask = mask;
        this.srcUri = srcUri;
        this.saveConfig = saveConfig;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            Context context = this.context.get();
            Bitmap bitmap = CropIwaBitmapManager.get().loadToMemory(
                    context, srcUri, saveConfig.getWidth(),
                    saveConfig.getHeight());

            if (bitmap == null) {
                return new NullPointerException("Failed to load bitmap");
            }

            Bitmap cropped = cropArea.applyCropTo(bitmap);

            cropped = mask.applyMaskTo(cropped);

            Uri dst = saveConfig.getDstUri();
            OutputStream os = context.getContentResolver().openOutputStream(dst);
            cropped.compress(saveConfig.getCompressFormat(), saveConfig.getQuality(), os);
            CropIwaUtils.closeSilently(os);

            bitmap.recycle();
            cropped.recycle();
        } catch (IOException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Throwable throwable) {
        if (throwable == null) {
            CropIwaResultReceiver.onCropCompleted(context.get(), saveConfig.getDstUri());
        } else {
            CropIwaResultReceiver.onCropFailed(context.get(), throwable);
        }
    }
}
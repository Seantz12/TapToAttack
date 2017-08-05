package com.example.seantz12.taptoattack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by seantz12 on 01/08/17.
 */

public class Background {

    Bitmap bgImage;
    Bitmap bgImageFlip;

    int width;
    int height;
    boolean flipped;
    float speed;

    int xClip;
    int startY;
    int endY;

    Background(Context context, int screenWidth, int screenHeight, String bitmapName, int sY, int eY, float s) {

        // Takes the name of the image being used, and assigns it to the bitmap
        // This next line "translates" it into a resource id
        int id = context.getResources().getIdentifier(bitmapName, "drawable",
                context.getPackageName());

        bgImage = BitmapFactory.decodeResource(context.getResources(), id);

        // Variable to determine if image is flipped or not, at first it isn't
        flipped = false;

        // Animation stuff, woo
        xClip = 0;
        // Positions height of bg
        startY = sY * (screenHeight / 100);
        endY = eY * (screenHeight / 100);
        speed = s;

        // Takes the image from the first lines, makes it into an actual bitmap
        bgImage = Bitmap.createScaledBitmap(bgImage, screenWidth, endY-startY, true);

        width = bgImage.getWidth();
        height = bgImage.getHeight();

        // Creates the flipped version for later
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        bgImageFlip = Bitmap.createBitmap(bgImage, 0, 0, width, height, matrix, true);
    }

    public void update(long fps) {
        // Shifts clipping accordingly
        xClip -= speed / fps;
        if (xClip >= width) {
            xClip = 0;
            flipped = !flipped;
        } else if (xClip <= 0) {
            xClip = width;
            flipped = !flipped;
        }

    }
}

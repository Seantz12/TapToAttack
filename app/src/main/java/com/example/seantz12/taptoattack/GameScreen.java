package com.example.seantz12.taptoattack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

public class GameScreen extends Activity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting screen details
        Display display = getWindowManager().getDefaultDisplay();
        Point resolution = new Point();
        display.getSize(resolution);

        Bundle bundle = getIntent().getExtras();


        gameView = new GameView(this, resolution.x, resolution.y, bundle.getString("enemy"), bundle.getInt("levelNum"));
        setContentView(gameView);
    }

    // Method runs on start of game
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    // On exit
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}

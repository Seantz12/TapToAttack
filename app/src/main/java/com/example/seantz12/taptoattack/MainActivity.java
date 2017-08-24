package com.example.seantz12.taptoattack;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity{

    // If you need me to explain this specific activity, well you should start from the basics of android
    // ^^ not meant to be insulting, a google tutorial will be way better at explaining
    // than I who sucks at coding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, LevelSelect.class);
        startActivity(intent);
    }
}

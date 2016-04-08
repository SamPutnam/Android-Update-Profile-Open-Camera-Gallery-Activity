package com.samuelputnam.one;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.app.AlertDialog;
import android.widget.Button;
import android.content.DialogInterface;
import android.app.Dialog;
import android.support.v4.app.Fragment;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button_Change);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(R.string.pick_profile_picture);

                alertDialog.setItems(R.array.change_button_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // open the camera or the gallery
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivity(intent);

                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });
    }
        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.overflow, menu);
            return true;
        }
}
package com.example.pm01e10002;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView imgFull;
    private Button btnAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imgFull = findViewById(R.id.imgFull);
        btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> finish());

        byte[] imagenBytes = getIntent().getByteArrayExtra("imagen");
        if (imagenBytes != null && imagenBytes.length > 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            imgFull.setImageBitmap(bmp);
        }
    }
}

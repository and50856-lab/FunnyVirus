package com.funnyvirus;

import android.app.Activity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.res.AssetFileDescriptor;

import java.io.InputStream;

public class MainActivity extends Activity {

    private MediaPlayer player;
    private boolean stopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        FrameLayout root = new FrameLayout(this);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_XY);

        try {
            InputStream stream = getAssets().open("prank.png");
            Drawable drawable = Drawable.createFromStream(stream, null);
            image.setImageDrawable(drawable);
            stream.close();
        } catch (Exception e) {
            Toast.makeText(this, "Image loading error", Toast.LENGTH_LONG).show();
        }

        root.addView(image, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Скрытая кнопка в левом нижнем углу.
        View secretButton = new View(this);
        secretButton.setContentDescription("Остановить розыгрыш");
        secretButton.setOnClickListener(v -> stopPrank());

        FrameLayout.LayoutParams secretParams =
            new FrameLayout.LayoutParams(dp(72), dp(72));

        secretParams.gravity = Gravity.BOTTOM | Gravity.START;
        root.addView(secretButton, secretParams);

        setContentView(root);

        startSound();
    }

    private int dp(int value) {
        return (int) (value * getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    private void startSound() {
        try {
            AssetFileDescriptor afd =
                getAssets().openFd("prank.mp3");

            player = new MediaPlayer();

            player.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            );

            player.setDataSource(
                afd.getFileDescriptor(),
                afd.getStartOffset(),
                afd.getLength()
            );

            afd.close();

            player.setLooping(true);
            player.prepare();
            player.start();

        } catch (Exception e) {
            Toast.makeText(this, "Audio loading error",
                Toast.LENGTH_LONG).show();
        }
    }

    private void stopPrank() {
        if (stopped) return;

        stopped = true;

        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }

            player.release();
            player = null;
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        stopPrank();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }

        super.onDestroy();
    }
}

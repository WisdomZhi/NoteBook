package cn.edu.gdqy.notebook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Properties;

public class FontSizeChangeActivity extends AppCompatActivity {
    private TextView tip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size_change);
        SeekBar seekBar = (SeekBar) findViewById(R.id.progress);
        tip  = findViewById(R.id.tip);
        Properties properties = FileUtils.readSizeFile(this);
        float fontsize = Float.parseFloat(properties.getProperty("fontsize"));
        tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize);
        int progress = Float.valueOf(((fontsize-18)/0.3f+1)*20).intValue();
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
    }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            refreshSeekBar(seekBar);
        }

        private void refreshSeekBar(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int old = progress;
            int remainder = progress%20;
            if (remainder != 0) {
                if (remainder > 10) {
                    progress = progress - remainder + 20;
                } else {
                    progress -= remainder;
                }
            }
            seekBar.setProgress(progress);

            float fontSize = (progress/20 - 1)*0.3f + 18;
            tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            FileUtils.updateSizeFile(fontSize, FontSizeChangeActivity.this);
        }
    }
}

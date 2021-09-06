package org.bokontep.wavesynth;

import android.content.Intent;
import android.graphics.Paint;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bokontep.midi.MidiConstants;
import org.bokontep.midi.MidiInputPortSelector;
import org.bokontep.midi.MidiOutputPortConnectionSelector;
import org.bokontep.midi.MidiPortConnector;
import org.bokontep.midi.MidiTools;

import java.io.IOException;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

import static android.view.MotionEvent.AXIS_Y;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "SoundPaint";

    private PApplet aSoundPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Paint visual UI init
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        aSoundPaint = new SoundPaint();
        PFragment fragment = new PFragment(aSoundPaint);
        fragment.setView(frame, this);

    }

    // PROCESSING PAINT OVERRIDES
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (aSoundPaint != null) {
            aSoundPaint.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (aSoundPaint != null) {
            aSoundPaint.onNewIntent(intent);
        }
    }
}
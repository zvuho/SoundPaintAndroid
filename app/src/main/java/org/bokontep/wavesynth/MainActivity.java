package org.bokontep.wavesynth;

import android.content.Intent;
import android.graphics.Paint;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.bokontep.midi.MidiInputPortSelector;
import org.bokontep.midi.MidiOutputPortConnectionSelector;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "Volna";

    private int[] scales =
            {
                    12, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, // chromatic
                    7, 0, 2, 4, 5, 7, 9, 11, 0, 0, 0, 0, 0, // major
                    7, 0, 2, 3, 5, 7, 8, 10, 0, 0, 0, 0, 0, // natural minor
                    7, 0, 2, 3, 5, 7, 8, 11, 0, 0, 0, 0, 0, // harmonic minor
                    7, 0, 2, 3, 5, 7, 9, 11, 0, 0, 0, 0, 0, // ascending melodic minor
                    8, 0, 2, 4, 6, 7, 9, 10, 11, 0, 0, 0, 0, // acoustic
                    10, 0, 1, 2, 4, 5, 7, 8, 9, 10, 11, 0, 0, // major2
                    8, 0, 1, 4, 5, 7, 8, 10, 11, 0, 0, 0, 0, // minor2
                    5, 0, 3, 5, 7, 10, 0, 0, 0, 0, 0, 0, 0, // pentatonic
                    6, 0, 3, 5, 6, 7, 10, 0, 0, 0, 0, 0, 0, // blues
                    5, 0, 2, 5, 7, 9, 0, 0, 0, 0, 0, 0, 0, // chinese pentatonic
                    6, 0, 2, 4, 6, 8, 10, 0, 0, 0, 0, 0, 0, // whole tone
                    8, 0, 2, 3, 5, 6, 8, 9, 11, 0, 0, 0, 0, // whole half
                    8, 0, 1, 3, 4, 6, 7, 9, 10, 0, 0, 0, 0  // half whole


            };
    private long lastTouchEventTime = 0;


    private String midiLog = "";
    private VolnaMidiReceiver midiReceiver;
    private Paint paint;
    private byte[] mByteBuffer = new byte[3];
    private int vel = -1;
    public long enterSettings;
    private int touchPoints = 0;
    private boolean play = false;
    private boolean record = false;
    private int maxSpread = 0;
    private int osc1Wave = 0;
    private int osc1WaveControl = 0;
    private int osc2Wave = 0;
    private int osc2WaveControl = 0;
    private int delayLevel = 0;
    private int delayTime = 0;
    private int delayFeedback = 0;
    private int tet = 12;

    private int lowOffset = 0;
    private int midOffset = 12;
    private int highOffset = 24;
    private float tune = 440.0f;
    private float octaveFactor = 2.0f;
    private long settingsPressTime = 5000;
    private String rootNoteStr = "";
    private boolean red = false;
    private MidiManager midiManager;
    public MidiOutputPortConnectionSelector midiPortSelector;
    public MidiInputPortSelector midiInputPortSelector;
    private int updateInterval = 20;
    private Handler mHandler;
    private Runnable screenUpdater;
    private Scope scope;
    private EditText lowOffsetEditText;
    private EditText midOffsetEditText;
    private EditText highOffsetEditText;
    private boolean legato = true;
    private int rootNote = 36;
    private int xNoteScale = 160;
    private int currentScale = 0;
    private volatile int[] notemap = new int[10];
    //private HashMap<Integer, Integer> notemap = new HashMap<>();
    private AppPreferences prefs;
    private SynthEngine engine;
    private String[] rootNotes =
            {
                    "C0(0)", "C#0(1)", "D0(2)", "D#0(3)", "E0(4)", "F0(5)", "F#0(6)", "G0(7)", "G#0(8)", "A0(9)", "A#0(10)", "B0(11)",
                    "C1(12)", "C#1(13)", "D1(14)", "D#1(15)", "E1(16)", "F1(17)", "F#1(18)", "G1(19)", "G#1(20)", "A1(21)", "A#1(22)", "B1(23)",
                    "C2(24)", "C#2(25)", "D2(26)", "D#2(27)", "E2(28)", "F2(29)", "F#2(30)", "G2(31)", "G#2(32)", "A2(33)", "A#2(34)", "B2(35)",
                    "C3(36)", "C#3(37)", "D3(38)", "D#3(39)", "E3(40)", "F3(41)", "F#3(42)", "G3(43)", "G#3(44)", "A3(45)", "A#3(46)", "B3(47)",
                    "C4(48)", "C#4(49)", "D4(50)", "D#4(51)", "E4(52)", "F4(53)", "F#4(54)", "G4(55)", "G#4(56)", "A4(57)", "A#4(58)", "B4(59)",
                    "C5(60)", "C#5(61)", "D5(62)", "D#5(63)", "E5(64)", "F5(65)", "F#5(66)", "G5(67)", "G#5(68)", "A5(69)", "A#5(70)", "B5(71)",
                    "C6(72)", "C#6(73)", "D6(74)", "D#6(75)", "E6(76)", "F6(77)", "F#6(78)", "G6(79)", "G#6(80)", "A6(81)", "A#6(82)", "B6(83)"

            };
    private String[] scaleNames =
            {
                    "chromatic",
                    "major",
                    "natural minor",
                    "harmonic minor",
                    "ascending melodic minor",
                    "acoustic",
                    "major2",
                    "minor2",
                    "pentatonic",
                    "blues",
                    "chinese pentatonic",
                    "whole tone",
                    "whole half",
                    "half whole"
            };



    private PApplet sketch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Paint visual UI init
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        setContentView(frame, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        sketch = new SoundPaint();
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);

        //Audio engine init

        prefs = new AppPreferences(this);
        engine = new SynthEngine(this, 44100);
        lowOffset = prefs.readInt("lowOffset",0);
        midOffset = prefs.readInt("midOffset",12);
        highOffset = prefs.readInt("highOffset",24);
        delayLevel = prefs.readInt("delayLevel",0);
        delayTime = prefs.readInt("delayTime",0);
        delayFeedback = prefs.readInt("delayFeedback",0);
        engine.initAudio();

        tune = (prefs.readInt("tune", 4400) / 10.0f);
        tet = prefs.readInt("tet", 12);
        octaveFactor = (prefs.readInt("octaveFactor",2000)/1000.0f);
        red = prefs.readInt("red", 0) == 0 ? false : true;
        legato = prefs.readInt("legato", 0)==0?false:true;

        rootNote = prefs.readInt("rootNote", 35);
        xNoteScale = prefs.readInt("xNoteScale", 160);
        currentScale = prefs.readInt("currentScale", 0);
        maxSpread = prefs.readInt("maxSpread", 0);
        osc1Wave = prefs.readInt("osc1Wave", 0);
        osc2Wave = prefs.readInt("osc2Wave", 0);
        osc1WaveControl = prefs.readInt("osc1WaveControl", 255);
        osc2WaveControl = prefs.readInt("osc2WaveControl", 255);
        engine.initSynthParameters();
        engine.setOsc1Volume(prefs.readInt("osc1Volume", 127));
        engine.setOsc2Volume(prefs.readInt("osc2Volume", 127));
        engine.setOsc1Attack(prefs.readInt("osc1Attack", 10));
        engine.setOsc1Decay(prefs.readInt("osc1Decay", 0));
        engine.setOsc1Sustain(prefs.readInt("osc1Sustain", 127));
        engine.setOsc1Release(prefs.readInt("osc1Release", 0));
        engine.setOsc2Attack(prefs.readInt("osc2Attack", 10));
        engine.setOsc2Decay(prefs.readInt("osc2Decay", 0));
        engine.setOsc2Sustain(prefs.readInt("osc2Sustain", 127));
        engine.setOsc2Release(prefs.readInt("osc2Release", 0));
        engine.setDelayLevel(delayLevel);
        engine.setDelayTime(delayTime);
        engine.setDelayFeedback(delayFeedback);
        engine.setTune(tune);
        engine.setTet(tet);
        engine.setOctaveFactor(octaveFactor);
        rootNoteStr = this.midiNoteToString(rootNote);
    }

    //AUDIO AUX FUNCTIONS

    public String midiNoteToString(int note) {
        note = note % tet;
        if (tet == 12) {
            switch (note) {
                case 0:
                    return "C";

                case 1:
                    return "C#";
                case 2:
                    return "D";
                case 3:
                    return "D#";
                case 4:
                    return "E";
                case 5:
                    return "F";
                case 6:
                    return "F#";
                case 7:
                    return "G";
                case 8:
                    return "G#";
                case 9:
                    return "A";
                case 10:
                    return "A#";
                case 11:
                    return "B";
            }
        } else {
            return "T" + (note % tet) + "_" + (note / tet);
        }
        return "";
    }

    public int transformNote(int noteIn) {
        if (tet != 12) {
            return noteIn % (11*tet);
        }
        int notesInScale = scales[currentScale * 13];
        int relnote = noteIn - rootNote;
        int octave = relnote / notesInScale;
        int noteInScale = relnote % notesInScale;
        int index = currentScale * 13 + 1 + noteInScale;
        int noteOut = -1;
        if (scales == null) {
            return 0;
        }
        if (index < scales.length && index >= 0) {
            noteOut = this.rootNote + scales[index] + octave * tet;
        } else {
            while (index >= scales.length) {
                index = index - tet;
            }
            if (index < 0) {
                index = 0;
            }
            noteOut = this.rootNote + scales[index] + octave * tet;
        }

        return noteOut % 128;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }
}
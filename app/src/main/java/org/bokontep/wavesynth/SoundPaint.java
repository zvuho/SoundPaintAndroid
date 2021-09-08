package org.bokontep.wavesynth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import org.bokontep.midi.MidiConstants;
import org.bokontep.midi.MidiInputPortSelector;
import org.bokontep.midi.MidiOutputPortConnectionSelector;
import org.bokontep.midi.MidiPortConnector;
import org.bokontep.midi.MidiTools;

import java.io.IOException;

import processing.core.PApplet;
import processing.core.PImage;

import static android.content.Context.MIDI_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;


public class SoundPaint extends PApplet {
    Activity activity = getActivity();

    Context context = getContext();

    // wavesynth variables
    private final int[] scales =
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

    private String midiLog = "";
    private VolnaMidiReceiver midiReceiver;
    private Paint paint;
    private final byte[] mByteBuffer = new byte[3];
    private int vel = -1;
    public long enterSettings;
    private int touchPoints = 0;
    private final boolean play = false;
    private final boolean record = false;
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
    private final long settingsPressTime = 5000;
    private String rootNoteStr = "";
    private boolean red = false;
    private MidiManager midiManager;
    public MidiOutputPortConnectionSelector midiPortSelector;
    public MidiInputPortSelector midiInputPortSelector;
    private final int updateInterval = 20;
    private Handler mHandler;
    private Runnable screenUpdater;
    private EditText lowOffsetEditText;
    private EditText midOffsetEditText;
    private EditText highOffsetEditText;
    private boolean legato = true;
        //MODIFIED VARS
    int waveform1 = 0;
    int waveform2 = 0;

    int oscdist = 0;
    private boolean actionMove = false;
    int midinote = -1;
    private int rootNote = 36;
    private int xNoteScale = 160;
    private int currentScale = 0;
    //private HashMap<Integer, Integer> notemap = new HashMap<>();
    private AppPreferences prefs;
    private SynthEngine engine;
    private final String[] rootNotes =
            {
                    "C0(0)", "C#0(1)", "D0(2)", "D#0(3)", "E0(4)", "F0(5)", "F#0(6)", "G0(7)", "G#0(8)", "A0(9)", "A#0(10)", "B0(11)",
                    "C1(12)", "C#1(13)", "D1(14)", "D#1(15)", "E1(16)", "F1(17)", "F#1(18)", "G1(19)", "G#1(20)", "A1(21)", "A#1(22)", "B1(23)",
                    "C2(24)", "C#2(25)", "D2(26)", "D#2(27)", "E2(28)", "F2(29)", "F#2(30)", "G2(31)", "G#2(32)", "A2(33)", "A#2(34)", "B2(35)",
                    "C3(36)", "C#3(37)", "D3(38)", "D#3(39)", "E3(40)", "F3(41)", "F#3(42)", "G3(43)", "G#3(44)", "A3(45)", "A#3(46)", "B3(47)",
                    "C4(48)", "C#4(49)", "D4(50)", "D#4(51)", "E4(52)", "F4(53)", "F#4(54)", "G4(55)", "G#4(56)", "A4(57)", "A#4(58)", "B4(59)",
                    "C5(60)", "C#5(61)", "D5(62)", "D#5(63)", "E5(64)", "F5(65)", "F#5(66)", "G5(67)", "G#5(68)", "A5(69)", "A#5(70)", "B5(71)",
                    "C6(72)", "C#6(73)", "D6(74)", "D#6(75)", "E6(76)", "F6(77)", "F#6(78)", "G6(79)", "G#6(80)", "A6(81)", "A#6(82)", "B6(83)"

            };
    private final String[] scaleNames =
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

    int Modo = 0;
    //Sinth parameters variables linked to colors
    public float Low = 261;
    public float High = 523;
    public int Octave = 3;
    boolean Inv = false;
    boolean Auto = false;

    //array of sinth notes
    int n=0;
    int colors[]= {
            254, 226, 199, 174, 150, 127, 106, 85, 66, 48, 31, 15, 0
    };
    //array of inverted sinth notes
    int v=0;
    int colorsV[]= {
            239, 223, 206, 188, 169, 148, 127, 104, 80, 55, 28, 0
    };

    //Modified version of PixelArray 1.2.1
    PImage img;
    int direction = 1;
    float signal;
    float freq=255; //Link Variable between PixelArray and Minim
    float amp=0; //Link Variable between PixelArray and Minim

    //Paint Variables
    int currX = 0, currY = 0, oldX=0, oldY=0, drawSat=255, drawBri=255;
    int ColSpacing=0;
    int ColOff = 0;


    int PenSize= 0;
    int huePik;
    boolean BW=false;
    boolean Drpr=false;


    public int getEventX() {
        return currX;
    }

    public int getEventY() {
        return currY;
    }


    public void settings() {
        fullScreen();
        smooth();
    }

    public void setup() {
        orientation(LANDSCAPE);
        colorMode(HSB, 255);
        background(255);

        //spacing between the colors in the pallete
        ColSpacing = (int) (map(63, 0,800, 0 , width));
        ColOff = (int) (map(7, 0, 800, 0 , width));
        PenSize= (int) (map(30, 0, 800, 0 , width));

        //Auto
        //Agente juan = new Agente();

        //Audio engine init
        prefs = new AppPreferences(context);
        engine = new SynthEngine(context, 44100);
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
        red = prefs.readInt("red", 0) != 0;
        legato = prefs.readInt("legato", 0) != 0;

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


    //WAVESYNTH AUX FUNCTIONS

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


    // PenSize slider
    public void Pensize() {
        PenSize = (mouseX - 335);
    }

    // returns true if mouse is inside this rectangle
    boolean inside(float left, float top, float right, float bottom ) {
        return (mouseX>left && mouseX<right && mouseY>top && mouseY<bottom );
    }

    public void Paint() {
        //if outside buttons, draw line.
        if (inside(0, 66, width - 50, height - 50)) {

            if (!BW) {

                //Draw line of set colour
                strokeWeight(PenSize);

                stroke(huePik, drawSat, drawBri);
                line(mouseX, mouseY, oldX, oldY);

            }

            if (BW) {
                //Draw line of set gray
                strokeWeight(PenSize);
                stroke(0, 0, huePik);
                line(mouseX, mouseY, oldX, oldY);

            }
        }
    }

//DIBUJA LOS BOTONES
void drawButtons() {




    //map(Val, 0, 800, 0 , width)
    //map(Val, 0, 600, 0 , height)


    //back for colors Mapped


    strokeWeight(1);
    stroke(0);
    noFill();
    line(map(5, 0, 800, 0, width), map(555, 0, 600, 0, height), map(745, 0, 800, 0, width), map(555, 0, 600, 0, height));
    fill(0, 0, 220);
    rect(0, 0, width-1, map(65, 0, 600, 0, height));


    //draw semitones Mapped
    fill(0, 0, 50);
    strokeWeight(2);
    line(map(89, 0, 800, 0, width), map(21, 0, 600, 0, height), map(89, 0, 800, 0, width), map(64, 0, 600, 0, height));
    line(map(215, 0, 800, 0, width), map(21, 0, 600, 0, height), map(215, 0, 800, 0, width), map(64, 0, 600, 0, height));
    line(map(311, 0, 800, 0, width), map(21, 0, 600, 0, height), map(311, 0, 800, 0, width), map(64, 0, 600, 0, height));
    line(map(404, 0, 800, 0, width), map(21, 0, 600, 0, height), map(404, 0, 800, 0, width), map(64, 0, 600, 0, height));
    line(map(532, 0, 800, 0, width), map(21, 0, 600, 0, height), map(532, 0, 800, 0, width), map(64, 0, 600, 0, height));
    line(map(658, 0, 800, 0, width), map(21, 0, 600, 0, height), map(658, 0, 800, 0, width), map(64, 0, 600, 0, height));
    strokeWeight(1);
    rect(map(65, 0, 800, 0, width), map(15, 0, 600, 0, height), map(48, 0, 800, 0, width), map(38, 0, 600, 0, height));
    rect(map(191, 0, 800, 0, width), map(15, 0, 600, 0, height), map(48, 0, 800, 0, width), map(38, 0, 600, 0, height));
    rect(map(381, 0, 800, 0, width), map(15, 0, 600, 0, height), map(48, 0, 800, 0, width), map(38, 0, 600, 0, height));
    rect(map(508, 0, 800, 0, width), map(15, 0, 600, 0, height), map(48, 0, 800, 0, width), map(38, 0, 600, 0, height));
    rect(map(635, 0, 800, 0, width), map(15, 0, 600, 0, height), map(48, 0, 800, 0, width), map(38, 0, 600, 0, height));


    //draw BLANK button mapped
    stroke(0);
    strokeWeight(1);
    noFill();
    rect(map(5, 0, 800, 0, width), map(70, 0, 600, 0, height), map(30, 0, 800, 0, width), map(30, 0, 600, 0, height));
    line(map(5, 0, 800, 0, width), map(70, 0, 600, 0, height), map(35, 0, 800, 0, width), map(100, 0, 600, 0, height));
    line(map(35, 0, 800, 0, width), map(70, 0, 600, 0, height), map(5, 0, 800, 0, width), map(100, 0, 600, 0, height));
    fill(0, 0, 0);
    textSize(map(10, 0, 600, 0, height));
    text("WHITE OUT", map(2, 0, 800, 0, width), map(112, 0, 600, 0, height));

    //Draw the MOUSE/AUTO Listen button mapped
    stroke(0);
    strokeWeight(1);
    fill(255);
    rect(map(650, 0, 800, 0, width), map(560, 0, 600, 0, height), map(20, 0, 800, 0, width), map(30, 0, 600, 0, height));
    fill(0, 150, 255);
    triangle(map(666, 0, 800, 0, width), map(580, 0, 600, 0, height), map(655, 0, 800, 0, width), map(563, 0, 600, 0, height), map(656, 0, 800, 0, width), map(583, 0, 600, 0, height));
    fill(255);
    rect(map(630, 0, 800, 0, width), map(560, 0, 600, 0, height), map(20, 0, 800, 0, width), map(30, 0, 600, 0, height));
    ellipse(map(640, 0, 800, 0, width), map(576, 0, 600, 0, height), map(14, 0, 800, 0, width), map(20, 0, 600, 0, height));
    line(map(634, 0, 800, 0, width), map(574, 0, 600, 0, height), map(646, 0, 800, 0, width), map(574, 0, 600, 0, height));
    line(map(640, 0, 800, 0, width), map(574, 0, 600, 0, height), map(640, 0, 800, 0, width), map(561, 0, 600, 0, height));
    if (!Auto) {
        noFill();
        stroke(255);
        rect(map(648, 0, 800, 0, width), map(558, 0, 600, 0, height), map(24, 0, 800, 0, width), map(34, 0, 600, 0, height));
        stroke(0);
        rect(map(628, 0, 800, 0, width), map(558, 0, 600, 0, height), map(24, 0, 800, 0, width), map(34, 0, 600, 0, height));
    }

    if (Auto==true) {
        noFill();
        stroke(255);
        rect(map(628, 0, 800, 0, width), map(558, 0, 600, 0, height), map(24, 0, 800, 0, width), map(34, 0, 600, 0, height));
        stroke(0);
        rect(map(648, 0, 800, 0, width), map(558, 0, 600, 0, height), map(24, 0, 800, 0, width), map(34, 0, 600, 0, height));
    }


    //draw BW & C button mapped
    stroke(0);
    fill(0, 0, 0);
    rect(map(240, 0, 800, 0, width), map(560, 0, 600, 0, height), map(15, 0, 800, 0, width), map(15, 0, 600, 0, height));
    fill(0, 0, 255);
    rect(map(240, 0, 800, 0, width), map(575, 0, 600, 0, height), map(15, 0, 800, 0, width), map(15, 0, 600, 0, height));
    fill(0, 255, 255);
    rect(map(265, 0, 800, 0, width), map(560, 0, 600, 0, height), map(15, 0, 800, 0, width), map(10, 0, 600, 0, height));
    fill(80, 255, 255);
    rect(map(265, 0, 800, 0, width), map(570, 0, 600, 0, height), map(15, 0, 800, 0, width), map(10, 0, 600, 0, height));
    fill(160, 255, 255);
    rect(map(265, 0, 800, 0, width), map(580, 0, 600, 0, height), map(15, 0, 800, 0, width), map(10, 0, 600, 0, height));
    if (!BW) {
        noFill();
        rect(map(262, 0, 800, 0, width), map(558, 0, 600, 0, height), map(21, 0, 800, 0, width), map(34, 0, 600, 0, height));
        stroke(255);
        rect(map(237, 0, 800, 0, width), map(558, 0, 600, 0, height), map(21, 0, 800, 0, width), map(34, 0, 600, 0, height));
    }
    if (BW) {
        noFill();
        rect(map(237, 0, 800, 0, width), map(558, 0, 600, 0, height), map(21, 0, 800, 0, width), map(34, 0, 600, 0, height));
        stroke(255);
        rect(map(262, 0, 800, 0, width), map(558, 0, 600, 0, height), map(21, 0, 800, 0, width), map(34, 0, 600, 0, height));
    }


    //draw LISTEN button mapped
    stroke(0);
    fill(0, 0, 200);
    rect(map(680, 0, 800, 0, width), map(560, 0, 600, 0, height), map(60, 0, 800, 0, width), map(30, 0, 600, 0, height));
    noFill();
    rect(map(685, 0, 800, 0, width), map(565, 0, 600, 0, height), map(50, 0, 800, 0, width), map(20, 0, 600, 0, height));
    fill(0, 0, 0);
    textSize(map(16, 0, 600, 0, height));
    text("LISTEN", map(690, 0, 800, 0, width), map(581, 0, 600, 0, height));

    //Draw DRPR button mapped
    fill(0, 0, 255);
    rect(map(710, 0, 800, 0, width), map(70, 0, 600, 0 , height), map(40, 0, 800, 0, width), map(30, 0, 600, 0 , height));
    fill(0);
    text("Drop", map(715, 0, 800, 0 , width), map(90, 0, 600, 0 , height));

    //Back for Octave slider mapped
    stroke(0);
    fill(0, 0, 70);
    rect(map(752, 0, 800, 0, width), 0, map(47, 0, 800, 0 , width), height-1);

    //Draw Octave slider mapped
    stroke(0);
    int o;
    for (o=0; o<7; o++)
    {
        fill(0, 0, 100);
        rect(map(770, 0, 800, 0 , width), map(20, 0, 600, 0 , height)+o*(map(560, 0, 600, 0 , height)/7), map(25, 0, 800, 0 , width), (map(540, 0, 600, 0 , height))/7);
    }
    fill(255);
    rect(map(770, 0, 800, 0 , width), map(20, 0, 600, 0 , height)+(6-Octave)*(map(560, 0, 600, 0 , height)/7), map(25, 0, 800, 0 , width), (map(540, 0, 600, 0 , height))/7);
    fill(0);
    text((Octave), map(780, 0, 800, 0 , width), map(65, 0, 600, 0 , height)+(6-Octave)*((map(560, 0, 600, 0 , height))/7));
    fill(0, 0, 255);
    text("octave", map(760, 0, 800, 0 , width), map(595, 0, 600, 0 , height));


    //Back for PenSize mapped
    stroke(0);
    fill(0, 0, 100);
    rect(map(335, 0, 800, 0 , width), map(560, 0, 600, 0 , height), map(165, 0, 800, 0 , width), map(30, 0, 600, 0 , height));
    //Draw PenSize selector
    fill(0);
    triangle(map(350, 0, 800, 0 , width), map(575, 0, 600, 0 , height), map(479, 0, 800, 0 , width), map(580, 0, 600, 0 , height), map(479, 0, 800, 0 , width),map(570, 0, 600, 0 , height));
    ellipseMode(CENTER);
    ellipse(map(345, 0, 800, 0 , width), map(575, 0, 600, 0 , height), 5, 5);
    ellipse(map(490, 0, 800, 0 , width), map(575, 0, 600, 0 , height), map(15, 0, 800, 0 , width), map(15, 0, 800, 0 , width));
    fill(255);
    text(PenSize, map(410, 0, 800, 0 , width), map(580, 0, 600, 0 , height));

    //Draw Inv button mapped

    fill(255);
    rect(map(630, 0, 800, 0, width), map(70, 0, 600, 0 , height), map(67, 0, 800, 0, width), map(30, 0, 600, 0 , height));
    fill(0);
    if (Inv==false) {
        text("RED>BLUE", map(633, 0, 800, 0 , width), map(90, 0, 600, 0 , height));
    }
    if (Inv==true) {
        text("BLUE>RED", map(633, 0, 800, 0 , width), map(90, 0, 600, 0 , height));
    }


    // draw color Piker mapped
    if (Inv==false) {
        if (BW==false) {
            int p;
            for (p=0; p<map(735, 0, 800, 0 , width); p++) {
                stroke (map(p, 0, map(734, 0, 800, 0 , width), 226, 0), 255, 255);
                line (map(741, 0, 800, 0 , width)-p, 1, map(741, 0, 800, 0 , width)-p, 19);
            }
            stroke(0);
            noFill();
            rect(ColOff, 0, map(736, 0, 800, 0 , width), 20);
        }

        if (BW==true) {
            int p;
            for (p=0; p<map(735, 0, 800, 0 , width); p++) {
                stroke (0, 0, map(p, 0, map(734, 0, 800, 0 , width), 255, 0));
                line (map(741, 0, 800, 0 , width)-p, 1, map(741, 0, 800, 0 , width)-p, 19);
            }
            stroke(0);
            noFill();
            rect(ColOff, 0, map(736, 0, 800, 0 , width), 20);
        }
    }

    if (Inv==true) {
        if (BW==false) {
            int p;
            for (p=0; p<map(735, 0, 800, 0 , width); p++) {
                stroke (map(p, 0, map(734, 0, 800, 0 , width), 226, 0), map(255, 0, 800, 0 , width), 255);
                line (ColOff+1+p, 1, ColOff+1+p, 19);
            }
            stroke(0);
            noFill();
            rect(ColOff, 0, map(736, 0, 800, 0 , width), 20);
        }

        if (BW==true) {
            int p;
            for (p=0; p<map(735, 0, 800, 0 , width); p++) {
                stroke (0, 0, map(p, 0, map(734, 0, 800, 0 , width), 255, 0));
                line (ColOff+1+p, 1, ColOff+1+p, 19);
            }
            stroke(0);
            noFill();
            rect(ColOff, 0, map(736, 0, 800, 0 , width), 20);
        }
    }


    //draw color selector buttons mapped
    if (BW==false) {

        if (Inv==false) {
            stroke(0);
            for (n=0; n<12; n++)
            {
                fill(colors[12-n], 255, 255);
                rect(ColOff+n*ColSpacing, map(20, 0, 600, 0 , height), map(40, 0, 800, 0 , width), map(30, 0, 600, 0 , height));
            }
        }

        if (Inv==true) {
            stroke(0);
            for (v=0; v<12; v++)
            {
                fill(colorsV[v], 255, 255);
                rect(ColOff+v*ColSpacing,  map(20, 0, 600, 0 , height), map(40, 0, 800, 0 , width), map(30, 0, 600, 0 , height));
            }
        }
    }

    if (BW==true) {

        if (Inv==false) {
            stroke(0);
            for (n=0; n<12; n++)
            {
                fill(0, 0, colors[12-n]);
                rect(ColOff+n*ColSpacing,  map(20, 0, 600, 0 , height), map(40, 0, 800, 0 , width), map(30, 0, 600, 0 , height));
            }
        }

        if (Inv==true) {
            stroke(0);
            for (v=0; v<12; v++)
            {
                fill(0, 0, colorsV[v]);
                rect(ColOff+v*ColSpacing,  map(20, 0, 600, 0 , height), map(40, 0, 800, 0 , width), map(30, 0, 600, 0 , height));
            }
        }
    }

    //Legends mapped
    int off = (int) (map(6, 0, 800, 0 , width));
    fill(0, 0, 0);
    text("C", map(23, 0, 800, 0 , width), map(39, 0, 600, 0 , height));
    text("C^", off + ColOff+16+1*ColSpacing, map(39, 0, 600, 0 , height));
    text("D", off +ColOff+16+2*ColSpacing, map(39, 0, 600, 0 , height));
    text("D^", off +ColOff+16+3*ColSpacing, map(39, 0, 600, 0 , height));
    text("E", off +ColOff+16+4*ColSpacing, map(39, 0, 600, 0 , height));
    text("F", off +ColOff+16+5*ColSpacing, map(39, 0, 600, 0 , height));
    text("F^", off +ColOff+16+6*ColSpacing, map(39, 0, 600, 0 , height));
    text("G", off +ColOff+16+7*ColSpacing, map(39, 0, 600, 0 , height));
    text("G^", off +ColOff+16+8*ColSpacing, map(39, 0, 600, 0 , height));
    text("A", off +ColOff+16+9*ColSpacing, map(39, 0, 600, 0 , height));
    text("A^", off +ColOff+16+10*ColSpacing, map(39, 0, 600, 0 , height));
    text("B", off +ColOff+16+11*ColSpacing, map(39, 0, 600, 0 , height));
}

    public void LoadPx() {
        //Prerequisite to listen or pick colors from array
        //Load display pixels to array "img" to read colours
        img = get();
        //Mode change to 2 makes app to start sounding now that image is loaded
        if (Drpr) {
            Modo = 3;
        } else {
            if (!Auto) {
                if (!BW) {
                    Modo = 2;
                }
                if (BW) {
                    Modo = 5;
                }
            } else {
                Modo = 4;
            }

        }
    }

    void Huepik() {

        if(Inv==false){
            huePik=(int) (map((mouseX-ColOff),0,map(735, 0, 800, 0 , width),0,226));
        } else {
            huePik=(int) (map((mouseX-ColOff),0,map(735, 0, 800, 0 , width),255,0));
        }

    }

    //SELECTION UN COLOR PARA PINT

    void Dropper() {
        //Make signal point at mouse position inside the pixels array
        int mx = constrain(mouseX, 0, img.width - 1);
        int my = constrain(mouseY, 0, img.height - 1);
        signal = my * img.width + mx;
        int sx = (int)signal % img.width;
        int sy = (int)signal / img.width;
        set(0, 0, img);  // fast way to draw an image

        //Draw Pick Now Message
        fill(0, 20, 200);
        rect(10, height - 40, 640, 30);
        fill(0, 0, 0);
        text("P    I    C    K        C    O    L    O    R        N    O    W", 23, height - 20);

        //Draw Pointer on the image for current pixel
        stroke(0, 0, 0);
        point(sx, sy);
        noFill();
        rect(sx - 5, sy - 5, 10, 10);

        //Make C the color under mouse
        int c = img.get(sx, sy);

        if (mousePressed) {
            if (mouseButton == LEFT) {
                if (inside(717, 70, 747, 100)) {
                } else {
                    //Set the dropped color to paint
                    drawSat = (int)saturation(c);
                    drawBri = (int)brightness(c);
                    huePik = (int)hue(c);
                    oldX = mouseX;
                    oldY = mouseY;
                    set(0, 0, img);
                    Modo = 0;
                    Drpr = false;
                }
            } else {
                background(c);
                Drpr = false;
                Modo = 0;
            }
        }
    }


    void buttonsChk(){
        //map(Val, 0, 800, 0 , width)
        //map(Val, 0, 600, 0 , height)


        //check if the BLANK button is pressed mapped
        if (inside (map(5, 0, 800, 0 , width), map(70, 0, 600, 0 , height), map(35, 0, 800, 0 , width), map(100, 0, 600, 0 , height))) {
            if (BW==false) {

                background(0, 0, 255);
            }
            if (BW==true) {
                background(150, 50, 100);
                ;
            }
        }

        //Drpr button mapped
        if (inside(map(710, 0, 800, 0 , width), map(70, 0, 600, 0 , height), map(750, 0, 800, 0 , width), map(100, 0, 600, 0 , height))) {
            Drpr=true;
            Modo=1;
        }


        //check Listen boton mapped
        if (inside(map(680, 0, 800, 0, width), map(560, 0, 600, 0, height), map(740, 0, 800, 0, width),map(590, 0, 600, 0, height))) {
            Modo = 1;
        }

        //check if AUTO button mapped
        if (inside(map(650, 0, 800, 0, width), map(560, 0, 600, 0, height), map(670, 0, 800, 0, width), map(590, 0, 600, 0, height))) {
            Auto = true;
        }

        //check if Mouse button mapped
        if (inside(map(630, 0, 800, 0, width), map(560, 0, 600, 0, height), map(650, 0, 800, 0, width), map(590, 0, 600, 0, height))) {
            Auto = false;
        }

        //Check if Inv button mapped
        if (inside(map(630, 0, 800, 0, width), map(70, 0, 600, 0 , height), map(697, 0, 800, 0, width), map(100, 0, 600, 0 , height))) {
            Inv=!Inv;
        }

        //check if BW button is pressed mapped
        if (inside(map(240, 0, 800, 0, width), map(560, 0, 600, 0 , height), map(255, 0, 800, 0, width), map(590, 0, 600, 0 , height))) {
            background(150, 50, 100);
            drawSat = 0;
            BW=true;
        }

        //check if C button is pressed mapped
        if (inside(map(265, 0, 800, 0, width), map(560, 0, 600, 0 , height), map(280, 0, 800, 0, width), map(590, 0, 600, 0 , height))) {
            background(0, 0, 255);
            BW=false;
        }

        //check if HueSlider is clicked mapped
        if (inside(ColOff, 0, map(741, 0, 800, 0 , width), map(19, 0, 600, 0 , height))) {
            drawSat = 255;
            drawBri = 255;
            Huepik();
        }
        //check if OCTAVE is clicked mapped
        if (inside(map(770, 0, 800, 0 , width), map(20, 0, 600, 0 , height), map(795, 0, 800, 0 , width), map(580, 0, 600, 0 , height))) {
            int o;
            for (o=0;o<7;o++) {
                if (mouseY>map(20, 0, 600, 0 , height)+o*((map(560, 0, 600, 0 , height))/7) && mouseY< (map(20, 0, 600, 0 , height)+(1+o)*(map(560, 0, 600, 0 , height))/7)) Octave=6-o;
            }
        }

        //check if PenSize is clicked mapped
        if (inside(map(335, 0, 800, 0 , width), map(560, 0, 600, 0 , height), map(500, 0, 800, 0 , width), map(590, 0, 600, 0 , height))) {
            PenSize=(mouseX - (int)(map(335, 0, 800, 0 , width)));
        }

        //color selector mapped A LOT OF REDUNDANT CODE!!
        if (BW==false) {

            if (Inv==false) {
                for (n=0;n<13;n++) {

                    if (inside(ColOff+n*ColSpacing, map(20, 0, 800, 0 , width), 40+ColOff+n*ColSpacing, map(65, 0, 600, 0 , height))) {


                        huePik = colors[12-n];
                        drawSat=255;
                        drawBri=255;


                        // background(colors[12-n], 255, 255);
                    }
                }
            }
            if (Inv==true) {
                for (v=0;v<13;v++) {

                    if (inside(ColOff+v*ColSpacing,  map(20, 0, 800, 0 , width), 40+ColOff+n*ColSpacing, map(65, 0, 600, 0 , height))) {
                        huePik = colorsV[v];
                        drawSat=255;
                        drawBri=255;

                        // background(colorsV[v], 255, 255);

                    }
                }
            }
        }

        if (BW==true) {
            if (Inv==false) {
                for (n=0;n<13;n++) {

                    if (inside(ColOff+n*ColSpacing, map(20, 0, 800, 0 , width), 40+ColOff+n*ColSpacing, map(65, 0, 600, 0 , height))) {

                        huePik = colors[12-n];
                        drawSat=0;
                        drawBri=huePik;

                    }
                }
            }

            if (Inv==true) {
                for (v=0;v<13;v++) {

                    if (inside(ColOff+v*ColSpacing, map(20, 0, 800, 0 , width), 40+ColOff+n*ColSpacing, map(65, 0, 600, 0 , height))) {


                        huePik = colorsV[v];

                        drawSat=0;

                    }
                }
            }
        }
    }

    void ListenCR(){

        //Range of Y is defined
        float height = 255;
        int offset = 0;


            int xPos = mouseX;
            int yPos = mouseY;

            //Get the pixel color from the view applet for each touch event.

            //int pixColor = this.pixels[yPos*width+xPos];
            //Color opaqueRed = Color.valueOf(pixColor);

            //aSoundPaint.get(x[i], y[i], 800, 600) less efficient way to ask for a color

            if (yPos> 2.0 * height / 3.0f) {
                offset = lowOffset;
            } else if (yPos < height / 3.0f) {
                offset = highOffset;
            } else {
                offset = midOffset;
            }

            oscdist = -((int) (((xNoteScale - xPos % xNoteScale) / xNoteScale) * 127) - 63);

            //Calculating of the note to be sent to the Synth engine

            midinote = (rootNote + ((int) xPos / xNoteScale)) % (11*tet);
            midinote = (transformNote(midinote+offset)  ) % (11*tet);


            if(midinote>=0) {
                int factor = (int) height / 3;

                //Here the Y axis ois used
                int wi = (int) yPos % factor;

                waveform1 = (this.osc1Wave + (wi * this.osc1WaveControl) / factor) % 256;
                waveform2 = (this.osc2Wave + (wi * this.osc2WaveControl) / factor) % 256;

                engine.sendMidiNoteOn(0, midinote, vel);
                midiNoteOn(0,midinote%128, vel);
                engine.selectWaveform(0, 0, midinote, waveform1);
                engine.selectWaveform(0, 1, midinote, waveform2);
            }

            //FIXED PRESSURE VARiABLE PRESSURE CODE IN ORIGINAL REPO
            float pressure =  0.25f;
            int tmp = ((int) (127.0 * pressure * 4));
            vel = tmp > 127 ? 127 : tmp;


        }



/*
        //Updates the main mouse variables of the system.
        //Make signal point at mouse position inside the pixels array
        oldX = currX;
        oldY = currY;
        currX = constrain(mouseX, 0, img.width-1);
        currY = constrain(mouseY, 0, img.height-1);
        signal = currX*img.width + currX;
        int sx = int(signal) % img.width;
        int sy = int(signal) / img.width;

        set(0, 0, img);  // fast way to draw an image


        fill(150, 255, 255);
        rect(10, height-40, 190, 30);
        fill(0, 0, 0);
        text("STRIKE KEY TO PAINT AGAIN", 23, height-20);

        //Draw Pointer on the image for current pixel
        stroke(0, 0, 0);
        point(sx, sy);
        noFill();
        rect(sx - 5, sy - 5, 10, 10);
        int c = img.get(sx, sy);

        //Octave selector

        if (Octave == 0) {
            Low = 33;
            High = 65;
        }


        if (Octave == 1) {
            Low = 65;
            High = 131;
        }



        if (Octave == 2) {
            Low = 131;
            High = 261;
        }


        if (Octave == 3) {
            Low = 261;
            High = 523;
        }

        if (Octave == 4) {
            Low = 523;
            High = 1046;
        }

        if (Octave == 5) {
            Low = 1046;
            High = 2093;
        }


        if (Octave == 6) {
            Low = 2093;
            High = 4186;
        }


        //Map color as sound freq for Minim.
        if (Inv==false) {
            freq = map(hue(c), 0, 255, Low, High);
            //Link Amp to Saturation
            amp = map(saturation(c)*brightness(c), 0, 65536, 0.0, 0.3);

            //Set freq for Minim Sinth
            wave.setFrequency(freq);
            wave.setAmplitude(amp);

            //Print variables to see changes
            print ("\n Brillo= " + brightness (c) + "   hue= " + hue (c) + "   Freq= " + freq + "   Amp= " + amp);
        }

        if (Inv==true) {
            freq = map(hue(c), 0, 255, High, Low);
            //Link Amp to Saturation
            amp = map(saturation(c)*brightness(c), 0, 65536, 0.0, 0.3);

            //Set freq for Minim Sinth
            wave.setFrequency(freq);
            wave.setAmplitude(amp);

            //Print variables to see changes
            print ("\n Brillo= " + brightness (c) + "   hue= " + hue (c) + "   Freq= " + freq + "   Amp= " + amp);
        }



        //Pressing any key brings back paint mode
        if (keyPressed == true) {
            wave.setAmplitude(0.0);

            //Erase PAINT TEXT
            fill(0, 0, 255);
            noStroke();
            rect(10, height-40, 190, 30);
            set(0,0,img);
            Modo = 0;
        }


 */




    public void draw() {


        //MODO = 0 => Run Paint color
        if (Modo == 0) {
            if (mousePressed) {
                Paint();
            }
            oldX = mouseX;
            oldY = mouseY;

            drawButtons();
        }

        //Modo = 1 => Load Pixels
        if (Modo == 1) {
            LoadPx();
        }

        //Modo = 2 => LISTEN COLOR
        if (Modo == 2) {
            ListenCR();
        }

        //Modo 3 Dropper
        if (Modo == 3) {
            //Dropper();
        }

        if (Modo == 4) {
            //Auto
            //juan.mover();
            //juan.dibujar();
        }

        //Modo = 5 => LISTEN B/W Sinth
        if (Modo == 5) {
            //ListenBW();
        }
    }

    public void mouseReleased() {

        if(Modo == 2) {
            engine.sendMidiNoteOff(0, midinote, 0);
            midiNoteOff(0, midinote % 127, 0);
        }
        buttonsChk();
    }
    public void mousePressed() {
        oldX = mouseX;
        oldY = mouseY;
    }
    public void mouseMoved() {
        actionMove = true;
        //scope.printLine("ACTION_MOVE");
        if (midinote >= 0) {
            int offset1 = 64 - oscdist;
            int offset2 = 64 + oscdist;
            if (offset1 < 0 || offset1 > 127) {
                offset1 = 0;
            }
            if (offset2 > 127 || offset2 < 0) {
                offset2 = 127;
            }

            engine.selectWaveform(0, 0, midinote, waveform1);
            engine.selectWaveform(0, 1, midinote, waveform2);


                float spreadFactor = (float) (maxSpread / 127.0);
                oscdist = (int) (spreadFactor * oscdist);
                engine.sendMidiNoteSpread(0, midinote, 63 + oscdist);

            midiPolyAftertouch(0,midinote,vel);

            midiSendCC(0,1,(waveform1>>1)%128);

        }


    }


    public void logMidi(byte[] data) {
        if (data != null) {
            if (data.length > 0) {
                this.midiLog = "RX:" + data[0];
            }
        }
    }

    private class PortsConnectedListener
            implements org.bokontep.midi.MidiPortConnector.OnPortsConnectedListener {
        @Override
        public void onPortsConnected(final MidiDevice.MidiConnection connection) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (connection == null) {
                        Toast.makeText(context,
                                "PORT BUSY", Toast.LENGTH_LONG)
                                .show();
                        midiPortSelector.clearSelection();
                    } else {
                        Toast.makeText(context,
                                "PORT OPENED!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });
        }
    }

    private void setupMidi(int spinnerID, int spinnerID2) {
        // Setup MIDI
        midiManager = (MidiManager) context.getSystemService(MIDI_SERVICE);

        MidiDeviceInfo synthInfo = MidiTools.findDevice(midiManager, "Bokontep",
                "Volna");
        int portIndex = 0;
        midiPortSelector = new MidiOutputPortConnectionSelector(midiManager, activity,
                spinnerID, synthInfo, portIndex);
        midiPortSelector.setConnectedListener(new MidiPortConnector.OnPortsConnectedListener() {
            @Override
            public void onPortsConnected(final MidiDevice.MidiConnection connection) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connection == null) {
                            Toast.makeText(context,
                                    "Port busy!", Toast.LENGTH_LONG)
                                    .show();
                            midiPortSelector.clearSelection();
                        } else {
                            Toast.makeText(context,
                                    "Port opened!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        });
        midiReceiver = new VolnaMidiReceiver(engine);

        VolnaMidiDeviceService.setMidiReceiver(midiReceiver);
        midiInputPortSelector = new MidiInputPortSelector(midiManager,activity,spinnerID2);
    }


    public void midiNoteOff(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_OFF | channel, pitch, velocity);
    }

    private void midiNoteOn(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_ON | channel, pitch, velocity);
    }
    private void midiSendCC(int channel, int cc, int data)
    {
        midiCommand(MidiConstants.STATUS_CONTROL_CHANGE | channel, cc, data);
    }
    private void midiPolyAftertouch(int channel, int pitch, int velocity)
    {
        midiCommand(MidiConstants.STATUS_POLYPHONIC_AFTERTOUCH | channel, pitch, velocity);
    }
    private void midiCommand(int status, int data1, int data2) {
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        mByteBuffer[2] = (byte) data2;
        long now = System.nanoTime();
        midiSend(mByteBuffer, 3, now);
    }

    private void midiCommand(int status, int data1) {
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        long now = System.nanoTime();
        midiSend(mByteBuffer, 2, now);
    }
    private void midiSend(byte[] buffer, int count, long timestamp)
    {
        try {
            if (this.midiInputPortSelector != null) {
                MidiReceiver receiver = this.midiInputPortSelector.getReceiver();
                if(receiver!=null) {
                    receiver.send(buffer, 0, count, timestamp);
                }
            }
        }
        catch (IOException e) {

        }

    }

}
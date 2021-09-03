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

    static final String TAG = "Volna";

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
    private long lastTouchEventTime = 0;


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
    private Scope scope;
    private EditText lowOffsetEditText;
    private EditText midOffsetEditText;
    private EditText highOffsetEditText;
    private boolean legato = true;
    private int rootNote = 36;
    private int xNoteScale = 160;
    private int currentScale = 0;
    private final int[] notemap = new int[10];
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


//WAVESYNTH OVERRIDES

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] activeIds = new int[10];
        boolean actionMove = false;
        for(int i=0;i<activeIds.length;i++)
        {
            activeIds[i]=-1;
        }
        lastTouchEventTime = new java.util.Date().getTime();
        int action = event.getActionMasked();
        int index = event.getActionIndex();

        //Range of Y is defined
        float height = event.getDevice().getMotionRange(AXIS_Y).getRange();

        int activepointers = event.getPointerCount() % 10; // do not track more than 10
        touchPoints = activepointers;

        float[] x = new float[10];
        float[] y = new float[10];
        for (int i = 0; i < 10; i++) {
            if (i < activepointers) {
                x[i] = event.getX(i);
                y[i] = event.getY(i);
            } else {
                x[i] = -1;
                y[i] = -1;
            }
        }

        String scopetext = "";
        if (tet == 12) {
            scopetext = rootNoteStr + " " + scaleNames[currentScale] + " notes:";

        } else {
            scopetext = "tet " + tet + " notes:";
        }

        //SoundPaint.setScopeText...

        int offset = 0;


        //How the
        String showIds = "";
        for (int i = 0; i < activepointers; i++) {
            int xPos = (int)x[i];
            int yPos = (int)x[i];

            //Get the pixel color from the view applet for each touch event.

            //int pixColor = aSoundPaint.pixels[yPos*width+xPos];
            //Color(pixColor);

            //aSoundPaint.get(x[i], y[i], 800, 600) less efficient way to ask for a color
            if (y[i]> 2.0 * height / 3.0f) {
                offset = lowOffset;
            } else if (y[i] < height / 3.0f) {
                offset = highOffset;
            } else {
                offset = midOffset;
            }

            //Get the id of the pointer
            int id = event.getPointerId(i);

            showIds = showIds+""+id + " ";
            int oscdist = -((int) (((xNoteScale - x[i] % xNoteScale) / xNoteScale) * 127) - 63);
            scopetext = scopetext + "[" + oscdist + "]";

            //Calculating of the note to be sent to the Synth engine

            int midinote = (rootNote + ((int) x[i] / xNoteScale)) % (11*tet);
            midinote = (transformNote(midinote+offset)  ) % (11*tet);
            int factor = (int) height / 3;

            //Here the Y axis of each event is used
            int wi = (int) y[i] % factor;

            int waveform1 = (this.osc1Wave + (wi * this.osc1WaveControl) / factor) % 256;
            int waveform2 = (this.osc2Wave + (wi * this.osc2WaveControl) / factor) % 256;

            engine.selectWaveform(0, 0, midinote, waveform1);
            engine.selectWaveform(0, 1, midinote, waveform2);
            float pressure = event.getPressure();
            if(pressure<0.25f)
            {
                pressure = 0.25f;
            }

            int tmp = ((int) (127.0 * pressure * 4));
            vel = tmp > 127 ? 127 : tmp;
            scopetext = scopetext + " " + midinote;
            if (vel >= 0) {
                scopetext = scopetext + "(" + vel + ")";
            }
            scope.setMarker("" + id, x[i], y[i]);
            int last = notemap[id];
            if (action == MotionEvent.ACTION_DOWN && id==index)  {
                //scope.printLine("ACTION_DOWN " + id);
                if(last>=0)
                {
                    engine.sendMidiNoteOff(0,last,0);
                    midiNoteOff(0,last%128,0);
                    notemap[id] = -1;
                }

                if(midinote>=0) {
                    engine.sendMidiNoteOn(0, midinote, vel);
                    midiNoteOn(0,midinote%128, vel);
                    engine.selectWaveform(0, 0, midinote, waveform1);
                    engine.selectWaveform(0, 1, midinote, waveform2);

                    notemap[id] = midinote;
                }

            }
            if(action == MotionEvent.ACTION_POINTER_DOWN && id==index)
            {
                //scope.printLine("ACTION_PONTER_DOWN " + id );
                if(last>=0)
                {
                    engine.sendMidiNoteOff(0,last,0);
                    midiNoteOff(0,last%128,0);

                    notemap[id] = -1;
                }

                if(midinote>=0) {
                    engine.sendMidiNoteOn(0, midinote, vel);
                    midiNoteOn(0,midinote%128,vel);

                    engine.selectWaveform(0, 0, midinote, waveform1);
                    engine.selectWaveform(0, 1, midinote, waveform2);

                    notemap[id] = midinote;
                }
            }
            if (action == MotionEvent.ACTION_MOVE )
            {
                actionMove = true;
                activeIds[i%10]=id;
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


                    if(legato)
                    {
                        if(last>=0 && last!=midinote) {
                            engine.sendMidiChangeNote(0, last, midinote, vel);
                            midiNoteOn(0,midinote,vel);
                            midiNoteOff(0,last%127,0);

                        }
                    }
                    else {
                        if(last>=0 && last!=midinote) {
                            engine.sendMidiNoteOff(0, last, 0);

                            engine.sendMidiNoteOn(0, midinote, vel);
                            midiNoteOff(0,last%128,0);
                            midiNoteOn(0,midinote,vel);
                        }
                    }
                    if(last>0 && last==midinote) {
                        float spreadFactor = (float) (maxSpread / 127.0);
                        oscdist = (int) (spreadFactor * oscdist);
                        engine.sendMidiNoteSpread(0, midinote, 63 + oscdist);
                    }

                    notemap[id]=midinote;
                    midiPolyAftertouch(0,midinote,vel);

                    midiSendCC(0,id+1,(waveform1>>1)%128);

                }


            }

            if (action == MotionEvent.ACTION_POINTER_UP && id==index && id>=0)
            {

                //scope.printLine("ACTION_POINTER_UP "+id);


                engine.sendMidiNoteOff(0, last, 0);
                engine.sendMidiNoteOff(0, midinote, 0);
                midiNoteOff(0,last%128,0);
                midiNoteOff(0,midinote%128,0);

                scope.unsetMarker("" + index);
                notemap[id]=-1;



            }
            if(action == MotionEvent.ACTION_UP )
            {
                //scope.printLine("ACTION_UP "+id);
                for(int n=0;n<notemap.length;n++) {
                    if(notemap[n]>0)
                    {
                        engine.sendMidiNoteOff(0, notemap[n], 0);
                        midiNoteOff(0,notemap[n]%127,0);
                    }

                    scope.unsetMarker("" + n);
                    notemap[n]=-1;

                }




            }

        }
        if(legato)
        {
            scopetext = scopetext+" L";
        }
        scope.setText(scopetext);

        return true;//return super.onTouchEvent(event);
    }

    public void logMidi(byte[] data) {
        if (data != null) {
            if (data.length > 0) {
                this.midiLog = "RX:" + data[0];
                scope.setMidilog(this.midiLog);
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
                        Toast.makeText(MainActivity.this,
                                "PORT BUSY", Toast.LENGTH_LONG)
                                .show();
                        midiPortSelector.clearSelection();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "PORT OPENED!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });
        }
    }

    private void setupMidi(int spinnerID, int spinnerID2) {
        // Setup MIDI
        midiManager = (MidiManager) getSystemService(MIDI_SERVICE);

        MidiDeviceInfo synthInfo = MidiTools.findDevice(midiManager, "Bokontep",
                "Volna");
        if (synthInfo != null) {
            scope.setMidilog("MIDI device found!");
        }
        int portIndex = 0;
        scope.setMidilog("");
        midiPortSelector = new MidiOutputPortConnectionSelector(midiManager, this,
                spinnerID, synthInfo, portIndex);
        midiPortSelector.setConnectedListener(new MidiPortConnector.OnPortsConnectedListener() {
            @Override
            public void onPortsConnected(final MidiDevice.MidiConnection connection) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connection == null) {
                            Toast.makeText(MainActivity.this,
                                    "Port busy!", Toast.LENGTH_LONG)
                                    .show();
                            midiPortSelector.clearSelection();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Port opened!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        });
        midiReceiver = new VolnaMidiReceiver(engine);

        VolnaMidiDeviceService.setMidiReceiver(midiReceiver);
        midiInputPortSelector = new MidiInputPortSelector(midiManager,this,spinnerID2);
    }


    private void midiNoteOff(int channel, int pitch, int velocity) {
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
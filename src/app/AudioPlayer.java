package app;

import app.coefs.Coefs;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Handles all audio performance
 * **/
public class AudioPlayer implements LineListener {

    private SourceDataLine sdl;
    private AudioInputStream ais;
    private AudioFormat format;

    /** Size of circular buffer **/
    private static final int SAMPLES_ONCE = 16384;
    private static final int    BUFF_SIZE = SAMPLES_ONCE * 16;
    private CircularBuffer buffer;

    /** FFT **/
    private FFT  inputSignal;
    private FFT outputSignal;

    private Evaluatable equalizer;

    private boolean paused = true;
    private boolean ended  = false;

    public AudioPlayer(File musicFile) {
        try {
            if (musicFile != null) {
                ais = AudioSystem.getAudioInputStream(musicFile);
                sdl = AudioSystem.getSourceDataLine(format);
                sdl.flush();
                format = ais.getFormat();
            }

            inputSignal  = new FFT(SAMPLES_ONCE);
            outputSignal = new FFT(SAMPLES_ONCE);

            equalizer = new Equalizer(SAMPLES_ONCE);

            buffer = new CircularBuffer(BUFF_SIZE, SAMPLES_ONCE, 2);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public void work() {
        try {

            sdl.open(format);
            sdl.start();
            paused = true;

            Evaluatable filter = new Filter(Coefs.filt3.length - 1, Coefs.filt3, SAMPLES_ONCE);

            byte[]   readBytes = new  byte[SAMPLES_ONCE * 4];
            short[]     sample = new short[SAMPLES_ONCE * 2];
            boolean putSuccess = true;
            int     readStatus = 0;

            short[] readSamples;

            while (readStatus != -1) {
                if (paused) pause();
                if (ended) {
                    close();
                    if (equalizer != null)
                        ((Equalizer)equalizer).close();

                    return;
                }

                if (putSuccess)
                    readStatus = ais.read(readBytes, 0, SAMPLES_ONCE * 4);

                sample = makeSamplesFromBytes(readBytes);

                inputSignal.put(sample);
                if (inputSignal.isEvaluated())
                    inputSignal.setEvaluated(false);

                putSuccess = buffer.put(sample);

                if (buffer.pull(sample)) {

                    sample = equalizer.evaluate(sample);
                    //sample = filter.evaluate(sample);


                    outputSignal.put(sample);
                    if (outputSignal.isEvaluated())
                        outputSignal.setEvaluated(false);

                    sdl.write(makeBytesFromSamples(sample), 0, SAMPLES_ONCE * 4);
                }
            }

            sdl.drain();
            sdl.close();

        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        paused = false;
    }

    public void stop() {
        paused = true;
    }

    private void pause() {
        if (paused) {
            while (true) {
                try {
                    if (!paused) break;
                    Thread.sleep(50);
                    if (ended) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        if (this.ais != null) {
            try {
                this.ais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.sdl != null) {
            this.sdl.close();
        }
    }


    public void chartWork(XYChart.Data<Number, Number>[] iData1,
                          XYChart.Data<Number, Number>[] iData2,
                          XYChart.Data<Number, Number>[] oData1,
                          XYChart.Data<Number, Number>[] oData2) {
        try {
            while (true) {

                if (!inputSignal.isEvaluated()) {
                    inputSignal.evaluate();
                    chart(iData1, iData2, inputSignal);
                    inputSignal.setEvaluated(true);
                }

                if (!outputSignal.isEvaluated()) {
                    outputSignal.evaluate();
                    chart(oData1, oData2, outputSignal);
                    outputSignal.setEvaluated(true);
                }

                Thread.sleep(50);
                if (ended) return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void chart(XYChart.Data<Number, Number>[] data1,
                       XYChart.Data<Number, Number>[] data2, FFT fft) {

        double[] result = fft.getResult();
        int size = result.length / 4;

        for (int i = 0; i < size; i++) {
            data1[i].setYValue((Math.log10(result[i * 2 + size    ]) - 1) / 2);
            data2[i].setYValue((Math.log10(result[i * 2 + size + 1]) - 1) / 2);
        }
    }


    private short[] makeSamplesFromBytes(byte[] src) {
        short[] buff = new short[src.length / 2];
        for (int i = 0; i < buff.length; i++)
            buff[i] = ByteBuffer.wrap(src, i * 2, 2).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();

        return buff;
    }

    private byte[] makeBytesFromSamples(short[] src) {
        byte[] buff = new byte[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            buff[2*i  ] = (byte) (src[i]);
            buff[2*i+1] = (byte) (src[i] >>> 8);
        }

        return buff;
    }

    @Override
    public void update(LineEvent event) {
    }

    public void endWork() {
        ended = true;
    }

    public static int getSamplesOnce() {
        return SAMPLES_ONCE;
    }
}

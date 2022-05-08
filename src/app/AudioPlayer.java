package app;

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
    private static final int BUFF_SIZE = 16;
    private CircularBuffer buffer;

    private boolean paused = true;

    public AudioPlayer(File musicFile) {
        try {
            InputMusic iMusic = new InputMusic(musicFile);
            sdl = iMusic.getSourceDataLine();
            ais = iMusic.getAudioInputStream();
            format = ais.getFormat();

            buffer = new CircularBuffer(BUFF_SIZE);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public void init() {
        try {

            sdl.open(format);
            sdl.start();
            paused = true;

            int    samplesOnce = 1; // Number of samples for each of 2 channels
            byte[]   readBytes = new  byte[samplesOnce * 4];
            short[] readSample = new short[samplesOnce * 2];
            boolean putSuccess = true;
            int     readStatus = 0;

            boolean tmp = false;

            Filter filter = new Filter(Coefs.filt1.length - 1, Coefs.filt1);

            while (readStatus != -1) {
                if (paused) pause();

                if (putSuccess)
                    readStatus = ais.read(readBytes, 0, 4);

                putSuccess = buffer.put(makeSamplesFromBytes(readBytes));

                if (buffer.pull(readSample)) {
                    readSample = filter.evaluate(readSample);
                    sdl.write(makeBytesFromSamples(readSample), 0, 4);
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        if(this.ais != null)
            try {
                this.ais.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        if(this.sdl != null)
            this.sdl.close();
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
}

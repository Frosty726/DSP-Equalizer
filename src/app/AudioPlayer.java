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
    private static final int BUFF_SIZE = 262144;
    private byte[] buff;
    private short[] sampleBuff;

    private boolean paused = true;

    public AudioPlayer(File musicFile) {
        try {
            InputMusic iMusic = new InputMusic(musicFile);
            sdl = iMusic.getSourceDataLine();
            ais = iMusic.getAudioInputStream();
            format = ais.getFormat();

            buff = new byte[BUFF_SIZE];
            sampleBuff = new short[BUFF_SIZE / 2];
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public void init() {
        try {

            sdl.open(format);
            sdl.start();
            paused = true;

            while ((ais.read(buff, 0, BUFF_SIZE)) != -1) {
                makeSamplesFromBytes();

                if (paused) pause();

                makeBytesFromSamples();
                sdl.write(buff, 0, BUFF_SIZE);
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


    private void makeSamplesFromBytes() {
        for(int i = 0, j = 0; i < this.buff.length; i += 2 , j++) {
            this.sampleBuff[j] = (short) (ByteBuffer.wrap(buff, i, 2).order(
                    java.nio.ByteOrder.LITTLE_ENDIAN).getShort());
        }
    }

    private void makeBytesFromSamples() {
        for(int i = 0, j = 0; i < sampleBuff.length && j < buff.length; i++) {
            this.buff[j    ] = (byte)(sampleBuff[i]);
            this.buff[j + 1] = (byte)(sampleBuff[i] >>> 8);
        }
    }

    @Override
    public void update(LineEvent event) {

    }
}

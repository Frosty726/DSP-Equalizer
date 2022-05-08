package app;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Class for pulling samples from music file
 **/
public class InputMusic {

    private AudioInputStream ais;
    private SourceDataLine sdl;

    public InputMusic(File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        if (file != null) {
            ais = AudioSystem.getAudioInputStream(file);
            AudioFormat format = ais.getFormat();
            sdl = AudioSystem.getSourceDataLine(format);
            sdl.flush();
        }
    }

    public AudioInputStream getAudioInputStream() {
        return this.ais;
    }

    public SourceDataLine getSourceDataLine() {
        return this.sdl;
    }

    public void closeAudioInputStream() {
        try {
            this.ais.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller {

    private boolean isPlaying = false;

    private AudioPlayer aPlayer;

    // Threads
    private Thread audioThread;

    @FXML
    private Button playStopButton;

    @FXML
    private Label musicTitle;


    public void playStop(ActionEvent e) {
        if (aPlayer != null) {
            if (!isPlaying) {
                isPlaying = true;
                playStopButton.setText("Stop");

                aPlayer.play();
            } else {
                isPlaying = false;
                playStopButton.setText("Play");

                aPlayer.stop();
            }
        }
    }

    public void open(ActionEvent e) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose the Song");
            fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("AudioFiles", "*.wav"));
            File selected = fc.showOpenDialog(new Stage());

            if (selected == null) return;
            musicTitle.setText(selected.getName());

            aPlayer = new AudioPlayer(selected);
            audioThread = new Thread(()->{
                aPlayer.init();
            });
            audioThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

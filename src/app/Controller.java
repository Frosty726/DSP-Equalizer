package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller {

    private boolean isPlaying = false;

    private AudioPlayer aPlayer;

    // Threads
    private Thread audioThread;
    private Thread chartThread;

    @FXML Slider slider1;
    @FXML Slider slider2;
    @FXML Slider slider3;
    @FXML Slider slider4;
    @FXML Slider slider5;
    @FXML Slider slider6;
    @FXML Slider slider7;
    @FXML Slider slider8;

    @FXML private Button playStopButton;
    @FXML private Button closeButton;

    @FXML private Label musicTitle;

    @FXML private NumberAxis iXAxis, iYAxis, oXAxis, oYAxis;
    @FXML private LineChart<Number, Number>  inputChart, outputChart;
    private XYChart.Data<Number, Number>[] iData1, iData2, oData1, oData2;


    @FXML
    public void initialize() {
        initCharts();
    }

    @FXML
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

    @FXML
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
                aPlayer.work();
            });
            audioThread.start();

            chartThread = new Thread(()->{
                aPlayer.chartWork(iData1, iData2, oData1, oData2);
            });
            chartThread.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void closeClick(ActionEvent e) {
        if(aPlayer != null) {
            aPlayer.endWork();
        }

        System.exit(0);
    }

    private void initCharts() {

        int size = 2048;

        /** 2 channels for input and 2 channels for output **/
        XYChart.Series<Number, Number> inputData1  = new XYChart.Series<>();
        XYChart.Series<Number, Number> inputData2  = new XYChart.Series<>();
        XYChart.Series<Number, Number> outputData1 = new XYChart.Series<>();
        XYChart.Series<Number, Number> outputData2 = new XYChart.Series<>();

        iData1 = new XYChart.Data[size];
        iData2 = new XYChart.Data[size];
        oData1 = new XYChart.Data[size];
        oData2 = new XYChart.Data[size];

        for (int i = 0; i < size; i++) {
            iData1[i] = new XYChart.Data<>(((double)44100) * i / size - 22050, (double)0);
            iData2[i] = new XYChart.Data<>(((double)44100) * i / size - 22050, (double)0);
            oData1[i] = new XYChart.Data<>(((double)44100) * i / size - 22050, (double)0);
            oData2[i] = new XYChart.Data<>(((double)44100) * i / size - 22050, (double)0);

            inputData1.getData().add(iData1[i]);
            inputData2.getData().add(iData2[i]);
            outputData1.getData().add(oData1[i]);
            outputData2.getData().add(oData2[i]);
        }

        inputChart.getData().addAll(inputData1, inputData2);
        outputChart.getData().addAll(outputData1, outputData2);

        inputChart.setCreateSymbols(false);
        outputChart.setCreateSymbols(false);
        inputChart.setAnimated(false);
        outputChart.setAnimated(false);

        inputChart.setTitle("INPUT");
        outputChart.setTitle("OUTPUT");

        iYAxis.setLowerBound(-0.3);
        iYAxis.setUpperBound(1.1);
        iYAxis.setAnimated(false);
        iYAxis.setAutoRanging(false);
        oYAxis.setLowerBound(-0.3);
        oYAxis.setUpperBound(1.1);
        oYAxis.setAutoRanging(false);
        oYAxis.setAnimated(false);

        iXAxis.setLowerBound(-22050);
        iXAxis.setUpperBound(22050);
        oXAxis.setLowerBound(-22050);
        oXAxis.setUpperBound(22050);
    }
}

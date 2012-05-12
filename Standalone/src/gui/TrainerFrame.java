package gui;

import ab.trainer.Trainer;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

public class TrainerFrame implements ActionListener {

    JFrame frame = new JFrame("Test Frame");
    JPanel j2 = new JPanel();
    JTextField t1 = new JTextField(20);
    JPanel j1 = new JPanel (new FlowLayout());
    JLabel l1 = new JLabel("Name here: ");
    private static final String OPEN_FILE_PICKER = "openfilepickerfororiginalfile";
    private static final String PLAY_FORWARD = "playforward";
    private static final String CUT_SEGMENT = "cutsegment";
    private static final String REPEAT_LAST_SEGMENT = "repeatlastsegment";
    private static final String RECORD = "record";
    private static final String REPLAY_RECORDING = "replayrecording";
    private static final String STOP_RECORDING = "stoprecording";
    private Trainer trainer;

    public TrainerFrame(Trainer trainer)
    {
        this.trainer = trainer;
        j1.add(l1);
        j1.add(t1);
        addButton("Select file", OPEN_FILE_PICKER);
        addButton("Play forward", PLAY_FORWARD);
        addButton("Pause/Cut", CUT_SEGMENT);
        addButton("RePlay Last", REPEAT_LAST_SEGMENT);
        addButton("Record", RECORD);
        addButton("Stop Recording", STOP_RECORDING);
        addButton("Replay Recording", REPLAY_RECORDING);
        frame.add(j1);
        frame.add(j2);
        frame.setLayout(new FlowLayout());
        frame.setSize(400, 150);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFile(new File("/home/ab/Music/C-Music/AudioBooks/French/Echo/A1/010_L1_page 11.mp3"));
    }

    private void addButton(String buttonLabel, String actionCommand) {
        JButton selectFileButton = new JButton (buttonLabel);
        selectFileButton.setActionCommand(actionCommand);
        selectFileButton.addActionListener(this);
        j2.add(selectFileButton);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();
        if (OPEN_FILE_PICKER.equals(actionCommand)) {
            selectOriginlaFile();
        } else
        if (PLAY_FORWARD.equals(actionCommand)) {
            trainer.playForward();
        } else
        if (CUT_SEGMENT.equals(actionCommand)) {
            trainer.cutSegment();
        } else
        if (REPEAT_LAST_SEGMENT.equals(actionCommand)) {
            trainer.playLastSegment();
        } else
        if (RECORD.equals(actionCommand)) {
            trainer.record();
        } else
        if (REPLAY_RECORDING.equals(actionCommand)) {
            trainer.replayRecording();
        } else
        if (STOP_RECORDING.equals(actionCommand)) {
            trainer.stopRecording();
        }
    }

    private void selectOriginlaFile() {
        JFileChooser chooser = new JFileChooser();
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
/*
        ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("mp3");
        filter.setDescription("JPG & GIF Images");
        chooser.setFileFilter(filter);
*/
        int returnVal = chooser.showOpenDialog(this.frame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            setFile(selectedFile);
        }    
    }

    private void setFile(File selectedFile) {
        try {
            trainer.setOriginalFilePath(selectedFile);
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

}
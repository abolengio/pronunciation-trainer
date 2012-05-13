package ab.trainer.gui;

import ab.trainer.Trainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

public class TrainerFrame  {

    JTextField t1 = new JTextField(20);
    JLabel l1 = new JLabel("Name here: ");

    private Trainer trainer;
    private FileChooser fileChooser;
    private FrameActionListener actionListener;

    public TrainerFrame(Trainer trainer, JFrame mainFrame, FileChooser fileChooser )
    {
        this.trainer = trainer;
        this.fileChooser = fileChooser;
        this.actionListener = new FrameActionListener(trainer);
        JPanel contentPanel = new JPanel(new GridLayout(4, 1));
        mainFrame.setTitle("Pronunciation Trainer");
        addContent(contentPanel);
        mainFrame.add(contentPanel);
        mainFrame.setSize(450, 150);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFile(new File("/home/ab/Music/C-Music/AudioBooks/French/Echo/A1/010_L1_page 11.mp3"));
    }

    private void addContent(JPanel contentPanel) {

        final JPanel selectFileButtons = new JPanel(new FlowLayout());

        selectFileButtons.add(addButton("Select file", FrameActionListener.OPEN_FILE_PICKER));
        selectFileButtons.add(l1);
        selectFileButtons.add(t1);
        contentPanel.add(selectFileButtons);
        final JPanel playCutButtons = new JPanel(new FlowLayout());
        playCutButtons.add(addButton("Play forward", FrameActionListener.PLAY_FORWARD));
        playCutButtons.add(addButton("Pause/Cut", FrameActionListener.CUT_SEGMENT));
        contentPanel.add(playCutButtons);
        final JPanel recordingButtons = new JPanel(new FlowLayout());
        recordingButtons.add(addButton("Record", FrameActionListener.RECORD));
        recordingButtons.add(addButton("Stop Recording", FrameActionListener.STOP_RECORDING));
        contentPanel.add(recordingButtons);
        final JPanel trainingButtons = new JPanel(new FlowLayout());

        trainingButtons.add(addButton("RePlay Last", FrameActionListener.REPEAT_LAST_SEGMENT));
        trainingButtons.add(addButton("Replay Recording", FrameActionListener.REPLAY_RECORDING));
        contentPanel.add(trainingButtons);
    }

    private JButton addButton(String buttonLabel, String actionCommand) {
        JButton button = new JButton (buttonLabel);
        button.setActionCommand(actionCommand);
        button.addActionListener(actionListener);
        return button;
    }

    private void selectOriginlaFile() {
        fileChooser.selectFile(actionListener);
    }

    private void setFile(File selectedFile) {
        try {
            trainer.setOriginalFilePath(selectedFile);
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

    public class FrameActionListener implements ActionListener, FileSelectedListener {

        static final String OPEN_FILE_PICKER = "openfilepickerfororiginalfile";
        static final String PLAY_FORWARD = "playforward";
        static final String CUT_SEGMENT = "cutsegment";
        static final String REPEAT_LAST_SEGMENT = "repeatlastsegment";
        static final String RECORD = "record";
        static final String REPLAY_RECORDING = "replayrecording";
        static final String STOP_RECORDING = "stoprecording";

        private Trainer trainer;

        public FrameActionListener(Trainer trainer){
            this.trainer = trainer;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String actionCommand = actionEvent.getActionCommand();
            if (FrameActionListener.OPEN_FILE_PICKER.equals(actionCommand)) {
                selectOriginlaFile();
            } else
            if (FrameActionListener.PLAY_FORWARD.equals(actionCommand)) {
                trainer.playForward();
            } else
            if (FrameActionListener.CUT_SEGMENT.equals(actionCommand)) {
                trainer.cutSegment();
            } else
            if (FrameActionListener.REPEAT_LAST_SEGMENT.equals(actionCommand)) {
                trainer.playLastSegment();
            } else
            if (FrameActionListener.RECORD.equals(actionCommand)) {
                trainer.record();
            } else
            if (FrameActionListener.REPLAY_RECORDING.equals(actionCommand)) {
                trainer.replayRecording();
            } else
            if (FrameActionListener.STOP_RECORDING.equals(actionCommand)) {
                trainer.stopRecording();
            }
        }

        @Override
        public void fileSelected(File selectedFile) {
            try {
                trainer.setOriginalFilePath(selectedFile);
            } catch (FileNotFoundException exc) {
                exc.printStackTrace();
            }
       }
    }
}
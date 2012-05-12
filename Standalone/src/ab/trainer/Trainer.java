package ab.trainer;

import media.MP3Player;
import xuggle.XugglePlayer;

import java.io.*;

import static java.lang.String.format;

public class Trainer {

    private MP3Player player;
    private MP3Player recordingPlayer = null;

    private long lastSegmentStart = 0L;
    private long lastSegmentEnd = 0L;
    private final TempRecorder recorder = new TempRecorder();

    public void setOriginalFilePath(File originalFile) throws FileNotFoundException {
        System.out.println("You chose to open this file: " + originalFile.getAbsolutePath());
        player = new XugglePlayer(originalFile);
        lastSegmentStart = 0L;
        lastSegmentEnd = 0L;
    }

    public void playForward() {
        player.play();
        lastSegmentStart = lastSegmentEnd;
    }

    
    public void cutSegment() {
       // 
        long newLastSegmentEnd = player.pause();
        if(newLastSegmentEnd > lastSegmentEnd) {
            lastSegmentStart = lastSegmentEnd;
        }
        lastSegmentEnd = newLastSegmentEnd;
        System.out.println(format("Last Segment: %s - %s", lastSegmentStart, lastSegmentEnd));
    }

    public void playLastSegment() {
        System.out.println(lastSegmentStart + ", "+ lastSegmentEnd);
        player.playSegment(lastSegmentStart, lastSegmentEnd);
    }

    public void replayRecording() {
        if(recorder.getRecordingFile() != null){
            recordingPlayer = new XugglePlayer(recorder.getRecordingFile());
            recordingPlayer.playFrom(0);
        }
    }

    public void record() {
        recorder.record();
    }

    public void stopRecording() {
        recorder.stopRecording();
    }
}

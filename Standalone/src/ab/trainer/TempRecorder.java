package ab.trainer;

import media.AudioRecorder;

import java.io.File;
import java.io.IOException;

public class TempRecorder {
    
    private File recordingFile = null;
    private AudioRecorder.Recorder recorder = null;

    public TempRecorder(){

    }

    public void record() {
        try {
            recorder = AudioRecorder.createRecorder(getCleanFile());
            recorder.start();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void stopRecording() {
        if(recorder != null) {
            recorder.stopRecording();
        }
    }

    private File getCleanFile() throws IOException {
        File file = getOrCreateFile();
        file.delete();
        file.createNewFile();
        return file; 
    }

    private File getOrCreateFile() throws IOException {
        if(recordingFile == null){
            recordingFile = File.createTempFile("recording", Long.toString(System.nanoTime()));
        }
        return recordingFile;
    }

    public File getRecordingFile() {
        return recordingFile;
    }
}

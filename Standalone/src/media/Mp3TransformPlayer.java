package media;

import org.mp3transform.Decoder;

import java.io.*;

public abstract class Mp3TransformPlayer implements MP3Player {
    private Decoder decoder = new Decoder();
    private File file;
    private final BufferedInputStream bufferedMusicStream;


    public Mp3TransformPlayer(File mp3) throws FileNotFoundException {
        this.file = mp3;
        FileInputStream in = new FileInputStream(file);
        bufferedMusicStream = new BufferedInputStream(in, 128 * 1024);
    }



    class Player extends Thread {

        public void run()
        {
            try {
                decoder.play(file.getName(), bufferedMusicStream);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public long pause() {
        decoder.pause();
        return 0;
    }

    @Override
    public void play() {
        new Player().start();
    }

    @Override
    public void playSegment(long startPosition, long endposition) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}

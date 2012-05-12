package media;

public interface MP3Player {
    long pause();
    void play();
    void playSegment(long startPosition, long endPosition);
    void playFrom(long startPosition);
}

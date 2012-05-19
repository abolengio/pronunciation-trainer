package xuggle;

/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *
 * This file is part of Xuggle-Xuggler-Main.
 *
 * Xuggle-Xuggler-Main is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Main is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Main.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import media.MP3Player;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Takes a media container, finds the first audio stream,
 * decodes that stream, and then plays
 * it on the default system device.
 * @author aclarke
 *
 */
public class XugglePlayer implements MP3Player
{

    /**
     * The audio line we'll output sound to; it'll be the default audio device on your system if available
     */
    private SourceDataLine mLine;
    private IContainer container;
    private int audioStreamId = -1;  //todo hide in a class responsible for reading appropriate stream
    private IStreamCoder audioCoder = null;
    private AtomicBoolean isPaused = new AtomicBoolean(false);

    /**
     * Takes a media container (file) as the first argument, opens it,
     * opens up the default audio device on your system, and plays back the audio.
     *
     * @param args Must contain one string which represents a filename
     */
    public static void main(String[] args)
    {
       // if (args.length <= 0)
        //    throw new IllegalArgumentException("must pass in a filename as the first argument");

        //String filename = "/home/ab/Music/C-Music/Aerosmith/Crazy.mp3";
        String filename = "/home/ab/Music/C-Music/AudioBooks/French/Echo/A1/002_L1_page 7.mp3";
        XugglePlayer xugglePlayer = new XugglePlayer(new File(filename));

        /*
        * Now, we start walking through the container looking at each packet.
        */
        xugglePlayer.play();


        /*
        * Technically since we're exiting anyway, these will be cleaned up by
        * the garbage collector... but because we're nice people and want
        * to be invited places for Christmas, we're going to show how to clean up.
        */
        xugglePlayer.close();
    }

    public void play() {
        isPaused.set(false);
        new Player().start();
    }

    @Override
    public void playSegment(long startPosition, long endposition) {
        isPaused.set(false);
        container.seekKeyFrame(audioStreamId, startPosition, 0);
        new Player(endposition).start();
    }

    @Override
    public void playFrom(long startPosition) {
        playSegment(startPosition, Long.MAX_VALUE);
    }

    @Override
    public long pause() {
        isPaused.set(true);
        synchronized (lastPauseEnd) {
            return lastPauseEnd.longValue();
        }
    }

    private Long lastPauseEnd = new Long(0);
    
    private void close() {
        closeJavaSound();


        if (audioCoder != null)
        {
            audioCoder.close();
            audioCoder = null;
        }
        if (container !=null)
        {
            container.close();
            container = null;
        }
    }

    class Player extends Thread {

        private Long playTillTimeStamp;

        Player(long playTillTimeStamp) {
            this.playTillTimeStamp = playTillTimeStamp;
        }

        Player() {
            this.playTillTimeStamp = null;
        }

        @Override
        public void run() {
            playTillStoppedOrEndOfStream();
        }

        private void playTillStoppedOrEndOfStream() {
            synchronized (lastPauseEnd){
                openJavaSound(audioCoder);
                IPacket packet = IPacket.make();
                while(!isPaused.get() && container.readNextPacket(packet) >= 0 && (playTillTimeStamp == null || packet.getDts() < playTillTimeStamp))
                {
                    //System.out.println("New Packet");
                    /*
                    * Now we have a packet, let's see if it belongs to our audio stream
                    */
                    if (packet.getStreamIndex() == audioStreamId)
                    {
                        /*
                        * We allocate a set of samples with the same number of channels as the
                        * coder tells us is in this buffer.
                        *
                        * We also pass in a buffer size (1024 in our example), although Xuggler
                        * will probably allocate more space than just the 1024 (it's not important why).
                        */
                        IAudioSamples samples = IAudioSamples.make(4096, audioCoder.getChannels());

                        /*
                        * A packet can actually contain multiple sets of samples (or frames of samples
                        * in audio-decoding speak).  So, we may need to call decode audio multiple
                        * times at different offsets in the packet's data.  We capture that here.
                        */
                        int offset = 0;

                        /*
                        * Keep going until we've processed all data
                        */
                        while(offset < packet.getSize())
                        {
                            int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                            if (bytesDecoded < 0)
                                throw new RuntimeException("got error decoding audio");
                            offset += bytesDecoded;
                            /*
                            * Some decoder will consume data in a packet, but will not be able to construct
                            * a full set of samples yet.  Therefore you should always check if you
                            * got a complete set of samples from the decoder
                            */
                            if (samples.isComplete())
                            {
                                /**
                                 * We're just going to dump all the samples into the line.
                                 */
                                byte[] rawBytes = samples.getData().getByteArray(0, samples.getSize());
                              //  mLine.write(rawBytes, 0, samples.getSize());
                                mLine.write(rawBytes, 0, rawBytes.length);
                            } else {
                                System.out.println("Samples is not complete");
                            }
                        }
                    }
                    else
                    {
                        /*
                        * This packet isn't part of our audio stream, so we just silently drop it.
                        */
                    }

                }
                if(isPaused.get()) {
                    lastPauseEnd = packet.getDts();
                    System.out.println("packet.getDts() = "+packet.getDts());
                    System.out.println("packet.getPts() = "+packet.getPts());
                }
                closeJavaSound();
            }
        }
    }

    public XugglePlayer(File filename){

        // Create a Xuggler container object
        container = IContainer.make();

        // Open up the container
        if (container.open(filename.getAbsolutePath(), IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("could not open file: " + filename);

        // query how many streams the call to open found
        int numStreams = container.getNumStreams();

        // and iterate through the streams to find the first audio stream

        for(int i = 0; i < numStreams; i++)
        {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
            {
                audioStreamId = i;
                audioCoder = coder;
                break;
            }
        }
        if (audioStreamId == -1)
            throw new RuntimeException("could not find audio stream in container: "+filename);

        /*
        * Now we have found the audio stream in this file.  Let's open up our decoder so it can
        * do work.
        */
        if (audioCoder.open() < 0)
            throw new RuntimeException("could not open audio decoder for container: "+filename);

        /*
        * And once we have that, we ask the Java Sound System to get itself ready.
        */
      //  openJavaSound(audioCoder);
        
    }

    private void openJavaSound(IStreamCoder aAudioCoder)
    {
        AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
                (int)IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
                aAudioCoder.getChannels(),
                true, /* xuggler defaults to signed 16 bit samples */
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try
        {
            mLine = (SourceDataLine) AudioSystem.getLine(info);
            /**
             * if that succeeded, try opening the line.
             */
            mLine.open(audioFormat);
            /**
             * And if that succeed, start the line.
             */
            mLine.start();
        }
        catch (LineUnavailableException e)
        {
            throw new RuntimeException("could not open audio line");
        }


    }

    private void closeJavaSound()
    {
        if (mLine != null)
        {
            /*
            * Wait for the line to finish playing
            */
            mLine.drain();
            /*
            * Close the line.
            */
            mLine.close();
            mLine=null;
        }
    }
}

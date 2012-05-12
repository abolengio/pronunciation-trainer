package media;


/*
 *	AudioRecorder.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2003 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioFileFormat;

/*	If the compilation fails because this class is not available,
	get gnu.getopt from the URL given in the comment below.
*/


// IDEA: recording format vs. storage format; possible conversion?
/**	<titleabbrev>AudioRecorder</titleabbrev>
 <title>Recording to an audio file (advanced version)</title>

 <formalpara><title>Purpose</title>
 <para>
 This program opens two lines: one for recording and one
 for playback. In an infinite loop, it reads data from
 the recording line and writes them to the playback line.
 You can use this to measure the delays inside Java Sound:
 Speak into the microphone and wait untill you hear
 yourself in the speakers.  This can be used to
 experience the effect of changing the buffer sizes: use
 the '-e' and '-i' options. You will notice that the
 delays change, too.
 </para></formalpara>

 <formalpara><title>Usage</title>
 <para>
 <synopsis>java AudioRecorder -l</synopsis>
 <synopsis>java AudioRecorder [-M &lt;mixername&gt;] [-e &lt;buffersize&gt;] [-i &lt;buffersize&gt;] &lt;audiofile&gt;</synopsis>
 </para></formalpara>

 <formalpara><title>Parameters</title>
 <variablelist>
 <varlistentry>
 <term><option>-l</option></term>
 <listitem><para>lists the available mixers</para></listitem>
 </varlistentry>
 <varlistentry>
 <term><option>-M &lt;mixername&gt;</option></term>
 <listitem><para>selects a mixer to play on</para></listitem>
 </varlistentry>
 <varlistentry>
 <term><option>-e &lt;buffersize&gt;</option></term>
 <listitem><para>the buffer size to use in the application ("extern")</para></listitem>
 </varlistentry>
 <varlistentry>
 <term><option>-i &lt;buffersize&gt;</option></term>
 <listitem><para>the buffer size to use in Java Sound ("intern")</para></listitem>
 </varlistentry>
 </variablelist>
 </formalpara>

 <formalpara><title>Bugs, limitations</title>
 <para>
 There is no way to stop the program besides brute force
 (ctrl-C). There is no way to set the audio quality.
 </para></formalpara>

 <formalpara><title>Source code</title>
 <para>
 <ulink url="AudioRecorder.java.html">AudioRecorder.java</ulink>,
 <ulink url="AudioCommon.java.html">AudioCommon.java</ulink>,
 <ulink url="http://www.urbanophile.com/arenn/hacking/download.html">gnu.getopt.Getopt</ulink>
 </para>
 </formalpara>

 */
public class AudioRecorder
{
    private static final SupportedFormat[]	SUPPORTED_FORMATS =
            {
                    new SupportedFormat("s8",
                            AudioFormat.Encoding.PCM_SIGNED, 8, true),
                    new SupportedFormat("u8",
                            AudioFormat.Encoding.PCM_UNSIGNED, 8, true),
                    new SupportedFormat("s16_le",
                            AudioFormat.Encoding.PCM_SIGNED, 16, false),
                    new SupportedFormat("s16_be",
                            AudioFormat.Encoding.PCM_SIGNED, 16, true),
                    new SupportedFormat("u16_le",
                            AudioFormat.Encoding.PCM_UNSIGNED, 16, false),
                    new SupportedFormat("u16_be",
                            AudioFormat.Encoding.PCM_UNSIGNED, 16, true),
                    new SupportedFormat("s24_le",
                            AudioFormat.Encoding.PCM_SIGNED, 24, false),
                    new SupportedFormat("s24_be",
                            AudioFormat.Encoding.PCM_SIGNED, 24, true),
                    new SupportedFormat("u24_le",
                            AudioFormat.Encoding.PCM_UNSIGNED, 24, false),
                    new SupportedFormat("u24_be",
                            AudioFormat.Encoding.PCM_UNSIGNED, 24, true),
                    new SupportedFormat("s32_le",
                            AudioFormat.Encoding.PCM_SIGNED, 32, false),
                    new SupportedFormat("s32_be",
                            AudioFormat.Encoding.PCM_SIGNED, 32, true),
                    new SupportedFormat("u32_le",
                            AudioFormat.Encoding.PCM_UNSIGNED, 32, false),
                    new SupportedFormat("u32_be",
                            AudioFormat.Encoding.PCM_UNSIGNED, 32, true),
            };

    private static final String	DEFAULT_FORMAT = "s16_le";
    private static final int	DEFAULT_CHANNELS = 2;
    private static final float	DEFAULT_RATE = 44100.0F;
    private static final AudioFileFormat.Type	DEFAULT_TARGET_TYPE = AudioFileFormat.Type.WAVE;

    private static boolean		sm_bDebug = false;
    public static final int DEFAULT_FORMAT_INDEX = 2;

    private static void printUsageAndExit()
    {
        out("AudioRecorder: usage:");
        out("\tjava AudioRecorder -l");
        out("\tjava AudioRecorder -L");
        out("\tjava AudioRecorder [-f <format>] [-c <numchannels>] [-r <samplingrate>] [-t <targettype>] [-M <mixername>] <soundfile>");
        System.exit(0);
    }

    public static void main(String[] args) {
/*
		 *	Parsing of command-line options takes place...
		 */
        String	strMixerName = null;
        String	strFormat = DEFAULT_FORMAT;
        int	nChannels = DEFAULT_CHANNELS;
        float	fRate = DEFAULT_RATE;
        String	strExtension = null;
        boolean	bDirectRecording = true;

        /*
           *	We make shure that there is only one more argument, which
           *	we take as the filename of the soundfile to store to.
           */
        String	strFilename = "/home/ab/test.wav";
        if (sm_bDebug) { out("AudioRecorder.main(): output filename: " + strFilename); }

        File	outputFile = new File(strFilename);

        /* For convenience, we have some shortcuts to set the
             properties needed for constructing an AudioFormat.
          */
        if (strFormat.equals("phone"))
        {
            // 8 kHz, 8 bit unsigned, mono
            fRate = 8000.0F;
            strFormat = "u8";
            nChannels = 1;
        }
        else if (strFormat.equals("radio"))
        {
            // 22.05 kHz, 16 bit signed, mono
            fRate = 22050.0F;
            strFormat = "s16_le";
            nChannels = 1;
        }
        else if (strFormat.equals("cd"))
        {
            // 44.1 kHz, 16 bit signed, stereo, little-endian
            fRate = 44100.0F;
            strFormat = "s16_le";
            nChannels = 2;
        }
        else if (strFormat.equals("dat"))
        {
            // 48 kHz, 16 bit signed, stereo, little-endian
            fRate = 48000.0F;
            strFormat = "s16_le";
            nChannels = 2;
        }

        /* Here, we are constructing the AudioFormat to use for the
             recording. Sample rate (fRate) and number of channels
             (nChannels) are already set safely, since they have
             default values set at the very top. The other properties
             needed for AudioFormat are derived from the 'format'
             specification (strFormat).
          */
        int	nOutputFormatIndex = -1;
        for (int i = 0; i < SUPPORTED_FORMATS.length; i++)
        {
            if (SUPPORTED_FORMATS[i].getName().equals(strFormat))
            {
                nOutputFormatIndex = i;
                break;
            }
        }
        /* If we haven't found the format (string) requested by the
             user, we switch to a default format.
          */
        if (nOutputFormatIndex == -1)
        {
            out("warning: output format '" + strFormat + "' not supported; using default output format '" + DEFAULT_FORMAT + "'");
            /* This is the index of "s16_le". Yes, it's
                  a bit quick & dirty to hardcode the index here.
               */
            nOutputFormatIndex = DEFAULT_FORMAT_INDEX;
        }
        SupportedFormat supportedFormat = SUPPORTED_FORMATS[nOutputFormatIndex];
        AudioFormat audioFormat = createAudioFormat(nChannels, fRate, supportedFormat);

        if (sm_bDebug) { out("AudioRecorder.main(): target audio format: " + audioFormat); }

        // extension
        // TODO:


        AudioFileFormat.Type	targetType = null;
        if (strExtension == null)
        {
            /* The user chose not to specify a target audio
                  file type explicitely. We are trying to guess
                  the type from the target file name extension.
               */
            int	nDotPosition = strFilename.lastIndexOf('.');
            if (nDotPosition != -1)
            {
                strExtension = strFilename.substring(nDotPosition + 1);
            }
        }
        if (strExtension != null)
        {
            targetType = AudioCommon.findTargetType(strExtension);
            if (targetType == null)
            {
                out("target type '" + strExtension + "' is not supported.");
                out("using default type '" + DEFAULT_TARGET_TYPE.getExtension() + "'");
                targetType = DEFAULT_TARGET_TYPE;
            }
        }
        else
        {
            out("target type is neither specified nor can be guessed from the target file name.");
            out("using default type '" + DEFAULT_TARGET_TYPE.getExtension() + "'");
            targetType = DEFAULT_TARGET_TYPE;
        }
        if (sm_bDebug) { out("AudioRecorder.main(): target audio file format type: " + targetType); }

        Recorder recorder = createRecorder(strMixerName, bDirectRecording, outputFile, audioFormat, targetType);

        if (sm_bDebug) { out("AudioRecorder.main(): Recorder: " + recorder); }

        out("Press ENTER to start the recording.");
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        recorder.start();
        out("Recording...");
        out("Press ENTER to stop the recording.");
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        recorder.stopRecording();
        out("Recording stopped.");
        // System.exit(0);
    }

    private static AudioFormat createAudioFormat(int nChannels, float fRate, SupportedFormat supportedFormat) {
        AudioFormat.Encoding encoding = supportedFormat.getEncoding();
        int	nBitsPerSample = supportedFormat.getSampleSize();
        boolean	bBigEndian = supportedFormat.getBigEndian();
        int	nFrameSize = (nBitsPerSample / 8) * nChannels;
        return new AudioFormat(encoding, fRate, nBitsPerSample, nChannels, nFrameSize, fRate, bBigEndian);
    }

    public static Recorder createRecorder(File outputFile) {
        SupportedFormat supportedFormat = SUPPORTED_FORMATS[DEFAULT_FORMAT_INDEX];
        AudioFormat audioFormat = createAudioFormat(DEFAULT_CHANNELS, DEFAULT_RATE, supportedFormat);

        return createRecorder(null, true, outputFile, audioFormat, DEFAULT_TARGET_TYPE);

    }

    private static Recorder createRecorder(String strMixerName, boolean bDirectRecording, File outputFile, AudioFormat audioFormat, AudioFileFormat.Type targetType) {
        int	nInternalBufferSize = AudioSystem.NOT_SPECIFIED;

        TargetDataLine targetDataLine = null;
        targetDataLine = AudioCommon.getTargetDataLine(
                strMixerName, audioFormat, nInternalBufferSize);
        if (targetDataLine == null)
        {
            out("can't get TargetDataLine, exiting.");
            System.exit(1);
        }

        Recorder recorder = null;
        if (bDirectRecording)
        {
            recorder = new DirectRecorder(
                    targetDataLine,
                    targetType,
                    outputFile);
        }
        else
        {
            recorder = new BufferingRecorder(
                    targetDataLine,
                    targetType,
                    outputFile);
        }
        return recorder;
    }

    /** TODO:
     */
    private static void out(String strMessage)
    {
        System.out.println(strMessage);
    }



///////////// inner classes ////////////////////


    /** TODO:
     */
    private static class SupportedFormat
    {
        /** The name of the format.
         */
        private String			m_strName;

        /** The encoding of the format.
         */
        private AudioFormat.Encoding	m_encoding;

        /** The sample size of the format.
         This value is in bits for a single sample
         (not for a frame).
         */
        private int			m_nSampleSize;

        /** The endianess of the format.
         */
        private boolean			m_bBigEndian;

        // sample size is in bits
        /** Construct a new supported format.
         @param strName the name of the format.
         @param encoding the encoding of the format.
         @param nSampleSize the sample size of the format, in bits.
         @param bBigEndian the endianess of the format.
         */
        public SupportedFormat(String strName,
                               AudioFormat.Encoding encoding,
                               int nSampleSize,
                               boolean bBigEndian)
        {
            m_strName = strName;
            m_encoding = encoding;
            m_nSampleSize = nSampleSize;
        }

        /** Returns the name of the format.
         */
        public String getName()
        {
            return m_strName;
        }

        /** Returns the encoding of the format.
         */
        public AudioFormat.Encoding getEncoding()
        {
            return m_encoding;
        }

        /** Returns the sample size of the format.
         This value is in bits.
         */
        public int getSampleSize()
        {
            return m_nSampleSize;
        }

        /** Returns the endianess of the format.
         */
        public boolean getBigEndian()
        {
            return m_bBigEndian;
        }
    }


    ///////////////////////////////////////////////


    public static interface Recorder
    {
        public void start();

        public void stopRecording();
    }



    public static class AbstractRecorder
            extends Thread
            implements Recorder
    {
        protected TargetDataLine	m_line;
        protected AudioFileFormat.Type	m_targetType;
        protected File			m_file;
        protected boolean		m_bRecording;



        public AbstractRecorder(TargetDataLine line,
                                AudioFileFormat.Type targetType,
                                File file)
        {
            m_line = line;
            m_targetType = targetType;
            m_file = file;
        }



        /**	Starts the recording.
         *	To accomplish this, (i) the line is started and (ii) the
         *	thread is started.
         */
        public void start()
        {
            m_line.start();
            super.start();
        }



        public void stopRecording()
        {
            // for recording, the line needs to be stopped
            // before draining (especially if you're still
            // reading from it)
            m_line.stop();
            m_line.drain();
            m_line.close();
            m_bRecording = false;
        }
    }



    public static class DirectRecorder
            extends AbstractRecorder
    {
        private AudioInputStream	m_audioInputStream;



        public DirectRecorder(TargetDataLine line,
                              AudioFileFormat.Type targetType,
                              File file)
        {
            super(line, targetType, file);
            m_audioInputStream = new AudioInputStream(line);
        }



        public void run()
        {
            try
            {
                System.out.println("Direct");
                if (sm_bDebug) { out("before AudioSystem.write"); }
                AudioSystem.write(
                        m_audioInputStream,
                        m_targetType,
                        m_file);
                if (sm_bDebug) { out("after AudioSystem.write"); }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }



    }



    public static class BufferingRecorder
            extends AbstractRecorder
    {
        public BufferingRecorder(TargetDataLine line,
                                 AudioFileFormat.Type targetType,
                                 File file)
        {
            super(line, targetType, file);
        }

        private byte maximum = 0;

        public void run()
        {
            ByteArrayOutputStream	byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream		outputStream = byteArrayOutputStream;
            // TODO: intelligent size
            byte[]	abBuffer = new byte[65536];
            AudioFormat	format = m_line.getFormat();
            int	nFrameSize = format.getFrameSize();
            int	nBufferFrames = abBuffer.length / nFrameSize;
            m_bRecording = true;
            while (m_bRecording)
            {
                if (sm_bDebug) { out("BufferingRecorder.run(): trying to read: " + nBufferFrames); }
                int	nFramesRead = m_line.read(abBuffer, 0, nBufferFrames);
                if (sm_bDebug) { out("BufferingRecorder.run(): read: " + nFramesRead); }
                int	nBytesToWrite = nFramesRead * nFrameSize;
                try
                {
                    amplify(abBuffer, 1);
                    outputStream.write(abBuffer, 0, nBytesToWrite);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println("Maximum: " + maximum);
            /* We close the ByteArrayOutputStream.
                */
            try
            {
                byteArrayOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


            byte[]	abData = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream	byteArrayInputStream = new ByteArrayInputStream(abData);

            AudioInputStream	audioInputStream = new AudioInputStream(byteArrayInputStream, format, abData.length / format.getFrameSize());
            try
            {
                AudioSystem.write(audioInputStream,  m_targetType, m_file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private void amplify(byte[] abBuffer, double volume) {
            for(int i = 0; i < abBuffer.length; i++ ){
                if(abBuffer[i] > maximum) {
                    maximum = abBuffer[i];
                    System.out.println("new maximum: " + maximum);
                }
                abBuffer[i] = (byte)(abBuffer[i] * volume);
            }
        }
    }
}



/*** AudioRecorder.java ***/
package media.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SoundStreamAmplifier {

    public static final int BYTE_IN_SHORT = Short.SIZE / Byte.SIZE;
    private static final int NOISE_LEVEL = 100;

    public byte[] amplify(byte[] sourceBuffer, byte[] resultBuffer, double level) {
        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;
        short[] shorts = new short[sourceBuffer.length/ BYTE_IN_SHORT];
        ByteBuffer.wrap(sourceBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        //do something with shorts
        for(int i =0 ; i < shorts.length; i++){
            if(shorts[i] > max) max = shorts[i];
            if(shorts[i] < min) min = shorts[i];
           // if(shorts[i] > NOISE_LEVEL || shorts[i] < 0 - NOISE_LEVEL)
                shorts[i] = (short) (shorts[i] * level);
        }
        //converting back to bites
        ByteBuffer.wrap(resultBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
        System.out.println("min = " + min + "; max = "+max);
        
        return resultBuffer;
    }
}

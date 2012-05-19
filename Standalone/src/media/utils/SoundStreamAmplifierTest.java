package media.utils;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SoundStreamAmplifierTest {

    private SoundStreamAmplifier soundStreamAmplifier;

    @Before
    public void setUp() throws Exception {
        soundStreamAmplifier = new SoundStreamAmplifier();
    }

    @Test
    public void shouldTransformToEqualByteArrayWhenAmplificationLevelIsOne(){
        byte[] buffer = new byte[]{0x7F,0x1B,0x10,0x11,0x6F,0x1B,0x1A,0x11};
        byte[] resultBuffer = new byte[buffer.length];
        soundStreamAmplifier.amplify(buffer, resultBuffer, 1.0);
        assertThat(resultBuffer, is(equalTo(buffer)));
    }
}

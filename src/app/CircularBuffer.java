package app;

public class CircularBuffer {

    private int samplesOnce; // Number of samples in one channel
    private int BUFF_SIZE;
    private short[] buffer;
    private int head;
    private int tail;

    public CircularBuffer(int size, int smplOnce, int numChannels) {
        samplesOnce = smplOnce * numChannels;
        BUFF_SIZE = size * numChannels;
        buffer = new short[BUFF_SIZE];
        head = 0;
        tail = 0;
    }

    public boolean put(short[] source) {
        if (source.length != samplesOnce) {
            System.out.println("Wrong num of samples");
            return false;
        }
        if (head - tail > BUFF_SIZE - 2 * samplesOnce)
            return false;

        System.arraycopy(source, 0, buffer, head, samplesOnce);
        head = (head + samplesOnce) % BUFF_SIZE;

        return true;
    }

    public boolean pull(short[] target) {
        if (target.length != samplesOnce) {
            System.out.println("Wrong num of samples");
            return false;
        }

        if (head - tail == 0) {
            return false;
        }

        System.arraycopy(buffer, tail, target, 0, samplesOnce);
        tail = (tail + samplesOnce) % BUFF_SIZE;

        return true;
    }

    public short[] getBuffer() {
        short[] buff = new short[buffer.length];
        System.arraycopy(buffer, 0, buff, 0, head);
        System.arraycopy(buffer, head, buff, 0, BUFF_SIZE - head);

        return buff;
    }

    /** Methods for filtering **/
    public void putQueue(final short[] src) {
        System.arraycopy(src, 0, buffer, head, samplesOnce);
        head = (head + samplesOnce) % BUFF_SIZE;
    }

    public short get(int index) {
        return buffer[(index + head + 1) % BUFF_SIZE];
    }
}

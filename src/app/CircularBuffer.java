package app;

public class CircularBuffer {

    private int BUFF_SIZE;
    private short[] buffer;
    private int head;
    private int tail;

    public CircularBuffer(int size) {
        BUFF_SIZE = size * 2;
        buffer = new short[BUFF_SIZE];
        head = 0;
        tail = 0;
    }

    public boolean put(short[] source) {
        if (head - tail > BUFF_SIZE - 2)
            return false;

        buffer[head % BUFF_SIZE    ] = source[0];
        buffer[head % BUFF_SIZE + 1] = source[1];
        head += 2;

        return true;
    }

    public boolean pull(short[] target) {
        if (head - tail == 0)
            return false;

        target[0] = buffer[tail % BUFF_SIZE    ];
        target[1] = buffer[tail % BUFF_SIZE + 1];
        tail += 2;

        return true;
    }

    public short[] getBuffer() {
        return buffer;
    }

    /** Methods for filtering **/
    public void putQueue(short[] src) {
        buffer[head % BUFF_SIZE    ] = src[0];
        buffer[head % BUFF_SIZE + 1] = src[1];
        head += 2;
    }

    public short get(int index) {
        return buffer[(index + head + 1) % BUFF_SIZE];
    }
}

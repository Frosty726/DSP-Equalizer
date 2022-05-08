package app;

public class CircularBuffer {

    private int BUFF_SIZE;
    private short[] buffer;
    private int head;
    private int tail;

    public CircularBuffer(int size) {
        BUFF_SIZE = size;
        buffer = new short[BUFF_SIZE];
        head = 0;
        tail = 0;
    }

    public boolean put(short[] source) {
        if (head - tail > BUFF_SIZE - 1)
            return false;

        buffer[head % BUFF_SIZE] = source[0];
        head++;

        return true;
    }

    public boolean pull(short[] target) {
        if (head - tail == 0)
            return false;

        target[0] = buffer[tail % BUFF_SIZE];
        tail++;

        return true;
    }

    public short[] getBuffer() {
        return buffer;
    }
}

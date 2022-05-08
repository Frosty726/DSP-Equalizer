package app;

public class Filter {

    private int      order;
    private double[] coefs;

    private CircularBuffer buffer;

    public Filter(int order, double[] coefs) {
        this.order = order;
        this.buffer = new CircularBuffer(order + 1);
        this.coefs = new double[order + 1];
        System.arraycopy(coefs, 0, this.coefs, 0, coefs.length);
    }

    public short[] evaluate(short[] sample) {
        buffer.putQueue(sample);
        return process();
    }

    private short[] process() {
        double[] mult = new double[2]; // 2 channels
        for (int i = 0; i < coefs.length; i++) {
            mult[0] += buffer.get(i*2  ) * coefs[coefs.length - i - 1];
            mult[1] += buffer.get(i*2+1) * coefs[coefs.length - i - 1];
        }

        return new short[]{(short)mult[0], (short)mult[1]};
    }
}

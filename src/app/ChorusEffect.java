package app;



public class ChorusEffect implements Evaluatable {

    private int          smplOnce;
    private CircularBuffer buffer;

    private int             delay[];
    private double   currentValue[];
    private final double     step[] = {0.1, 0.3, 0.5};


    public ChorusEffect(int smplOnce) {
        this.smplOnce = smplOnce;
        buffer        = new CircularBuffer(smplOnce * 4, smplOnce, 2);
        currentValue  = new double[step.length];
        delay         = new int[step.length];
    }

    @Override
    public short[] evaluate(short[] samples) {
        short[] result = new short[smplOnce * 2];
        for (int i = 0; i < step.length; i++) {
            currentValue[i] += step[i];
            delay[i] = (int)(Math.sin(currentValue[i]) * 440) + 800;
        }

        buffer.putQueue(samples);
        for (int i = 0; i < result.length; i++) {
            result[i] = buffer.get(i);
            for (int j = 0; j < step.length; j++)
                result[i] += buffer.get(i + delay[j]) / step.length;
        }

        return result;
    }
}

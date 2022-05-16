package app;

public class ClippingEffect implements Evaluatable {

    private short upperBound;
    private short lowerBound;

    public ClippingEffect() {
        upperBound = Short.MAX_VALUE;
        lowerBound = Short.MIN_VALUE;
    }

    @Override
    public short[] evaluate(short[] samples) {

        short[] result = new short[samples.length];

        for (int i = 0; i < samples.length; i++) {

            if (samples[i] > upperBound)
                result[i] = upperBound;
            else if (samples[i] < lowerBound)
                result[i] = lowerBound;
            else
                result[i] = samples[i];

        }

        return result;
    }

    public void setLowerBound(short lowerBound) {
        this.lowerBound = (short)-Math.abs(lowerBound);
    }

    public void setUpperBound(short upperBound) {
        this.upperBound = (short)Math.abs(upperBound);
    }

    public void setBound(short bound) {
        this.lowerBound = (short)-Math.abs(bound);
        this.upperBound = (short) Math.abs(bound);
    }
}
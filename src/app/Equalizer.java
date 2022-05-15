package app;

import app.coefs.Coefs;

public class Equalizer implements Evaluatable {

    /** Enum for threads handling **/
    private enum THREAD {
        EQUALIZER,
        FILTER
    }

    private static final int NUM_OF_FILTERS = 8;
    private int         smplOnce;

    private Filter[]     filters;

    private short[]       sample;
    private short[]   samplesRes;

    private double[]        gain;

    private static final double minGain = Math.pow(10, -70.0 / 20);

    private int    numCalculated; // Number of filters completed calculations
    private boolean      endWork; // Flag: 1 - complete work, 0 - continue work

    public Equalizer(int smplOnce) {

        this.smplOnce = smplOnce;

        filters    = new Filter[NUM_OF_FILTERS];
        filters[0] = new Filter(Coefs.filt1.length - 1, Coefs.filt1, smplOnce);
        filters[1] = new Filter(Coefs.filt2.length - 1, Coefs.filt2, smplOnce);
        filters[2] = new Filter(Coefs.filt3.length - 1, Coefs.filt3, smplOnce);
        filters[3] = new Filter(Coefs.filt4.length - 1, Coefs.filt4, smplOnce);
        filters[4] = new Filter(Coefs.filt5.length - 1, Coefs.filt5, smplOnce);
        filters[5] = new Filter(Coefs.filt6.length - 1, Coefs.filt6, smplOnce);
        filters[6] = new Filter(Coefs.filt7.length - 1, Coefs.filt7, smplOnce);
        filters[7] = new Filter(Coefs.filt8.length - 1, Coefs.filt8, smplOnce);

        sample     = new short[2 * smplOnce];
        samplesRes = new short[NUM_OF_FILTERS * 2 * smplOnce];

        gain       = new double[NUM_OF_FILTERS];
        for (int i = 0; i < NUM_OF_FILTERS; i++)
            gain[i] = 1;

        endWork       = false;
        numCalculated = 0;

        for (int i = 0; i < NUM_OF_FILTERS; i++) {
            int index = i;
            new Thread(()->{
                filterWork(index);
            }).start();
        }
    }

    private void filterWork(int index) {
        Filter filter = filters[index];
        short[]  smpl = new short[2 * smplOnce];
        int  memIndex = index * smplOnce * 2;
        report(THREAD.FILTER);

        while (true) {
            if (endWork) return;

            smpl = filter.evaluate(this.sample);
            System.arraycopy(smpl, 0, samplesRes, memIndex, smpl.length);

            report(THREAD.FILTER);
        }
    }

    @Override
    public short[] evaluate(short[] sample) {

        this.sample = sample;
        report(THREAD.EQUALIZER);

        short[] smpl = new short[2 * smplOnce];
        int memIndex = 0;

        for (int i = 0; i < NUM_OF_FILTERS; i++) {
            memIndex = i * smplOnce * 2;
            for (int j = 0; j < smpl.length; j++)
                smpl[j] += samplesRes[memIndex + j] * gain[i];
        }

        return smpl;
    }

    public void close() {
        endWork = true;
    }

    public int getNumCalculated() {
        return numCalculated;
    }

    private void report(THREAD who) {
        try {
            switch (who) {

                case EQUALIZER:

                    numCalculated = 0;
                    filtersRoom(THREAD.EQUALIZER);
                    equalizerRoom(THREAD.EQUALIZER);

                    break;

                case FILTER:

                    incNumCalc();
                    if (numCalculated == NUM_OF_FILTERS)
                        equalizerRoom(THREAD.FILTER);

                    filtersRoom(THREAD.FILTER);

                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setGain(int index, double value) {
        if (value > 1) {
            gain[index] = 1;
            System.out.println(
                    "Error: get wrong slider value.\n" +
                    "Gained: " + value + '\n' +
                    "Replaced with: " + 1);
            return;
        }

        if (value < minGain) {
            gain[index] = minGain;
            System.out.println(
                    "Error: get wrong slider value.\n" +
                    "Gained: " + value + '\n' +
                    "Replaced with: " + minGain);
            return;
        }

        gain[index] = value;
    }

    public static int getNumOfFilters() {
        return NUM_OF_FILTERS;
    }

    /** Functions for handling work time of threads of equalizer and its filters **/
    private synchronized void filtersRoom(THREAD who) throws InterruptedException {
        switch (who) {
            case EQUALIZER -> notifyAll();
            case    FILTER -> wait();
        }
    }

    private synchronized void equalizerRoom(THREAD who) throws InterruptedException {
        switch (who) {
            case EQUALIZER -> wait();
            case    FILTER -> notify();
        }
    }

    private synchronized void incNumCalc() {
        numCalculated++;
    }
}

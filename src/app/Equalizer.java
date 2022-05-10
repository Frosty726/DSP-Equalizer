package app;

import app.coefs.Coefs;

public class Equalizer implements Evaluatable {

    private static final int NUM_OF_FILTERS = 8;

    private Filter[]     filters;

    private short[]       sample;
    private short[]   samplesRes;

    private int      numOfThread;
    private boolean      endWork;

    public Equalizer() {

        filters = new Filter[NUM_OF_FILTERS];
        filters[0] = new Filter(Coefs.filt1.length - 1, Coefs.filt1);
        filters[1] = new Filter(Coefs.filt2.length - 1, Coefs.filt2);
        filters[2] = new Filter(Coefs.filt3.length - 1, Coefs.filt3);
        filters[3] = new Filter(Coefs.filt4.length - 1, Coefs.filt4);
        filters[4] = new Filter(Coefs.filt5.length - 1, Coefs.filt5);
        filters[5] = new Filter(Coefs.filt6.length - 1, Coefs.filt6);
        filters[6] = new Filter(Coefs.filt7.length - 1, Coefs.filt7);
        filters[7] = new Filter(Coefs.filt8.length - 1, Coefs.filt8);

        samplesRes = new short[NUM_OF_FILTERS * 2];

        endWork      = false;
        numOfThread  = 0;

        for (int i = 0; i < NUM_OF_FILTERS; i++) {
            int index = i;
            new Thread(()->{
                filterWork(index);
            }).start();
        }
    }

    private synchronized void report() {
        try {
            numOfThread++;

            if (numOfThread < NUM_OF_FILTERS + 1) {
                wait();
            }
            else {
                numOfThread = 1;
                notifyAll();
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void filterWork(int index) {
        Filter filter = filters[index];
        short[]  smpl = new short[2];
        report();

        while (true) {
            if (endWork) return;
            report();

            smpl = filter.evaluate(this.sample);
            samplesRes[index * 2    ] = smpl[0];
            samplesRes[index * 2 + 1] = smpl[1];

            report();
        }
    }

    @Override
    public short[] evaluate(short[] sample) {
        this.sample = sample;
        report();

        for (int i = 0; i < sample.length; i++)
            this.sample[i] = 0;

        for (int i = 0; i < NUM_OF_FILTERS; i++) {
            this.sample[0] += samplesRes[2 * i    ];
            this.sample[1] += samplesRes[2 * i + 1];
        }

        return this.sample;
    }

    public void close() {
        endWork = true;
    }
}

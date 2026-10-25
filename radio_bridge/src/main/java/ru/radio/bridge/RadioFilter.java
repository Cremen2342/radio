package ru.radio.bridge;

import java.util.Random;

/** Light "radio" voice effect: band-pass + gentle soft clip + faint hiss. One instance per speaking session (keeps filter state). */
public final class RadioFilter {
    private static final double SAMPLE_RATE = 48000.0;

    private final RadioConfig cfg;
    private final Biquad highpass;
    private final Biquad lowpass;
    private final Random rnd = new Random();
    private final double driveNorm;

    public RadioFilter(RadioConfig cfg) {
        this.cfg = cfg;
        this.highpass = Biquad.highpass(cfg.highpassHz, SAMPLE_RATE, 0.707);
        this.lowpass = Biquad.lowpass(cfg.lowpassHz, SAMPLE_RATE, 0.707);
        this.driveNorm = Math.tanh(cfg.drive);
    }

    /** Processes one 20 ms mono frame in place. */
    public void process(short[] pcm) {
        for (int i = 0; i < pcm.length; i++) {
            double x = pcm[i] / 32768.0;
            x = highpass.process(x);
            x = lowpass.process(x);
            x = Math.tanh(x * cfg.drive) / driveNorm;
            x *= cfg.gain;
            x += (rnd.nextDouble() * 2.0 - 1.0) * cfg.noise;
            if (x > 1.0) x = 1.0; else if (x < -1.0) x = -1.0;
            pcm[i] = (short) Math.round(x * 32767.0);
        }
    }

    /** RBJ cookbook biquad, transposed direct form II. */
    static final class Biquad {
        private final double b0, b1, b2, a1, a2;
        private double z1, z2;

        private Biquad(double b0, double b1, double b2, double a1, double a2) {
            this.b0 = b0; this.b1 = b1; this.b2 = b2; this.a1 = a1; this.a2 = a2;
        }

        static Biquad lowpass(double fc, double fs, double q) {
            double w = 2 * Math.PI * fc / fs, c = Math.cos(w), alpha = Math.sin(w) / (2 * q), a0 = 1 + alpha;
            return new Biquad((1 - c) / 2 / a0, (1 - c) / a0, (1 - c) / 2 / a0, -2 * c / a0, (1 - alpha) / a0);
        }

        static Biquad highpass(double fc, double fs, double q) {
            double w = 2 * Math.PI * fc / fs, c = Math.cos(w), alpha = Math.sin(w) / (2 * q), a0 = 1 + alpha;
            return new Biquad((1 + c) / 2 / a0, -(1 + c) / a0, (1 + c) / 2 / a0, -2 * c / a0, (1 - alpha) / a0);
        }

        double process(double x) {
            double y = b0 * x + z1;
            z1 = b1 * x - a1 * y + z2;
            z2 = b2 * x - a2 * y;
            return y;
        }
    }
}

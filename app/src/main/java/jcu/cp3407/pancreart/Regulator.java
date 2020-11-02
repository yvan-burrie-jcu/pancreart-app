package jcu.cp3407.pancreart;

import java.util.*;

public class Regulator extends Task {

    Regulator(Timer timer, double speed) {
        super(timer, 60 * 10 / Simulator.DELAY_RATE, speed);
    }

    @Override
    public void run() {
        handler.onRegulateDosage();
    }
}

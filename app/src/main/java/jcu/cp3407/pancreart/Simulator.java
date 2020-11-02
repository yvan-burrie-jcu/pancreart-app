package jcu.cp3407.pancreart;

import java.util.*;

public class Simulator extends Task {

    final static boolean LOG_ENABLED = true;

    final static long DELAY_RATE = 1;

    ArrayList<Routine> routines = new ArrayList<>();

    static abstract class Routine {

        String name;

        long start;

        long duration;

        double gravity; // There are 4 calories in 1g of glucose.

        double rate = 0;

        Routine(String name, double startHour, double durationHours) {
            this.name = name;
            this.start = (long) (startHour * 60 * 60);
            this.duration = (long) (durationHours * 60 * 60);
        }

        double calculatePeakPercent(long now) {
            double difference = 0;
            if (now >= start) {
                long end = start + duration;
                if (now < end) {
                    long whence = now - start;
                    long half = duration / 2;
                    if (whence > half) {
                        difference = duration - whence;
                    } else {
                        difference = whence;
                    }
                }
            }
            return 200d / duration * difference;
        }
    }

    static class Meal extends Routine {

        Meal(String name, double startHour, double durationHours) {
            super(name, startHour, durationHours);
            rate = +50;
        }
    }

    static class Exercise extends Routine {

        Exercise(String name, double startHour, double durationHours) {
            super(name, startHour, durationHours);
            rate = -30;
        }
    }

    static class Stress extends Routine {

        Stress(String name, double startHour, double durationHours) {
            super(name, startHour, durationHours);
            rate = -20;
        }
    }

    static class Sleep extends Routine {

        Sleep(String name, double startHour, double durationHours) {
            super(name, startHour, durationHours);
            rate = -10;
        }
    }

    BatteryPowerTask batteryPowerTask;

    static class BatteryPowerTask extends Task {

        final static long DELAY = 10 / DELAY_RATE;

        double percent = 100;

        boolean charging = false;

        final double CHARGE_RATE = 0.1;

        final double CONSUMPTION_RATE = -0.01;

        final int LOW_PERCENT = 10;

        boolean lowPercentNotice = false;

        BatteryPowerTask(Timer timer, double speed) {
            super(timer, DELAY, speed);
        }

        @Override
        public void run() {
            if (LOG_ENABLED) {
                System.out.println(" - Running Battery Power Task: " + percent + "%");
            }
            if (percent > 0) {
                percent += CONSUMPTION_RATE;
                if (percent < 0) {
                    percent = 0;
                }
                if (percent < 100 && charging) {
                    percent += CHARGE_RATE;
                }
                if (lowPercentNotice) {
                    if (percent > LOW_PERCENT) {
                        lowPercentNotice = false;
                    }
                } else {
                    if (percent <= LOW_PERCENT) {
                        lowPercentNotice = true;
                        handler.onBatteryPowerLow(percent);
                    }
                }
            }
        }
    }

    GlucoseDetectorTask glucoseDetectorTask;

    class GlucoseDetectorTask extends Task {

        final static long DELAY = 60 * 5 / DELAY_RATE;

        double amount = 120;

        GlucoseDetectorTask(Timer timer, double speed) {
            super(timer, DELAY, speed);
        }

        @Override
        public void run() {
            if (LOG_ENABLED) {
                System.out.println(" - Running Glucose Detector Task: " + amount);
            }
            if (Simulator.this.batteryPowerTask.percent > 0) {
                handler.onGlucoseDetected(amount);
            }
        }
    }

    InsulinInjectorTask insulinInjectorTask;

    class InsulinInjectorTask extends Task {

        final static long DELAY = 60 * 10 / DELAY_RATE;

        double reservoirAmount = 2000;

        boolean reservoirRemoved = false;

        boolean reservoirRemovedNotice = false;

        boolean reservoirLowNotice = false;

        final static double RESERVOIR_LOW = 20;

        InsulinInjectorTask(Timer timer, double speed) {
            super(timer, DELAY, speed);
        }

        @Override
        public void run() {
            if (LOG_ENABLED) {
                System.out.println(" - Running Insulin Injector Task: " + reservoirAmount);
            }
            if (Simulator.this.batteryPowerTask.percent > 0) {
                if (reservoirRemoved) {
                    if (reservoirRemovedNotice) {
                        return;
                    }
                    handler.onInsulinReservoirRemoved();
                    reservoirRemovedNotice = true;
                    return;
                }
                if (reservoirRemovedNotice) {
                    reservoirRemovedNotice = false;
                }
                if (reservoirLowNotice) {
                    if (reservoirAmount > RESERVOIR_LOW) {
                        reservoirLowNotice = false;
                    }
                }
                if (reservoirAmount <= RESERVOIR_LOW) {
                    // todo: notice
                    handler.onInsulinReservoirLow();
                    reservoirLowNotice = true;
                }
            }
        }

        /**
         * Simulate an attempt to inject a dose of insulin.
         */
        public void dose(double amount) {
            if (Simulator.this.batteryPowerTask.percent > 0) {
                if (reservoirAmount >= amount) {
                    reservoirAmount -= amount;
                    Simulator.this.insulinDisperserTask.amount += amount;
                    handler.onInsulinInjected(amount);
                }
            }
        }

        /**
         * Simulate a human trying to refill the reservoir.
         */
        public void refill(double amount) {
//            try {
                long oneSecond = (long) (speed * 1000);
//                wait(oneSecond * 20); // wait for 20 seconds - the time it takes for a human to refill
                reservoirAmount = amount;
//            } catch (InterruptedException ignored) {
//            }
        }
    }

    InsulinDisperserTask insulinDisperserTask;

    class InsulinDisperserTask extends Task {

        final static long DELAY = 10 / DELAY_RATE;

        /**
         * The amount of insulin in the blood stream that has not been used.
         */
        double amount;

        final static double UNIT = 34.7; // One unit reduces glucose by 50 ml

        InsulinDisperserTask(Timer timer, double speed) {
            super(timer, DELAY, speed);
        }

        @Override
        public void run() {
            if (LOG_ENABLED) {
                System.out.println(" - Running Insulin Disperser Task: " + amount);
            }
            if (amount >= UNIT / 100) {
                amount -= UNIT / 100;
                // Simulate insulin in blood lowering glucose
//                Simulator.this.glucoseDetectorTask.amount -= 0.005;
                if (Simulator.this.glucoseDetectorTask.amount < 0) {
                    Simulator.this.glucoseDetectorTask.amount = 0;
                }
            }
        }
    }

    long days = 1;

    Random random = new Random();

    final static double[] GLUCOSE_DECREMENT = {
            -0.050,
            -0.075,
            -0.100,
    };

    long currentTime = 0;

    Simulator(Timer timer, double speed) {
        super(timer, 60, speed);
        // Register sub-tasks
        registerTask(batteryPowerTask = new BatteryPowerTask(timer, speed));
        registerTask(glucoseDetectorTask = new GlucoseDetectorTask(timer, speed));
        registerTask(insulinInjectorTask = new InsulinInjectorTask(timer, speed));
        registerTask(insulinDisperserTask = new InsulinDisperserTask(timer, speed));
    }

    @Override
    public void run() {
        // Update 24 hours
        if (currentTime > 24 * 60 * 60) {
            currentTime = 0;
            days += 1;
        }
        long hours = currentTime / 3600;
        long minutes = (currentTime - (hours * 3600)) / 60;
        long second = currentTime - (hours * 3600) - (minutes * 60);
        System.out.println(" - Running Simulator: " + hours + ":" + minutes + ":" + second);

        // Go through each routine and run them if they are active
        for (Routine routine : routines) {
            // Modify glucose amount according to the routine
            double glucoseChange = routine.calculatePeakPercent(currentTime) * routine.rate / 1000;
//            System.out.println(" - Glucose Change: " + glucoseChange);
            glucoseDetectorTask.amount += glucoseChange;
        }

        // Regardless of anything, glucose always drops with some randomness
        double value = random.nextFloat() * (GLUCOSE_DECREMENT[2] - GLUCOSE_DECREMENT[0]) + GLUCOSE_DECREMENT[0];
//        System.out.println("Value: " + value);
        glucoseDetectorTask.amount += value;
//        glucoseDetectorTask.amount += GLUCOSE_DECREMENT[1];

        currentTime += interval;
    }
}

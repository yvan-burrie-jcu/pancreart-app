package jcu.cp3407.pancreart;

public class PodHandler {

    public void onBatteryPowerLow(double percent) {
        System.out.println("Battery power low! " + percent + "%");
    }

    public void onGlucoseDetected(double amount) {
        System.out.println("Glucose detected! " + amount);
    }

    public void onInsulinInjected(double amount) {
        System.out.println("Insulin injected! " + amount);
    }

    public void onInsulinReservoirLow() {
        System.out.println("Insulin reservoir low!");
    }

    public void onInsulinReservoirRemoved() {
        System.out.println("Insulin reservoir removed!");
    }

    public void onRegulateDosage() {
        System.out.println("Regulating dosage!");
    }
}

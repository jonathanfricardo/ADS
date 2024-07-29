package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTracker2 {

    private final static String VAULT_NAME = "/2023-09";
    TrafficTracker trafficTracker;

    Car scoda;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");

        scoda = new Car("27-IP-IX", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014, 1, 31));
    }

    @Test
    public void checkCalculateTotalFines() {
        assertEquals(5610.00, trafficTracker.calculateTotalFines());
    }

    @Test
    public void checkTopViolationsByCar() {
        Violation violation1 = new Violation(scoda, "Amsterdam");

        violation1.setOffencesCount(56);

        assertEquals(violation1.getOffencesCount(),
                trafficTracker.topViolationsByCar(5).get(0).getOffencesCount());
    }

    @Test
    public void checkTopViolationsByCity() {
        Violation violation1 = new Violation(scoda, "Amsterdam");

        violation1.setOffencesCount(56);

        assertEquals(violation1.getCity(),
                trafficTracker.topViolationsByCity(5).get(0).getCity());
    }
}

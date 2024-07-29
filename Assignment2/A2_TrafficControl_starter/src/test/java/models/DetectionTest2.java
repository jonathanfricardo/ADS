package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class DetectionTest2 {
    Car scoda, audi, bmw, mercedes, icova, volvo1, volvo2, daf1, daf2, kamaz;
    List<Car> cars;

    Violation violation1, violation2;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        scoda = new Car("1-AAA-02", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014, 1, 31));
        audi = new Car("AA-11-BB", 4, Car.CarType.Car, Car.FuelType.Diesel, LocalDate.of(1998, 1, 31));
        mercedes = new Car("VV-11-BB", 4, Car.CarType.Van, Car.FuelType.Diesel, LocalDate.of(1998, 1, 31));
        bmw = new Car("A-123-BB", 4, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2019, 1, 31));
        icova = new Car("1-TTT-99", 5, Car.CarType.Truck, Car.FuelType.Lpg, LocalDate.of(2011, 1, 31));
        volvo1 = new Car("1-TTT-01", 5, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2009, 1, 31));
        volvo2 = new Car("1-TTT-02", 6, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2011, 1, 31));
        daf1 = new Car("1-CCC-01", 5, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2009, 1, 31));
        daf2 = new Car("1-CCC-02", 6, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2011, 1, 31));
        kamaz = new Car("1-AAAA-0000");
        cars = new ArrayList<>(List.of(scoda, audi, mercedes, bmw, icova, volvo1, volvo2, daf1, daf2, kamaz));

        violation1 = new Violation(daf1, "Amsterdam");
        violation2 = new Violation(icova, "Rotterdam");
    }

    @Test
    public void canDetectPurpleValidation() {
        Detection detection1 = Detection.fromLine("1-CCC-01,Amsterdam,2022-10-01T12:11:10", cars);
        Detection detection2 = Detection.fromLine("1-TTT-99, Rotterdam, 2022-10-01T12:11:10", cars);
        Detection detection3 = Detection.fromLine("1-AAA-02, Rotterdam, 2022-10-01T12:11:10", cars);

        //check if violation is made and detected
        assertNotNull(detection1.validatePurple());
        assertNotNull(detection2.validatePurple());
        assertNull(detection3.validatePurple());

        //check if cars in violation are the same as declared
        assertSame(daf1, detection1.validatePurple().getCar());
        assertSame(icova, detection2.validatePurple().getCar());

        //check if cities in violation are the same as declared
        assertEquals("Amsterdam", detection1.validatePurple().getCity());
        assertEquals("Rotterdam", detection2.validatePurple().getCity());
    }

    @Test
    public void checkHigherEmissionCatecoryForTruckAndCoach() {
        Detection detection1 = Detection.fromLine("1-TTT-02,Amsterdam,2022-10-01T12:11:10", cars);
        Detection detection2 = Detection.fromLine("1-CCC-02, Rotterdam, 2022-10-01T12:11:10", cars);

        //checks if truck or coach don't get a violation
        assertNull(detection1.validatePurple());
        assertNull(detection2.validatePurple());
    }

    @Test
    public void checkLowerEmissionCatecoryForCars() {
        Detection detection1 = Detection.fromLine("AA-11-BB,Amsterdam,2022-10-01T12:11:10", cars);
        Detection detection2 = Detection.fromLine("VV-11-BB, Rotterdam, 2022-10-01T12:11:10", cars);

        //checks if other type of cars don't get a violation
        assertNull(detection1.validatePurple());
        assertNull(detection2.validatePurple());
    }

}

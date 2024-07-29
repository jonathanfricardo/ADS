package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static models.Car.CarType;
import static models.Car.FuelType;

public class Detection {
    private final Car car;                  // the car that was detected
    private final String city;              // the name of the city where the detector was located
    private final LocalDateTime dateTime;   // date and time of the detection event

    /* Representation Invariant:
     *      every Detection shall be associated with a valid Car
     */

    public Detection(Car car, String city, LocalDateTime dateTime) {
        this.car = car;
        this.city = city;
        this.dateTime = dateTime;
    }

    /**
     * Parses detection information from a line of text about a car that has entered an environmentally controlled zone
     * of a specified city.
     * the format of the text line is: lisensePlate, city, dateTime
     * The licensePlate shall be matched with a car from the provided list.
     * If no matching car can be found, a new Car shall be instantiated with the given lisensePlate and added to the list
     * (besides the license plate number there will be no other information available about this car)
     *
     * @param textLine
     * @param cars     a list of known cars, ordered and searchable by licensePlate
     *                 (i.e. the indexOf method of the list shall only consider the lisensePlate when comparing cars)
     * @return a new Detection instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Detection fromLine(String textLine, List<Car> cars) {
        Detection newDetection;

        // TODO convert the information in the textLine into a new Detection instance
        //  use the cars.indexOf to find the car that is associated with the licensePlate of the detection
        //  if no car can be found a new Car shall be instantiated and added to the list and associated with the detection

        //split the given textLine into an array of 3 strings
        String[] parts = textLine.split(",");

        //assign all 3 values
        String licensePlate = parts[0].trim();

        String city = parts[1].trim();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String time = parts[2].trim();
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

        //check if textLine is correct / right format
        if (city.length() < 1 || time.length() < 1) {
            return null;
        }

        //creates temporary car
        Car newCar = new Car(licensePlate);
        int indexOfNewCar = cars.indexOf(newCar);

        //create a new Detection if car was found
        if (indexOfNewCar != -1) {
            newDetection = new Detection(cars.get(indexOfNewCar), city, dateTime);
        } else {
            //if car is not in the list, create a new car.
            cars.add(newCar);
            newDetection = new Detection(newCar, city, dateTime);
        }

        return newDetection;
    }

    /**
     * Validates a detection against the purple conditions for entering an environmentally restricted zone
     * I.e.:
     * Diesel trucks and diesel coaches with an emission category of below 6 may not enter a purple zone
     *
     * @return a Violation instance if the detection saw an offence against the purple zone rule/
     * null if no offence was found.
     */
    public Violation validatePurple() {
        // TODO validate that diesel trucks and diesel coaches have an emission category of 6 or above

        Car car = this.getCar();

        //Checks if the emission is valid, and the car type.
        if (car.getEmissionCategory() < 6 && (car.getCarType().equals(CarType.Truck) || car.getCarType().equals(CarType.Coach))) {
            //returns a new violation
            return new Violation(car, city);
        } else {
            return null;
        }
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


    @Override
    public String toString() {
        // TODO represent the detection in the format: licensePlate/city/dateTime

        return car.getLicensePlate() + "/" + city + "/" + dateTime;
    }

}

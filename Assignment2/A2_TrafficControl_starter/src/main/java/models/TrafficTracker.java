package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class TrafficTracker {
    private final String TRAFFIC_FILE_EXTENSION = ".txt";
    private final String TRAFFIC_FILE_PATTERN = ".+\\" + TRAFFIC_FILE_EXTENSION;

    private OrderedList<Car> cars;                  // the reference list of all known Cars registered by the RDW
    private OrderedList<Violation> violations;      // the accumulation of all offences by car and by city

    public TrafficTracker() {

        //Creates an empty OrderedArrayList with a new comparator for the cars
        this.cars = new OrderedArrayList<>(Comparator.comparing(Car::getLicensePlate));

        //Creates an empty OrderedArrayList for the violations
        this.violations = new OrderedArrayList<>((o1, o2) -> {

            if (o1 != null && o2 != null) {
                //compares the cars of the violation
                int carComparison = o1.getCar().compareTo(o2.getCar());

                if (carComparison == 0) {
                    //returns the city if the cars match
                    return o1.getCity().compareTo(o2.getCity());
                }
                return carComparison;
            }

            return -1;
        });
    }

    /**
     * imports all registered cars from a resource file that has been provided by the RDW
     *
     * @param resourceName
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();

        // load all cars from the text file
        int numberOfLines = importItemsFromFile(this.cars,
                createFileFromURL(TrafficTracker.class.getResource(resourceName)),
                Car::fromLine);

        // sort the cars for efficient later retrieval
        this.cars.sort();

        System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * imports and merges all raw detection data of all entry gates of all cities from the hierarchical file structure of the vault
     * accumulates any offences against purple rules into this.violations
     *
     * @param resourceName
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences =
                this.mergeDetectionsFromVaultRecursively(
                        createFileFromURL(TrafficTracker.class.getResource(resourceName)));

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * traverses the detections vault recursively and processes every data file that it finds
     *
     * @param file
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            //  retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);


            //loops through all files of the files in the current directory and eventually adds up all the number of offences
            for (File value : filesInDirectory) {
                totalNumberOfOffences += this.mergeDetectionsFromVaultRecursively(value);
            }


        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw detection files
            // process the content of this file and merge the offences found into this.violations
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }


        return totalNumberOfOffences;
    }

    /**
     * imports another batch detection data from the filePath text file
     * and merges the offences into the earlier imported and accumulated violations
     *
     * @param file
     */
    private int mergeDetectionsFromFile(File file) {

        // re-sort the accumulated violations for efficient searching and merging
        this.violations.sort();

        // use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        //create a scanner to read the given file
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //loop through all the lines until the file is completely read
        while (scanner.hasNext()) {
            //adds a new detection to the array
            newDetections.add(Detection.fromLine(scanner.nextLine(), this.cars));

        }

        System.out.printf("Imported %d detections from %s.\n", newDetections.size(), file.getPath());

        int totalNumberOfOffences = 0; // tracks the number of offences that emerges from the data in this file

        //loops through all detections
        for (Detection detection : newDetections) {
            if (detection != null) {
                //check if the detection is violating anything.
                Violation violation = detection.validatePurple();
                if (violation != null) {
                    //adds the violation to the existing array of violations, and merging the offences count
                    this.violations.merge(violation, (Violation::combineOffencesCounts));

                    totalNumberOfOffences++;
                }
            }

        }


        return totalNumberOfOffences;
    }

    /**
     * calculates the total revenue of fines from all violations,
     * Trucks pay €25 per offence, Coaches €35 per offence
     *
     * @return the total amount of money recovered from all violations
     */
    public double calculateTotalFines() {

        return this.violations.aggregate(
                // TODO provide a calculator function for the specified fine scheme
                //  of €25 per truck-offence and €35 per coach-offence

                //checks if its a truck a coach or something else and returns right amount of €
                violation -> {
                    if (violation.getCar().getCarType().equals(Car.CarType.Truck)) {
                        return 25.0 * violation.getOffencesCount();
                    } else if (violation.getCar().getCarType().equals(Car.CarType.Coach)) {
                        return 35.0 * violation.getOffencesCount();
                    } else return 0.0;
                }
        );
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by car across all cities.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCar(int topNumber) {

        // TODO merge all violations from this.violations into a new OrderedArrayList
        //   which orders and aggregates violations by city
        // TODO sort the new list by decreasing offencesCount.
        // TODO use .subList to return only the topNumber of violations from the sorted list
        //  (You may want to prepare/reuse a local private method for all this)
        
        //aggregated list
        List<Violation> aggregatedViolationsByCar = new ArrayList<>();

        //sort violations by city
        violations.sort(Comparator.comparing(Violation::getCar));

        Car currentCar = null;
        int offencesCount = 0;

        //loops through all violations
        for (Violation violation : violations) {
            //checks if this violations.city doesn't matches with current city
            if(!violation.getCar().equals(currentCar)) {
                //add new city in the list
                if (currentCar != null) {
                    aggregatedViolationsByCar.add(new Violation(currentCar, null, offencesCount));
                }

                //resets count for new city
                currentCar = violation.getCar();
                offencesCount = 0;
            }

            //aggregates offences per city.
            offencesCount += violation.getOffencesCount();
        }

        // Add the last aggregated violation
        if (currentCar != null) {
            aggregatedViolationsByCar.add(new Violation(currentCar, null, offencesCount));
        }

        aggregatedViolationsByCar.sort((v1, v2) -> Integer.compare(v2.getOffencesCount(), v1.getOffencesCount()));

        return aggregatedViolationsByCar.subList(0, topNumber);

    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by city across all cars.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCity(int topNumber) {
        // TODO merge all violations from this.violations into a new OrderedArrayList
        //   which orders and aggregates violations by Car
        // TODO sort the new list by decreasing offencesCount.
        // TODO use .subList to return only the topNumber of violations from the sorted list
        //  (You may want to prepare/reuse a local private method for all this)


        //aggregated list
        List<Violation> aggregatedViolationsByCity = new ArrayList<>();

        //sort violations by city
        violations.sort(Comparator.comparing(Violation::getCity));

        String currentCity = null;
        int offencesCount = 0;

        //loops through all violations
        for (Violation violation : violations) {
            //checks if this violations.city doesn't matches with current city
            if(!violation.getCity().equals(currentCity)) {
                //add new city in the list
                if (currentCity != null) {
                    aggregatedViolationsByCity.add(new Violation(null, currentCity, offencesCount));
                }

                //resets count for new city
                currentCity = violation.getCity();
                offencesCount = 0;
            }

            //aggregates offences per city.
            offencesCount += violation.getOffencesCount();
        }

        // Add the last aggregated violation
        if (currentCity != null) {
            aggregatedViolationsByCity.add(new Violation(null, currentCity, offencesCount));
        }

        aggregatedViolationsByCity.sort((v1, v2) -> Integer.compare(v2.getOffencesCount(), v1.getOffencesCount()));

        return aggregatedViolationsByCity.subList(0, topNumber);
    }

    /**
     * Sorts given list from highest offencescount to lowest offencescount
     * @param topNumber
     * @param mergedViolations
     * @return subList of original list
     */
    private List<Violation> sortTopViolations(int topNumber, OrderedArrayList<Violation> mergedViolations){

        //adds all violations to given list
        mergedViolations.addAll(this.violations);

        //sorts given list on offencecount
        mergedViolations.sort((violation1, violation2) ->
                Integer.compare(violation2.getOffencesCount(), violation1.getOffencesCount()));

        //returns specified top of array
        return mergedViolations.subList(0, topNumber);
    }


    /**
     * imports a collection of items from a text file which provides one line for each item
     *
     * @param items     the list to which imported items shall be added
     * @param file      the source text file
     * @param converter a function that can convert a text line into a new item instance
     * @param <E>       the (generic) type of each item
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);

        // read all source lines from the scanner,
        // convert each line to an item of type E
        // and add each successfully converted item into the list
        while (scanner.hasNext()) {
            // input another line with author information
            String line = scanner.nextLine();
            numberOfLines++;

            //converts the line to an instance of E

            E e = converter.apply(line);

            //adds a successfully converted item to the list of items
            items.add(numberOfLines - 1, e);

        }

        return numberOfLines;
    }

    /**
     * helper method to create a scanner on a file and handle the exception
     *
     * @param file
     * @return
     */
    private static Scanner createFileScanner(File file) {
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + file.getPath());
        }
    }

    private static File createFileFromURL(URL url) {
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error found on URL: " + url.getPath());
        }
    }

    public OrderedList<Car> getCars() {
        return this.cars;
    }

    public OrderedList<Violation> getViolations() {
        return this.violations;
    }
}

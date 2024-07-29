package models;

public class Violation {
    private final Car car;
    private final String city;
    private int offencesCount;

    public Violation(Car car, String city) {
        this.car = car;
        this.city = city;
        this.offencesCount = 1;
    }

    public Violation(Car car, String city, int offencesCount) {
        this.car = car;
        this.city = city;
        this.offencesCount = offencesCount;
    }

    public static int compareByLicensePlateAndCity(Violation v1, Violation v2) {
        // TODO compute the sort order of v1 vs v2 as per conventions of Comparator<Violation>

        //check if licenseplates are equal
        int check = v1.car.getLicensePlate().compareTo(v2.car.getLicensePlate());

        //if licenseplates are not equal retun check, else compare the cities and return the result
        if (check != 0){
            return check;
        } else return v1.city.compareTo(v2.city);
    }



    /**
     * Aggregates this violation with the other violation by adding their counts and
     * nullifying identifying attributes car and/or city that do not match
     * identifying attributes that match are retained in the result.
     * This method can be used for aggregating violations applying different grouping criteria
     * @param other
     * @return  a new violation with the accumulated offencesCount and matching identifying attributes.
     */
    public Violation combineOffencesCounts(Violation other) {
        Violation combinedViolation = new Violation(
                // nullify the car attribute iff this.car does not match other.car
                this.car != null && this.car.equals(other.car) ? this.car : null,
                // nullify the city attribute iff this.city does not match other.city
                this.city != null && this.city.equals(other.city) ? this.city : null);

        // add the offences counts of both original violations
        combinedViolation.setOffencesCount(this.offencesCount + other.offencesCount);

        return combinedViolation;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public int getOffencesCount() {
        return offencesCount;
    }

    public void setOffencesCount(int offencesCount) {
        this.offencesCount = offencesCount;
    }

    // TODO represent the violation in the format: licensePlate/city/offencesCount
    @Override
    public String toString() {

        if (car == null) {
            return "null/" + city + "/" + offencesCount;
        }

        if (city == null) {
            return car.getLicensePlate() + "/null/" + offencesCount;
        }



        return car.getLicensePlate() + "/" + city + "/" + offencesCount;
    }
}

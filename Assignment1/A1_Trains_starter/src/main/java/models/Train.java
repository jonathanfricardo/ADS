package models;

import javax.lang.model.util.ElementScanner6;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /**
     * Indicates whether the train has at least one connected Wagon
     *
     * @return
     */
    public boolean hasWagons() {
        return firstWagon != null;

    }

    /**
     * A train is a passenger train when its first wagon is a PassengerWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return
     */
    public boolean isPassengerTrain() {
        return firstWagon instanceof PassengerWagon;
    }

    /**
     * A train is a freight train when its first wagon is a FreightWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return
     */
    public boolean isFreightTrain() {
        return firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        if (!hasWagons()) {
            return 0;
        } else {
            return firstWagon.getSequenceLength();
        }
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        if (this.hasWagons()) {
            return firstWagon.getLastWagonAttached();
        } else {
            return null;
        }
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        Wagon wagon = this.firstWagon;
        int totalNumberOfSeats = 0;

        // check if there are any wagons
        if (wagon == null) {
            return 0;
        }

        // checks if the train is a passenger train
        if (isPassengerTrain()) {
            int totalNumberOfWagons = this.getNumberOfWagons();

            // loops through all wagons and combines the number of seats of all wagons into variable
            for (int i = 0; i < totalNumberOfWagons; i++) {
                totalNumberOfSeats += ((PassengerWagon) wagon).getNumberOfSeats();
                wagon = wagon.getNextWagon();
            }
            return totalNumberOfSeats;
        } else {
            return 0;
        }
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        Wagon wagon = this.firstWagon;
        int totalWeight = 0;

        // check if there are any wagons
        if (wagon == null) {
            return 0;
        }

        // checks if the train is a freight train
        if (isFreightTrain()) {
            int totalNumberOfWagons = this.getNumberOfWagons();
            // loops through all wagons and combines the weight of all wagons into variable
            for (int i = 0; i < totalNumberOfWagons; i++) {
                totalWeight += ((FreightWagon) wagon).getMaxWeight();
                wagon = wagon.getNextWagon();
            }
            return totalWeight;
        } else {
            return 0;
        }
    }

    /**
     * Finds the wagon at the given position (starting at 0 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        int i = 0;
        Wagon wagon = this.firstWagon;

        // checks if there are any wagons or wagons at negative position
        if (wagon == null || position < 0) {
            return null;
        }

        // checks if train only has one wagon, if so it returns the first wagon
        if (position == 0) {
            return this.firstWagon;
        } else {
            // loops through all wagons until the wagon at 'position' is found or no wagon is found at 'position'
            while (wagon.hasNextWagon()) {
                wagon = wagon.getNextWagon();
                i++;
                // checks after 'wagon' was altered because position 0 is already checked
                if (position == i) {
                    return wagon;
                }
                // if the iterator is equal to the number of wagons there are no wagons left so return null
                else if (i > this.getNumberOfWagons()) {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon wagon = this.firstWagon;

        // check if there are any wagons
        if (wagon == null) {
            return null;
        }

        int totalNumberOfWagons = this.getNumberOfWagons();

        // loops through all wagons until a wagon's id matches with paramater or no wagons are left
        for (int i = 0; i < totalNumberOfWagons; i++) {
            // if id's match it returns the wagon
            if (wagon.id == wagonId) {
                return wagon;
            } else {
                wagon = wagon.getNextWagon();
            }
        }
        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        Wagon currentWagon = this.firstWagon;

        // checks if wagon is null
        if (wagon == null) {
            return false;
        }

        // checks if the current wagon is null
        if (!hasWagons()) {
            return true;
        }

        int currentAmountOfWagons = this.getNumberOfWagons();
        int totalAmountOfWagonsIfAttached = currentAmountOfWagons + wagon.getSequenceLength();

        // loops through all wagons to see if the given wagon is equal to one of the existing wagons
        for (int i = 0; i < currentAmountOfWagons; i++) {
            if (wagon.equals(currentWagon)) {
                return false;
            } else {
                currentWagon = currentWagon.getNextWagon();
            }
        }

        // checks if the amount of attached wagons is too much for the engine
        if (totalAmountOfWagonsIfAttached > engine.getMaxWagons()) {
            return false;
        }
        // checks if wagon is of same type as train
        if ((wagon instanceof FreightWagon && this.isFreightTrain())) {
            return true;
        }
        // checks if wagon is of same type as train
        if ((wagon instanceof PassengerWagon && this.isPassengerTrain())) {
            return true;
        }
        return false;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        // checks if wagon can be attached
        if (!canAttach(wagon)) {
            return false;
        }

        // checks if wagon is a leading wagon and if it isn't it detaches the wagon infront of it
        if (wagon.getPreviousWagon() != null) {
            wagon.detachFront();
        }

        // if it's an empty train it sets the first wagon to the given wagon
        if (!hasWagons()) {
            this.setFirstWagon(wagon);

            return true;
        } else {
            // attaches given wagon to the current wagon
            this.getLastWagonAttached().attachTail(wagon);
            return true;
        }
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        // checks if wagon can be attached
        if (!canAttach(wagon)) {
            return false;
        } else {
            if (!hasWagons()) {
                this.setFirstWagon(wagon);
                return true;
            }

            // detaches given wagon from the front
            wagon.detachFront();

            // sets the given wagon as new first wagon
            this.setFirstWagon(wagon);

            // attaches current wagon to the given wagon
            wagon.getLastWagonAttached().attachTail(this.firstWagon);

            return true;
        }
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 0 <= position <= numWagons
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        Wagon oldTail = this.findWagonAtPosition(position);

        // checks if wagon can be attached to train
        if (!this.canAttach(wagon)) {
            return false;
        }

        // checks if there is a wagon infront of the current wagon, if so it detaches it
        Wagon wagonInFrontOfCurrentWagon = wagon.getPreviousWagon();
        if (wagonInFrontOfCurrentWagon != null) {
            wagon.detachFront();
        }

        // checks if there
        if (oldTail == null) {
            attachToRear(wagon);

            return true;
        }

        // checks if new front is not null, if so sets firstwagon to given wagon and attaches given wagon to the tail
        Wagon newFront = oldTail.getPreviousWagon();
        if (newFront == null) {
            this.setFirstWagon(wagon);
            wagon.getLastWagonAttached().attachTail(oldTail);

            return true;
        }
        newFront.detachTail();
        oldTail.detachFront();

        wagon.getLastWagonAttached().attachTail(oldTail);
        wagon.reAttachTo(newFront);

        return true;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        // TODO

        Wagon givenWagon = this.findWagonById(wagonId);

        // checks if the given wagon is equal to the first wagon, and if so it sets the firstwagon to the wagon behind the given wagon
        if (givenWagon.equals(this.firstWagon)) {
            this.setFirstWagon(givenWagon.getNextWagon());
        }

        // checks if wagon can be attached to given train, else given wagon is removed from sequence an attached to the back of the given train
        if (!toTrain.canAttach(givenWagon)) {
            return false;
        } else {
            givenWagon.removeFromSequence();
            toTrain.attachToRear(givenWagon);
            return true;
        }
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 0 <= position < numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        Wagon givenWagon = this.findWagonAtPosition(position);

        // checks if given wagon is null and that the given wagon can be attached
        if (givenWagon == null || !toTrain.canAttach(givenWagon)) {
            return false;
        }

        // checks if the position is 0 and if so, sets the first wagon to 0 and attaches the given wagon onto the given train
        if (position == 0) {
            this.setFirstWagon(null);
            toTrain.attachToRear(givenWagon);
            return true;
        }

        // detaches the front of the given wagon anf attaches given wagon to given train
        givenWagon.detachFront();
        toTrain.attachToRear(givenWagon);
        return true;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        // TODO

    }

    @Override
    public String toString() {
        StringBuilder allWagons = new StringBuilder();
        Wagon wagon = this.firstWagon;
        int totalNumberOfWagons = this.firstWagon.getSequenceLength();

        for (int i = 0; i < totalNumberOfWagons; i++) {
            allWagons.append(wagon);

            wagon = wagon.getNextWagon();
        }

        return engine.toString() + " " + allWagons + " with " + totalNumberOfWagons +
                " wagons from " + this.origin + " to " + this.destination;
    }

    // TODO string representation of a train
}

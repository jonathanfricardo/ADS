package models;

public abstract class Wagon {
    protected int id;               // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    // a.k.a. the successor of this wagon in a sequence
    // set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
    // a.k.a. the predecessor of this wagon in a sequence
    // set to null if no predecessor is connected


    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon (int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * @return  whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {

        return getNextWagon() != null;
    }

    /**
     * @return  whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {

        return getPreviousWagon() != null;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     * @return  the last wagon
     */
    public Wagon getLastWagonAttached() {
        // TODO find the last wagon in the sequence

        Wagon wagon = this;

        while (wagon.hasNextWagon()) {
            wagon = wagon.getNextWagon();
        }

        return wagon;
    }

    /**
     * @return  the length of the sequence of wagons towards the end of its tail
     * including this wagon itself.
     */
    public int getSequenceLength() {
        // TODO traverse the sequence and find its length
        int i = 1;
        Wagon wagon = this;

        while(wagon.hasNextWagon()) {
            wagon = wagon.getNextWagon();
            i++;
        }

        return i;
    }

    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *          The exception should include a message that reports the conflicting connection,
     *          e.g.: "%s is already pulling %s"
     *          or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail)  {

        //throws exception when this wagon has a next wagon.
        if (this.hasNextWagon()) {
            throw new IllegalStateException(String.format("%s is already pulling %s", this, this.getNextWagon()));
        }

        //throws exception when tail has a previous wagon.
        if(tail.hasPreviousWagon()) {
            throw new IllegalStateException(String.format("%s has already been attached to %s", tail, tail.getPreviousWagon()));
        }

        //check if the connection can be made.
        if(!this.hasNextWagon() || !tail.hasPreviousWagon()) {
            this.nextWagon = tail;
            tail.previousWagon = this;
        }
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     * @return the first wagon of the tail that has been detached
     *          or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        // TODO detach the tail from this wagon (sustaining the invariant propositions).
        //  and return the head wagon of that tail

        Wagon wagon = this;
        Wagon detachedWagon = wagon.nextWagon;

        if (detachedWagon != null) {
            wagon.nextWagon = null;
            detachedWagon.previousWagon = null;
            return detachedWagon;
        } else {
            return null;
        }

    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     * @return  the former previousWagon that has been detached from,
     *          or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        // TODO detach this wagon from its predecessor (sustaining the invariant propositions).
        //   and return that predecessor

        Wagon wagon = this;
        Wagon previousAttachedWagon = wagon.previousWagon;

        if (wagon.hasPreviousWagon()) {
            previousAttachedWagon.nextWagon = null;
            wagon.previousWagon = null;
            return previousAttachedWagon;
        } else {
            return null;
        }

    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon form its predecessor,
     * and the <code>front</code> wagon from its current tail.
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        // TODO detach any existing connections that will be rearranged

        Wagon wagon = this;

        //Checks if front has a connection to a wagon
        if (front.hasNextWagon()) {
            Wagon frontNextWagon = front.nextWagon;
            //remove connection
            front.detachTail();
        }

        if (wagon.hasPreviousWagon()) {
            Wagon previousAttachedWagon = wagon.previousWagon;
            //remove connection
            wagon.detachFront();
        }

        // TODO attach this wagon to its new predecessor front (sustaining the invariant propositions).
        //connects this wagon with front
        front.attachTail(wagon);
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        // TODO
        Wagon wagon = this;
        Wagon previousAttachedWagon = null;
        Wagon nextAttachedWagon = null;

        //Check if this wagon has any connections
        if(wagon.hasPreviousWagon()) {
            previousAttachedWagon = wagon.previousWagon;
            //remove connections
            wagon.detachFront();
        }

        if(wagon.hasNextWagon()) {
            nextAttachedWagon = wagon.nextWagon;
            //remove connections
            wagon.detachTail();
        }

        //reconnect the wagons

        if(previousAttachedWagon != null && nextAttachedWagon != null) {
            previousAttachedWagon.attachTail(nextAttachedWagon);
        }

    }


    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        // TODO provide an iterative implementation,
        //   using attach- and detach methods of this class

        Wagon wagon = this;
        Wagon wagonInFrontOfThisWagon = null;

        if (this.hasPreviousWagon()) {
            wagonInFrontOfThisWagon = wagon.previousWagon;
        }
        Wagon formerTail = wagon.getLastWagonAttached();
        Wagon newReverseTail = wagon.getLastWagonAttached();

        wagon.detachFront();

        while (wagon != newReverseTail){

            Wagon next = formerTail.getPreviousWagon();

            next.removeFromSequence();
            newReverseTail.attachTail(next);
            newReverseTail = next;


        }

        if (wagonInFrontOfThisWagon != null) {
            wagonInFrontOfThisWagon.attachTail(formerTail);
        }

        return formerTail;
    }

    // TODO string representation of a Wagon

    @Override
    public String toString() {
        return "[Wagon-" + id + "]";
    }
}

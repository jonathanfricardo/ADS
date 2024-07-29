package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E>
        extends ArrayList<E>
        implements OrderedList<E> {

    protected Comparator<? super E> sortOrder;   // the comparator that has been used with the latest sort
    protected int nSorted;                       // the number of sorted items in the first section of the list
    // representation-invariant
    //      all items at index positions 0 <= index < nSorted have been ordered by the given sortOrder comparator
    //      other items at index position nSorted <= index < size() can be in any order amongst themselves
    //              and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> sortOrder) {
        super();
        this.sortOrder = sortOrder;
        this.nSorted = 0;
    }

    public Comparator<? super E> getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.sortOrder = c;
        this.nSorted = this.size();
    }

    // TODO override the ArrayList.add(index, item), ArrayList.remove(index) and Collection.remove(object) methods
    //  such that they both meet the ArrayList contract of these methods (see ArrayList JavaDoc)
    //  and sustain the representation invariant of OrderedArrayList
    //  (hint: only change nSorted as required to guarantee the representation invariant,
    //   do not invoke a sort or reorder items otherwise differently than is specified by the ArrayList contract)

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        if (index < nSorted) {
            nSorted = index;
        }
    }

    @Override
    public E remove(int index) {
        nSorted--;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        nSorted--;
        return super.remove(o);
    }


    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.sortOrder);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an sortOrder for the list
        if (this.getSortOrder() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            // some arbitrary choice to use the iterative or the recursive version
            return indexOfByRecursiveBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {

        // TODO implement an iterative binary search on the sorted section of the arrayList, 0 <= index < nSorted
        //   to find the position of an item that matches searchItem (this.sortOrder comparator yields a 0 result)

        int from = 0;
        int to = nSorted - 1;
        int position = -1;

        //loops through the sorted part of the list and returns the position of the searched item if found
        while (from <= to) {
            int mid = (from + to) / 2;
            int compare = this.sortOrder.compare(searchItem, this.get(mid));
            if (compare < 0) {
                to = mid - 1;
            } else if (compare > 0) {
                from = mid + 1;
            } else {
                return mid;
            }
        }

        // TODO if no match was found, attempt a linear search of searchItem in the section nSorted <= index < size()
        //if no item was found in the sorted list, it loops through unsorted list and returns position of the searched item
        for (int i = nSorted; i < this.size(); i++) {
            int compare = this.sortOrder.compare(searchItem, this.get(i));
            if (compare == 0) {
                position = i;
            }
        }


        return position;
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {

        // TODO implement a recursive binary search on the sorted section of the arrayList, 0 <= index < nSorted
        //   to find the position of an item that matches searchItem (this.sortOrder comparator yields a 0 result)

        int begin = 0;
        int endOfSortedList = nSorted - 1;

        //catches result of recursive function, either -1 or 0
        int position = helperFunctionRecursiveSearch(begin, endOfSortedList, searchItem);


        // TODO if no match was found, attempt a linear search of searchItem in the section nSorted <= index < size()
        //if no item was found in the sorted list, it loops through unsorted list and returns position of the searched item
        if (position == -1) {
            for (int i = nSorted; i < this.size(); i++) {
                int compare = this.sortOrder.compare(searchItem, this.get(i));
                if (compare == 0) {
                    position = i;
                }
            }
        }

        return position;
    }

    /**
     * Helper function for recursive method
     *
     * @param begin
     * @param end
     * @param searchItem
     * @return
     * @author Rowin Schenk
     */
    private int helperFunctionRecursiveSearch(int begin, int end, E searchItem) {
        //check if beginning is greater than the end
        if (begin > end) {
            return -1;
        }

        int mid = (begin + end) / 2;
        //compares item to the middle item
        int compare = this.sortOrder.compare(searchItem, this.get(mid));

        //checks compare and calls new funtion with corresponding parameters to find item
        if (compare < 0) {
            return helperFunctionRecursiveSearch(begin, mid - 1, searchItem);
        } else if (compare > 0) {
            return helperFunctionRecursiveSearch(mid + 1, end, searchItem);
        }

        //if compare equals 0, return mid
        return mid;
    }


    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            // TODO retrieve the matched item and
            //  replace the matched item in the list with the merger of the matched item and the newItem

            //retieves matched item
            E matchedItem = this.get(matchedItemIndex);

            //merges matched item with the new item
            E mergedItem = merger.apply(matchedItem, newItem);

            //sets the content of the index of the matched item to the new merged item
            this.set(matchedItemIndex, mergedItem);

            return true;
        }
    }

    /**
     * calculates the total sum of contributions of all items in the list
     *
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        double sum = 0.0;

        // TODO loop over all items and use the mapper
        //  to calculate and accumulate the contribution of each item

        //loops through each item E in this list and adds the contribution to variable
        for (E item : this) {
            sum += mapper.apply(item);
        }

        return sum;
    }
}

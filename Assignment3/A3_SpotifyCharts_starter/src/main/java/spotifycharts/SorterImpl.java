package spotifycharts;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     * @param items
     * @param comparator
     * @return  the items sorted in place
     */
    public List<E> selInsBubSort(List<E> items, Comparator<E> comparator) {
        // TODO implement selection sort or insertion sort or bubble sort
        // Selection sort below
        int n = items.size();

        // Traverse through all array elements
        for (int i = 0; i < n - 1; i++) {
            // Find the minimum element in the unsorted part of the list
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(items.get(j), items.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }

            // Swap the found minimum element with the first unsorted element
            E temp = items.get(minIndex);
            items.set(minIndex, items.get(i));
            items.set(i, temp);
        }

        return items;
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     * @param items
     * @param comparator
     * @return  the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        // TODO provide a recursive quickSort implementation,
        //  that is different from the example given in the lecture

        // Call the recursive quickSort method to sort the entire list
        quickSortRecursive(items, comparator, 0, items.size() - 1);

        return items;   // replace as you find appropriate
    }

    /**
     * Recursive method for performing quick sort on a sublist of items
     * @param items       the list of items to be sorted
     * @param comparator  the comparator to determine the ordering of items
     * @param low         the index of the first element in the sublist
     * @param high        the index of the last element in the sublist
     */
    private void quickSortRecursive(List<E> items, Comparator<E> comparator, int low, int high) {
        if (low < high) {
            // Partition the list and get the index of the pivot element
            int pivotIndex = partition(items, comparator, low, high);

            // Recursively sort the sublists on both sides of the pivot
            quickSortRecursive(items, comparator, low, pivotIndex - 1);
            quickSortRecursive(items, comparator, pivotIndex + 1, high);
        }
    }

    /**
     * Partitions the list into two sublists and returns the index of the pivot element
     * @param items       the list of items to be sorted
     * @param comparator  the comparator to determine the ordering of items
     * @param low         the index of the first element in the sublist
     * @param high        the index of the last element in the sublist
     * @return            the index of the pivot element after partitioning
     */
    private int partition(List<E> items, Comparator<E> comparator, int low, int high) {
        // Choose the pivot element (in this case, the last element)
        E pivot = items.get(high);

        // Index of the smaller element
        int i = low - 1;

        // Traverse the sublist and swap elements to partition them around the pivot
        for (int j = low; j < high; j++) {
            if (comparator.compare(items.get(j), pivot) <= 0) {
                i++;
                // Swap items[i] and items[j]
                E temp = items.get(i);
                items.set(i, items.get(j));
                items.set(j, temp);
            }
        }

        // Swap items[i+1] and pivot
        E temp = items.get(i + 1);
        items.set(i + 1, items.get(high));
        items.set(high, temp);

        // Return the index of the pivot element
        return i + 1;
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     * @param numTops       the size of the lead collection of items to be found and sorted
     * @param items
     * @param comparator
     * @return              the items list with its first numTops items sorted according to comparator
     *                      all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator;

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops-1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection

            // TODO swap item[0] and item[i];
            //  this moves item[0] to its designated position

            swap(items, 0, i);

            // TODO the new root may have violated the heap condition
            //  repair the heap condition on the remaining heap of size i

            heapify(items, i, 0, reverseComparator);

        }

        return items;
    }

    // Helper method to swap two elements in the list
    private void swap(List<E> items, int i, int j) {
        E temp = items.get(i);
        items.set(i, items.get(j));
        items.set(j, temp);
    }

    // Helper method to heapify a subtree rooted with node i which is an index in items[]
    // n is the size of the heap
    private void heapify(List<E> items, int n, int i, Comparator<E> comparator) {
        int largest = i; // Initialize largest as root
        int leftChild = 2 * i + 1; // left = 2*i + 1
        int rightChild = 2 * i + 2; // right = 2*i + 2

        // If left child is larger than root
        if (leftChild < n && comparator.compare(items.get(leftChild), items.get(largest)) > 0) {
            largest = leftChild;
        }

        // If right child is larger than largest so far
        if (rightChild < n && comparator.compare(items.get(rightChild), items.get(largest)) > 0) {
            largest = rightChild;
        }

        // If largest is not root
        if (largest != i) {
            swap(items, i, largest);

            // Recursively heapify the affected sub-tree
            heapify(items, n, largest, comparator);
        }
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     *                      all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        // TODO swim items[heapSize-1] up the heap until
        //      i==0 || items[(i-1]/2] <= items[i]

        int currentIndex = heapSize - 1;

        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;

            // Check if the current item is greater than its parent
            if (comparator.compare(items.get(currentIndex), items.get(parentIndex)) > 0) {
                // Swap the current item with its parent
                swap(items, currentIndex, parentIndex);
                currentIndex = parentIndex;
            } else {
                // The heap condition is satisfied
                break;
            }
        }

    }
    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     *                      all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        // TODO sink items[0] down the heap until
        //      2*i+1>=heapSize || (items[i] <= items[2*i+1] && items[i] <= items[2*i+2])

        int currentIndex = 0;

        while (2 * currentIndex + 1 < heapSize) {
            int leftChild = 2 * currentIndex + 1;
            int rightChild = 2 * currentIndex + 2;

            // Find the index of the larger child
            int largerChild = (rightChild < heapSize && comparator.compare(items.get(rightChild), items.get(leftChild)) > 0)
                    ? rightChild
                    : leftChild;

            // Check if the current item is smaller than its larger child
            if (comparator.compare(items.get(currentIndex), items.get(largerChild)) < 0) {
                // Swap the current item with its larger child
                swap(items, currentIndex, largerChild);
                currentIndex = largerChild;
            } else {
                // The heap condition is satisfied
                break;
            }
        }


    }
}

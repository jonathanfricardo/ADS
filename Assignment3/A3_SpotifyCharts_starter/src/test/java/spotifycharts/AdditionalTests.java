package spotifycharts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AdditionalTests {

    private SongSorter songSorter;
    private List<Song> fewSongs;


    @BeforeEach
    void setup() {
        ChartsCalculator chartsCalculator = new ChartsCalculator(2L);
        this.songSorter = new SongSorter();
        fewSongs = new ArrayList(chartsCalculator.registerStreamedSongs(23));
    }

    @Test
    void topsHeapSortAndCollectionSortYieldSameOrder() {

        List<Song> fewSortedSongs = new ArrayList<>(fewSongs);
        Collections.shuffle(fewSortedSongs);

        // Using the wrong comparator in topsHeapSort
        songSorter.topsHeapSort(5, fewSortedSongs, Comparator.comparing(Song::getArtist));
        fewSongs.sort(Comparator.comparing(Song::getTitle));

        // Detect the bug by checking if the first element in the sorted list is incorrect
        assertNotEquals(fewSongs.get(0).getTitle(), fewSortedSongs.get(0).getTitle());
    }

    @Test
    void topsHeapSortAndCollectionSortYieldSameOrderWithBug() {

        List<Song> fewSortedSongs = new ArrayList<>(fewSongs);
        Collections.shuffle(fewSortedSongs);

        //Incorrectly passing an unshuffled list to topsHeapSort
        songSorter.topsHeapSort(5, fewSongs, Comparator.comparing(Song::getTitle));
        fewSongs.sort(Comparator.comparing(Song::getTitle));

        //  checking if the sorted lists are not equal
        assertNotEquals(fewSongs, fewSortedSongs);
    }

}

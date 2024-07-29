package spotifycharts;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongComparatorTest {

    @Test
    void testCompareByHighestStreamCountTotalStability() {
        Song song1 = new Song("Artist 1", "Song 1", Song.Language.EN);
        song1.setStreamsCountOfCountry(Song.Country.NL, 100);

        assertEquals(0, song1.compareByHighestStreamsCountTotal(song1));

        Song song2 = new Song("Artist 2", "Song 2", Song.Language.EN);
        song2.setStreamsCountOfCountry(Song.Country.NL, 100);

        assertEquals(0, song1.compareByHighestStreamsCountTotal(song2));
        assertEquals(0, song2.compareByHighestStreamsCountTotal(song1));
    }

    @Test
    void testCompareForDutchNationalChartStability() {
        Song song1 = new Song("Artist 1", "Song 1", Song.Language.EN);
        song1.setStreamsCountOfCountry(Song.Country.NL, 150);

        assertEquals(0, song1.compareByHighestStreamsCountTotal(song1));

        Song song2 = new Song("Artist 2", "Song 2", Song.Language.EN);
        song2.setStreamsCountOfCountry(Song.Country.NL, 150);

        assertEquals(0, song1.compareByHighestStreamsCountTotal(song2));
        assertEquals(0, song2.compareByHighestStreamsCountTotal(song1));
    }

}

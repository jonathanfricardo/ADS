package spotifycharts;

import java.util.HashMap;

public class Song {

    public enum Language {
        EN, // English
        NL, // Dutch
        DE, // German
        FR, // French
        SP, // Spanish
        IT, // Italian
    }

    public enum Country {
        UK, // United Kingdom
        NL, // Netherlands
        DE, // Germany
        BE, // Belgium
        FR, // France
        SP, // Spain
        IT  // Italy
    }

    private final String artist;
    private final String title;
    private final Language language;

    // TODO add instance variable(s) to track the streams counts per country
    //  choose a data structure that you deem to be most appropriate for this application.
    HashMap<String, Integer> streamsCountPerCountry = new HashMap<String, Integer>();

    /**
     * Constructs a new instance of Song based on given attribute values
     */
    public Song(String artist, String title, Language language) {
        this.artist = artist;
        this.title = title;
        this.language = language;

        // TODO initialise streams counts per country as appropriate.
        streamsCountPerCountry.put("UK", getStreamsCountOfCountry(Country.UK));
        streamsCountPerCountry.put("NL", getStreamsCountOfCountry(Country.NL));
        streamsCountPerCountry.put("DE", getStreamsCountOfCountry(Country.DE));
        streamsCountPerCountry.put("BE", getStreamsCountOfCountry(Country.BE));
        streamsCountPerCountry.put("FR", getStreamsCountOfCountry(Country.FR));
        streamsCountPerCountry.put("SP", getStreamsCountOfCountry(Country.SP));
        streamsCountPerCountry.put("IT", getStreamsCountOfCountry(Country.IT));
    }

    /**
     * Sets the given streams count for the given country on this song
     *
     * @param country
     * @param streamsCount
     */
    public void setStreamsCountOfCountry(Country country, int streamsCount) {
        // TODO register the streams count for the given country.
        streamsCountPerCountry.put(country.name(), streamsCount);
    }

    /**
     * retrieves the streams count of a given country from this song
     *
     * @param country
     * @return
     */
    public int getStreamsCountOfCountry(Country country) {
        // TODO retrieve the streams count for the given country.

        //turns country into string
        String countryString = country.toString();

        if (streamsCountPerCountry.get(countryString) == null) {
            return 0;
        } else return streamsCountPerCountry.get(countryString);
    }

    /**
     * Calculates/retrieves the total of all streams counts across all countries from this song
     *
     * @return
     */
    public int getStreamsCountTotal() {
        // TODO calculate/get the total number of streams across all countries
        int totalAmountOfStreams = 0;

        //loops through all values in the hashmap
        for (int amountOfStreamsPerCountry : streamsCountPerCountry.values()) {
            totalAmountOfStreams += amountOfStreamsPerCountry;
        }

        return totalAmountOfStreams;
    }


    /**
     * compares this song with the other song
     * ordening songs with the highest total number of streams upfront
     *
     * @param other the other song to compare against
     * @return negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestStreamsCountTotal(Song other) {
        // TODO compare the total of stream counts of this song across all countries
        //  with the total of the other song

        return Integer.compare(other.getStreamsCountTotal(), this.getStreamsCountTotal());
    }

    /**
     * compares this song with the other song
     * ordening all Dutch songs upfront and then by decreasing total number of streams
     *
     * @param other the other song to compare against
     * @return negative number, zero or positive number according to Comparator conventions
     */
    public int compareForDutchNationalChart(Song other) {
        // TODO compare this song with the other song
        //  ordening all Dutch songs upfront and then by decreasing total number of streams

        if (other.getLanguage() == Language.NL && this.getLanguage() == Language.NL) {
            return this.compareByHighestStreamsCountTotal(other);
        } else if (other.getLanguage() == Language.NL && this.getLanguage() != Language.NL) {
            return 1;
        } else if (other.getLanguage() != Language.NL && this.getLanguage() == Language.NL) {
            return -1;
        } else return this.compareByHighestStreamsCountTotal(other);
    }


    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    // TODO provide a toString implementation to format songs as in "artist/title{language}(total streamsCount)"


    @Override
    public String toString() {
        return artist + "/" + title + "{" + language + "}(" + getStreamsCountTotal() + ")";
    }
}

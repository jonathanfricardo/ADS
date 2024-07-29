package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;


        this.parties = new HashMap<>();

        this.constituencies = new TreeSet<>(Comparator.comparing(Constituency::getId));


    }

    /**
     * finds all (unique) parties registered for this election
     *
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {

        return new HashSet<>(parties.values());

    }

    /**
     * finds the party with a given id
     *
     * @param id
     * @return the party with given id, or null if no such party exists.
     */
    public Party getParty(int id) {

        return this.parties.get(id);
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id
     *
     * @return alle unique candidates organised by increasing party-id
     */
    public List<Candidate> getAllCandidates() {

        return this.getParties().stream().flatMap(p -> p.getCandidates().stream()).distinct().collect(Collectors.toList());
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     *
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {

        Map<Constituency, Integer> candidatesPerConstituency = new HashMap<>();

        //loop through constituencies
        for (Constituency constituency : this.constituencies) {

            //checks if this constituency contains the given party
            if (constituency.getParties().contains(party)) {
                //counts number of registrations
                int count = 0;
                count = constituency.getCandidates(party).size();

                if (count > 0) {
                    candidatesPerConstituency.put(constituency, count);
                }
            }
        }

        return candidatesPerConstituency; // replace by a proper outcome
    }

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     *
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {

        Map<String, Integer> countPerName = new HashMap<>();

        //loop through all candidates
        for (Candidate candidate : this.getAllCandidates()) {

            //counts duplicate name if it's in the map already
            if (!countPerName.containsKey(candidate.getFullName())) {
                countPerName.put(candidate.getFullName(), 1);
            } else {
                countPerName.merge(candidate.getFullName(), 1, Integer::sum);
            }
        }

        Set<Candidate> doubleNames;

        //convert collection into stream and filter based on count
        doubleNames = this.getAllCandidates().stream()
                .filter(candidate -> countPerName.getOrDefault(candidate.getFullName(), 0) > 1)
                .collect(Collectors.toSet());

        //sorts the list on full name (and hashcode, so duplicates don't disappear)
        TreeSet<Candidate> sortedDuplicateNames = new TreeSet<>(Comparator.comparing(Candidate::getFullName).thenComparing(Candidate::hashCode));
        sortedDuplicateNames.addAll(doubleNames);

        return sortedDuplicateNames;
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     *
     * @param firstZipCode
     * @param lastZipCode
     * @return the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {

        Collection<PollingStation> pollingStations = new TreeSet<>(Comparator.comparing(PollingStation::getZipCode).thenComparing(PollingStation::getId));

        //loops through all constituencies
        for (Constituency constituency : this.constituencies) {
            //checks for polling station with given zipcode range
            NavigableSet<PollingStation> pollingStationsOfConstituency = constituency.getPollingStationsByZipCodeRange(firstZipCode, lastZipCode);
            pollingStations.addAll(pollingStationsOfConstituency);
        }

        return pollingStations; // replace by a proper outcome
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     *
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {

        Map<Party, Integer> votesByParty = new HashMap<>();

        //loops through constituencies and counts all votes per party
        for (Constituency constituency : this.constituencies) {
            votesByParty.putAll(constituency.getVotesByParty());
        }

        return votesByParty; // replace by a proper outcome
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     *
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {

        Map<Party, Integer> votesByGivenParty = new HashMap<>();

        //looping through all polling stations from given stations
        for (PollingStation pollingStation : pollingStations) {
            Map<Party, Integer> votesByPartyInStation = pollingStation.getVotesByParty();

            //sum up votes by party across polling stations
            for (Map.Entry<Party, Integer> entry : votesByPartyInStation.entrySet()) {
                Party party = entry.getKey();
                int votes = entry.getValue();

                //merge votes for the party into the overall map
                votesByGivenParty.merge(party, votes, Integer::sum);
            }

        }

        return votesByGivenParty; // replace by a proper outcome
    }


    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     *
     * @return the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party, Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {

        int totalVotes = 0;

        //calculate total votes
        for (Map.Entry<Party, Integer> entry : votesCounts.entrySet()) {
            totalVotes = totalVotes + entry.getValue();
        }

        Map<Party, Double> percentagePerParty = new HashMap<>();

        //new map with the percentages per party
        for (Map.Entry<Party, Integer> entry : votesCounts.entrySet()) {
            double percentage = ((double) entry.getValue() / totalVotes) * 100.0;
            percentagePerParty.put(entry.getKey(), percentage);
        }

        //convert map into list, and sort it based on value
        List<Map.Entry<Party, Double>> sortedPercentageList = new ArrayList<>(percentagePerParty.entrySet());

        //sort the list on value (reveserved for highest value first), then sort for party id.
        sortedPercentageList.sort(
                Comparator.<Map.Entry<Party, Double>>comparingDouble(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(e -> e.getKey().getId())
        );

        //return sublist based on given tops
        return sortedPercentageList.subList(0, tops); // replace by a proper outcome
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     * <p>
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {

        Map<PollingStation, Double> mapOfPollingstationsWithTheirDeviation = new HashMap<>();
        double lowestDeviation = 100;
        PollingStation mostRepresentativePollingstation = null;

        //gets all pollingstations
        for (PollingStation pollingStation : this.getPollingStationsByZipCodeRange("0000AA", "9999ZZ")) {
            double deviationCurrentPollingstation = this.euclidianVotesDistributionDeviation(this.getVotesByParty(), pollingStation.getVotesByParty());
            mapOfPollingstationsWithTheirDeviation.put(pollingStation, deviationCurrentPollingstation);
        }

        //loops through map of pollingstations to find pollingstation with the lowest deviation
        for (Map.Entry<PollingStation, Double> entry : mapOfPollingstationsWithTheirDeviation.entrySet()) {
            if (entry.getValue() < lowestDeviation) {
                lowestDeviation = entry.getValue();
                mostRepresentativePollingstation = entry.getKey();
            }
        }

        return mostRepresentativePollingstation; // replace by a proper outcome
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero

        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue() / (double) totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(), 0) / (double) totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     *
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
    }


    public String prepareSummary(int partyId) {

        Party party = this.getParty(partyId);
        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n\n");

        // TODO report total number of candidates in the given party
        summary.append("Total number of candidates: ").append(party.getCandidates().size()).append("\n\n");
        // TODO report the list with all candidates in the given party
        for (Candidate candidate : party.getCandidates()) {
            summary.append(candidate).append("\n");
        }
        // TODO report total number of registrations for the given party
        summary.append("\n");
        int totalRegistrations = 0;
        for (Map.Entry<Constituency, Integer> entry : this.numberOfRegistrationsByConstituency(party).entrySet()) {
            totalRegistrations += entry.getValue();
        }
        summary.append("Total number of registrations: ").append(totalRegistrations).append("\n\n");
        // TODO report the map of number of registrations by constituency for the given party
        summary.append(this.numberOfRegistrationsByConstituency(party));

        return summary.toString();
    }

    public String prepareSummary() {

        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of ").append(this.name).append(":\n");

        // TODO report the total number of parties in the election
        summary.append(getParties().size()).append(" Participating parties:\n");
        // TODO report the list of all parties ordered by increasing party-Id
        summary.append(getParties());
        // TODO report the total number of constituencies in the election
        summary.append("\nTotal number of constituencies = ").append(getConstituencies().size());
        // TODO report the total number of polling stations in the election
        summary.append("\nTotal number of polling stations = ").append(this.getPollingStationsByZipCodeRange("0000AA", "9999ZZ").size());
        // TODO report the total number of (different) candidates in the election
        summary.append("\nTotal number of candidates in the election = ").append(getAllCandidates().size());
        // TODO report the list with all candidates which have a counter part with a duplicate name in a different party
        summary.append("\nDifferent candidates with duplicate names across different parties are:\n").append(getCandidatesWithDuplicateNames());

        // TODO report the sorted list of overall election results ordered by decreasing party percentage
        summary.append("\n\nOverall election results by party percentage:\n").append(sortedElectionResultsByPartyPercentage(getVotesByParty().size(), getVotesByParty()));
        // TODO report the polling stations within the Amsterdam Wibautstraat area with zipcodes between 1091AA and 1091ZZ
        summary.append("\n\nPolling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n").append(getPollingStationsByZipCodeRange("1091AA", "1091ZZ"));
        // TODO report the top 10 sorted election results within the Amsterdam Wibautstraat area
        //   with zipcodes between 1091AA and 1091ZZ ordered by decreasing party percentage
        summary.append("\nTop 10 election results by party percentage in Amsterdam area with zip codes 1091AA-1091ZZ:\n").append(sortedElectionResultsByPartyPercentage(10, getVotesByPartyAcrossPollingStations(getPollingStationsByZipCodeRange("1091AA", "1091ZZ"))));
        // TODO report the most representative polling station across the election
        summary.append("\nMost representative polling station is:\n").append(findMostRepresentativePollingStation());
        // TODO report the sorted election results by decreasing party percentage of the most representative polling station
        summary.append(sortedElectionResultsByPartyPercentage( findMostRepresentativePollingStation().getVotesByParty().size(), findMostRepresentativePollingStation().getVotesByParty()));

        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     *
     * @param folderName the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException        als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

}

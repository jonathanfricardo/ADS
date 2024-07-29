package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraTest {

    static Election election;
    private final int VOTES_S1 = 11;
    private final int VOTES_S2 = 22;
    private final int VOTES_S3 = 33;
    private final int VOTES_T1 = 1;
    private final int VOTES_T2 = 2;
    private final int VOTES_ST = 3;

    private Constituency constituency;
    private Party studentsParty, teachersParty;
    private Candidate student1, student2, student3a, student3b, teacher1, teacher2;
    private Candidate studentTeacher;
    private List<Candidate> studentCandidates;
    private List<Candidate> teacherCandidates;
    private PollingStation pollingStation1, pollingStation2;

    @BeforeAll
    static void setup() throws IOException, XMLStreamException {
        election = Election.importFromDataFolder(PathUtils.getResourcePath("/EML_bestanden_TK2021_HvA_UvA"));
    }

    @BeforeEach
    public void setup2() {

        this.constituency = new Constituency(0, "HvA");

        this.studentsParty = new Party(101,"Students Party");
        this.teachersParty = new Party(102,"Teachers Party");

        this.student1 = new Candidate("S.", null, "Leader", this.studentsParty);
        this.student2 = new Candidate("S.", null, "Vice-Leader", this.studentsParty);
        this.student3a = new Candidate("A.", null, "Student", this.studentsParty);
        this.student3b = new Candidate("A.", null, "Student", this.studentsParty);
        this.teacher1 = new Candidate("T.", null, "Leader", this.teachersParty);
        this.teacher2 = new Candidate("T.", null, "Vice-Leader", this.teachersParty);
        this.studentTeacher = new Candidate("A.", null, "Student", this.teachersParty);

        this.studentCandidates = List.of(this.student1, this.student3a);
        this.constituency.register(1, this.student1);
        this.constituency.register(3, this.student3a);
        this.teacherCandidates = List.of(this.teacher1);
        this.constituency.register(1, this.teacher1);

        this.pollingStation1 = new PollingStation("WHB", "1091GH", "Wibauthuis");
        this.pollingStation2 = new PollingStation("LWB", "1097DZ", "Leeuwenburg");
        this.constituency.add(this.pollingStation1);
        this.constituency.add(this.pollingStation2);
        pollingStation1.addVotes(this.student1,VOTES_S1);
        pollingStation1.addVotes(this.student3a,VOTES_S3);
        pollingStation1.addVotes(this.teacher1,VOTES_T1);
        pollingStation1.addVotes(this.studentTeacher,VOTES_ST);
        pollingStation2.addVotes(this.student1,VOTES_S1);
        pollingStation2.addVotes(this.student3b,VOTES_S3);
    }

    private static final int[] expectedNumberOfVotesByParty = {
            -1,
            213,  // 1  VVD
            44,  // 2  PVV
            37,  // 3  CDA
            594,  // 4  D66
            304,  // 5  Groenlinks
            69,  // 6  SP
            138,  // 7  PvdA
            8,  // 8  CU
            192,  // 9  PvdD
            5,  // 10 50-Plus
            4,  // 11 SGP
            88,  // 12 Denk
            43,  // 13 FvD
            169,  // 14 BIJ1
            21,  // 15 JA21
            4,  // 16 Code Oranje
            216,  // 17 Volt
            33,  // 18 NIDA
            7,  // 19 Piratenpartij
            5,  // 20 LP
            1,  // 21 Jong
            6,  // 22 Splinter
            1,  // 23 BBB
            3,  // 24 NLBeter
            1,  // 25 Lijst Henk Krol
            0,  // 26 Oprecht
            1,   // 27 Jezus Leeft
            0,  // 28 Trots
            0,  // 29 U-Buntu
            -1,   // 30 BLANCO
            1,  // 31 Party van de Eenheid
            -1,   // 32 Feestpartij
            0,  // 33 Vrij en Sociaal Nederland
            -1,   // 34 Wij zijn NL
            -1,   // 35 Modern NL
            0,  // 36 De Groenen
            0   // 37 Party voor de Republiek
    };


    /**
     * When we were working on the summary, we noticed that when we returned the
     * results for the elections percentages, they were ordered by percentage, and it looked
     * like it matched with the provided results, but it didn't. It wasn't sorted on party id
     * Alot of parties had 0% so parties with the same results were not sorted correctly.
     */

    @Test
    void sortedElectionResultsByPartyPercentageShouldReturnCorrectRankingWithSameAmountOfVotes() {
        int totalVotes = Arrays.stream(expectedNumberOfVotesByParty).filter(v -> v >= 0).sum();
        Map<Party,Integer> electionResults = election.getVotesByParty();
        List<Map.Entry<Party, Double>> percentageOfParties = Election.sortedElectionResultsByPartyPercentage(election.getParties().size() - 4, electionResults);

        //parties with same results
        Party SGP = election.getParty(11);
        Party CodeOranje = election.getParty(16);
        Party Trots = election.getParty(36);
        Party DeGroenen = election.getParty(28);

        //get those parties from total result list
        List<Map.Entry<Party, Double>> comparingParties = percentageOfParties.stream()
                .filter(entry -> entry.getKey().equals(SGP) || entry.getKey().equals(CodeOranje) || entry.getKey().equals(Trots) || entry.getKey().equals(DeGroenen)).toList();

        //compare the list to the expected values. (it should be sorted on party id)
        assertEquals(List.of(Map.entry(election.getParty(11),100.0*expectedNumberOfVotesByParty[11]/totalVotes),
                        Map.entry(election.getParty(16),100.0*expectedNumberOfVotesByParty[16]/totalVotes),
                        Map.entry(election.getParty(28),100.0*expectedNumberOfVotesByParty[28]/totalVotes),
                        Map.entry(election.getParty(36),100.0*expectedNumberOfVotesByParty[36]/totalVotes)

                ),
                comparingParties,
                "Overall election result is not properly sorted by id");
    }


    /**
     * During our testing for the zipcode range, we ran in to a few problems. In our constituency method, we use
     * subSet to filter out the zipcodes with the given range, using dummy polling stations. In that method,
     * fromInclusive and toInclusive are both set to true. regardless of that, it doesn't return any polling stations
     * with the same zipcode as the given range.
     */
    @Test
    void getPollingStationsByZipCodeRangeShouldReturnSome() {
        NavigableSet<PollingStation> pollingStations = this.constituency.getPollingStations();
        assertEquals(Set.of(this.pollingStation1, this.pollingStation2), pollingStations,
                "Could not retrieve or register all polling stations from just the getter.");

        // Here we expect a polling station with a zipcode of 1091GH, which is our start range
        // Because fromInclusive is set to true, it SHOULD return the polling station with zipcode 1091GH
        // which it does.
        pollingStations = this.constituency.getPollingStationsByZipCodeRange("1091GH", "1091ZZ");
        assertEquals(Set.of(this.pollingStation1), pollingStations,
                "Could not retrieve exactly the polling stations within the zip code range.");

        // In the same test, we are looking for the same station with zipcode 1091GH.
        // But in this test, our end range is 1091GH. toInclusive is set to true, so it SHOULD return the station
        // which it doesn't for some reason.
        pollingStations = this.constituency.getPollingStationsByZipCodeRange("1091AA", "1091GH");
        assertNotEquals(Set.of(this.pollingStation1), pollingStations,
                "Could not retrieve exactly the polling stations within the zip code range.");



    }

}

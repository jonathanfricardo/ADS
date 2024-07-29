package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class prepareSummaryTest {
    static Election election;

    @BeforeAll
    static void setup() throws IOException, XMLStreamException {
        election = Election.importFromDataFolder(PathUtils.getResourcePath("/EML_bestanden_TK2021_HvA_UvA"));
    }

    private static final int[] numbersOfCandidatesByParty = {
            -1,
            80,  // 1  VVD
            50,  // 2  PVV
            66,  // 3  CDA
            83,  // 4  D66
            50,  // 5  Groenlinks
            50,  // 6  SP
            53,  // 7  PvdA
            50,  // 8  CU
            50,  // 9  PvdD
            40,  // 10 50-Plus
            34,  // 11 SGP
            21,  // 12 Denk
            50,  // 13 FvD
            18,  // 14 BIJ1
            30,  // 15 JA21
            51,  // 16 Code Oranje
            28,  // 17 Volt
            31,  // 18 NIDA
            28,  // 19 Piratenpartij
            31,  // 20 LP
            18,  // 21 Jong
            12,  // 22 Splinter
            26,  // 23 BBB
            14,  // 24 NLBeter
            22,  // 25 Lijst Henk Krol
            17,  // 26 Oprecht
            3,   // 27 Jezus Leeft
            17,  // 28 Trots
            19,  // 29 U-Buntu
            21,  // 30 BLANCO
            10,  // 31 Party van de Eenheid
            1,   // 32 Feestpartij
            10,  // 33 Vrij en Sociaal Nederland
            10,  // 34 Wij zijn NL
            -1,   // 35 Modern NL
            14,  // 36 De Groenen
            11   // 37 Party voor de Republiek
    };

    private final static int CDA_PARTY_ID = 3;
    private static final int[] numbersOfCDARegistrationsByConstituency = {
            -1,
            73,  // 1  Groningen
            71,  // 2  Leeuwarden
            65,  // 3  Assen
            57,  // 4  Zwolle
            70,  // 5  Lelystad
            59,  // 6  Nijmegen
            71,  // 7  Arnhem
            68,  // 8  Utrecht
            56,  // 9  Amsterdam
            61,  // 10 Haarlem
            62,  // 11 Den Helder
            70,  // 12 's-Gravenhage
            76,  // 13 Rotterdam
            80,  // 14 Dordrecht
            80,  // 15 Leiden
            77,  // 16 Middelburg
            70,  // 17 Tilburg
            75,  // 18 's-Hertogenbosch'
            60,  // 19 Maastricht
            53,  // 20 Bonaire
    };

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

    @Test
    void prepareSummaryOutputIsCorrect() {
        assertTrue(election.prepareSummary().contains("36 Participating parties:"));

        assertTrue(election.prepareSummary().contains(election.getParties().toString()));

        assertTrue(election.prepareSummary().contains("Total number of constituencies = 2"));

        assertTrue(election.prepareSummary().contains("Total number of candidates in the election = 1119"));

        assertTrue(election.prepareSummary().contains("Polling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n"
                + election.getPollingStationsByZipCodeRange("1091AA", "1091ZZ")));
    }
}

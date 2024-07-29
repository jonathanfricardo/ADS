package maze_escape;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AdditionalTests {

    Country nl, be, de, lux, fr, uk, ro, hu;
    Continent europe = new Continent();

    @BeforeAll
    static void beforeALl() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        nl = new Country("NL", 18);
        be = new Country("BE", 12);
        nl.addBorder(be,100);
        de = new Country("DE", 83);
        de.addBorder(nl,200);
        de.addBorder(be,30);
        lux = new Country("LUX",1);
        lux.addBorder(be,60);
        lux.addBorder(de,50);
        fr = new Country("FR", 67);
        fr.addBorder(lux,30);
        fr.addBorder(be,110);
        fr.addBorder(de,50);
        uk = new Country("UK", 67);
        uk.addBorder(be,70);
        uk.addBorder(fr,150);
        uk.addBorder(nl,250);

        ro = new Country("RO", 19);
        hu = new Country("HU", 10);
        ro.addBorder(hu,250);
    }

    /**
     * In our testing of the adjacency list, we ran into an issue.
     * We used the raw toString, which included a space between all neighbors.
     * However, in the given test, it shouldn't have a space .
     *
     * We used .replaceAll with a regex to remove the additional spaces.
     *
     */
    @Test
    void correctFormatOfAdjacencyList(){
        //Adjencency list with spaces (incorrect)
        assertNotEquals("Graph adjacency list:\n" +
                "NL: [DE ,BE, UK]\n" +
                "DE: [BE, FR, NL, LUX]\n" +
                "BE: [DE, UK, FR, NL, LUX]\n" +
                "UK: [BE, FR, NL]\n" +
                "FR: [DE, BE, UK, LUX]\n" +
                "LUX: [DE, BE, FR]\n", europe.formatAdjacencyList(nl));

        //Adjacency list without spaces (correct)
        assertEquals("Graph adjacency list:\n" +
                "NL: [DE,BE,UK]\n" +
                "DE: [BE,FR,NL,LUX]\n" +
                "BE: [DE,UK,FR,NL,LUX]\n" +
                "UK: [BE,FR,NL]\n" +
                "FR: [DE,BE,UK,LUX]\n" +
                "LUX: [DE,BE,FR]\n", europe.formatAdjacencyList(nl));
    }

    /**
     * In our testing of the depth first algorithm, we ran in to an issue.
     * Our exploration loop continued even though our target vertex was found.
     * Later we added the correct exit for the recursive loop
     */

    @Test
    void depthFirstShouldFindCorrectTargetVertex() {
        AbstractGraph.GPath path = europe.depthFirstSearch(uk,lux);
        assertNotNull(path);

        //assert if the path contains the target vertex
        assertTrue(path.getVertices().contains(lux));

        //assert that the target vertex is NOT the last vertex
        assertEquals(lux, path.getVertices().stream().reduce((c1,c2)->c2).get(),
                "Last country should should not be target vertex, you looped over it already...");
    }
}

package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.util.Objects;
import java.util.Random;

/**
 * An electable Candidate of a Party.
 * Every candidate shall have a unique (full) name within the party
 * (Different candidates of different parties may have duplicate names)
 */
public class Candidate {

    private final String firstName;
    private final String lastNamePrefix;
    private final String lastName;
    private Party party = null;

    public Candidate(String firstName, String lastNamePrefix, String lastName) {
        this.firstName = firstName;
        this.lastNamePrefix = lastNamePrefix;
        this.lastName = lastName;
    }
    public Candidate(String firstName, String lastNamePrefix, String lastName, Party party) {
        this(firstName, lastNamePrefix, lastName);
        this.setParty(party);
    }

    /**
     * Composes the full name of a candidate from its optional name components
     * Every candidate shall have at least a valid last name
     * Other name components could be null
     * @param firstName
     * @param lastNamePrefix
     * @param lastName
     * @return
     */
    public static String fullName(String firstName, String lastNamePrefix, String lastName) {
        // every candidate shall have a last name
        String fullName;

        if (firstName != null && lastNamePrefix == null) {
            fullName = firstName + " " + lastName;
            return fullName;
        }

        if (firstName == null && lastNamePrefix != null) {
            fullName = lastNamePrefix + " " + lastName;
            return fullName;
        }

        if (firstName == null){
            return lastName;
        }

        fullName = firstName + " " + lastNamePrefix + " " + lastName;

        return fullName;
    }

    public String getFullName() {
        return fullName(this.firstName, this.lastNamePrefix, this.lastName);
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "partyId=" + party.getId() +
                ",name='" + getFullName() + "'" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;
        Candidate other = (Candidate) o;

        //same name and same party
        if (this.getFullName().equals(((Candidate) o).getFullName()) && this.getParty() == ((Candidate) o).getParty()) return true;

        //same name different party
        if (this.getFullName().equals(((Candidate) o).getFullName()) && this.getParty() != ((Candidate) o).getParty()) return false;


        return false; // replace by a proper outcome
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = 31 * hash + this.getFullName().hashCode();
        hash = 31 * hash + this.party.hashCode();

        return hash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public static final String CANDIDATE = "Candidate";
    public static final String CANDIDATE_IDENTIFIER = "CandidateIdentifier";
    public static final String RANK = "Id";
    private static final String PERSON_NAME = "PersonName";
    private static final String NAME_LINE = "NameLine";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME_PREFIX = "NamePrefix";
    private static final String LAST_NAME = "LastName";
    /**
     * Auxiliary method for parsing the data from the EML files
     * This method can be used as-is and does not require your investigation or extension.
     */
    public static void importFromXml(XMLParser parser, Constituency constituency, Party party) throws XMLStreamException {
        parser.nextBeginTag(CANDIDATE);
        int rank = 0;
        String firstName = null;
        String lastNamePrefix = null;
        String lastName = "";
        if (parser.findBeginTag(CANDIDATE_IDENTIFIER)) {
            rank = parser.getIntegerAttributeValue(null, RANK, 0);
        }
        if (parser.findBeginTag(PERSON_NAME)) {
            if (parser.findBeginTag(NAME_LINE)) {
                parser.findAndAcceptEndTag(NAME_LINE);
            }
            if (parser.getLocalName().equals(FIRST_NAME)) {
                firstName = parser.getElementText().trim();
                parser.findAndAcceptEndTag(FIRST_NAME);
            }
            if (parser.getLocalName().equals(LAST_NAME_PREFIX)) {
                if (parser.findBeginTag(LAST_NAME_PREFIX)) {
                    lastNamePrefix = parser.getElementText().trim();
                    parser.findAndAcceptEndTag(LAST_NAME_PREFIX);
                }
            }
            if (parser.findBeginTag(LAST_NAME)) {
                lastName = parser.getElementText().trim();
                parser.findAndAcceptEndTag(LAST_NAME);
            }
            parser.findAndAcceptEndTag(PERSON_NAME);
        }
        parser.findAndAcceptEndTag(CANDIDATE);

        // fix duplicate names "Bos" at ranks 20 and 38 of party 13 (Martin and Johnny) in all contingencies
        if (firstName == null && rank == 20 && party.getId() == 13) firstName = "M.";
        if (firstName == null && rank == 38 && party.getId() == 13) firstName = "J.";

        // get the (shared) candidate instance from the party of add a new one
        Candidate candidate = party.addOrGetCandidate(new Candidate(firstName, lastNamePrefix, lastName));

        // register the candidate at the given rank into the contingency
        boolean registrationResult = constituency.register(rank, candidate);
        if (!registrationResult) {
            System.out.printf("Registration of %s in %s at rank=%d has failed\n", candidate, constituency, rank);
        }
    }
}

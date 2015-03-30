package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubjectAliasMapTest {

    @Mock
    private RestServerHelper restServerHelper;

    private SubjectAliasMap subjectAliasMap;
    private Optional<String> emptyString = Optional.empty();

    final String projectName1 = "MyProject1";
    final String projectName2 = "MyProject2";


    private final String patientId1 = "PatientOne1";
    private final String xnatSubjectName1 = "ResearchIdPatientOne";
    private final String hashedPatientId1 = OneWayHash.hashUid(patientId1);

    private final String patientId2 = "PatientTwo2";
    private final String xnatSubjectName2 = "ResearchIdPatientTwo";
    private final String hashedPatientId2 = OneWayHash.hashUid(patientId2);

    private final String patientId3 = "PatientThree3";
    private final String xnatSubjectName3 = "ResearchIdPatientThree";
    private final String hashedPatientId3 = OneWayHash.hashUid(patientId3);

    private final String patientId4 = "PatientFour4";
    private final String xnatSubjectName4 = "ResearchIdPatientFour";
    private final String hashedPatientId4 = OneWayHash.hashUid(patientId4);

    @Before
    public void setup() {
        subjectAliasMap = new SubjectAliasMap(restServerHelper);
    }

    @Test
    public void testNoId() throws IOException {
        {
            // Check there is no existing ID
            when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId1)).thenReturn(emptyString);
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId1);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }
    }

    @Test
    public void testAddSubjectAlias() throws IOException {
        {
            // Add a pseudo ID
            subjectAliasMap.addSubjectAlias(projectName1, patientId1, xnatSubjectName1);
            verify(restServerHelper, times(1)).createPseudonymIfNotExisting(projectName1, xnatSubjectName1, hashedPatientId1);
        }

        {
            // Check a different ID is not found
            when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId2)).thenReturn(emptyString);
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId2);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }

        {
            // Check the newly added pseudo ID has been found, with no server call required
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }

        {
            // Check the ID is not found for a different project
            when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId1)).thenReturn(emptyString);
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName2, patientId1);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }
    }

    @Test
    public void testGettingNameFromServer() throws IOException {
        // Set up server to return a pseudonym for id1 but not id2 (for project 1)
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId3)).thenReturn(emptyString);

        // And a different sequence for project 2
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId1)).thenReturn(emptyString);
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId4)).thenReturn(emptyString);

        // Project 1
        {
            // Check id1 returns a subject
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }

        {
            // Check id2 returns a subject
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId2);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName2);
        }

        {
            // Check id3 does not return a subject
            Assert.assertFalse(subjectAliasMap.getSubjectAlias(projectName1, patientId3).isPresent());
        }

        // Project 2
        {
            // Check id1 does not return a subject
            Assert.assertFalse(subjectAliasMap.getSubjectAlias(projectName2, patientId1).isPresent());
        }
        {
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName2, patientId2);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName2);
        }

        {
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName2, patientId3);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName3);
        }

        {
            Assert.assertFalse(subjectAliasMap.getSubjectAlias(projectName2, patientId4).isPresent());
        }
        {
            // Now set the server response for id 3 and check this works
            when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(projectName1, patientId3);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName3);
        }
    }

    @Test
    public void testCachingOfServerValues() throws IOException {

        // Set up server to return a pseudonym for id1 and id2 but not id3, and a different sequence for project 2
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId3)).thenReturn(emptyString);

        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId1)).thenReturn(emptyString);
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));

        {
            // Trigger caching of ids 1 and 2, while id 3 should not cache as it has not been set
            subjectAliasMap.getSubjectAlias(projectName1, patientId1);
            subjectAliasMap.getSubjectAlias(projectName1, patientId2);
            subjectAliasMap.getSubjectAlias(projectName1, patientId3);
            subjectAliasMap.getSubjectAlias(projectName2, patientId1);
            subjectAliasMap.getSubjectAlias(projectName2, patientId2);
            subjectAliasMap.getSubjectAlias(projectName2, patientId3);

            // Get all ids again. Where ids have been cached, this should not result in any further server calls
            subjectAliasMap.getSubjectAlias(projectName1, patientId1);
            subjectAliasMap.getSubjectAlias(projectName1, patientId2);
            subjectAliasMap.getSubjectAlias(projectName1, patientId3);
            subjectAliasMap.getSubjectAlias(projectName2, patientId1);
            subjectAliasMap.getSubjectAlias(projectName2, patientId2);
            subjectAliasMap.getSubjectAlias(projectName2, patientId3);

            // Verify that the server has only been called once for the ids that were set
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName1, hashedPatientId1);
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName1, hashedPatientId2);
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName2, hashedPatientId2);
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName2, hashedPatientId3);

            // Verify that the server was called both times for the unset ids
            verify(restServerHelper, times(2)).getSubjectPseudonym(projectName1, hashedPatientId3);
            verify(restServerHelper, times(2)).getSubjectPseudonym(projectName2, hashedPatientId1);

            // Now set the response for the previously unset ids and query them. This should trigger one extra call to the server, during which the result it cached.
            when(restServerHelper.getSubjectPseudonym(projectName1, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            when(restServerHelper.getSubjectPseudonym(projectName2, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
            subjectAliasMap.getSubjectAlias(projectName1, patientId3);
            subjectAliasMap.getSubjectAlias(projectName1, patientId3);
            subjectAliasMap.getSubjectAlias(projectName2, patientId1);
            subjectAliasMap.getSubjectAlias(projectName2, patientId1);
            verify(restServerHelper, times(3)).getSubjectPseudonym(projectName1, hashedPatientId3);
            verify(restServerHelper, times(3)).getSubjectPseudonym(projectName2, hashedPatientId1);
        }

    }

}
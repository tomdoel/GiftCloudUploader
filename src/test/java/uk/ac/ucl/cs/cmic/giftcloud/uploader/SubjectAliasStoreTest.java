/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubjectAliasStoreTest {

    @Mock
    private GiftCloudServer giftCloudServer;

    @Mock
    private GiftCloudReporter giftCloudReporter;

    @Mock
    private PatientListStore patientListStore;

    private SubjectAliasStore subjectAliasStore;
    private Optional<GiftCloudLabel.SubjectLabel> emptyString = Optional.empty();

    final String projectName1 = "MyProject1";
    final String projectName2 = "MyProject2";

    private final boolean requireHashing = true;


    private final String patientId1 = "PatientOne1";
    private final String patientName1 = "PatientName1";
    private final GiftCloudLabel.SubjectLabel xnatSubjectName1 = GiftCloudLabel.SubjectLabel.getFactory().create("ResearchIdPatientOne");
    private final String hashedPatientId1 = OneWayHash.hashUid(patientId1);

    private final String patientId2 = "PatientTwo2";
    private final String patientName2 = "PatientName2";
    private final GiftCloudLabel.SubjectLabel xnatSubjectName2 = GiftCloudLabel.SubjectLabel.getFactory().create("ResearchIdPatientTwo");
    private final String hashedPatientId2 = OneWayHash.hashUid(patientId2);

    private final String patientId3 = "PatientThree3";
    private final String patientName3 = "PatientName3";
    private final GiftCloudLabel.SubjectLabel xnatSubjectName3 = GiftCloudLabel.SubjectLabel.getFactory().create("ResearchIdPatientThree");
    private final String hashedPatientId3 = OneWayHash.hashUid(patientId3);

    private final String patientId4 = "PatientFour4";
    private final String patientName4 = "PatientName4";
    private final GiftCloudLabel.SubjectLabel xnatSubjectName4 = GiftCloudLabel.SubjectLabel.getFactory().create("ResearchIdPatientFour");
    private final String hashedPatientId4 = OneWayHash.hashUid(patientId4);

    @Before
    public void setup() {
        subjectAliasStore = new SubjectAliasStore(patientListStore, giftCloudReporter);
    }

    @Test
    public void testNoId() throws IOException {
        {
            // Check there is no existing ID
            when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId1)).thenReturn(emptyString);
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, patientName1);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testAddSubjectAliasNullProject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, null, patientId1, xnatSubjectName1, patientName1);
    }

    @Test
    public void testAddSubjectAliasEmptyProject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, "", patientId1, xnatSubjectName1, patientName1);
    }

    @Test
    public void testAddSubjectAliasNullSubject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, null, patientName1);
    }

    @Test
    public void testAddSubjectAliasEmptySubject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, GiftCloudLabel.SubjectLabel.getFactory().create(""), patientName1);
    }

    @Test
    public void testAddSubjectAliasNullPatientId() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, projectName1, null, xnatSubjectName1, patientName1);
    }

    @Test
    public void testAddSubjectAliasEmptyPatientId() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, projectName1, "", xnatSubjectName1, patientName1);
    }

    @Test
    public void testGetSubjectAliasNullProject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, null, patientId1, patientName1);
    }

    @Test
    public void testGetSubjectAliasEmptyProject() throws IOException {
        exception.expect(IllegalArgumentException.class);
        subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, "", patientId1, patientName1);
    }

    @Test
    public void testGetSubjectAliasNullPatientId() throws IOException {
        Optional<GiftCloudLabel.SubjectLabel> subjectName = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, null, patientName1);
        Assert.assertFalse(subjectName.isPresent());
    }

    @Test
    public void testGetSubjectAliasEmptyPatientId() throws IOException {
        Optional<GiftCloudLabel.SubjectLabel> subjectName = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, "", patientName1);
        Assert.assertFalse(subjectName.isPresent());
    }

    @Test
    public void testAddSubjectAlias() throws IOException {
        final boolean requireHashing = true;
        {
            // Add a pseudo ID
            subjectAliasStore.addSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, xnatSubjectName1, patientName1);
            verify(giftCloudServer, times(1)).createSubjectAliasIfNotExisting(projectName1, xnatSubjectName1, hashedPatientId1);
        }

        {
            // Check a different ID is not found
            when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId2)).thenReturn(emptyString);
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId2, patientName2);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }

        {
            // Check the newly added pseudo ID has been found, with no server call required
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, patientName1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }

        {
            // Check the ID is not found for a different project
            when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId1)).thenReturn(emptyString);
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }
    }

    @Test
    public void testGettingNameFromServer() throws IOException {
        final boolean requireHashing = true;

        // Set up server to return a pseudonym for id1 but not id2 (for project 1)
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId3)).thenReturn(emptyString);

        // And a different sequence for project 2
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId1)).thenReturn(emptyString);
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId4)).thenReturn(emptyString);

        // Project 1
        {
            // Check id1 returns a subject
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, patientName1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }

        {
            // Check id2 returns a subject
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId2, patientName2);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName2);
        }

        {
            // Check id3 does not return a subject
            Assert.assertFalse(subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3).isPresent());
        }

        // Project 2
        {
            // Check id1 does not return a subject
            Assert.assertFalse(subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1).isPresent());
        }
        {
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId2, patientName2);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName2);
        }

        {
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId3, patientName3);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName3);
        }

        {
            Assert.assertFalse(subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId4, patientName4).isPresent());
        }
        {
            // Now set the server response for id 3 and check this works
            when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            final Optional<GiftCloudLabel.SubjectLabel> subjectIdOptional = subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName3);
        }
    }

    @Test
    public void testCachingOfServerValues() throws IOException {

        final boolean requireHashing = true;

        // Set up server to return a pseudonym for id1 and id2 but not id3, and a different sequence for project 2
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId3)).thenReturn(emptyString);

        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId1)).thenReturn(emptyString);
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));

        {
            // Trigger caching of ids 1 and 2, while id 3 should not cache as it has not been set
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, patientName1);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId2, patientName2);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId2, patientName2);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId3, patientName3);

            // Get all ids again. Where ids have been cached, this should not result in any further server calls
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId1, patientName1);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId2, patientName2);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId2, patientName2);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId3, patientName3);

            // Verify that the server has only been called once for the ids that were set
            verify(giftCloudServer, times(1)).getSubjectLabel(projectName1, hashedPatientId1);
            verify(giftCloudServer, times(1)).getSubjectLabel(projectName1, hashedPatientId2);
            verify(giftCloudServer, times(1)).getSubjectLabel(projectName2, hashedPatientId2);
            verify(giftCloudServer, times(1)).getSubjectLabel(projectName2, hashedPatientId3);

            // Verify that the server was called both times for the unset ids
            verify(giftCloudServer, times(2)).getSubjectLabel(projectName1, hashedPatientId3);
            verify(giftCloudServer, times(2)).getSubjectLabel(projectName2, hashedPatientId1);

            // Now set the response for the previously unset ids and query them. This should trigger one extra call to the server, during which the result it cached.
            when(giftCloudServer.getSubjectLabel(projectName1, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            when(giftCloudServer.getSubjectLabel(projectName2, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName1, patientId3, patientName3);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1);
            subjectAliasStore.getSubjectAlias(requireHashing, giftCloudServer, projectName2, patientId1, patientName1);
            verify(giftCloudServer, times(3)).getSubjectLabel(projectName1, hashedPatientId3);
            verify(giftCloudServer, times(3)).getSubjectLabel(projectName2, hashedPatientId1);
        }

    }

}
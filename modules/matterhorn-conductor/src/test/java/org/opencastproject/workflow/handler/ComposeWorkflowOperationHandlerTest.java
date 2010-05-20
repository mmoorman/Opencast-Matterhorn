/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.workflow.handler;

import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.composer.api.EncodingProfile;
import org.opencastproject.composer.api.EncodingProfile.MediaType;
import org.opencastproject.media.mediapackage.MediaPackage;
import org.opencastproject.media.mediapackage.MediaPackageBuilder;
import org.opencastproject.media.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.media.mediapackage.Track;
import org.opencastproject.receipt.api.Receipt;
import org.opencastproject.util.MimeTypes;
import org.opencastproject.workflow.api.WorkflowInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowInstance.WorkflowState;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposeWorkflowOperationHandlerTest {
  private ComposeWorkflowOperationHandler operationHandler;

  // local resources
  private MediaPackage mp;
  private MediaPackage mpEncode;
  private Receipt receipt;
  private Track[] encodedTracks;
  EncodingProfile[] profileList;

  // mock services and objects
  private EncodingProfile profile = null;
  private ComposerService composerService = null;

  // constant metadata values
  private static final String PROFILE_ID = "flash.http";
  private static final String SOURCE_TRACK_ID = "compose-workflow-operation-test-source-track-id";
  //private static final String SOURCE_VIDEO_ID = "compose-workflow-operation-test-source-video-id";
  //private static final String SOURCE_AUDIO_ID = "compose-workflow-operation-test-source-audio-id";
  private static final String ENCODED_TRACK_ID = "compose-workflow-operation-test-encode-track-id";

  @Before
  public void setup() throws Exception {
    MediaPackageBuilder builder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();

    // test resources
    URI uriMP = InspectWorkflowOperationHandler.class.getResource("/compose_mediapackage.xml").toURI();
    URI uriMPEncode = InspectWorkflowOperationHandler.class.getResource("/compose_encode_mediapackage.xml").toURI();
    mp = builder.loadFromXml(uriMP.toURL().openStream());
    mpEncode = builder.loadFromXml(uriMPEncode.toURL().openStream());
    encodedTracks = mpEncode.getTracks();

    // set up service
    operationHandler = new ComposeWorkflowOperationHandler();

  }

  @Test
  public void testComposeWOHEncodedTrack() throws Exception {
    // set up mock profile
    profile = EasyMock.createNiceMock(EncodingProfile.class);
    EasyMock.expect(profile.getIdentifier()).andReturn(PROFILE_ID);
    EasyMock.expect(profile.getApplicableMediaType()).andReturn(MediaType.Stream);
    EasyMock.expect(profile.getOutputType()).andReturn(MediaType.Stream);
    EasyMock.expect(profile.getMimeType()).andReturn(MimeTypes.MPEG4.asString()).times(2);
    profileList = new EncodingProfile[] { profile };
    EasyMock.replay(profile);

    // set up mock receipt
    receipt = EasyMock.createNiceMock(Receipt.class);
    EasyMock.expect(receipt.getElement()).andReturn(encodedTracks[0]);
    EasyMock.replay(receipt);

    // set up mock composer service
    composerService = EasyMock.createNiceMock(ComposerService.class);
    EasyMock.expect(composerService.listProfiles()).andReturn(profileList);
    EasyMock.expect(
            composerService.encode((MediaPackage) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                    (String) EasyMock.anyObject(), (String) EasyMock.anyObject(), EasyMock.anyBoolean())).andReturn(
            receipt);
    EasyMock.replay(composerService);
    operationHandler.setComposerService(composerService);

    // operation configuration
    String targetTags = "engage,rss";
    Map<String, String> configurations = new HashMap<String, String>();
    configurations.put("source-video-flavor", "presentation/source");
    configurations.put("source-audio-flavor", "*/source");
    configurations.put("target-tags", targetTags);
    configurations.put("target-flavor", "presenter/delivery");
    configurations.put("encoding-profile", "flash.http");

    // run the operation handler
    WorkflowOperationResult result = getWorkflowOperationResult(mp, configurations);

    // check track metadata
    MediaPackage mpNew = result.getMediaPackage();
    Track trackEncoded = mpNew.getTrack(ENCODED_TRACK_ID);
    Assert.assertEquals("presenter/delivery", trackEncoded.getFlavor().toString());
    Assert.assertArrayEquals(targetTags.split("\\W"), trackEncoded.getTags());
    Assert.assertEquals(MimeTypes.MPEG4, trackEncoded.getMimeType());
    Assert.assertEquals(SOURCE_TRACK_ID, trackEncoded.getReference().getIdentifier());
  }

  @Test
  public void testComposeWOHMissingData() throws Exception {
    // set up mock profile
    profile = EasyMock.createNiceMock(EncodingProfile.class);
    EasyMock.expect(profile.getIdentifier()).andReturn(PROFILE_ID);
    EasyMock.expect(profile.getApplicableMediaType()).andReturn(MediaType.Stream);
    EasyMock.expect(profile.getOutputType()).andReturn(MediaType.Stream);
    EasyMock.expect(profile.getMimeType()).andReturn(MimeTypes.MPEG4.asString()).times(2);
    profileList = new EncodingProfile[] { profile };
    EasyMock.replay(profile);

    // set up mock receipt
    receipt = EasyMock.createNiceMock(Receipt.class);
    EasyMock.expect(receipt.getElement()).andReturn(encodedTracks[0]);
    EasyMock.replay(receipt);

    // set up mock composer service
    composerService = EasyMock.createNiceMock(ComposerService.class);
    EasyMock.expect(composerService.listProfiles()).andReturn(profileList);
    EasyMock.expect(
            composerService.encode((MediaPackage) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                    (String) EasyMock.anyObject(), (String) EasyMock.anyObject(), EasyMock.anyBoolean())).andReturn(
            receipt);
    EasyMock.replay(composerService);
    operationHandler.setComposerService(composerService);

    Map<String, String> configurations = new HashMap<String, String>();
    try {
      // no source flavour
      getWorkflowOperationResult(mp, configurations);
      Assert.fail("Since neither source audio nor source video flavour is specified exception should be thrown");
    } catch (WorkflowOperationException e) {
      // expecting exception
    }

    try {
      // no source flavour
      configurations.put("source-video-flavor", "presentation/source");
      getWorkflowOperationResult(mp, configurations);
      Assert.fail("Since encoding profile is not specified exception should be thrown");
    } catch (WorkflowOperationException e) {
      // expecting exception
    }

  }

  private WorkflowOperationResult getWorkflowOperationResult(MediaPackage mp, Map<String, String> configurations)
          throws WorkflowOperationException {
    // Add the mediapackage to a workflow instance
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.setId("workflow-encode-test");
    workflowInstance.setState(WorkflowState.RUNNING);
    workflowInstance.setMediaPackage(mp);
    WorkflowOperationInstanceImpl operationInstance = new WorkflowOperationInstanceImpl();
    for (String key : configurations.keySet()) {
      operationInstance.setConfiguration(key, configurations.get(key));
    }

    List<WorkflowOperationInstance> operationsList = new ArrayList<WorkflowOperationInstance>();
    operationsList.add(operationInstance);
    workflowInstance.setOperations(operationsList);
    workflowInstance.next(); // Simulate starting the workflow

    // Run the media package through the operation handler, ensuring that metadata gets added
    return operationHandler.start(workflowInstance);
  }
}
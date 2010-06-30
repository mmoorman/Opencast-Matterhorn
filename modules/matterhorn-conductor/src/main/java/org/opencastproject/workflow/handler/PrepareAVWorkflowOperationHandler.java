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
import org.opencastproject.composer.api.EncoderException;
import org.opencastproject.composer.api.EncodingProfile;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageException;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.remote.api.Receipt;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowBuilder;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The <tt>prepare media</tt> operation will make sure that media where audio and video track come in separate files
 * will be muxed prior to further processing.
 */
public class PrepareAVWorkflowOperationHandler extends AbstractWorkflowOperationHandler {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ComposeWorkflowOperationHandler.class);

  /** Name of the 'encode to a/v work copy' encoding profile */
  public static final String PREPARE_AV_PROFILE = "av.work";

  /** Name of the muxing encoding profile */
  public static final String MUX_AV_PROFILE = "mux-av.work";

  /** Name of the 'encode to audio only work copy' encoding profile */
  public static final String PREPARE_AONLY_PROFILE = "audio-only.work";

  /** Name of the 'encode to video only work copy' encoding profile */
  public static final String PREPARE_VONLY_PROFILE = "video-only.work";

  /** The configuration options for this handler */
  private static final SortedMap<String, String> CONFIG_OPTIONS;

  static {
    CONFIG_OPTIONS = new TreeMap<String, String>();
    CONFIG_OPTIONS.put("source-flavor", "The \"flavor\" of the track to use as a video source input");
    CONFIG_OPTIONS.put("encoding-profile", "The encoding profile to use (default is 'mux-av.http')");
    CONFIG_OPTIONS.put("target-flavor", "The flavor to apply to the encoded file");
    CONFIG_OPTIONS.put("reencode", "Indicating whether audio and video tracks should be reencoded");
    CONFIG_OPTIONS.put("target-tags", "The tags to apply to the encoded file");
  }

  /** The composer service */
  private ComposerService composerService = null;

  /** The local workspace */
  private Workspace workspace = null;

  /**
   * Callback for the OSGi declarative services configuration.
   * 
   * @param composerService
   *          the local composer service
   */
  protected void setComposerService(ComposerService composerService) {
    this.composerService = composerService;
  }

  /**
   * Callback for declarative services configuration that will introduce us to the local workspace service.
   * Implementation assumes that the reference is configured as being static.
   * 
   * @param workspace
   *          an instance of the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowOperationHandler#getConfigurationOptions()
   */
  @Override
  public SortedMap<String, String> getConfigurationOptions() {
    return CONFIG_OPTIONS;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance)
   */
  public WorkflowOperationResult start(final WorkflowInstance workflowInstance) throws WorkflowOperationException {
    logger.debug("Running a/v muxing workflow operation on workflow {}", workflowInstance.getId());
    MediaPackage resultingMediaPackage = null;
    try {
      resultingMediaPackage = mux(workflowInstance.getMediaPackage(), workflowInstance.getCurrentOperation());
    } catch (Exception e) {
      throw new WorkflowOperationException(e);
    }
    logger.debug("Media package tracks muxed");
    return WorkflowBuilder.getInstance().buildWorkflowOperationResult(resultingMediaPackage, Action.CONTINUE);
  }

  /**
   * Merges audio and video track of the selected flavor and adds it to the media package. If there is nothing to mux, a
   * new track with the target flavor is created (pointing to the original url).
   * 
   * @param src
   *          The source media package
   * @param operation
   *          the mux workflow operation
   * @return the mediapackage
   * @throws EncoderException
   *           if encoding fails
   * @throws IOException
   *           if read/write operations from and to the workspace fail
   * @throws NotFoundException
   *           if the workspace does not contain the requested element
   */
  private MediaPackage mux(MediaPackage src, WorkflowOperationInstance operation) throws EncoderException,
          WorkflowOperationException, MediaPackageException, NotFoundException, IOException {
    MediaPackage mediaPackage = (MediaPackage) src.clone();

    // Read the configuration properties
    String sourceFlavorName = StringUtils.trimToNull(operation.getConfiguration("source-flavor"));
    String targetTrackTags = StringUtils.trimToNull(operation.getConfiguration("target-tags"));
    String targetTrackFlavorName = StringUtils.trimToNull(operation.getConfiguration("target-flavor"));
    String encodingProfileName = StringUtils.trimToNull(operation.getConfiguration("encoding-profile"));

    // Make sure the source flavor is properly set
    if (sourceFlavorName == null)
      throw new IllegalStateException("Source flavor must be specified");
    MediaPackageElementFlavor sourceFlavor = MediaPackageElementFlavor.parseFlavor(sourceFlavorName);

    // Make sure the target flavor is properly set
    if (targetTrackFlavorName == null)
      throw new IllegalStateException("Target flavor must be specified");
    MediaPackageElementFlavor targetFlavor = MediaPackageElementFlavor.parseFlavor(targetTrackFlavorName);

    if (encodingProfileName == null)
      encodingProfileName = MUX_AV_PROFILE;

    // Find the encoding profile
    EncodingProfile profile = composerService.getProfile(encodingProfileName);
    if (profile == null) {
      throw new IllegalStateException("Encoding profile '" + encodingProfileName + "' was not found");
    }

    // Reencode when there is no need for muxing?
    boolean reencode = true;
    if (StringUtils.trimToNull(operation.getConfiguration("reencode")) != null) {
      reencode = Boolean.parseBoolean(operation.getConfiguration("reencode"));
    }

    // Select those tracks that have matching flavors
    Track[] tracks = mediaPackage.getTracks(sourceFlavor);

    Track audioTrack = null;
    Track videoTrack = null;

    switch (tracks.length) {
    case 0:
      logger.info("No audio/video tracks with flavor '{}' found to prepare", sourceFlavor);
      return mediaPackage;
    case 1:
      audioTrack = videoTrack = tracks[0];
      break;
    case 2:
      for (Track track : tracks) {
        if (track.hasAudio() && !track.hasVideo()) {
          audioTrack = track;
        } else if (!track.hasAudio() && track.hasVideo()) {
          videoTrack = track;
        }
      }
      break;
    default:
      logger.error("More than two tracks with flavor {} found. No idea what we should be doing", sourceFlavor);
      throw new WorkflowOperationException("More than two tracks with flavor '" + sourceFlavor + "' found");
    }

    Receipt receipt = null;
    Track composedTrack = null;

    // Make sure we have a matching combination
    if (audioTrack == null && videoTrack != null) {
      if (reencode) {
        logger.info("Encoding video only track {} to work version", videoTrack);
        receipt = composerService.encode(videoTrack, PREPARE_VONLY_PROFILE, true);
        composedTrack = (Track) receipt.getElement();
      } else {
        composedTrack = (Track) videoTrack.clone();
        composedTrack.setIdentifier(null);
      }
    } else if (videoTrack == null && audioTrack != null) {
      if (reencode) {
        logger.info("Encoding audio only track {} to work version", audioTrack);
        receipt = composerService.encode(audioTrack, PREPARE_AONLY_PROFILE, true);
        composedTrack = (Track) receipt.getElement();
      } else {
        composedTrack = (Track) audioTrack.clone();
        composedTrack.setIdentifier(null);
      }
    } else if (audioTrack == videoTrack) {
      if (reencode) {
        logger.info("Encoding audiovisual track {} to work version", videoTrack);
        receipt = composerService.encode(videoTrack, PREPARE_AV_PROFILE, true);
        composedTrack = (Track) receipt.getElement();
      } else {
        composedTrack = (Track) videoTrack.clone();
        composedTrack.setIdentifier(null);
      }
    } else {
      logger.info("Muxing audio and video only track {} to work version", videoTrack);
      receipt = composerService.mux(videoTrack, audioTrack, profile.getIdentifier(), true);
      composedTrack = (Track) receipt.getElement();
    }

    // Update the track's flavor
    composedTrack.setFlavor(targetFlavor);
    logger.debug("Composed track has flavor '{}'", composedTrack.getFlavor());

    // Add the target tags
    List<String> targetTags = asList(targetTrackTags);
    for (String tag : targetTags) {
      logger.trace("Tagging composed track with '{}'", tag);
      composedTrack.addTag(tag);
    }

    mediaPackage.add(composedTrack);
    composedTrack.setURI(workspace.moveTo(composedTrack.getURI(), mediaPackage.getIdentifier().toString(), composedTrack.getIdentifier()));
    return mediaPackage;
  }

}
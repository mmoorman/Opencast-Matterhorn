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

import java.net.URI;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementBuilderFactory;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.remote.api.Job;
import org.opencastproject.workflow.api.AbstractResumableWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowBuilder;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation that holds for user-entered trim points.
 */
public class TrimWorkflowOperationHandler extends AbstractResumableWorkflowOperationHandler {

  private static final Logger logger = LoggerFactory.getLogger(TrimWorkflowOperationHandler.class);

  /** The configuration options for this handler */
  private static final SortedMap<String, String> CONFIG_OPTIONS;

  /** Path to the hold ui resources */
  private static final String HOLD_UI_PATH = "/ui/operation/trim/index.html";

  /** Name of the configuration option that provides the source flavors we are looking for */
  private static final String SOURCE_FLAVOR_PROPERTY = "source-flavor";

  /** Name of the configuration option that provides the target flavors we will produce */
  private static final String TARGET_FLAVOR_SUBTYPE_PROPERTY = "target-flavor-subtype";

  /** Name of the configuration option that provides the encoding profile */
  private static final String ENCODING_PROFILE_PROPERTY = "encoding-profile";

  /** The composer service */
  private ComposerService composerService;

  /** The workspace */
  private Workspace workspace;

  static {
    CONFIG_OPTIONS = new TreeMap<String, String>();
    CONFIG_OPTIONS.put(SOURCE_FLAVOR_PROPERTY, "The flavors to trim");
    CONFIG_OPTIONS.put(TARGET_FLAVOR_SUBTYPE_PROPERTY,
            "The flavor subtype of the target track.  If this is set to \"trimmed\", "
                    + "a source track of \"presenter/work\" would become \"presenter/trimmed\"");
    CONFIG_OPTIONS.put(REQUIRED_PROPERTY, "The configuration key that must be set to true for "
            + "this operation to run.");
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

  public void activate(ComponentContext cc) {
    super.activate(cc);
    setHoldActionTitle("Trim");
    registerHoldStateUserInterface(HOLD_UI_PATH);
    logger.info("Registering trim hold state ui from classpath {}", HOLD_UI_PATH);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance) throws WorkflowOperationException {
    if (!"true".equalsIgnoreCase(workflowInstance.getCurrentOperation().getConfiguration(REQUIRED_PROPERTY))) {
      // If we do not hold for trim, we still need to put tracks in the mediapackage with the right flavor
      MediaPackage mediaPackage = workflowInstance.getMediaPackage();
      WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();
      String configuredSourceFlavor = currentOperation.getConfiguration(SOURCE_FLAVOR_PROPERTY);
      String configuredTargetFlavorSubtype = currentOperation.getConfiguration(TARGET_FLAVOR_SUBTYPE_PROPERTY);
      MediaPackageElementFlavor matchingFlavor = MediaPackageElementFlavor.parseFlavor(configuredSourceFlavor);
      for (Track t : workflowInstance.getMediaPackage().getTracks()) {
        MediaPackageElementFlavor trackFlavor = t.getFlavor();
        if (trackFlavor != null && trackFlavor.matches(matchingFlavor)) {
          Track clonedTrack = (Track) t.clone();
          clonedTrack.setIdentifier(null);
          clonedTrack.setURI(t.getURI()); // use the same URI as the original
          clonedTrack.setFlavor(new MediaPackageElementFlavor(trackFlavor.getType(), configuredTargetFlavorSubtype));
          mediaPackage.addDerived(clonedTrack, t);
        }
      }
      return WorkflowBuilder.getInstance().buildWorkflowOperationResult(mediaPackage, Action.CONTINUE);
    }

    logger.info("Holding for trim...");

    return WorkflowBuilder.getInstance().buildWorkflowOperationResult(Action.PAUSE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.ResumableWorkflowOperationHandler#resume(org.opencastproject.workflow.api.WorkflowInstance,
   *      java.util.Map)
   */
  @Override
  public WorkflowOperationResult resume(WorkflowInstance workflowInstance, Map<String, String> properties)
          throws WorkflowOperationException {
    logger.info("Trimming workflow {} using {}", workflowInstance.getId(), properties);

    // Get the source flavor to match
    WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();
    String configuredSourceFlavor = currentOperation.getConfiguration(SOURCE_FLAVOR_PROPERTY);
    String configuredTargetFlavorSubtype = currentOperation.getConfiguration(TARGET_FLAVOR_SUBTYPE_PROPERTY);
    MediaPackageElementFlavor matchingFlavor = MediaPackageElementFlavor.parseFlavor(configuredSourceFlavor);
    for (Track t : workflowInstance.getMediaPackage().getTracks()) {
      MediaPackageElementFlavor trackFlavor = t.getFlavor();
      if (trackFlavor != null && matchingFlavor.matches(trackFlavor)) {
        String profileId = currentOperation.getConfiguration(ENCODING_PROFILE_PROPERTY);
        long start = Long.parseLong(properties.get("trimin"));
        long duration = Long.parseLong(properties.get("trimout"));
        Track trimmedTrack = null;
        try {
          // Trim the track
          Job receipt = composerService.trim(t, profileId, start, duration, true);
          trimmedTrack = (Track) receipt.getElement();
          if (trimmedTrack == null) {
            throw new WorkflowOperationException("Trimming failed to produce a track");
          }

          // Put the new track in the mediapackage area of the workspace
          String fileName = getFileNameFromElements(t, trimmedTrack);
          URI uri = workspace.moveTo(trimmedTrack.getURI(), workflowInstance.getMediaPackage().getIdentifier()
                  .compact(), trimmedTrack.getIdentifier(), fileName);
          trimmedTrack.setURI(uri);

          // Fix the flavor
          MediaPackageElementFlavor trimmedFlavor = new MediaPackageElementFlavor(t.getFlavor().getType(),
                  configuredTargetFlavorSubtype);
          trimmedTrack.setFlavor(trimmedFlavor);

          // Add the trimmed track to the mediapackage
          workflowInstance.getMediaPackage().addDerived(trimmedTrack, t);
        } catch (Exception e) {
          logger.warn("Unable to trim: {}", e);
          throw new WorkflowOperationException(e);
        }
      }
    }
    return super.resume(workflowInstance, properties);
  }

  /**
   * Sets the composer service.
   * 
   * @param composerService
   *          the composer service
   */
  public void setComposerService(ComposerService composerService) {
    this.composerService = composerService;
  }

  /**
   * Sets the workspace
   * 
   * @param workspace
   *          the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }
}
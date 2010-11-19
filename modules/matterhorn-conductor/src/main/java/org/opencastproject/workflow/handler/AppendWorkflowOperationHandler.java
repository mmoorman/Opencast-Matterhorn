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

import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.AbstractResumableWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowBuilder;
import org.opencastproject.workflow.api.WorkflowDatabaseException;
import org.opencastproject.workflow.api.WorkflowDefinition;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationDefinition;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workflow.api.WorkflowService;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Workflow operation handler that will add a number of workflow operations to the current workflow.
 * <p>
 * There are basically two ways that the handler works. First, it will look into the workflow configuration for a
 * property named <code>workflow.definition</code>. If that is found, it will take the operations from that workflow,
 * append them and continue.
 * <p>
 * If not, the operation handler will enter a hold state and ask the user which workflow to use.
 */
public class AppendWorkflowOperationHandler extends AbstractResumableWorkflowOperationHandler {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AppendWorkflowOperationHandler.class);

  /** Configuration value for the workflow operation definition */
  public static final String OPT_WORKFLOW = "workflow.definition";

  /** Path to the hold state ui */
  public static final String UI_RESOURCE_PATH = "/ui/operation/append/index.html";

  /** The workflow service instance */
  WorkflowService workflowService = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.AbstractResumableWorkflowOperationHandler#activate(org.osgi.service.component.ComponentContext)
   */
  @Override
  public void activate(ComponentContext componentContext) {
    super.activate(componentContext);

    // Register the supported configuration options
    addConfigurationOption(OPT_WORKFLOW, "Workflow definition identifier");

    // Add the ui piece that displays the capture information
    registerHoldStateUserInterface(UI_RESOURCE_PATH);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.AbstractResumableWorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance) throws WorkflowOperationException {
    String workflowDefinitionId = workflowInstance.getConfiguration(OPT_WORKFLOW);
    if (append(workflowInstance, workflowDefinitionId))
      return WorkflowBuilder.getInstance().buildWorkflowOperationResult(Action.CONTINUE);
    else
      logger.info("Entering hold state to ask for workflow");
    return WorkflowBuilder.getInstance().buildWorkflowOperationResult(Action.PAUSE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.AbstractResumableWorkflowOperationHandler#resume(org.opencastproject.workflow.api.WorkflowInstance,
   *      java.util.Map)
   */
  @Override
  public WorkflowOperationResult resume(WorkflowInstance workflowInstance, Map<String, String> properties) {
    String workflowDefinitionId = workflowInstance.getConfiguration(OPT_WORKFLOW);
    if (append(workflowInstance, workflowDefinitionId))
      return WorkflowBuilder.getInstance().buildWorkflowOperationResult(Action.CONTINUE);
    else
      logger.info("Entering hold state to ask for workflow");
    return WorkflowBuilder.getInstance().buildWorkflowOperationResult(Action.PAUSE);
  }

  /**
   * Adds the operations found in the workflow defined by <code>workflowDefintionId</code> to the workflow instance and
   * returns <code>true</code> if everything worked fine, <code>false</code> otherwhise.
   * 
   * 
   * @param workflowInstance
   *          the instance to update
   * @param workflowDefinitionId
   *          id of the workflow definition
   * @return <code>true</code> if all operations have been added
   */
  protected boolean append(WorkflowInstance workflowInstance, String workflowDefinitionId) {
    if (StringUtils.isBlank(workflowDefinitionId))
      return false;

    try {
      WorkflowDefinition definition = workflowService.getWorkflowDefinitionById(workflowDefinitionId);
      if (definition != null) {
        List<WorkflowOperationInstance> operations = new ArrayList<WorkflowOperationInstance>();
        for (WorkflowOperationDefinition operationDefinition : definition.getOperations()) {
          WorkflowOperationInstance operation = new WorkflowOperationInstanceImpl(operationDefinition);
          logger.debug("Adding workflow operation '{}' to '{}'", operationDefinition.getId(), workflowInstance.getId());
          operations.add(operation);
        }
        List<WorkflowOperationInstance> currentOperations = workflowInstance.getOperations();
        currentOperations.addAll(operations);
        workflowInstance.setOperations(currentOperations);
        return true;
      }
    } catch (WorkflowDatabaseException e) {
      logger.warn("Error querying workflow service for '{}'", workflowDefinitionId, e);
    } catch (NotFoundException e) {
      logger.warn("Workflow '{}' not found. Entering hold state to resolve", workflowDefinitionId);
    }

    logger.info("Entering hold state to ask for workflow");
    return false;
  }

  /**
   * Callback from the OSGi environment that will pass a reference to the workflow service upon component acitvation.
   * 
   * @param service
   *          the workflow service
   */
  void setWorkflowService(WorkflowService service) {
    this.workflowService = service;
  }

}
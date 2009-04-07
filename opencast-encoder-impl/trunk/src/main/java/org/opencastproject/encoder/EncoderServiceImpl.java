/**
 *  Copyright 2009 Opencast Project (http://www.opencastproject.org)
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
package org.opencastproject.encoder;

import org.opencastproject.encoder.api.EncoderService;
import org.opencastproject.status.api.StatusMessage;
import org.opencastproject.status.impl.StatusMessageImpl;

import java.util.UUID;

import javax.jws.WebService;
@WebService(endpointInterface = "org.opencastproject.encoder.api.EncoderService",
    serviceName = "EncoderService")
public class EncoderServiceImpl implements EncoderService {

  public StatusMessage encode(String pathIn, String pathOut, String statusServiceEndpoint) {
    // TODO actually start or queue (and change the message parameter) the encoding job
    return new StatusMessageImpl("encoding job started", UUID.randomUUID().toString(),
        EncoderService.class.getName());
  }

}

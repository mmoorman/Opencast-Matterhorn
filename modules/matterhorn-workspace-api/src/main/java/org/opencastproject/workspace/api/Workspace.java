/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencastproject.workspace.api;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * Provides efficient access java.io.File objects from potentially remote URIs. This helper service
 * prevents different service implementations running in the same osgi container from downloading
 * remote files multiple times.
 * 
 * Additionally, when the system is configured to use shared storage, this performance gain is also
 * achieved across distributed osgi containers.  The methods from WorkingFileRepository are also
 * available as a convenience to clients.
 */
public interface Workspace {
  /**
   * Gets a locally cached {@link File} for the given URI.
   * 
   * @param uri
   * @return
   */
  File get(URI uri);

  /**
   * Store the data stream under the given media package and element IDs.
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @param in
   */
  URI put(String mediaPackageID, String mediaPackageElementID, InputStream in);

  /**
   * Store the data stream under the given media package and element IDs, specifying a filename.
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @param fileName
   * @param in
   */
  URI put(String mediaPackageID, String mediaPackageElementID, String fileName, InputStream in);

  /**
   * Stream the file stored under the given media package and element IDs.
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @return
   */
  InputStream get(String mediaPackageID, String mediaPackageElementID);
  
  /**
   * Delete the file stored at the given media package and element IDs.
   * @param mediaPackageID
   * @param mediaPackageElementID
   */
  void delete(String mediaPackageID, String mediaPackageElementID);

  /**
   * Get the URL of the file stored under the given media package and element IDs.
   * @param mediaPackageID
   * @param mediaPackageElementID
   * @return
   * @throws MalformedURLException 
   */
  URI getURI(String mediaPackageID, String mediaPackageElementID) throws MalformedURLException;

}
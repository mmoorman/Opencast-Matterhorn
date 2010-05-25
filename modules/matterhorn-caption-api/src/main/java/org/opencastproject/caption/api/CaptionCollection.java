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
package org.opencastproject.caption.api;

import java.util.Iterator;


/**
 * Represents collection of all captions.
 * 
 */
public interface CaptionCollection {

  /**
   * Returns collection name.
   * @return collection name
   */
  // FIXME needed?
  String getCollectionName();
  
  /**
   * Add single caption to the end of the collection.
   * @param caption
   */
  void addCaption(Caption caption);
  
  // void setGlobalTextStyles(HashMap<String, String> globalTextStyles);
  // void setTxtStylesGlobalAtt(key, value)
  
  /**
   * Get iterator over caption collection.
   * @param iterator over captions
   */
  Iterator<Caption> getCollectionIterator();
}
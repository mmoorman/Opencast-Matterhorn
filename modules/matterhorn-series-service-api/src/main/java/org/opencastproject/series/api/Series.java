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

package org.opencastproject.series.api;

import org.opencastproject.metadata.dublincore.DublinCoreCatalog;

import java.util.List;

public interface Series extends Comparable<Series> {

  public abstract String getSeriesId();

  public abstract void setSeriesId(String seriesId);

  public abstract void addToMetadata(String key, String value);

  public abstract List<SeriesMetadata> getMetadata();

  public abstract void setMetadata(List<SeriesMetadata> metadata);

  public abstract DublinCoreCatalog getDublinCore();

  public abstract boolean valid();
  
  public String getDescription();
  
  public String getFromMetadata(String key);

}

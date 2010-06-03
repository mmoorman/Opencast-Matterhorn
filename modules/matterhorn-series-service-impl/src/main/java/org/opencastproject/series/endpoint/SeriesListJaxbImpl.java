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
package org.opencastproject.series.endpoint;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A {@link List} of {@link SeriesJaxbImpl}s
 */
@XmlType(name="SeriesList", namespace="http://series.opencastproject.org")
@XmlRootElement(name="SeriesList", namespace="http://series.opencastproject.org")
@XmlAccessorType(XmlAccessType.FIELD)
public class SeriesListJaxbImpl {

  protected LinkedList<SeriesJaxbImpl> series;
  
  public SeriesListJaxbImpl(){
    series = new LinkedList<SeriesJaxbImpl>();
  }
  
  public SeriesListJaxbImpl(LinkedList<SeriesJaxbImpl> s){
    series = s;
  }
}
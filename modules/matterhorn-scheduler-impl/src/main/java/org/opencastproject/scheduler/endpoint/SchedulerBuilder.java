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
package org.opencastproject.scheduler.endpoint;

import org.apache.commons.io.IOUtils;
import org.opencastproject.scheduler.impl.jpa.Event;
import org.opencastproject.scheduler.impl.jpa.Metadata;
import org.opencastproject.scheduler.impl.jpa.RecurringEvent;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

public class SchedulerBuilder {

  /** Builder singleton */
  private static SchedulerBuilder instance = null;
  
  protected JAXBContext jaxbContext = null;
  protected JAXBContext jaxbContextJPA = null;
  
  /**
   *  Set up the JAXBContext.
   *
   */
  private SchedulerBuilder() throws JAXBException {
    jaxbContext = JAXBContext.newInstance("org.opencastproject.scheduler.endpoint", SchedulerBuilder.class.getClassLoader());
    jaxbContextJPA = JAXBContext.newInstance("org.opencastproject.scheduler.impl.jpa", SchedulerBuilder.class.getClassLoader());
  }
  
  /**
   * Returns an instance of the {@link SchedulerBuilder}
   *
   * @return a factory
   */
  public static SchedulerBuilder getInstance() {
    if(instance == null){
      try {
        instance = new SchedulerBuilder();
      }
      catch (JAXBException e) {
        throw new RuntimeException(e.getLinkedException() != null ? e.getLinkedException() : e);
      }
    }
    return instance;
  }
  
  /**
   * This loads an event from an input stream containing an XML representation of an event.
   *
   * @param   in - the input stream
   * @return  a SchedulerEventJaxImpl
   * @throws   And exception if creation of the event fails.
   */
  public SchedulerEventJaxbImpl parseSchedulerEventJaxbImpl(InputStream in) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
                                  SchedulerEventJaxbImpl.class).getValue();
  }
  
  /**
   * Take a string, convert to input stream and pass to parseSchedulerEventJaxbImpl()
   *
   * @param   in - a string containing event representation
   * @return  a SchedulerEventJaxbImpl
   * @throws  Exception if creation fails
   */
  public SchedulerEventJaxbImpl parseSchedulerEventJaxbImpl(String in) throws Exception {
    return parseSchedulerEventJaxbImpl(IOUtils.toInputStream(in, "UTF8"));
  }
  
  /**
   * todo: Comment me!
   */
  public SchedulerFilterJaxbImpl parseSchedulerFilterJaxbImpl(InputStream in) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
                                  SchedulerFilterJaxbImpl.class).getValue();
  }
  
  /**
   * Todo: comment me!
   */
  public SchedulerFilterJaxbImpl parseSchedulerFilterJaxbImpl(String in) throws Exception {
    return parseSchedulerFilterJaxbImpl(IOUtils.toInputStream(in, "UTF8"));
  }

  public Event parseEvent(String in) throws Exception {
    return parseEvent(IOUtils.toInputStream(in, "UTF8"));
  }
  
  public Event parseEvent(InputStream in) throws Exception {
    Unmarshaller unmarshaller = jaxbContextJPA.createUnmarshaller();
    return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
                                  Event.class).getValue();
  }
  
  public RecurringEvent parseRecurringEvent(String in) throws Exception {
    return parseRecurringEvent(IOUtils.toInputStream(in, "UTF8"));
  }
  
  public RecurringEvent parseRecurringEvent(InputStream in) throws Exception {
    Unmarshaller unmarshaller = jaxbContextJPA.createUnmarshaller();
    return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
                                  RecurringEvent.class).getValue();
  } 
  
  public String marshallRecurringEvent (RecurringEvent e) throws Exception {
    Marshaller marshaller = jaxbContextJPA.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(e, writer);
    return writer.toString();
  }
  
  public String marshallEvent (Event e) throws Exception {
    Marshaller marshaller = jaxbContextJPA.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(e, writer);
    return writer.toString();
  }    
  
  public Metadata parseMetadata(String in) throws Exception {
    return parseMetadata(IOUtils.toInputStream(in, "UTF8"));
  }
  
  public Metadata parseMetadata(InputStream in) throws Exception {
    Unmarshaller unmarshaller = jaxbContextJPA.createUnmarshaller();
    return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
                                  Metadata.class).getValue();
  }   
}

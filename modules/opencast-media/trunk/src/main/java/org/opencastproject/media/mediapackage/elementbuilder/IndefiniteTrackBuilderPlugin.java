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

package org.opencastproject.media.mediapackage.elementbuilder;

import org.opencastproject.media.mediapackage.MediaPackageElement;
import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.media.mediapackage.MediaPackageElements;
import org.opencastproject.media.mediapackage.MediaPackageException;
import org.opencastproject.media.mediapackage.Track;
import org.opencastproject.media.mediapackage.track.TrackImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.net.URL;

import javax.xml.xpath.XPathExpressionException;

/**
 * This implementation of the {@link MediaPackageElementBuilderPlugin} recognizes video tracks and provides the
 * functionality of reading it on behalf of the media package.
 * 
 * @author Christoph E. Driessen <ced@neopoly.de>
 * @version $Id: IndefiniteTrackBuilderPlugin.java 2905 2009-07-15 16:16:05Z ced $
 */
public class IndefiniteTrackBuilderPlugin extends AbstractTrackBuilderPlugin {

  /**
   * the logging facility provided by log4j
   */
  private final static Logger log_ = LoggerFactory.getLogger(IndefiniteTrackBuilderPlugin.class);

  public IndefiniteTrackBuilderPlugin() throws IllegalStateException {
    setPriority(0);
  }

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#accept(org.opencastproject.media.mediapackage.MediaPackageElement.Type,
   *      org.opencastproject.media.mediapackage.MediaPackageElementFlavor)
   */
  public boolean accept(MediaPackageElement.Type type, MediaPackageElementFlavor flavor) {
    return type.equals(MediaPackageElement.Type.Track) && flavor.equals(MediaPackageElements.INDEFINITE_TRACK);
  }

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#accept(org.w3c.dom.Node)
   */
  public boolean accept(Node elementNode) {
    try {
      String name = elementNode.getNodeName();
      String flavor = xpath.evaluate("@type", elementNode);
      return name.equalsIgnoreCase(MediaPackageElement.Type.Track.toString())
              && MediaPackageElements.INDEFINITE_TRACK.eq(flavor);
    } catch (XPathExpressionException e) {
      return false;
    }
  }

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#accept(java.net.URL,
   *      org.opencastproject.media.mediapackage.MediaPackageElement.Type,
   *      org.opencastproject.media.mediapackage.MediaPackageElementFlavor)
   */
  public boolean accept(URL url, MediaPackageElement.Type type, MediaPackageElementFlavor flavor) {
    return MediaPackageElement.Type.Track.equals(type) && MediaPackageElements.INDEFINITE_TRACK.equals(flavor);
  }

  /**
   * @see org.opencastproject.media.mediapackage.elementbuilder.MediaPackageElementBuilderPlugin#elementFromURL(java.net.URL)
   */
  public MediaPackageElement elementFromURL(URL url) throws MediaPackageException {
    log_.trace("Creating video track from " + url);
    Track track = TrackImpl.fromURL(url);
    return track;
  }

  @Override
  public String toString() {
    return "Indefinite Track Builder Plugin";
  }

  @Override
  protected TrackImpl trackFromManifest(String id, URL url) {
    TrackImpl track = (TrackImpl) TrackImpl.fromURL(url);
    track.setIdentifier(id);
    return track;
  }

}
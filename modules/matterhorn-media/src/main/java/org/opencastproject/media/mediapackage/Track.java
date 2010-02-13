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

package org.opencastproject.media.mediapackage;

import org.opencastproject.media.mediapackage.track.TrackImpl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This interface describes methods and fields for audio and video tracks as
 * part of a media package.
 * 
 * @author Tobias Wunden <tobias.wunden@id.ethz.ch>
 * @author Christoph E. Driessen <ced@neopoly.de>
 * @version $Id: Track.java 2905 2009-07-15 16:16:05Z ced $
 */
@XmlJavaTypeAdapter(TrackImpl.Adapter.class)
public interface Track extends MediaPackageElement {

  /**
   * Media package element type.
   */
  Type TYPE = Type.Track;

  /**
   * Return the streams that make up the track. Tracks consist of at least one
   * stream.
   */
  Stream[] getStreams();

  /**
   * Returns <code>true</code> if the track features an audio stream.
   * 
   * @return <code>true</code> if the track has an audio stream
   */
  boolean hasAudio();

  /**
   * Returns <code>true</code> if the track features a video stream.
   * 
   * @return <code>true</code> if the track has a video stream
   */
  boolean hasVideo();

  /**
   * Returns the track duration in milliseconds.
   * 
   * @return the track duration
   */
  long getDuration();

  /**
   * Returns the track's description with details about framerate, codecs etc.
   * 
   * @return the track description.
   */
  String getDescription();
}
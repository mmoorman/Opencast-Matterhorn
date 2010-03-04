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
package org.opencastproject.capture.pipeline;


import com.sun.jna.Pointer;

import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadDirection;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.Structure;
import org.gstreamer.elements.AppSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing video monitoring services that can be incorporated into
 * GStreamer pipelines and used for confidence monitoring.
 */
public class VideoMonitoring {
  
  private static final Logger logger = LoggerFactory.getLogger(VideoMonitoring.class);
  
  /**
   * Add a method for confidence monitoring to a pipeline capturing video by
   * teeing the raw data a pulling it to the appsink element.
   * 
   * @param p pipeline to add video monitoring to
   * @param interval how often to grab data from the pipeline
   * @return the pipeline with the video monitoring added, or null on failure
   */
  public static boolean addVideoMonitor(Pipeline pipeline, Element src, Element sink, final long interval) {
    
      // if it doesn't accept any caps, then it is not an appropriate video src
      if (!src.getSrcPads().get(0).acceptCaps(Caps.anyCaps())) {
        return false;
      }
      
      Element tee, queue0, queue1, decodebin;
      final Element jpegenc;
      AppSink appsink;
      
      // the items to be tee'd and added to the pipeline
      tee = ElementFactory.make("tee", null);
      queue0 = ElementFactory.make("queue", null);
      queue1 = ElementFactory.make("queue", null);
      decodebin = ElementFactory.make("decodebin", null);
      jpegenc = ElementFactory.make("jpegenc", null);
      appsink = (AppSink) ElementFactory.make("appsink", null);
      
      tee.set("silent", "false");
      appsink.set("emit-signals", "true");
      
      pipeline.addMany(tee, queue0, queue1, decodebin, jpegenc, appsink);
      src.unlink(sink);
      
      if (!src.link(tee)) {
        logger.error("Could not link {} with {}", src.toString(), tee.toString());
        return false;
      }
      if (!tee.link(queue0)) {
        logger.error("Could not link {} with {}", tee.toString(), queue0.toString());
        return false;
      }
      if (!queue0.link(sink)) {
        logger.error("Could not link {} with {}", queue0.toString(), sink.toString());
        return false;
      }
      if (!tee.link(queue1)) {
        logger.error("Could not link {} with {}", tee.toString(), queue1.toString());
        return false;
      }
      if (!queue1.link(decodebin)) {
        logger.error("Could not link {} with {}", queue1.toString(), decodebin.toString());
        return false;
      }
      decodebin.connect(new Element.PAD_ADDED() {
        public void padAdded(Element element, final Pad pad) {
          PadLinkReturn ret = pad.link(jpegenc.getStaticPad("sink"));
          if (ret != PadLinkReturn.OK) {
            logger.error("Could not link decodebin: " + ret.toString());
          }
        }
      });
      Pad pad = new Pad(null, PadDirection.SRC);
      decodebin.addPad(pad);
      if (!jpegenc.link(appsink)) {
        logger.error("Could not link {} with {}", jpegenc.toString(), appsink.toString());
        return false;
      }

      
      appsink.connect(new AppSink.NEW_BUFFER() {
        
        long previous = -1;
        public void newBuffer(Element elem, Pointer userData) {
          AppSink appsink = (AppSink) elem;
          long seconds = appsink.getClock().getTime().getSeconds();
          Buffer buffer = appsink.pullBuffer();
          if (seconds % 30 == 0 && seconds != previous) {
            previous = seconds;
            Caps caps = buffer.getCaps();
            Structure s = caps.getStructure(0);
            int width = s.getInteger("width");
            int height = s.getInteger("height");
            logger.info("Grabbed buffer: {}, {}", width, height);
            
          }
          buffer = null;
        }
        
      });
      
      return true;
  }

}

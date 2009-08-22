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

package org.opencastproject.media.mediapackage.attachment;

import org.opencastproject.media.mediapackage.AbstractMediaPackageElement;
import org.opencastproject.media.mediapackage.Attachment;
import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.util.Checksum;
import org.opencastproject.util.MimeType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Basic implementation of an attachment.
 * 
 * @author Tobias wunden <tobias.wunden@id.ethz.ch>
 * @version $Id: AttachmentImpl.java 2226 2009-02-09 13:06:20Z wunden $
 */
public class AttachmentImpl extends AbstractMediaPackageElement implements Attachment {

  /** Serial version UID */
  private static final long serialVersionUID = 6626531251856698138L;

  /**
   * Creates an attachment.
   * 
   * @param identifier
   *          the attachment identifier
   * @param flavor
   *          the attachment type
   * @param url
   *          the attachments location
   * @param size
   *          the attachments size
   * @param checksum
   *          the attachments checksum
   * @param mimeType
   *          the attachments mime type
   */
  protected AttachmentImpl(String identifier, MediaPackageElementFlavor flavor, URL url, long size, Checksum checksum,
          MimeType mimeType) {
    super(identifier, Type.Attachment, flavor, url, size, checksum, mimeType);
  }

  /**
   * Creates an attachment.
   * 
   * @param flavor
   *          the attachment type
   * @param url
   *          the attachment location
   * @param size
   *          the attachment size
   * @param checksum
   *          the attachment checksum
   * @param mimeType
   *          the attachment mime type
   */
  protected AttachmentImpl(MediaPackageElementFlavor flavor, URL url, long size, Checksum checksum, MimeType mimeType) {
    super(Type.Attachment, flavor, url, size, checksum, mimeType);
  }

  /**
   * Creates an attachment.
   * 
   * @parm identifier the attachment identifier
   * @param url
   *          the attachments location
   */
  protected AttachmentImpl(String identifier, URL url) {
    this(identifier, null, url, 0, null, null);
  }

  /**
   * Creates an attachment.
   * 
   * @parm identifier the attachment identifier
   * @param flavor
   *          the attachment type
   * @param url
   *          the attachments location
   * @param size
   *          the attachments size
   * @param checksum
   *          the attachments checksum
   * @param mimeType
   *          the attachments mime type
   */
  protected AttachmentImpl(URL url) {
    this(null, null, url, 0, null, null);
  }

  /**
   * Creates a new attachment from the url.
   * 
   * @param url
   *          the attachment location
   * @return the attachment
   */
  public static Attachment fromURL(URL url) {
    return new AttachmentImpl(url);
  }

  /**
   * @see org.opencastproject.media.mediapackage.AbstractMediaPackageElement#toManifest(org.w3c.dom.Document)
   */
  @Override
  public Node toManifest(Document document) {
    Node node = super.toManifest(document);
    return node;
  }

  /**
   * @see org.opencastproject.media.mediapackage.AbstractMediaPackageElement#toString()
   */
  @Override
  public String toString() {
    return "attachment (" + getMimeType() + ")";
  }

}
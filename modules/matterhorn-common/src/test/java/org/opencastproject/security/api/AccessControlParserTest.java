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
package org.opencastproject.security.api;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests JAXB un/marshalling of acces control lists
 */
public class AccessControlParserTest {

  @Test
  public void testAclParsing() throws Exception {
    // Construct an ACL with 100 entries
    AccessControlList acl = new AccessControlList();
    List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        entries.add(new AccessControlEntry(Integer.toString(i), Integer.toString(j), (i + j % 2 == 0)));
      }
    }
    acl.setEntries(entries);
    
    // Get the acl as an xml string
    String xml = AccessControlParser.toXml(acl);
    
    // Now convert back to an acl and confirm that the roles, etc are as expected
    AccessControlList aclAfterMarshaling = AccessControlParser.parseAcl(xml);
    for(AccessControlEntry entry : aclAfterMarshaling.getEntries()) {
      int role = Integer.parseInt(entry.getRole());
      int action = Integer.parseInt(entry.getAction());
      boolean allowed = entry.isAllow();
      Assert.assertEquals(allowed, role+action %2 == 0);
    }
  }
}
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
package org.opencastproject.analysis.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.opencastproject.analysis.text.ocropus.OcropusTextFrame;

import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;
import java.io.InputStream;

/**
 * Test case for class {@link OcropusTextFrame}.
 */
public class OcropusTextFrameTest {

  /** Path to the test frame */
  protected String testFile = "/ocropus.html";

  /** The test frame */
  protected OcropusTextFrame textFrame = null;
  
  /** The text without punctuation */
  protected String text = "Land and Vegetation Key players on the Climate Scene";
  
  /** Number of words on the frame */
  protected int wordsOnFrame = 9;
  
  /** Top boundary coordinate */
  protected int top = 46;

  /** Left boundary coordinate */
  protected int left = 5;

  /** Boundary width */
  protected int width = 291;

  /** Boundary height */
  protected int height = 20;

  /** Text boundaries */
  protected Rectangle textBoundaries = new Rectangle(left, top, width, height);
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    InputStream is = this.getClass().getResourceAsStream(testFile);
    textFrame = OcropusTextFrame.parse(is);
  }

  /**
   * Test method for {@link org.opencastproject.analysis.text.ocropus.OcropusTextFrame#getWords()}.
   */
  @Test
  public void testGetText() {
    assertEquals(wordsOnFrame, textFrame.getWords().length);
    assertEquals(text.split(" ")[0], textFrame.getWords()[0].getWord());
    assertEquals(textBoundaries, textFrame.getWords()[0].getBoundaries());
  }

  /**
   * Test method for {@link org.opencastproject.analysis.text.ocropus.OcropusTextFrame#hasText()}.
   */
  @Test
  public void testHasText() {
    assertTrue(textFrame.hasText());
    assertFalse((new OcropusTextFrame()).hasText());
  }

}
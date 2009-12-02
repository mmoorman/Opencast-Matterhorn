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
package org.opencastproject.util.doc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Represents a single parameter for an endpoint (required or optional)
 */
public class Param {
  public static enum Type {
    TEXT, STRING, BOOLEAN, FILE, ENUM
  };

  String name; // unique key
  String defaultValue;
  String type;
  String description;
  List<String> choices;
  Map<String, String> attributes = new HashMap<String, String>();
  boolean required = false;

  /**
   * Create a parameter for this endpoint, the thing you are adding it to indicates if required or optional
   * 
   * @param name
   *          the parameter name (this is the parameter itself)
   * @param type
   *          [optional] the type of this parameter
   * @param defaultValue
   *          [optional] the default value which is used if this param is missing
   * @param description
   *          [optional] the description to display with this param
   */
  public Param(String name, Type type, String defaultValue, String description) {
    this(name, type, defaultValue, description, null);
  }

  /**
   * Create a parameter for this endpoint, the thing you are adding it to indicates if required or optional
   * 
   * @param name
   *          the parameter name (this is the parameter itself)
   * @param type
   *          [optional] the type of this parameter
   * @param defaultValue
   *          [optional] the default value which is used if this param is missing
   * @param description
   *          [optional] the description to display with this param
   * @param choices
   *          [optional] a list of valid choices for this parameter (only used for the enum type)
   */
  public Param(String name, Type type, String defaultValue, String description, String[] choices) {
    if (!DocData.isValidName(name)) {
      throw new IllegalArgumentException("name must not be null and must be alphanumeric");
    }
    if (type == null) {
      type = Type.STRING;
    }
    this.name = name;
    this.type = type.name().toLowerCase();
    this.description = description;
    this.defaultValue = defaultValue;
    setChoices(choices);
  }

  /**
   * @param choice
   *          the choice to add to the list of choices
   */
  public void addChoice(String choice) {
    if (choices == null) {
      choices = new Vector<String>();
    }
    choices.add(choice);
  }
  
  public void setChoices(String[] choices) {
    if (choices != null) {
      this.choices = new Vector<String>(choices.length);
      for (int i = 0; i < choices.length; i++) {
        addChoice(choices[i]);
      }
    } else {
      choices = null;
    }
  }

  /**
   * Attributes are used for adjusting rendering of form elements
   * related to this parameter
   * @param key the attribute key (e.g. size)
   * @param value the attribute value (e.g. 80)
   */
  public void setAttribute(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("key must be set");
    }
    if (value == null) {
      this.attributes.remove(key);
    } else {
      this.attributes.put(key, value);
    }
  }

  public String getAttribute(String key) {
    if (key == null) {
      return null;
    }
    return this.attributes.get(key);
  }

  public String getName() {
    return name;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getDefaultValueHtml() {
    if (defaultValue != null) {
      if (defaultValue.length() > 20) {
        return "<strong title=\""+defaultValue+"\">TEXT</strong>";
      }
    }
    return defaultValue;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getChoices() {
    return choices;
  }
  
  public Map<String, String> getAttributes() {
    return attributes;
  }

  public boolean isRequired() {
    return required;
  }

  /**
   * @param required
   *          if true then this parameter is require, otherwise it is optional
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  @Override
  public String toString() {
    return "PAR:"+name + ":(" + type + "):" + defaultValue + ":" + choices;
  }
}


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
package org.opencastproject.search.impl;

import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.search.api.SearchQuery;

/**
 * TODO: Comment me!
 *
 */
public class SearchQueryImpl implements SearchQuery {
  boolean includeEpisode = true;
  boolean includeSeries = false;
  boolean sortByDate = false;
  String id;
  String text;
  String query;
  int limit = -1;
  int offset = -1;
  String[] tags = null;
  MediaPackageElementFlavor[] flavors = null;
  
  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#includeEpisodes(boolean)
   */
  @Override
  public SearchQuery includeEpisodes(boolean includeEpisode) {
    this.includeEpisode = includeEpisode;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#includeSeries(boolean)
   */
  @Override
  public SearchQuery includeSeries(boolean includeSeries) {
    this.includeSeries = includeSeries;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withId(java.lang.String)
   */
  @Override
  public SearchQuery withId(String id) {
    this.id = id;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withLimit(int)
   */
  @Override
  public SearchQuery withLimit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withOffset(int)
   */
  @Override
  public SearchQuery withOffset(int offset) {
    this.offset = offset;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withQuery(java.lang.String)
   */
  @Override
  public SearchQuery withQuery(String q) {
    this.query = q;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withText(java.lang.String)
   */
  @Override
  public SearchQuery withText(String text) {
    this.text = text;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getId()
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getLimit()
   */
  @Override
  public int getLimit() {
    return limit;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getOffset()
   */
  @Override
  public int getOffset() {
    return offset;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getQuery()
   */
  @Override
  public String getQuery() {
    return query;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getText()
   */
  @Override
  public String getText() {
    return text;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#isIncludeEpisodes()
   */
  @Override
  public boolean isIncludeEpisodes() {
    return includeEpisode;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#isIncludeSeries()
   */
  @Override
  public boolean isIncludeSeries() {
    return includeSeries;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withCreationDateSort(boolean)
   */
  @Override
  public SearchQuery withCreationDateSort(boolean sortByDate) {
    this.sortByDate = sortByDate;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#isSortByCreationDate()
   */
  @Override
  public boolean isSortByCreationDate() {
    return sortByDate;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getElementFlavors()
   */
  @Override
  public MediaPackageElementFlavor[] getElementFlavors() {
    return flavors;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#getElementTags()
   */
  @Override
  public String[] getElementTags() {
    return tags;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withElementFlavors(org.opencastproject.media.mediapackage.MediaPackageElementFlavor[])
   */
  @Override
  public SearchQuery withElementFlavors(MediaPackageElementFlavor[] flavors) {
    this.flavors = flavors;
    return this;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.search.api.SearchQuery#withElementTags(java.lang.String[])
   */
  @Override
  public SearchQuery withElementTags(String[] tags) {
    this.tags = tags;
    return this;
  }  
}
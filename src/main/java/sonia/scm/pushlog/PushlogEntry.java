/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.pushlog;

//~--- non-JDK imports --------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "push")
@XmlAccessorType(XmlAccessType.FIELD)
public class PushlogEntry
{

  /**
   * Constructs ...
   *
   */
  public PushlogEntry() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param username
   */
  public PushlogEntry(long id, String username)
  {
    this.id = id;
    this.username = username;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   */
  public void add(String id)
  {
    getChangesets().add(id);
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public boolean contains(String id)
  {
    return getChangesets().contains(id);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public long getId()
  {
    return id;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Set<String> getChangesets()
  {
    if (changesets == null)
    {
      changesets = new LinkedHashSet<>();
    }

    return changesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "changeset")
  private Set<String> changesets;

  /** Field description */
  private long id;

  /** Field description */
  private String username;
}

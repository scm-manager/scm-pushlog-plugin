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

import com.google.common.collect.Lists;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "pushlog")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pushlog implements Serializable
{

  private static final long serialVersionUID = 6674106880546613670L;

  public PushlogEntry createEntry(String username)
  {
    PushlogEntry entry = new PushlogEntry(++lastEntryId, username);

    getEntries().add(entry);

    return entry;
  }

  public String get(String id)
  {
    String username = null;

    for (PushlogEntry entry : getEntries())
    {
      if (entry.contains(id))
      {
        username = entry.getUsername();

        break;
      }
    }

    return username;
  }

  public List<PushlogEntry> getEntries()
  {
    if (entries == null)
    {
      entries = Lists.newArrayList();
    }

    return entries;
  }

  public PushlogEntry getEntry(long id)
  {
    PushlogEntry entry = null;

    for (PushlogEntry e : getEntries())
    {
      if (e.getId() == id)
      {
        entry = e;

        break;
      }
    }
    return entry;
  }

  @XmlElement(name = "entry")
  private List<PushlogEntry> entries;

  @XmlElement(name = "last-entry-id")
  private long lastEntryId = 0;
}

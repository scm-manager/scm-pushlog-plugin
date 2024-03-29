/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.pushlog;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

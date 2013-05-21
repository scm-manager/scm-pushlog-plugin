/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.pushlog;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "pushlog")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pushlog implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 6674106880546613670L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Pushlog() {}

  /**
   * Constructs ...
   *
   *
   * @param repositoryId
   */
  public Pushlog(String repositoryId)
  {
    this.repositoryId = repositoryId;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  public PushlogEntry createEntry(String username)
  {
    PushlogEntry entry = new PushlogEntry(++lastEntryId, username);

    getEntries().add(entry);

    return entry;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public String get(String id)
  {
    String username = null;

    for (PushlogEntry entry : entries)
    {
      if (entry.contains(id))
      {
        username = entry.getUsername();

        break;
      }
    }

    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PushlogEntry> getEntries()
  {
    if (entries == null)
    {
      entries = Lists.newArrayList();
    }

    return entries;
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public PushlogEntry getEntry(long id)
  {
    PushlogEntry entry = null;

    for (PushlogEntry e : entries)
    {
      if (e.getId() == id)
      {
        entry = e;

        break;
      }
    }

    return entry;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRepositoryId()
  {
    return repositoryId;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name="entry")
  @XmlElementWrapper(name = "entries")
  private List<PushlogEntry> entries;

  /** Field description */
  @XmlElement(name = "last-entry-id")
  private long lastEntryId = 0;

  /** Field description */
  @XmlElement(name = "repository-id")
  private String repositoryId;
}

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "push")
@XmlAccessorType(XmlAccessType.FIELD)
public class PushlogEntry {

  @XmlElement(name = "changeset")
  private Set<String> changesets;
  private long id;
  private String username;
  private Long contributionTime;

  public PushlogEntry() {
  }

  public PushlogEntry(long id, String username, long contributionTime) {
    this.id = id;
    this.username = username;
    this.contributionTime = contributionTime;
  }

  public void add(String id) {
    getChangesets().add(id);
  }

  public boolean contains(String id) {
    return getChangesets().contains(id);
  }

  private Set<String> getChangesets() {
    if (changesets == null) {
      changesets = new LinkedHashSet<>();
    }
    return changesets;
  }
}

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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@QueryableType(Repository.class)
public class PushlogEntry {

  private long pushlogId;
  private String username;
  private Instant contributionTime;
  private String description;

  public PushlogEntry(String username, Instant contributionTime) {
    this(username, contributionTime, null);
  }

  public PushlogEntry(String username, Instant contributionTime, String description) {
    this.username = username;
    this.contributionTime = contributionTime;
    this.description = description;
  }
}

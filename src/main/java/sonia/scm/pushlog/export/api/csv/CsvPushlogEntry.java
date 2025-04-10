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

package sonia.scm.pushlog.export.api.csv;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

/**
 * Represents a pushlog entry as persisted in a CSV file.
 *
 * @param pushlogId {@link Long}
 * @param revision  {@link String}
 * @param username  {@link String}
 * @param timestamp {@link ZonedDateTime}
 */
@JsonPropertyOrder({"pushlogId", "revision", "username", "timestamp"})
public record CsvPushlogEntry(
  long pushlogId,
  String revision,
  String username,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS 'UTC'Z") ZonedDateTime timestamp) {

  public static final String HEADER = "PushlogId,Revision,Username,Timestamp";
}

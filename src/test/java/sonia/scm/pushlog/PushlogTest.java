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

import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class PushlogTest {
  @Test
  void shouldGetPushlogEntry() {
    Pushlog pushlog = new Pushlog();
    PushlogEntry entry1 = pushlog.createEntry("user1");
    entry1.add("1");

    Optional<PushlogEntry> expectedEntry = pushlog.get("1");
    assertThat(expectedEntry).contains(entry1);
  }

  @Test
  void shouldNotGetPushlogEntry() {
    Pushlog pushlog = new Pushlog();
    PushlogEntry entry1 = pushlog.createEntry("user1");
    entry1.add("1");

    Optional<PushlogEntry> expectedEntry = pushlog.get("2");
    assertThat(expectedEntry).isEmpty();
  }
}

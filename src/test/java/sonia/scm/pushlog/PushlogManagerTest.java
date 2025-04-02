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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.QueryableStoreExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(QueryableStoreExtension.class)
@QueryableStoreExtension.QueryableTypes(PushlogEntry.class)
class PushlogManagerTest {

  private PushlogManager manager;
  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void initStore(PushlogEntryStoreFactory storeFactory) {
    manager = new PushlogManager(storeFactory);
  }

  @Test
  void shouldStoreNewEntries(PushlogEntryStoreFactory storeFactory) {
    PushlogEntry entry = new PushlogEntry("1", "trillian", Instant.now());
    manager.store(entry, repository, List.of("r1", "r2"));

    Map<String, PushlogEntry> all = storeFactory.getMutable(repository).getAll();
    assertThat(all).hasSize(2);
    assertThat(all.get("r1")).isEqualTo(entry);
    assertThat(all.get("r2")).isEqualTo(entry);
  }

  @Test
  void shouldNotReplaceExistingEntries(PushlogEntryStoreFactory storeFactory) {
    PushlogEntry existingEntry = new PushlogEntry("1", "trillian", Instant.now());
    manager.store(existingEntry, repository, List.of("r1"));

    PushlogEntry newEntry = new PushlogEntry("2", "arthur", Instant.now());
    manager.store(newEntry, repository, List.of("r1", "r2"));


    Map<String, PushlogEntry> all = storeFactory.getMutable(repository).getAll();
    assertThat(all).hasSize(2);
    assertThat(all.get("r1")).isEqualTo(existingEntry);
    assertThat(all.get("r2")).isEqualTo(newEntry);
  }

  @Test
  void shouldSortContributionsByTimestamp(PushlogEntryStoreFactory storeFactory) {
    int entries = 5;
    Instant baseTime = Instant.now().plusSeconds(1800);
    for (int i = 0; i < entries; i++) {
      PushlogEntry entry = new PushlogEntry(Integer.toString(i), "user" + i, baseTime.plusSeconds(i * 10));
      manager.store(entry, repository, List.of("r" + (5 - i)));
    }
    assertThat(storeFactory.getMutable(repository).getAll()).hasSize(entries);

    List<PushlogEntry> all = storeFactory.get(repository).query()
      .orderBy(PushlogEntryQueryFields.CONTRIBUTIONTIME, QueryableStore.Order.ASC)
      .findAll();

    assertThat(all)
      .extracting(PushlogEntry::getPushlogId)
      .containsExactly("0", "1", "2", "3", "4");
  }
}

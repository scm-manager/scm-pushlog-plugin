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

package sonia.scm.pushlog.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.pushlog.PushlogEntryStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryByteDataStoreFactory;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStoreExtension;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.store.StoreException;
import sonia.scm.store.TypedStoreParametersBuilder;
import sonia.scm.update.StoreUpdateStepUtilFactory;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, QueryableStoreExtension.class})
@QueryableStoreExtension.QueryableTypes({PushlogEntry.class})
class MoveToQueryableUpdateStepTest {

  private final String repositoryId = "hog";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private StoreUpdateStepUtilFactory storeUpdateStepUtilFactory;

  private final DataStoreFactory storeFactory = new InMemoryByteDataStoreFactory();

  private MoveToQueryableUpdateStep updateStep;

  @Nested
  class WithStore {

    @BeforeEach
    void init(QueryableStoreFactory queryableStoreFactory) {
      when(storeUpdateStepUtilFactory.forQueryableType(PushlogEntry.class, repositoryId)).thenReturn(
        queryableStoreFactory.getForMaintenance(PushlogEntry.class, repositoryId));

      updateStep = new MoveToQueryableUpdateStep(storeUpdateStepUtilFactory, storeFactory);
    }

    @Test
    void shouldUpdateOldPushlogEntries(PushlogEntryStoreFactory pushlogStoreFactory) {
      MoveToQueryableUpdateStep.XmlPushlogEntry entry = new MoveToQueryableUpdateStep.XmlPushlogEntry(
        1L,
        "trillian",
        1234567L
      );
      entry.add("42");
      MoveToQueryableUpdateStep.XmlPushlog pushlog = new MoveToQueryableUpdateStep.XmlPushlog();
      pushlog.entries = new ArrayList<>();
      pushlog.entries.add(entry);
      storeFactory.withType(MoveToQueryableUpdateStep.XmlPushlog.class).withName("pushlog").forRepository(repositoryId).build().put("pushlog",
        pushlog
      );

      updateStep.doUpdate(new RepositoryUpdateContext(repositoryId));

      try (QueryableMutableStore<PushlogEntry> store = pushlogStoreFactory.getMutable(repositoryId)) {
        Map<String, PushlogEntry> all = store.getAll();
        assertThat(all).hasSize(1);
        assertThat(all.get("42")).extracting("username").isEqualTo("trillian");
      }
    }

    @Test
    void shouldIgnoreOldPushlogWithoutEntries(PushlogEntryStoreFactory pushlogStoreFactory) {
      MoveToQueryableUpdateStep.XmlPushlog pushlog = new MoveToQueryableUpdateStep.XmlPushlog();
      storeFactory.withType(MoveToQueryableUpdateStep.XmlPushlog.class).withName("pushlog").forRepository(repositoryId).build().put("pushlog",
        pushlog
      );

      updateStep.doUpdate(new RepositoryUpdateContext(repositoryId));

      try (QueryableMutableStore<PushlogEntry> store = pushlogStoreFactory.getMutable(repositoryId)) {
        Map<String, PushlogEntry> all = store.getAll();
        assertThat(all).hasSize(0);
      }
    }
  }

  @Test
  void shouldHandleDefectOldPushlog(PushlogEntryStoreFactory pushlogStoreFactory) {
    DataStoreFactory mockedStoreFactory = mock(DataStoreFactory.class);
    TypedStoreParametersBuilder builderMock = mock(TypedStoreParametersBuilder.class, Answers.RETURNS_DEEP_STUBS);
    DataStore storeMock = mock(DataStore.class);
    when(mockedStoreFactory.withType(MoveToQueryableUpdateStep.XmlPushlog.class)).thenReturn(builderMock);
    when(builderMock.withName("pushlog").forRepository(repositoryId).build())
      .thenReturn(storeMock);
    when(storeMock.getOptional("pushlog"))
      .thenThrow(new StoreException("This is just a test"));

    updateStep = new MoveToQueryableUpdateStep(storeUpdateStepUtilFactory, mockedStoreFactory);
    updateStep.doUpdate(new RepositoryUpdateContext(repositoryId));

    try (QueryableMutableStore<PushlogEntry> store = pushlogStoreFactory.getMutable(repositoryId)) {
      Map<String, PushlogEntry> all = store.getAll();
      assertThat(all).hasSize(0);
    }
  }
}

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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.StoreException;

import java.util.Collection;

import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * @author Sebastian Sdorra
 */
@Slf4j
@Singleton
public class PushlogManager {

  private final PushlogEntryStoreFactory storeFactory;

  @Inject
  public PushlogManager(PushlogEntryStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void store(PushlogEntry entry, Repository repository, Collection<String> revisions) {
    log.debug("store pushlog for repository {}", repository);
    try (QueryableMutableStore<PushlogEntry> store = storeFactory.getMutable(repository.getId())) {
      store.transactional(() -> {
          Long maxId = store.query(PushlogEntryQueryFields.REPOSITORY_ID.eq(repository.getId()))
            .max(PushlogEntryQueryFields.PUSHLOGID);
          entry.setPushlogId(maxId == null ? 1 : maxId + 1);
          revisions.forEach(
            revision -> {
              if (store.getOptional(revision).isEmpty()) {
                store.put(revision, entry);
              }
            }
          );
          return true;
        }
      );
    }
    log.debug("stored {} pushlogs for repository {}", revisions.size(), repository);
  }

  public Optional<PushlogEntry> get(Repository repository, String id) {
    try (QueryableMutableStore<PushlogEntry> store = storeFactory.getMutable(repository.getId())) {
      return store.getOptional(id);
    }
  }

  /**
   * Returns a map of all pushlogs from a {@link Repository} with the revision as its key and {@link PushlogEntry} entries.
   *
   * @param repository Repository from where the entries are fetched from.
   * @param consumer Consumer function receiving {@link QueryableStore.Result} objects. Each of them contains
   *                                 a {@link String} key representing the revision and the {@link PushlogEntry} value.
   * @param order Whether the result is ordered in a descending or ascending fashion.
   */
  public void doExport(Repository repository, Consumer<QueryableStore.Result<PushlogEntry>> consumer, QueryableStore.Order order) {
    log.debug("start export for repository {} with order {}", repository, order);
    try (QueryableStore<PushlogEntry> store = storeFactory.get(repository.getId())) {
      store
        .query()
        .withIds()
        .orderBy(PushlogEntryQueryFields.PUSHLOGID, order)
        .forEach(consumer);
    } catch(Exception e) {
      throw new StoreException(
        format("An exception occurred while trying to export pushlog entries from repository %s", repository), e);
    }
  }
}

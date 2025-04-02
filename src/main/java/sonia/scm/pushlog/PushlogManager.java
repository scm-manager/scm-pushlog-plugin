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

import java.util.Collection;
import java.util.Optional;

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
}

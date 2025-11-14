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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class PushlogManager {

  private static final String NAME = "pushlog";

  private static final Logger logger = LoggerFactory.getLogger(PushlogManager.class);

  private final Map<String, Lock> locks = Collections.synchronizedMap(new HashMap<>());
  private final DataStoreFactory dataStoreFactory;

  @Inject
  public PushlogManager(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  public Pushlog get(Repository repository) {
    return getDatastore(repository).getOptional(NAME).orElseGet(Pushlog::new);
  }

  public void editPushlog(Repository repository, Consumer<Pushlog> callback) {
    Lock lock = getLock(repository.getId());
    try {
      logger.trace("acquiring lock for pushlog for repository {}", repository);
      lock.lock();
      logger.trace("locked pushlog for repository {}", repository);


      Pushlog pushlog = get(repository);
      callback.accept(pushlog);

      logger.debug("store pushlog for repository {}", repository);
      getDatastore(repository).put(NAME, pushlog);
    } finally {
      logger.trace("unlock repository {}", repository);
      getLock(repository.getId()).unlock();
    }
  }

  private DataStore<Pushlog> getDatastore(Repository repository) {
    return dataStoreFactory.withType(Pushlog.class).withName(NAME).forRepository(repository).build();
  }

  private Lock getLock(String id) {
    return locks.computeIfAbsent(id, _i -> new ReentrantLock());
  }
}

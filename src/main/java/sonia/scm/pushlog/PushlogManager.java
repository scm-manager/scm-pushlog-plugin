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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class PushlogManager {

  private static final String NAME = "pushlog";

  private static final Logger logger = LoggerFactory.getLogger(PushlogManager.class);

  private static final Object LOCK_STORE = new Object();
  private static final Object LOCK_GET = new Object();

  private final Map<String, Lock> locks = new HashMap<>();
  private final DataStoreFactory dataStoreFactory;

  @Inject
  public PushlogManager(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }


  public void store(Pushlog pushlog, Repository repository) {
    synchronized (LOCK_STORE) {
      try {
        logger.debug("store pushlog for repository {}", repository);
        getDatastore(repository).put(NAME, pushlog);
      } finally {
        logger.trace("unlock repository {}", repository);
        getLock(repository.getId()).unlock();
      }
    }
  }

  public Pushlog get(Repository repository) {
    Pushlog pushlog = getDatastore(repository).get(NAME);

    if (pushlog == null) {
      pushlog = new Pushlog();
    }

    return pushlog;
  }

  private DataStore<Pushlog> getDatastore(Repository repository) {
    return dataStoreFactory.withType(Pushlog.class).withName(NAME).forRepository(repository).build();
  }


  public Pushlog getAndLock(Repository repository) {
    synchronized (LOCK_GET) {
      getLock(repository.getId()).lock();

      logger.trace("lock pushlog for repository {}", repository);

      return get(repository);
    }
  }

  private Lock getLock(String id) {
    Lock lock = locks.get(id);

    if (lock == null) {
      lock = new ReentrantLock();
      locks.put(id, lock);
    }

    return lock;
  }


}

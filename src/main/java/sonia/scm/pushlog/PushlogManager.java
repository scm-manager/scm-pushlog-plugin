/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.pushlog;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

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

  private final Map<String, Lock> locks = Maps.newHashMap();
  private final DataStoreFactory dataStoreFactory;

  @Inject
  public PushlogManager(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }


  public void store(Pushlog pushlog, Repository repository) {
    synchronized (LOCK_STORE) {
      try {
        logger.debug("store pushlog for repository {}",
          pushlog.getRepositoryId());
        getDatastore(repository).put(NAME, pushlog);
      } finally {
        logger.trace("unlock repository {}", pushlog.getRepositoryId());
        getLock(pushlog.getRepositoryId()).unlock();
      }
    }
  }

  public Pushlog get(Repository repository) {
    Pushlog pushlog = getDatastore(repository).get(NAME);

    if (pushlog == null) {
      pushlog = new Pushlog(repository.getId());
    }

    return pushlog;
  }

  private DataStore<Pushlog> getDatastore(Repository repository) {
    return dataStoreFactory.withType(Pushlog.class).withName(NAME).forRepository(repository).build();
  }


  public Pushlog getAndLock(Repository repository) {
    synchronized (LOCK_GET) {
      getLock(repository.getId()).lock();

      logger.trace("lock pushlog for repository {}", repository.getId());

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

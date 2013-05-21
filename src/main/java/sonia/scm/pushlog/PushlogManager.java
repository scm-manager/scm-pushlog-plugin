/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.pushlog;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class PushlogManager
{

  /** Field description */
  private static final String NAME = "pushlog";

  /**
   * the logger for PushlogManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PushlogManager.class);

  /** Field description */
  private static final Object LOCK_STORE = new Object();

  /** Field description */
  private static final Object LOCK_GET = new Object();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param dataStoreFactory
   */
  @Inject
  public PushlogManager(DataStoreFactory dataStoreFactory)
  {
    pushlogStore = dataStoreFactory.getStore(Pushlog.class, NAME);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void handleEvent(RepositoryEvent event)
  {
    if (event.getEventType() == HandlerEvent.DELETE)
    {
      pushlogStore.remove(event.getItem().getId());
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param pushlog
   */
  public void store(Pushlog pushlog)
  {
    synchronized (LOCK_STORE)
    {
      try
      {
        logger.debug("store pushlog for repository {}",
          pushlog.getRepositoryId());
        pushlogStore.put(pushlog.getRepositoryId(), pushlog);
      }
      finally
      {
        logger.trace("unlock repository {}", pushlog.getRepositoryId());
        getLock(pushlog.getRepositoryId()).unlock();
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public Pushlog get(Repository repository)
  {
    Pushlog pushlog = pushlogStore.get(repository.getId());

    if (pushlog == null)
    {
      pushlog = new Pushlog(repository.getId());
    }

    return pushlog;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public Pushlog getAndLock(Repository repository)
  {
    synchronized (LOCK_GET)
    {
      getLock(repository.getId()).lock();

      logger.trace("lock pushlog for repository {}", repository.getId());

      return get(repository);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @param id
   *
   * @return
   */
  private Lock getLock(String id)
  {
    Lock lock = locks.get(id);

    if (lock == null)
    {
      lock = new ReentrantLock();
      locks.put(id, lock);
    }

    return lock;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, Lock> locks = Maps.newHashMap();

  /** Field description */
  private DataStore<Pushlog> pushlogStore;
}

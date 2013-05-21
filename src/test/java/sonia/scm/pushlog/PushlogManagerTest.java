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

import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.repository.Repository;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.JAXBDataStoreFactory;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian Sdorra
 */
public class PushlogManagerTest extends AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @throws InterruptedException
   */
  @Test
  public void testConcurrent() throws InterruptedException
  {
    DataStoreFactory dataStoreFactory =
      new JAXBDataStoreFactory(contextProvider, new UUIDKeyGenerator());
    final PushlogManager manager = new PushlogManager(dataStoreFactory);
    final Repository repository = new Repository("abc", "git", "abc");

    final AtomicLong counter = new AtomicLong();

    ExecutorService service = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 100; i++)
    {
      service.execute(new Runnable()
      {

        @Override
        public void run()
        {
          Pushlog pushlog = null;

          try
          {
            pushlog = manager.getAndLock(repository);

            PushlogEntry entry = pushlog.createEntry("user");
            for (int i = 0; i < 10; i++)
            {
              entry.add( "c" + counter.incrementAndGet() );
            }

          }
          finally
          {
            manager.store(pushlog);
          }
        }
      });
    }

    service.shutdown();
    service.awaitTermination(5, TimeUnit.MINUTES);

    assertEquals(100, manager.get(repository).getEntries().size());
  }
}
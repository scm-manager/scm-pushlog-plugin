/**
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

import org.junit.Test;
import sonia.scm.AbstractTestBase;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class PushlogManagerTest extends AbstractTestBase {

    @Test
    public void testConcurrent() throws InterruptedException {
        final InMemoryDataStore dataStore = new InMemoryDataStore<Pushlog>();
        DataStoreFactory factory = new InMemoryDataStoreFactory(dataStore);
        final PushlogManager manager = new PushlogManager(factory);
        final Repository repository = new Repository("abc", "git", "abc", "def");

        final AtomicLong counter = new AtomicLong();

        ExecutorService service = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            service.execute(() -> {
                Pushlog pushlog = null;

                try {
                    pushlog = manager.getAndLock(repository);

                    PushlogEntry entry = pushlog.createEntry("user");
                    for (int i1 = 0; i1 < 10; i1++) {
                        entry.add("c" + counter.incrementAndGet());
                    }

                } finally {
                    manager.store(pushlog, repository);
                }
            });
        }

        service.shutdown();
        service.awaitTermination(5, TimeUnit.MINUTES);

        assertEquals(100, manager.get(repository).getEntries().size());
    }
}

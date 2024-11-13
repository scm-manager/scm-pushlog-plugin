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

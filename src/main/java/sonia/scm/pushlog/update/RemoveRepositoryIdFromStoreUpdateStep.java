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
package sonia.scm.pushlog.update;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.pushlog.Pushlog;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.version.Version;

import javax.inject.Inject;

@Extension
public class RemoveRepositoryIdFromStoreUpdateStep implements UpdateStep {

  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final DataStoreFactory storeFactory;

  @Inject
  public RemoveRepositoryIdFromStoreUpdateStep(RepositoryUpdateIterator repositoryUpdateIterator, DataStoreFactory storeFactory) {
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate()  {
    repositoryUpdateIterator.forEachRepository(this::doUpdate);
  }

  private void doUpdate(String repositoryId) {
    DataStore<Pushlog> store = storeFactory
      .withType(Pushlog.class)
      .withName("pushlog")
      .forRepository(repositoryId)
      .build();
    store.getOptional(repositoryId)
      .ifPresent(pushlog -> moveStore(store, repositoryId, pushlog));
  }

  private void moveStore(DataStore<Pushlog> store, String repositoryId, Pushlog pushlog) {
    store.put("pushlog", pushlog);
    store.remove(repositoryId);
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.pushlog.data.xml";
  }
}

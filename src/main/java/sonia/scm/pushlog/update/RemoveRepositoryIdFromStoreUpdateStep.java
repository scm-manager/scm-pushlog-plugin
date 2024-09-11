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

package sonia.scm.pushlog.update;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.pushlog.Pushlog;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.version.Version;

import jakarta.inject.Inject;

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

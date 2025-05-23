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

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.version.Version;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    DataStore<XmlPushlog> store = storeFactory
      .withType(XmlPushlog.class)
      .withName("pushlog")
      .forRepository(repositoryId)
      .build();
    store.getOptional(repositoryId)
      .ifPresent(pushlog -> moveStore(store, repositoryId, pushlog));
  }

  private void moveStore(DataStore<XmlPushlog> store, String repositoryId, XmlPushlog pushlog) {
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

  @XmlRootElement(name = "pushlog")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class XmlPushlog implements Serializable {

    @XmlElement(name = "entry")
    private List<XmlPushlogEntry> entries;

    @XmlElement(name = "last-entry-id")
    private long lastEntryId = 0;
  }

  @Getter
  @Setter
  @XmlRootElement(name = "push")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class XmlPushlogEntry {

    @XmlElement(name = "changeset")
    private Set<String> changesets;
    private long id;
    private String username;
    private Long contributionTime;

    public XmlPushlogEntry() {
    }

    public XmlPushlogEntry(long id, String username, long contributionTime) {
      this.id = id;
      this.username = username;
      this.contributionTime = contributionTime;
    }

    public void add(String id) {
      getChangesets().add(id);
    }
    public Set<String> getChangesets() {
      if (changesets == null) {
        changesets = new LinkedHashSet<>();
      }
      return changesets;
    }
  }
}

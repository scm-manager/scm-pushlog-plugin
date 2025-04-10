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

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.update.StoreUpdateStepUtilFactory;
import sonia.scm.version.Version;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@Extension
public class MoveToQueryableUpdateStep implements RepositoryUpdateStep {

  private final StoreUpdateStepUtilFactory utilFactory;
  private final DataStoreFactory dataStoreFactory;

  @Inject
  public MoveToQueryableUpdateStep(StoreUpdateStepUtilFactory utilFactory, DataStoreFactory dataStoreFactory) {
    this.utilFactory = utilFactory;
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override
  public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) {
    DataStore<XmlPushlog> oldStore = dataStoreFactory
      .withType(XmlPushlog.class)
      .withName("pushlog")
      .forRepository(repositoryUpdateContext.getRepositoryId())
      .build();

    oldStore.getOptional("pushlog")
      .ifPresent(pushlog -> utilFactory
        .forQueryableType(PushlogEntry.class, repositoryUpdateContext.getRepositoryId())
        .writeAll(
          pushlog.entries.stream().flatMap(entry -> {
            Instant contributionTime = entry.getContributionTime() == null
              ? null
              : Instant.ofEpochMilli(entry.getContributionTime());
            return entry.getChangesets()
              .stream()
              .map(changeset -> new QueryableMaintenanceStore.Row<>(
                new String[]{repositoryUpdateContext.getRepositoryId()},
                String.valueOf(changeset),
                new PushlogEntry(entry.getId(), entry.getUsername(), contributionTime)
              ));
          })
        ));
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.2.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.pushlog.data.xml";
  }

  @XmlRootElement(name = "pushlog")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class XmlPushlog implements Serializable {

    @SuppressWarnings("java:S1948") // we use an ArrayList, which is serializable.
    @XmlElement(name = "entry")
    ArrayList<XmlPushlogEntry> entries;
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

    @VisibleForTesting
    public XmlPushlogEntry(long id, String username, long contributionTime) {
      this.id = id;
      this.username = username;
      this.contributionTime = contributionTime;
    }

    public void add(String id) {
      getChangesets().add(id);
    }

    private Set<String> getChangesets() {
      if (changesets == null) {
        changesets = new LinkedHashSet<>();
      }
      return changesets;
    }
  }
}

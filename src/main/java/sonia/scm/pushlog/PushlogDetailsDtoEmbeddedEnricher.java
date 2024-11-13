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

import jakarta.inject.Inject;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import java.time.Instant;
import java.util.Optional;

@Extension
@Enrich(Changeset.class)
public class PushlogDetailsDtoEmbeddedEnricher implements HalEnricher {
  private final PushlogManager pushlogManager;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public PushlogDetailsDtoEmbeddedEnricher(PushlogManager pushlogManager, UserDisplayManager userDisplayManager) {
    this.pushlogManager = pushlogManager;
    this.userDisplayManager = userDisplayManager;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Changeset changeset = context.oneRequireByType(Changeset.class);
    Repository repository = context.oneRequireByType(Repository.class);

    Optional<PushlogEntry> optionalPushlogEntry = pushlogManager.get(repository).get(changeset.getId());

    if (optionalPushlogEntry.isPresent()) {
      PushlogDetailsDto dto = createDto(optionalPushlogEntry.get());
      appender.appendEmbedded("pushlogDetails", dto);
    }
  }

  private PushlogDetailsDto createDto(PushlogEntry pushlogEntry) {
    PushlogDetailsDto dto = new PushlogDetailsDto();
    if (pushlogEntry.getContributionTime() != null) {
      Instant timestampAsInstant = Instant.ofEpochMilli(pushlogEntry.getContributionTime());
      dto.setPublishedTime(timestampAsInstant);
    }
    Optional<DisplayUser> optionalDisplayUser = userDisplayManager.get(pushlogEntry.getUsername());
    if (optionalDisplayUser.isPresent()) {
      DisplayUser displayUser = optionalDisplayUser.get();
      dto.setPerson(new Person(displayUser.getDisplayName(), displayUser.getMail()));
    } else {
      dto.setPerson(new Person(pushlogEntry.getUsername()));
    }
    return dto;
  }
}

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

import de.otto.edison.hal.HalRepresentation;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class PushlogDetailsDtoEmbeddedEnricherTest {

  @Mock
  private HalEnricherContext context;
  @Mock
  private HalAppender appender;
  @Mock
  private Changeset changeset;
  @Mock
  private Pushlog pushlog;
  @Mock
  private PushlogManager pushlogManager;
  @Mock
  private UserDisplayManager userDisplayManager;

  @InjectMocks
  private PushlogDetailsDtoEmbeddedEnricher enricher;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private DisplayUser displayUser;

  @BeforeEach
  void init() {
    when(pushlogManager.get(repository)).thenReturn(pushlog);
    User user = new User("username", "displayUsername", "username@test.com");
    displayUser = DisplayUser.from(user);
  }

  @Test
  void shouldAppendEmbeddedPushlogDetailsDtoWithTimestampAndDisplayUser() {
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(context.oneRequireByType(Changeset.class)).thenReturn(changeset);
    PushlogEntry pushlogEntry = new PushlogEntry(1, "username", System.currentTimeMillis());
    when(pushlog.get(changeset.getId())).thenReturn(Optional.of(pushlogEntry));
    when(userDisplayManager.get(pushlogEntry.getUsername())).thenReturn(Optional.of(displayUser));

    enricher.enrich(context, appender);

    PushlogDetailsDto expectedDto = new PushlogDetailsDto();
    expectedDto.setPublishedTime(Instant.ofEpochMilli(pushlogEntry.getContributionTime()));
    expectedDto.setPerson(new Person(displayUser.getDisplayName(), displayUser.getMail()));
    verify(appender).appendEmbedded("pushlogDetails", expectedDto);
  }

  @Test
  void shouldAppendEmbeddedPushlogDetailsDtoWithoutTimestampAndDisplayUser() {
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(context.oneRequireByType(Changeset.class)).thenReturn(changeset);
    PushlogEntry pushlogEntry = new PushlogEntry();
    pushlogEntry.setUsername("username");
    when(pushlog.get(changeset.getId())).thenReturn(Optional.of(pushlogEntry));

    enricher.enrich(context, appender);

    PushlogDetailsDto expectedDto = new PushlogDetailsDto();
    expectedDto.setPublishedTime(null);
    expectedDto.setPerson(new Person(pushlogEntry.getUsername()));

    verify(appender).appendEmbedded("pushlogDetails", expectedDto);
  }

  @Test
  void shouldNotAppendEmbeddedPushlogDetailsDtoWithoutTimestamp() {
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
    when(context.oneRequireByType(Changeset.class)).thenReturn(changeset);
    when(pushlog.get(changeset.getId())).thenReturn(Optional.empty());

    enricher.enrich(context, appender);

    verify(appender, never()).appendEmbedded(anyString(), (HalRepresentation) any());
  }
}
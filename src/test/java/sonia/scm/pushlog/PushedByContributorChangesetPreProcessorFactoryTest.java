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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Contributor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushedByContributorChangesetPreProcessorFactoryTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private UserDisplayManager userDisplayManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PushlogManager pushlogManager;

  @InjectMocks
  private PushedByContributorChangesetPreProcessorFactory contributorProcessor;

  @Test
  void shouldReturnEmptyList() {
    when(pushlogManager.get(REPOSITORY).get("1")).thenReturn("");

    Changeset changeset = new Changeset();
    changeset.setId("1");
    contributorProcessor.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    assertThat(contributors).isNullOrEmpty();
  }

  @Test
  void shouldReturnListWithPusherDisplayName() {
    String pusherName = "trillian";
    String pusherDisplayName = "Tricia McMillan";
    String pusherMail = "trillian@hitchhiker.org";
    when(pushlogManager.get(REPOSITORY).get("1")).thenReturn(pusherName);
    when(userDisplayManager.get(pusherName)).thenReturn(Optional.of(DisplayUser.from(new User(pusherName, pusherDisplayName, pusherMail))));

    Changeset changeset = new Changeset();
    changeset.setId("1");
    contributorProcessor.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();
    assertThat(contributor.getType()).isEqualTo("Pushed-by");
    assertThat(contributor.getPerson().getName()).isEqualTo(pusherDisplayName);
    assertThat(contributor.getPerson().getMail()).isEqualTo(pusherMail);
  }

  @Test
  void shouldReturnListWithPusherUsernameAndWithoutMail() {
    String pusherName = "trillian";
    when(pushlogManager.get(REPOSITORY).get("1")).thenReturn(pusherName);
    when(userDisplayManager.get(pusherName)).thenReturn(Optional.empty());

    Changeset changeset = new Changeset();
    changeset.setId("1");
    contributorProcessor.createPreProcessor(REPOSITORY).process(changeset);
    Collection<Contributor> contributors = changeset.getContributors();

    Contributor contributor = contributors.iterator().next();
    assertThat(contributor.getType()).isEqualTo("Pushed-by");
    assertThat(contributor.getPerson().getName()).isEqualTo(pusherName);
    assertThat(contributor.getPerson().getMail()).isNull();
  }

}

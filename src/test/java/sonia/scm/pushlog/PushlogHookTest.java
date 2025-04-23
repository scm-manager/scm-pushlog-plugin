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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.security.Role;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushlogHookTest {

  @Mock
  private PushlogManager pushlogManager;
  @Mock
  private Subject subject;
  @Mock
  private Repository repository;
  @Mock
  private HookContext context;
  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookChangesetBuilder changesetBuilder;
  @Mock
  private Changeset changeset1;
  @Mock
  private Changeset changeset2;

  private PushlogHook pushlogHook;
  private final Instant creationDate = Instant.now();

  @BeforeEach
  public void setUp() {
    pushlogHook = new PushlogHook(pushlogManager);
  }

  @Test
  void testOnEventWithoutUserRole() {
    when(subject.hasRole(Role.USER)).thenReturn(false);

    try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
      securityUtilsMock.when(SecurityUtils::getSubject).thenReturn(subject);

      pushlogHook.onEvent(event);
      verify(subject).hasRole(Role.USER);
      verifyNoInteractions(pushlogManager);
    }
  }

  @Test
  void testOnEventWithEmptyUsername() {
    when(subject.hasRole(Role.USER)).thenReturn(true);
    when(subject.getPrincipal()).thenReturn("");

    try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
      securityUtilsMock.when(SecurityUtils::getSubject).thenReturn(subject);
      pushlogHook.onEvent(event);

      verifyNoInteractions(pushlogManager);
    }
  }

  @Test
  void testOnEventWithNullRepository() {
    when(subject.hasRole(Role.USER)).thenReturn(true);
    when(subject.getPrincipal()).thenReturn("testUser");

    when(event.getRepository()).thenReturn(null);

    try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
      securityUtilsMock.when(SecurityUtils::getSubject).thenReturn(subject);
      pushlogHook.onEvent(event);

      verifyNoInteractions(pushlogManager);
    }
  }

  @Test
  void testOnEventWithoutChangesets() {
    when(subject.hasRole(Role.USER)).thenReturn(true);
    when(subject.getPrincipal()).thenReturn("testUser");

    when(event.getRepository()).thenReturn(repository);
    when(event.getContext()).thenReturn(context);
    when(context.getChangesetProvider()).thenReturn(changesetBuilder);

    try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
      securityUtilsMock.when(SecurityUtils::getSubject).thenReturn(subject);
      pushlogHook.onEvent(event);

      verifyNoInteractions(pushlogManager);
    }
  }

  @Test
  void testOnEventSuccessfulPushWithOneLineCommitMessage() {
    when(changeset1.getDescription()).thenReturn("Commit Message");
    when(changeset2.getDescription()).thenReturn("Second commit message");
    executeSuccessfulPushTestCases(() -> {
      pushlogHook.onEvent(event);
      verify(pushlogManager).storeRevisionEntryMap(
        Map.of(
          "rev1", new PushlogEntry("testUser", creationDate, "Commit Message"),
          "rev2", new PushlogEntry("testUser", creationDate, "Second commit message")
        ),
        repository
      );
    });
  }

  @Test
  void testOnEventSuccessfulPushWithLimitedOneLineCommitMessage() {
    when(changeset1.getDescription()).thenReturn("a".repeat(100));
    when(changeset2.getDescription()).thenReturn("b".repeat(101));
    executeSuccessfulPushTestCases(() -> {
      pushlogHook.onEvent(event);
      verify(pushlogManager).storeRevisionEntryMap(
        Map.of(
          "rev1", new PushlogEntry("testUser", creationDate, "a".repeat(100)),
          "rev2", new PushlogEntry("testUser", creationDate, "b".repeat(100) + "...")
        ),
        repository
      );
    });
  }

  @Test
  void testOnEventSuccessfulPushWithMultiLineCommitMessage() {
    when(changeset1.getDescription()).thenReturn("First Line\nSecond Line");
    when(changeset2.getDescription()).thenReturn("1. Line\n2. Line\n3. Line");
    executeSuccessfulPushTestCases(() -> {
      pushlogHook.onEvent(event);
      verify(pushlogManager).storeRevisionEntryMap(
        Map.of(
          "rev1", new PushlogEntry("testUser", creationDate, "First Line"),
          "rev2", new PushlogEntry("testUser", creationDate, "1. Line")
        ),
        repository
      );
    });
  }

  @Test
  void testOnEventSuccessfulPushWithLimitedMultiLineCommitMessage() {
    when(changeset1.getDescription()).thenReturn("First Line\n" + "a".repeat(100));
    when(changeset2.getDescription()).thenReturn("b".repeat(101) + "\nSecond Line");
    executeSuccessfulPushTestCases(() -> {
      pushlogHook.onEvent(event);
      verify(pushlogManager).storeRevisionEntryMap(
        Map.of(
          "rev1", new PushlogEntry("testUser", creationDate, "First Line"),
          "rev2", new PushlogEntry("testUser", creationDate, "b".repeat(100) + "...")
        ),
        repository
      );
    });
  }

  private void executeSuccessfulPushTestCases(Runnable testCase) {
    when(subject.hasRole(Role.USER)).thenReturn(true);
    when(subject.getPrincipal()).thenReturn("testUser");
    when(event.getRepository()).thenReturn(repository);
    when(event.getCreationDate()).thenReturn(creationDate);
    when(event.getContext()).thenReturn(context);
    when(context.getChangesetProvider()).thenReturn(changesetBuilder);

    when(changesetBuilder.getChangesets()).thenReturn(Arrays.asList(changeset1, changeset2));
    when(changeset1.getId()).thenReturn("rev1");
    when(changeset2.getId()).thenReturn("rev2");

    try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
      securityUtilsMock.when(SecurityUtils::getSubject).thenReturn(subject);
      testCase.run();
    }
  }
}

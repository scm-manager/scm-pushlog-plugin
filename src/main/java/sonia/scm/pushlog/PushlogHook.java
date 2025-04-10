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

import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.security.Role;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Extension
@EagerSingleton
public class PushlogHook {

  private final PushlogManager pushlogManager;

  @Inject
  public PushlogHook(PushlogManager pushlogManager) {
    this.pushlogManager = pushlogManager;
  }

  @Subscribe
  public void onEvent(PostReceiveRepositoryHookEvent event) {
    Subject subject = SecurityUtils.getSubject();

    if (subject.hasRole(Role.USER)) {
      String username = (String) subject.getPrincipal();

      if (!Strings.isNullOrEmpty(username)) {
        handlePushEvent(username, event);
      } else {
        log.warn("username is null or empty");
      }
    } else {
      log.warn("subject has no user role, skip pushlog");
    }
  }

  private void handlePushEvent(String username, RepositoryHookEvent event) {
    Repository repository = event.getRepository();

    if (repository != null) {
      Iterable<Changeset> changesets = event.getContext().getChangesetProvider().getChangesets();

      if (!Iterables.isEmpty(changesets)) {
        handlePush(username, repository, event.getCreationDate(), changesets);
      } else {
        log.debug("received hook without changesets");
      }
    } else {
      log.warn("received hook without repository");
    }
  }

  private void handlePush(String username, Repository repository,
                          Instant creationDate, Iterable<Changeset> changesets) {
    Collection<String> revisions = new ArrayList<>();
    for (Changeset c : changesets) {
      revisions.add(c.getId());
    }
    pushlogManager.store(new PushlogEntry(username, creationDate), repository, revisions);
  }
}

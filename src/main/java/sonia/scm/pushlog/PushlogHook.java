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

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.security.Role;

/**
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class PushlogHook {

  private static final Logger logger = LoggerFactory.getLogger(PushlogHook.class);

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
        logger.warn("username is null or empty");
      }
    } else {
      logger.warn("subject has no user role, skip pushlog");
    }
  }


  private void handlePush(String username, Repository repository,
                          Iterable<Changeset> changesets) {
    Pushlog pushlog = null;

    try {
      pushlog = pushlogManager.getAndLock(repository);

      PushlogEntry entry = pushlog.createEntry(username);

      for (Changeset c : changesets) {
        if (pushlog.get(c.getId()).isPresent()) {
          logger.warn("found changeset with existing log entry (id {} in {}); skipping further analysis for this push", c.getId(), repository);
          break;
        } else {
          entry.add(c.getId());
        }
      }

    } finally {
      if (pushlog != null) {
        pushlogManager.store(pushlog, repository);
      }
    }
  }


  private void handlePushEvent(String username, RepositoryHookEvent event) {
    Repository repository = event.getRepository();

    if (repository != null) {
      Iterable<Changeset> changesets = event.getContext().getChangesetProvider().getChangesets();

      if (!Iterables.isEmpty(changesets)) {
        handlePush(username, repository, changesets);
      } else {
        logger.info("received hook without changesets");
      }
    } else {
      logger.warn("received hook without repository");
    }
  }

}

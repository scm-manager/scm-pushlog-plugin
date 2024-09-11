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

import com.google.common.base.Strings;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.ChangesetPreProcessor;
import sonia.scm.repository.ChangesetPreProcessorFactory;
import sonia.scm.repository.Contributor;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import jakarta.inject.Inject;
import java.util.Optional;

@Extension
public class PushedByContributorChangesetPreProcessorFactory implements ChangesetPreProcessorFactory {

  private static final String CONTRIBUTOR_TYPE = "Pushed-by";

  private final PushlogManager pushlogManager;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public PushedByContributorChangesetPreProcessorFactory(PushlogManager pushlogManager, UserDisplayManager userDisplayManager) {
    this.pushlogManager = pushlogManager;
    this.userDisplayManager = userDisplayManager;
  }

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return changeset -> {
      String pusherName = pushlogManager.get(repository).get(changeset.getId());

      if (!Strings.isNullOrEmpty(pusherName)) {
        Contributor contributor = createContributor(pusherName);
        changeset.addContributor(contributor);
      }
    };
  }

  private Contributor createContributor(String pusherName) {

    Optional<DisplayUser> optionalDisplayUser = userDisplayManager.get(pusherName);
    if (optionalDisplayUser.isPresent()) {
      DisplayUser displayUser = optionalDisplayUser.get();
      return new Contributor(CONTRIBUTOR_TYPE, new Person(displayUser.getDisplayName(), displayUser.getMail()));
    } else {
      return new Contributor(CONTRIBUTOR_TYPE, new Person(pusherName));
    }
  }
}

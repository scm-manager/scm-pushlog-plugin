/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
 *
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class PushlogHook {


    private static final Logger logger =
            LoggerFactory.getLogger(PushlogHook.class);
    private PushlogManager pushlogManager;


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
                entry.add(c.getId());
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
                logger.warn("received hook without changesets");
            }
        } else {
            logger.warn("received hook without repository");
        }
    }

}

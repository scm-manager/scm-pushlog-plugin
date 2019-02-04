/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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

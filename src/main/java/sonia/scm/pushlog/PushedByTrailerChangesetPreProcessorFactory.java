/*
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

import com.google.common.base.Strings;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.ChangesetPreProcessor;
import sonia.scm.repository.ChangesetPreProcessorFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Trailer;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import javax.inject.Inject;
import java.util.Optional;

@Extension
public class PushedByTrailerChangesetPreProcessorFactory implements ChangesetPreProcessorFactory {

  private static final String TRAILER_TYPE = "Pushed-by";

  private final PushlogManager pushlogManager;
  private final UserDisplayManager userDisplayManager;

  @Inject
  public PushedByTrailerChangesetPreProcessorFactory(PushlogManager pushlogManager, UserDisplayManager userDisplayManager) {
    this.pushlogManager = pushlogManager;
    this.userDisplayManager = userDisplayManager;
  }

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return changeset -> {
      String pusherName = pushlogManager.get(repository).get(changeset.getId());

      if (!Strings.isNullOrEmpty(pusherName)) {
        Trailer trailer = createTrailer(pusherName);
        changeset.addTrailer(trailer);
      }
    };
  }

  private Trailer createTrailer(String pusherName) {

    Optional<DisplayUser> optionalDisplayUser = userDisplayManager.get(pusherName);
    if (optionalDisplayUser.isPresent()) {
      DisplayUser displayUser = optionalDisplayUser.get();
      return new Trailer(TRAILER_TYPE, new Person(displayUser.getDisplayName(), displayUser.getMail()));
    } else {
      return new Trailer(TRAILER_TYPE, new Person(pusherName));
    }
  }
}

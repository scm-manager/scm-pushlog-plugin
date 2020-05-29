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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.TrailerPersonDto;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushedByChangesetTrailerTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private UserDisplayManager userDisplayManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PushlogManager pushlogManager;

  @InjectMocks
  private PushedByChangesetTrailer changesetTrailer;

  @Test
  void shouldReturnEmptyList() {
    when(pushlogManager.get(REPOSITORY).get("1")).thenReturn("");

    Changeset changeset = new Changeset();
    changeset.setId("1");
    List<TrailerPersonDto> trailers = changesetTrailer.getTrailers(REPOSITORY, changeset);

    assertThat(trailers).isEmpty();
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
    List<TrailerPersonDto> trailers = changesetTrailer.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPersonDto = trailers.get(0);
    assertThat(trailerPersonDto.getTrailerType()).isEqualTo("Pushed-by");
    assertThat(trailerPersonDto.getName()).isEqualTo(pusherDisplayName);
    assertThat(trailerPersonDto.getMail()).isEqualTo(pusherMail);
  }

  @Test
  void shouldReturnListWithPusherUsernameAndWithoutMail() {
    String pusherName = "trillian";
    when(pushlogManager.get(REPOSITORY).get("1")).thenReturn(pusherName);
    when(userDisplayManager.get(pusherName)).thenReturn(Optional.empty());

    Changeset changeset = new Changeset();
    changeset.setId("1");
    List<TrailerPersonDto> trailers = changesetTrailer.getTrailers(REPOSITORY, changeset);

    TrailerPersonDto trailerPersonDto = trailers.get(0);
    assertThat(trailerPersonDto.getTrailerType()).isEqualTo("Pushed-by");
    assertThat(trailerPersonDto.getName()).isEqualTo(pusherName);
    assertThat(trailerPersonDto.getMail()).isNull();
  }

}

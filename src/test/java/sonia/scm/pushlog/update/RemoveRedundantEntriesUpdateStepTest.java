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

package sonia.scm.pushlog.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.pushlog.Pushlog;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class RemoveRedundantEntriesUpdateStepTest {

  @Mock
  private RepositoryUpdateIterator repositoryUpdateIterator;

  private final DataStoreFactory dataStoreFactory = new InMemoryDataStoreFactory(new InMemoryDataStore<Pushlog>());

  private RemoveRedundantEntriesUpdateStep updateStep;

  @BeforeEach
  void initUpdateStep() {
    updateStep = new RemoveRedundantEntriesUpdateStep(repositoryUpdateIterator, dataStoreFactory);
    doAnswer(invocation -> {
      invocation.getArgument(0, Consumer.class).accept("1");
      return null;
    }).when(repositoryUpdateIterator).forEachRepository(any());
  }

  @Test
  void shouldDoNothingIfNoPushlogIsPresent() {
    updateStep.doUpdate();

    assertThat(getStore().getOptional("pushlog")).isEmpty();
  }

  @Test
  void shouldHandleEmptyPushlog() {
    getStore().put("pushlog", new Pushlog());

    updateStep.doUpdate();

    assertThat(getStore().get("pushlog").getEntries()).isEmpty();
  }

  @Test
  void shouldKeepCorrectPushlogs() {
    Pushlog pushlog = new Pushlog();
    PushlogEntry entry1 = pushlog.createEntry("dent");
    entry1.add("1");
    entry1.add("2");
    PushlogEntry entry2 = pushlog.createEntry("trillian");
    entry2.add("3");
    entry2.add("4");
    getStore().put("pushlog", pushlog);

    updateStep.doUpdate();

    assertThat(getStore().get("pushlog").getEntry(1).contains("1"))
      .isTrue();
    assertThat(getStore().get("pushlog").getEntry(1).contains("2"))
      .isTrue();
    assertThat(getStore().get("pushlog").getEntry(2).contains("3"))
      .isTrue();
    assertThat(getStore().get("pushlog").getEntry(2).contains("4"))
      .isTrue();
  }

  @Test
  void shouldRemoveDoublePushlogs() {
    Pushlog pushlog = new Pushlog();
    PushlogEntry entry1 = pushlog.createEntry("dent");
    entry1.add("1");
    PushlogEntry entry2 = pushlog.createEntry("trillian");
    entry2.add("1");
    entry2.add("2");
    getStore().put("pushlog", pushlog);

    updateStep.doUpdate();

    assertThat(getStore().get("pushlog").getEntry(1).contains("1"))
      .isTrue();
    assertThat(getStore().get("pushlog").getEntry(2).contains("1"))
      .isFalse();
    assertThat(getStore().get("pushlog").getEntry(2).contains("2"))
      .isTrue();
  }

  @Test
  void shouldNotFailForNullChangesets() {
    Pushlog pushlog = new Pushlog();
    PushlogEntry entry1 = pushlog.createEntry("dent");
    entry1.add("1");
    PushlogEntry entry2 = pushlog.createEntry("marvin");
    PushlogEntry entry3 = pushlog.createEntry("trillian");
    entry3.add("1");
    getStore().put("pushlog", pushlog);

    updateStep.doUpdate();

    assertThat(getStore().get("pushlog").getEntry(3)).isNull();
  }

  private DataStore<Pushlog> getStore() {
    return dataStoreFactory.withType(Pushlog.class).withName("pushlog").forRepository("1").build();
  }
}

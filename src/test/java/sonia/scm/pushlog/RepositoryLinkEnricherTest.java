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

import jakarta.inject.Provider;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class RepositoryLinkEnricherTest {

  private final Repository repository = RepositoryTestData.createHeartOfGold("git");
  private final String TEST_ID = "id-test";
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Mock
  private HalAppender halAppender;

  @BeforeEach
  void setUp() {
    repository.setId(TEST_ID);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = () -> scmPathInfoStore;
  }

  @Nested
  class Enrich {

    @Test
    @SubjectAware(value = "trillian", permissions = "repository:read:" + TEST_ID)
    void shouldEnrichRepositoryWithExportLink() {
      RepositoryLinkEnricher enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider);
      HalEnricherContext context = HalEnricherContext.of(repository);

      enricher.enrich(context, halAppender);

      verify(halAppender, times(1)).appendLink(
        "pushlogExport",
        String.format("https://scm-manager.org/scm/api/v2/pushlogs/csv/%s/%s/export", repository.getNamespace(), repository.getName()
        )
      );
    }

    @Test
    @SubjectAware(value = "strangerDanger", permissions = "repository:read:someBogus")
    void shouldNotEnrichRepositoryWithoutPermission() {
      RepositoryLinkEnricher enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider);
      HalEnricherContext context = HalEnricherContext.of(repository);

      enricher.enrich(context, halAppender);

      verifyNoInteractions(halAppender);
    }

  }
}

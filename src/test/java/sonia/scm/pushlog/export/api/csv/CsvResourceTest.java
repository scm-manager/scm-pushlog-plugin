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

package sonia.scm.pushlog.export.api.csv;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.pushlog.PushlogEntryStoreFactory;
import sonia.scm.pushlog.PushlogManager;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.QueryableStoreExtension;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static sonia.scm.pushlog.export.api.csv.CsvPushlogEntry.HEADER;
import static sonia.scm.pushlog.export.api.csv.CsvResourceTest.TimestampsAvailable.MIXED;
import static sonia.scm.pushlog.export.api.csv.CsvResourceTest.TimestampsAvailable.YES;

@ExtendWith({MockitoExtension.class, QueryableStoreExtension.class})
@QueryableStoreExtension.QueryableTypes(PushlogEntry.class)
class CsvResourceTest {

  static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'UTC'Z");
  private final Repository repository = RepositoryTestData.createHeartOfGold("git");
  static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault(); // using this time zone as a non-UTC value

  private static final String REVISION_1 = "35fd6a";
  private static final String REVISION_2 = "f5fd6b";
  private static final String REVISION_3 = "05fd6c";

  private static final String USER_1 = "user1";
  private static final String USER_2 = "user2";
  private static final String USER_3 = "user3";

  private static final ZonedDateTime TEST_TIME_NEWEST = Instant.now().atZone(DEFAULT_ZONE);
  private static final ZonedDateTime TEST_TIME_MID = TEST_TIME_NEWEST.minusDays(299);
  private static final ZonedDateTime TEST_TIME_OLDEST = TEST_TIME_NEWEST.minusDays(735);

  public enum TimestampsAvailable {
    NO, MIXED, YES;

    @Override
    public String toString() {
      if (this == NO) {
        return "No entries have timestamps";
      }
      if (this == MIXED) {
        return "Some but not all entries have timestamps";
      }
      return "All entries have timestamps";
    }
  }

  @Mock
  private RepositoryManager repositoryManager;

  private PushlogEntryStoreFactory entryStoreFactory;

  private RestDispatcher dispatcher;

  @BeforeEach
  void setUp(PushlogEntryStoreFactory entryStoreFactory) {
    this.entryStoreFactory = entryStoreFactory;
    PushlogManager pushlogManager = new PushlogManager(entryStoreFactory);
    CsvResource resource = new CsvResource(pushlogManager, repositoryManager);

    lenient()
      .when(
        repositoryManager.get(
          new NamespaceAndName(repository.getNamespace(), repository.getName())
        )
      )
      .thenReturn(repository);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  void setDbResult(Map<String, PushlogEntry> pushlogs) {
    for (Map.Entry<String, PushlogEntry> pushlog : pushlogs.entrySet()) {
      entryStoreFactory.getMutable(repository).put(pushlog.getKey(), pushlog.getValue());
    }
  }

  private Map<String, PushlogEntry> createEntries(TimestampsAvailable timestampsAvailable) {
    Map<String, PushlogEntry> entries = new HashMap<>();

    entries.put(REVISION_1, new PushlogEntry(2, USER_1,
      timestampsAvailable.equals(YES) || timestampsAvailable.equals(MIXED) ? TEST_TIME_NEWEST.toInstant() : null, "First Commit"));
    entries.put(REVISION_2, new PushlogEntry(1, USER_2,
      timestampsAvailable.equals(YES) ? TEST_TIME_MID.toInstant() : null, "Second Commit"));
    entries.put(REVISION_3, new PushlogEntry(0, USER_3,
      timestampsAvailable.equals(YES) || timestampsAvailable.equals(MIXED) ? TEST_TIME_OLDEST.toInstant() : null, null));

    return entries;
  }

  private String withEmptyStringIfNull(Instant time, ZonedDateTime zonedDateTime) {
    if (time == null) {
      return "";
    } else {
      return FORMATTER.format(zonedDateTime);
    }
  }

  @Nested
  class GetAll {

    @Test
    void shouldParseSingleEntryCsv() throws URISyntaxException, UnsupportedEncodingException {
      Instant testTime = Instant.now().minus(365, ChronoUnit.DAYS);

      setDbResult(Map.of(REVISION_1, new PushlogEntry(1, "darthvader", testTime, "Commit Message")));

      MockHttpRequest request = MockHttpRequest.get("/v2/pushlogs/csv/" + repository.getNamespace() + "/" + repository.getName());
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(response.getOutputHeaders().get("Content-Type").toString().split(",")[0]).contains(MediaType.TEXT_PLAIN);

      String[] lines = response.getContentAsString().split("\n");

      assertThat(lines).hasSize(2);
      assertThat(lines[0]).isEqualTo(HEADER);
      assertThat(lines[1]).isEqualTo(
        format("%s,%s,%s,%s,%s", 1, REVISION_1, "darthvader", testTime.atZone(DEFAULT_ZONE).format(FORMATTER), "Commit Message")
      );
    }

    @ParameterizedTest
    @EnumSource(TimestampsAvailable.class)
    void shouldListEntriesInDescendingOrderByDefault(TimestampsAvailable timestampsAvailable) throws URISyntaxException, IOException {

      Map<String, PushlogEntry> entries = createEntries(timestampsAvailable);
      setDbResult(entries);

      MockHttpRequest request = MockHttpRequest.get("/v2/pushlogs/csv/" + repository.getNamespace() + "/" + repository.getName());
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      String[] lines = response.getContentAsString().split("\n");

      assertThat(lines).hasSize(4);

      assertThat(lines[0]).isEqualTo(HEADER);
      assertThat(lines[1]).isEqualTo(
        format("%s,%s,%s,%s,%s",
          2,
          REVISION_1,
          USER_1,
          withEmptyStringIfNull(entries.get(REVISION_1).getContributionTime(), TEST_TIME_NEWEST),
          "First Commit"
        )
      );
      assertThat(lines[2]).isEqualTo(
        format("%s,%s,%s,%s,%s",
          1,
          REVISION_2,
          USER_2,
          withEmptyStringIfNull(entries.get(REVISION_2).getContributionTime(), TEST_TIME_MID),
          "Second Commit"
        )
      );
      assertThat(lines[3]).isEqualTo(
        format("%s,%s,%s,%s,",
          0,
          REVISION_3,
          USER_3,
          withEmptyStringIfNull(entries.get(REVISION_3).getContributionTime(), TEST_TIME_OLDEST)
        )
      );
    }

    @Test
    void shouldThrow404IfRepositoryNotFound() throws URISyntaxException, UnsupportedEncodingException {
      MockHttpRequest request = MockHttpRequest.get("/v2/pushlogs/csv/" + "thisdoesnt/exist");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(404);
      assertThat(response.getContentAsString()).contains("Repository \"thisdoesnt/exist\" not found.");
    }
  }

  @Nested
  class Export {

    @Test
    void shouldPutEntriesInDownloadableFile() throws URISyntaxException, UnsupportedEncodingException {
      Map<String, PushlogEntry> entries = createEntries(YES);
      setDbResult(entries);

      MockHttpRequest request = MockHttpRequest.get("/v2/pushlogs/csv/" + repository.getNamespace() + "/" + repository.getName() + "/export");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getOutputHeaders().get("Content-Type").toString().split(",")[0]).contains("text/csv");

      String[] lines = response.getContentAsString().split("\n");

      assertThat(lines).hasSize(4);
      assertThat(lines[0]).isEqualTo(HEADER);
      assertThat(lines[1]).isEqualTo(
        format("%s,%s,%s,%s,%s",
          2,
          REVISION_1,
          USER_1,
          withEmptyStringIfNull(entries.get(REVISION_1).getContributionTime(), TEST_TIME_NEWEST),
          "First Commit"
        )
      );
      assertThat(lines[2]).isEqualTo(
        format("%s,%s,%s,%s,%s",
          1,
          REVISION_2,
          USER_2,
          withEmptyStringIfNull(entries.get(REVISION_2).getContributionTime(), TEST_TIME_MID),
          "Second Commit"
        )
      );
      assertThat(lines[3]).isEqualTo(
        format("%s,%s,%s,%s,",
          0,
          REVISION_3,
          USER_3,
          withEmptyStringIfNull(entries.get(REVISION_3).getContributionTime(), TEST_TIME_OLDEST)
        )
      );
    }
  }
}

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

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.pushlog.PushlogManager;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.QueryableStore;

import java.io.IOException;
import java.time.ZoneId;

@Path(CsvResource.CSV_PATH_V2)
public class CsvResource {
  static final String CSV_PATH_V2 = "v2/pushlogs/csv";
  private static final CsvMapper MAPPER = new CsvMapper();
  private static final ZoneId ZONE_ID = ZoneId.systemDefault();

  static {
    MAPPER.registerModule(new JavaTimeModule());
  }

  private final RepositoryManager repositoryManager;
  private final PushlogManager pushlogManager;

  @Inject
  public CsvResource(PushlogManager pushlogManager, RepositoryManager repositoryManager) {
    this.pushlogManager = pushlogManager;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(
    summary = "Collecting all pushlogs for repository",
    description = "Returns a collection of all pushlogs for a given repository in conna-separated format.",
    tags = "Pushlog"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the changeset")
  @ApiResponse(
    responseCode = "404",
    description = "repository not found",
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    ))
  @Path("{namespace}/{name}")
  public StreamingOutput getAll(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name
  ) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));

    if (repository == null) {
      throw new NotFoundException(String.format("Repository \"%s/%s\" not found.", namespace, name));
    }

    CsvSchema schema = MAPPER.schemaFor(CsvPushlogEntry.class).withoutQuoteChar();
    ObjectWriter writer = MAPPER.writer(schema);
    return os ->
    {
      os.write((CsvPushlogEntry.HEADER + "\n").getBytes());
      pushlogManager.doExport(
        repository,
        csvEntry -> {
          try {
            os.write(writer.writeValueAsString(from(csvEntry)).getBytes());
          } catch (IOException e) {
            throw new CsvPushlogException(csvEntry, e);
          }
        },
        QueryableStore.Order.DESC);
    };
  }

  private CsvPushlogEntry from(QueryableStore.Result<PushlogEntry> result) {
    return new CsvPushlogEntry(
      result.getEntity().getPushlogId(),
      result.getId(),
      result.getEntity().getUsername(),
      result.getEntity().getContributionTime() != null ? result.getEntity().getContributionTime().atZone(ZONE_ID) : null,
      result.getEntity().getDescription()
    );
  }

  @GET
  @Produces("text/csv")
  @Operation(
    summary = "Exporting all pushlogs for repository in a downloadable file",
    description = "Exports a collection of all pushlogs for a given repository in a file in conna-separated format.",
    tags = "Pushlog"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = "text/csv"
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the changeset")
  @ApiResponse(
    responseCode = "404",
    description = "repository not found",
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    ))
  @Path("{namespace}/{name}/export")
  public Response export(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name) {
    return Response.ok(getAll(namespace, name), "text/csv").build();
  }
}

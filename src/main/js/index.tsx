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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import PushlogDetails from "./PushlogDetails";
import RepositoryPushlogDownloadLink from "./RepositoryPushlogDownloadLink";

binder.bind<extensionPoints.ChangesetsContributorTableRow>("changesets.contributor.table.row", PushlogDetails, {
  predicate: (props) => !!props.changeset._embedded?.pushlogDetails,
});

binder.bind<extensionPoints.RepositoryInformationTableBottom>("repository.information.table.bottom", RepositoryPushlogDownloadLink, {
  predicate:(props) => !!props.repository._links.pushlogExport,
});

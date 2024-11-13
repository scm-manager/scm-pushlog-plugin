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

import React from "react";
import { Changeset } from "@scm-manager/ui-types";
import { Contributor, ContributorRow, DateFromNow } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  changeset: Changeset;
};

type PushlogDetails = {
  contributorType: string;
  person: string;
  publishedTime: string;
};

const PushlogDetails: React.FC<Props> = ({ changeset }) => {
  const [t] = useTranslation("plugins");
  const pushlogDetails = changeset._embedded.pushlogDetails as PushlogDetails;

  return (
    <ContributorRow label={t("changeset.contributor.type.pushedBy")}>
      <Contributor person={pushlogDetails.person} />{" "}
      {pushlogDetails.publishedTime ? <DateFromNow date={pushlogDetails.publishedTime} /> : null}
    </ContributorRow>
  );
};
export default PushlogDetails;

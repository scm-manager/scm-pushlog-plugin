// @flow

import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  changeset: Changeset,

  t: string => string
};

type State = {};
class PushlogCommitter extends React.Component<Props, State> {
  render() {
    const { changeset, t } = this.props;
    if (!changeset || !changeset._embedded.committer) {
      return null;
    }
    return <>, {t("scm-pushlog-plugin.pushedBy")} {changeset._embedded.committer.name}</>;
  }
}

export default translate("plugins")(PushlogCommitter);

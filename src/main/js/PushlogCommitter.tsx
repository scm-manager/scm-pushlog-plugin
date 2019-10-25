import React from "react";
import { Changeset } from "@scm-manager/ui-types";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  changeset: Changeset;
};

class PushlogCommitter extends React.Component<Props> {
  render() {
    const { changeset, t } = this.props;
    if (!changeset || !changeset._embedded.committer) {
      return null;
    }
    return (
      <>
        , {t("scm-pushlog-plugin.pushedBy")} {changeset._embedded.committer.name}
      </>
    );
  }
}

export default withTranslation("plugins")(PushlogCommitter);

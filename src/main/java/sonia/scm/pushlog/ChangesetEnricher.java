package sonia.scm.pushlog;

import com.google.inject.Inject;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

@Extension
@Enrich(Changeset.class)
public class ChangesetEnricher implements HalEnricher {

    private PushlogManager pushlogManager;

    @Inject
    protected ChangesetEnricher(PushlogManager pushlogManager) {
        super();
        this.pushlogManager = pushlogManager;
    }

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
        Changeset changeset = context.oneRequireByType(Changeset.class);
        Repository repository = context.oneRequireByType(Repository.class);
        Pushlog pushlog = pushlogManager.get(repository);
        String username = pushlog.get(changeset.getId());

        if (username != null) {
            appender.appendEmbedded("committer", new CommitterDto(username));
        }
    }
}

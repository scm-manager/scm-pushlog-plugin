package sonia.scm.pushlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

@Extension
public class ChangesetEnricher extends JsonEnricherBase {

    @Inject
    protected ChangesetEnricher(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void enrich(JsonEnricherContext context) {
        if (resultHasMediaType(VndMediaType.CHANGESET, context)) {
            JsonNode changesetNode = context.getResponseEntity();

        }
    }
}

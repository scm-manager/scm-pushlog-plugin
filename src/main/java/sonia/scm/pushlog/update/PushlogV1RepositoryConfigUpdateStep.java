package sonia.scm.pushlog.update;

import com.google.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.PropertyFileAccess;
import sonia.scm.version.Version;

import java.io.IOException;

import static sonia.scm.version.Version.*;

@Extension
public class PushlogV1RepositoryConfigUpdateStep implements UpdateStep {

  private final PropertyFileAccess propertyFileAccess;

  @Inject
  public PushlogV1RepositoryConfigUpdateStep(PropertyFileAccess propertyFileAccess) {
    this.propertyFileAccess = propertyFileAccess;
  }

  @Override
  public void doUpdate() throws IOException {
    PropertyFileAccess.StoreFileTools pushlogStoreAccess = propertyFileAccess.forStoreName("pushlog");
    pushlogStoreAccess.forStoreFiles(pushlogStoreAccess::moveAsRepositoryStore);
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.pushlog.data.xml";
  }
}

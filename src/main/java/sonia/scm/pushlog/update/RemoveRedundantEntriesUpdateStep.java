package sonia.scm.pushlog.update;

import lombok.extern.slf4j.Slf4j;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.pushlog.Pushlog;
import sonia.scm.pushlog.PushlogEntry;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
@Extension
public class RemoveRedundantEntriesUpdateStep implements UpdateStep {

  private static final String PUSHLOG_STORE_NAME = "pushlog";

  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final DataStoreFactory storeFactory;

  @Inject
  public RemoveRedundantEntriesUpdateStep(RepositoryUpdateIterator repositoryUpdateIterator, DataStoreFactory storeFactory) {
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    repositoryUpdateIterator.forEachRepository(this::doUpdate);
  }

  private void doUpdate(String repositoryId) {
    DataStore<Pushlog> store = storeFactory
      .withType(Pushlog.class)
      .withName(PUSHLOG_STORE_NAME)
      .forRepository(repositoryId)
      .build();
    store.getOptional(PUSHLOG_STORE_NAME)
      .ifPresent(pushlog -> optimize(store, pushlog));
  }

  private void optimize(DataStore<Pushlog> store, Pushlog pushlog) {
    if (optimize(pushlog)) {
      log.info("found illegal multiple pushlog entries for repository {}; cleaning up", pushlog.getRepositoryId());
      store.put(PUSHLOG_STORE_NAME, pushlog);
    }
  }

  @SuppressWarnings({"java:S3011", "unchecked"}) // reflection is used only for this update step and there should be acceptable
  private boolean optimize(Pushlog pushlog) {
    Set<String> foundChangesets = new HashSet<>();
    boolean errorsFound = false;
    for (Iterator<PushlogEntry> entryIterator = pushlog.getEntries().iterator(); entryIterator.hasNext(); ) {
      PushlogEntry entry = entryIterator.next();
      try {
        Field changesetsField = PushlogEntry.class.getDeclaredField("changesets");
        changesetsField.setAccessible(true);
        Set<String> changesets = (Set<String>) changesetsField.get(entry);
        if (changesets != null) {
          errorsFound = checkChangesets(foundChangesets, errorsFound, changesets);
          if (changesets.isEmpty()) {
            entryIterator.remove();
          }
        }
      } catch (Exception e) {
        log.warn("could not clean up pushlog for repository {}", pushlog.getRepositoryId(), e);
        return false;
      }
    }
    return errorsFound;
  }

  private static boolean checkChangesets(Set<String> foundChangesets, boolean errorsFound, Set<String> changesets) {
    for (Iterator<String> changesetIterator = changesets.iterator(); changesetIterator.hasNext(); ) {
      String currentChangeset = changesetIterator.next();
      if (foundChangesets.contains(currentChangeset)) {
        changesetIterator.remove();
        errorsFound = true;
      }
      foundChangesets.add(currentChangeset);
    }
    return errorsFound;
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.2");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.pushlog.data.xml";
  }
}

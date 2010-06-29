package com.opengamma.financial.livedata.user;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifierTemplate;

/**
 * In memory, per user, snapshot and availability provider.
 */
public class InMemoryUserSnapshotProvider extends InMemoryLKVSnapshotProvider {

  private final UniqueIdentifierTemplate _uidTemplate;

  public InMemoryUserSnapshotProvider(final UniqueIdentifierTemplate uidTemplate) {
    _uidTemplate = uidTemplate;
  }

  public ValueRequirement makeValueRequirement(final String name, final ComputationTargetType type,
      final String identifier) {
    return new ValueRequirement(name, type, _uidTemplate.uid(identifier));
  }

  public void putValue(final ValueRequirement valueRequirement, final Object value) {
    addValue(new ComputedValue(new ValueSpecification(valueRequirement), value));
  }

  public Object getValue(final ValueRequirement valueRequirement) {
    final ComputedValue value = getCurrentValue(valueRequirement);
    return (value != null) ? value.getValue() : null;
  }

}

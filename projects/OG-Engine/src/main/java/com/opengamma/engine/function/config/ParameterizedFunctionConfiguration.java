/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * Parameterized function configuration representation
 */
@BeanDefinition
public class ParameterizedFunctionConfiguration extends StaticFunctionConfiguration implements FunctionConfiguration {
  
  private static final long serialVersionUID = 1L;
  
  @PropertyDefinition(set = "setClearAddAll")
  private List<String> _parameter = Lists.newArrayList();
  
  /**
   * Creates an instance
   * 
   * @param definitionClassName the definition class name, not null.
   * @param parameter the list of parameters, not null.
   */
  public ParameterizedFunctionConfiguration(final String definitionClassName, final Collection<String> parameter) {
    super(definitionClassName);
    ArgumentChecker.notNull(parameter, "parameters");
    _parameter.addAll(parameter);
  }
  
  /**
   * Constructor for builders
   */
  ParameterizedFunctionConfiguration() {
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ParameterizedFunctionConfiguration}.
   * @return the meta-bean, not null
   */
  public static ParameterizedFunctionConfiguration.Meta meta() {
    return ParameterizedFunctionConfiguration.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ParameterizedFunctionConfiguration.Meta.INSTANCE);
  }

  @Override
  public ParameterizedFunctionConfiguration.Meta metaBean() {
    return ParameterizedFunctionConfiguration.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1954460585:  // parameter
        return getParameter();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1954460585:  // parameter
        setParameter((List<String>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ParameterizedFunctionConfiguration other = (ParameterizedFunctionConfiguration) obj;
      return JodaBeanUtils.equal(getParameter(), other.getParameter()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getParameter());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parameter.
   * @return the value of the property
   */
  public List<String> getParameter() {
    return _parameter;
  }

  /**
   * Sets the parameter.
   * @param parameter  the new value of the property
   */
  public void setParameter(List<String> parameter) {
    this._parameter.clear();
    this._parameter.addAll(parameter);
  }

  /**
   * Gets the the {@code parameter} property.
   * @return the property, not null
   */
  public final Property<List<String>> parameter() {
    return metaBean().parameter().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ParameterizedFunctionConfiguration}.
   */
  public static class Meta extends StaticFunctionConfiguration.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code parameter} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<String>> _parameter = DirectMetaProperty.ofReadWrite(
        this, "parameter", ParameterizedFunctionConfiguration.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "parameter");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1954460585:  // parameter
          return _parameter;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ParameterizedFunctionConfiguration> builder() {
      return new DirectBeanBuilder<ParameterizedFunctionConfiguration>(new ParameterizedFunctionConfiguration());
    }

    @Override
    public Class<? extends ParameterizedFunctionConfiguration> beanType() {
      return ParameterizedFunctionConfiguration.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code parameter} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<String>> parameter() {
      return _parameter;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

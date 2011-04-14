/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.util.ArgumentChecker;

/**
 * A bundle representation
 */
@BeanDefinition
public class Bundle extends DirectBean implements BundleNode {

  /**
   * List of nodes, either a bundle or fragment
   */
  @PropertyDefinition
  private final List<BundleNode> _childNodes = new ArrayList<BundleNode>();
  
  /**
   * The Id for the bundle
   */
  @PropertyDefinition
  private String _id;
  
  /**
   * The type of bundle js or css
   */
  @PropertyDefinition
  private BundleType _type;

  /**
   * Creates a bundle.
   */
  public Bundle() {
  }
  
  /**
   * Creates a bundle with an Id.
   * @param id the bundle name, not - null
   */
  public Bundle(final String id) {
    ArgumentChecker.notNull(id, "id");
    _id = id;
    _type = BundleType.getType(id);
  }

  @Override
  public List<Fragment> getAllFragment() {
    List<Fragment> result = new ArrayList<Fragment>();
    for (BundleNode node : getChildNodes()) {
      result.addAll(node.getAllFragment());
    }
    return result;
  }
  
  /**
   * Adds a child node to this node.
   * 
   * @param childNode  the child node, not null
   */
  public void addChildNode(final BundleNode childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    getChildNodes().add(childNode);
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Bundle}.
   * @return the meta-bean, not null
   */
  public static Bundle.Meta meta() {
    return Bundle.Meta.INSTANCE;
  }

  @Override
  public Bundle.Meta metaBean() {
    return Bundle.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 1339293429:  // childNodes
        return getChildNodes();
      case 3355:  // id
        return getId();
      case 3575610:  // type
        return getType();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 1339293429:  // childNodes
        setChildNodes((List<BundleNode>) newValue);
        return;
      case 3355:  // id
        setId((String) newValue);
        return;
      case 3575610:  // type
        setType((BundleType) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets list of nodes, either a bundle or fragment
   * @return the value of the property
   */
  public List<BundleNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Sets list of nodes, either a bundle or fragment
   * @param childNodes  the new value of the property
   */
  public void setChildNodes(List<BundleNode> childNodes) {
    this._childNodes.clear();
    this._childNodes.addAll(childNodes);
  }

  /**
   * Gets the the {@code childNodes} property.
   * @return the property, not null
   */
  public final Property<List<BundleNode>> childNodes() {
    return metaBean().childNodes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Id for the bundle
   * @return the value of the property
   */
  public String getId() {
    return _id;
  }

  /**
   * Sets the Id for the bundle
   * @param id  the new value of the property
   */
  public void setId(String id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<String> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type of bundle js or css
   * @return the value of the property
   */
  public BundleType getType() {
    return _type;
  }

  /**
   * Sets the type of bundle js or css
   * @param type  the new value of the property
   */
  public void setType(BundleType type) {
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * @return the property, not null
   */
  public final Property<BundleType> type() {
    return metaBean().type().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Bundle}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code childNodes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<BundleNode>> _childNodes = DirectMetaProperty.ofReadWrite(this, "childNodes", (Class) List.class);
    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<String> _id = DirectMetaProperty.ofReadWrite(this, "id", String.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<BundleType> _type = DirectMetaProperty.ofReadWrite(this, "type", BundleType.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("childNodes", _childNodes);
      temp.put("id", _id);
      temp.put("type", _type);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public Bundle createBean() {
      return new Bundle();
    }

    @Override
    public Class<? extends Bundle> beanType() {
      return Bundle.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code childNodes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<BundleNode>> childNodes() {
      return _childNodes;
    }

    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BundleType> type() {
      return _type;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

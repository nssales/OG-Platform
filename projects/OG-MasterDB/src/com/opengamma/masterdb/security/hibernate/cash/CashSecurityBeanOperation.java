/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.cash;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierToIdentifierBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * Bean/security conversion operations.
 */
public final class CashSecurityBeanOperation extends AbstractSecurityBeanOperation<CashSecurity, CashSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CashSecurityBeanOperation INSTANCE = new CashSecurityBeanOperation();

  private CashSecurityBeanOperation() {
    super("CASH", CashSecurity.class, CashSecurityBean.class);
  }

  @Override
  public boolean beanEquals(final OperationContext context, CashSecurityBean bean, CashSecurity security) {
    return ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency()), security.getCurrency())
        && ObjectUtils.equals(identifierBeanToIdentifier(bean.getRegion()), security.getRegion())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getMaturity()), security.getMaturity())
        && bean.getRate() == security.getRate() // should maybe use closeEquals()?
        && bean.getAmount() == security.getAmount();
  }

  @Override
  public CashSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CashSecurity security) {
    CurrencyBean currencyBean = secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode());
    IdentifierBean regionIdentifier = identifierToIdentifierBean(security.getRegion());
    ZonedDateTimeBean maturityBean = dateTimeWithZoneToZonedDateTimeBean(security.getMaturity());
    final CashSecurityBean bean = new CashSecurityBean();
    bean.setCurrency(currencyBean);
    bean.setRegion(regionIdentifier);
    bean.setMaturity(maturityBean);
    bean.setRate(security.getRate());
    bean.setAmount(security.getAmount());
    return bean;
  }

  @Override
  public CashSecurity createSecurity(final OperationContext context, CashSecurityBean bean) {
    final CashSecurity security = new CashSecurity(currencyBeanToCurrency(bean.getCurrency()), identifierBeanToIdentifier(bean.getRegion()), 
                                                   zonedDateTimeBeanToDateTimeWithZone(bean.getMaturity()), bean.getRate(), bean.getAmount());
    return security;
  }

}

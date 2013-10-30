/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.example;

import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.sesame.MarketData;
import com.opengamma.sesame.function.Output;

/**
 * Trivial example function that returns the description of a cash flow security.
 */
public interface CashFlowDescriptionFunction {

  /**
   * Returns a description of the security
   *
   * @param marketData Not used
   * @param security A security
   * @return A description of the security
   */
  @Output(OutputNames.DESCRIPTION)
  String getDescription(MarketData marketData, CashFlowSecurity security);
}

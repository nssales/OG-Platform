/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;

/**
 * 
 * @author emcleod
 */
public class BlackDermanToyDataBundle {
  private final DiscountCurve _yieldCurve;
  private final VolatilityCurve _volatilityCurve;
  private final ZonedDateTime _date;

  public BlackDermanToyDataBundle(final DiscountCurve yieldCurve, final VolatilityCurve volatilityCurve, final ZonedDateTime date) {
    _yieldCurve = yieldCurve;
    _volatilityCurve = volatilityCurve;
    _date = date;
  }

  public DiscountCurve getYieldCurve() {
    return _yieldCurve;
  }

  public VolatilityCurve getVolatilityCurve() {
    return _volatilityCurve;
  }

  public ZonedDateTime getDate() {
    return _date;
  }

  public Double getInterestRate(final Double t) {
    return getYieldCurve().getInterestRate(t);
  }

  public Double getVolatility(final Double t) {
    return getVolatilityCurve().getVolatility(t);
  }
}

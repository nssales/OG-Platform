/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class SampleStandardDeviationCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _variance = new SampleVarianceCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two points to calculate standard deviation");
    return Math.sqrt(_variance.evaluate(x));
  }

}

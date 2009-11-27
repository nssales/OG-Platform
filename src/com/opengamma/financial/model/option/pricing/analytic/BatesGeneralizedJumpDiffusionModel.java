/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.BatesGeneralizedJumpDiffusionModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */

public class BatesGeneralizedJumpDiffusionModel extends AnalyticOptionModel<OptionDefinition, BatesGeneralizedJumpDiffusionModelOptionDataBundle> {
  protected final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  protected final int N = 50;

  @Override
  public Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Definition was null");
    final Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double> pricingFunction = new Function1D<BatesGeneralizedJumpDiffusionModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final BatesGeneralizedJumpDiffusionModelOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final double s = data.getSpot();
        final DiscountCurve discountCurve = data.getDiscountCurve();
        final VolatilitySurface volSurface = data.getVolatilitySurface();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final double k = definition.getStrike();
        final double sigma = data.getVolatility(t, k);
        double b = data.getCostOfCarry();
        final double lambda = data.getLambda();
        final double expectedJumpSize = data.getExpectedJumpSize();
        final double delta = data.getDelta();
        final double gamma = Math.log(1 + expectedJumpSize);
        final double sigmaSq = sigma * sigma;
        double z;
        final double lambdaT = lambda * t;
        double mult = Math.exp(-lambdaT);
        b -= lambda * expectedJumpSize;
        StandardOptionDataBundle bsmData = new StandardOptionDataBundle(discountCurve, b, volSurface, s, date);
        final Function1D<StandardOptionDataBundle, Double> bsmFunction = _bsm.getPricingFunction(definition);
        double price = mult * bsmFunction.evaluate(bsmData);
        for (int i = 1; i < N; i++) {
          z = Math.sqrt(sigmaSq + delta * delta * i / t);
          b += gamma / t;
          bsmData = bsmData.withVolatilitySurface(new ConstantVolatilitySurface(z)).withCostOfCarry(b);
          mult *= lambdaT / i;
          price += mult * bsmFunction.evaluate(bsmData);
        }
        return price;
      }
    };
    return pricingFunction;
  }
}

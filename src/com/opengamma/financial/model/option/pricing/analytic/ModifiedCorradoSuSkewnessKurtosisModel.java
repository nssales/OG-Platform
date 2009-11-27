/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */

public class ModifiedCorradoSuSkewnessKurtosisModel extends AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> {
  final BlackScholesMertonModel _bsm = new BlackScholesMertonModel();
  final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Definition was null");
    final Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final SkewKurtosisOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double skew = data.getAnnualizedSkew();
        final double kurtosis = data.getAnnualizedFisherKurtosis();
        final double sigmaT = sigma * Math.sqrt(t);
        OptionDefinition callDefinition = definition;
        if (!definition.isCall()) {
          callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
        }
        final Function1D<StandardOptionDataBundle, Double> bsm = _bsm.getPricingFunction(callDefinition);
        final double bsmCall = bsm.evaluate(data);
        final double w = getW(sigma, t, skew, kurtosis);
        final double d = getD(s, k, sigma, t, b, w, sigmaT);
        final double call = bsmCall + skew * getQ3(s, d, w, sigmaT) + kurtosis * getQ4(s, d, w, sigmaT);
        if (!definition.isCall())
          return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
        return call;
      }
    };
    return pricingFunction;
  }

  double getW(final double sigma, final double t, final double skew, final double kurtosis) {
    final double sigma3 = sigma * sigma * sigma;
    return skew * sigma3 * Math.pow(t, 1.5) / 6. + kurtosis * sigma * sigma3 * t * t / 24.;
  }

  double getD(final double s, final double k, final double sigma, final double t, final double b, final double w, final double sigmaT) {
    return getD1(s, k, t, sigma, b) - Math.log(1 + w) / sigmaT;
  }

  double getQ3(final double s, final double d, final double w, final double sigmaT) {
    return s * sigmaT * (2 * sigmaT - d) * _normal.getPDF(d) / (6 * (1 + w));
  }

  double getQ4(final double s, final double d, final double w, final double sigmaT) {
    return s * sigmaT * (d * d - 3 * d * sigmaT + 3 * sigmaT * sigmaT - 1) * _normal.getPDF(d) / (24 * (1 + w));
  }
}

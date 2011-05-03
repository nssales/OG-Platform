/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.CharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.FFTPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.math.cube.Cube;
import com.opengamma.math.cube.FunctionalDoublesCube;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class HestonPDETestCase {

  private static BoundaryCondition2D F_LOWER;
  private static BoundaryCondition2D F_UPPER;
  private static BoundaryCondition2D V_LOWER;
  private static BoundaryCondition2D V_UPPER;

  private static final double F0 = 0.05;
  private static final double V0 = 0.01;
  private static final double KAPPA = 0.2;// changed from 0.2
  private static final double THETA = 0.07;
  private static final double OMEGA = 0.2;
  private static final double RHO = -0.50;// changed from -0.5

  private static final double STRIKE = 0.06;
  private static final double T = 1.0;
  private static final double RATE = 0.0;

  // private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  // private static final OptionDefinition OPTION;
  private static final ConvectionDiffusion2DPDEDataBundle DATA;

  private static Cube<Double, Double, Double, Double> A;
  private static Cube<Double, Double, Double, Double> B;
  private static Cube<Double, Double, Double, Double> C;
  private static Cube<Double, Double, Double, Double> D;
  private static Cube<Double, Double, Double, Double> E;
  private static Cube<Double, Double, Double, Double> F;

  static {

    // final Function<Double, Double> volZeroBoundary = new Function<Double, Double>() {
    // @Override
    // public Double evaluate(final Double... tx) {
    // Validate.isTrue(tx.length == 2);
    // double x = tx[1];
    // return Math.max(x - STRIKE, 0);
    // }
    // };
    //
    // final Function<Double, Double> volInfiniteBoundary = new Function<Double, Double>() {
    // @Override
    // public Double evaluate(final Double... tx) {
    // Validate.isTrue(tx.length == 2);
    // double x = tx[1];
    // return x;
    // }
    // };

    F_LOWER = new DirichletBoundaryCondition2D(0.0, 0.0); // option worth zero if spot is zero
    F_UPPER = new SecondDerivativeBoundaryCondition2D(0.0, 5 * F0); // option price linear in spot for spot -> infinity
    V_LOWER = new SecondDerivativeBoundaryCondition2D(0.0, 0.0);
    V_UPPER = new SecondDerivativeBoundaryCondition2D(0.0, 5 * V0);
    // F_UPPER = new DirichletBoundaryCondition2D(0.0, 5 * F0); // a knock-out barrier
    // V_LOWER = new DirichletBoundaryCondition2D(0.0, 0.0);
    // V_UPPER = new DirichletBoundaryCondition2D(0.0, 5 * V0);

    // TODO these more sophisticated boundary conditions don't work
    // V_LOWER = new DirichletBoundaryCondition2D(FunctionalDoublesSurface.from(volZeroBoundary), 0); // TODO should be solving the convection PDE on this boundary
    // V_UPPER = new DirichletBoundaryCondition2D(FunctionalDoublesSurface.from(volInfiniteBoundary), 5 * V0); // option the same as underlying for vol -> infinity

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        double y = txy[2];
        return -0.5 * x * x * y;
      }
    };
    A = FunctionalDoublesCube.from(a);

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        return -x * RATE;
      }
    };
    B = FunctionalDoublesCube.from(b);

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        return RATE;
      }
    };
    C = FunctionalDoublesCube.from(c);

    final Function<Double, Double> d = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double y = txy[2];
        return -0.5 * OMEGA * OMEGA * y;
      }
    };
    D = FunctionalDoublesCube.from(d);

    final Function<Double, Double> e = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        double y = txy[2];

        return -x * y * OMEGA * RHO;
      }
    };
    E = FunctionalDoublesCube.from(e);

    final Function<Double, Double> f = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double y = txy[2];
        return -KAPPA * (THETA - y);
      }
    };
    F = FunctionalDoublesCube.from(f);

    final Function<Double, Double> payoff = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... xy) {
        Validate.isTrue(xy.length == 2);
        double x = xy[0];
        return Math.max(x - STRIKE, 0);
      }
    };

    DATA = new ConvectionDiffusion2DPDEDataBundle(A, B, C, D, E, F, FunctionalDoublesSurface.from(payoff));
  }

  public void testCallPrice(ConvectionDiffusionPDESolver2D solver, int timeSteps, int spotSteps, int volSqrSteps, boolean print) {

    double deltaX = (F_UPPER.getLevel() - F_LOWER.getLevel()) / spotSteps;
    double deltaY = (V_UPPER.getLevel() - V_LOWER.getLevel()) / volSqrSteps;

    double[][] res = solver.solve(DATA, timeSteps, spotSteps, volSqrSteps, T, F_LOWER, F_UPPER, V_LOWER, V_UPPER);

    if (print) {
      int xSteps = res.length - 1;
      int ySteps = res[0].length - 1;
      for (int j = 0; j <= ySteps; j++) {
        System.out.print("\t" + (V_LOWER.getLevel() + j * deltaY));
      }
      System.out.print("\n");
      for (int i = 0; i <= xSteps; i++) {
        System.out.print(F_LOWER.getLevel() + i * deltaX);
        for (int j = 0; j <= ySteps; j++) {
          System.out.print("\t" + res[i][j]);
        }
        System.out.print("\n");
      }
    }

    // TODO There is no guarantee that F0 and V0 are grid points (it depends on the chosen step sizes), so we should do a surface interpolation (what fun!)
    double pdfPrice = res[(int) (F0 / deltaX)][(int) (V0 / deltaY)];

    // System.out.print("\n");
    FFTPricer pricer = new FFTPricer();
    final CharacteristicExponent heston = new HestonCharacteristicExponent(KAPPA, THETA, V0, OMEGA, RHO);

    final int n = 51;
    final double alpha = -0.5;
    final double tol = 1e-12;
    EuropeanVanillaOption option = new EuropeanVanillaOption(F0, T, true);
    final BlackFunctionData data = new BlackFunctionData(F0, 1.0, 0.2);
    final double[][] strikeNprice = pricer.price(data, option, heston, STRIKE / 2, STRIKE * 2, n, alpha, tol);

    int nStrikes = strikeNprice.length;
    double[] k = new double[nStrikes];
    double[] price = new double[nStrikes];

    for (int i = 0; i < nStrikes; i++) {
      k[i] = strikeNprice[i][0];
      price[i] = strikeNprice[i][1];
    }

    Interpolator1D<Interpolator1DDataBundle> interpolator = Interpolator1DFactory.getInterpolator("DoubleQuadratic");
    final Interpolator1DDataBundle dataBundle = interpolator.getDataBundleFromSortedArrays(k, price);

    double fftPrice = interpolator.interpolate(dataBundle, STRIKE);

    // System.out.println(fftPrice + "\t" + pdfPrice);
    assertEquals(fftPrice, pdfPrice, 2e-6);

    // for (int i = 0; i < strikeNprice.length; i++) {
    // System.out.println(strikeNprice[i][0] + "\t" + strikeNprice[i][1]);
    // }

  }

}

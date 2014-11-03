/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.VariableNotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AbstractAnnuityDefinitionBuilder.CouponStub;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Test the builder of floating annuities.
 */
public class FloatingAnnuityDefinitionBuilderTest {

  /** USD conventions */
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR =
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_FEDFUND =
      new AdjustedDateParameters(NYC, BusinessDayConventions.MODIFIED_FOLLOWING);
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_FIXING_FEDFUND =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final IndexON USDFEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  /** Overnight Arithmetic Average - Leg details */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 7, 18);
  private static final int TENOR_YEAR_1 = 10;
  private static final LocalDate MATURITY_DATE_1 = EFFECTIVE_DATE_1.plus(Period.ofYears(TENOR_YEAR_1));
  private static final Period PAYMENT_PERIOD = Period.ofMonths(3);
  //  private static final int SPOT_OFFSET = 2;
  //  private static final int PAY_OFFSET = 0;
  //  private static final int CUT_OFF_OFFSET = 2;
  private static final double SPREAD_1 = 0.0010;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  /** Ibor - Leg details */

  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_AMOUNT = 1.0E-2;

  private static final AnnuityDefinition<? extends CouponDefinition> ONAA_LEG_1_DEFINITION =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(PAYER_1).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1)
          .index(USDFEDFUND).
          accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_FEDFUND).accrualPeriodParameters(ADJUSTED_DATE_FEDFUND).
          dayCount(USDFEDFUND.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_FEDFUND).currency(USD)
          .spread(SPREAD_1).
          build();

  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_1_DEFINITION =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(PAYER_1).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1)
          .index(USDLIBOR3M).
          accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
          .spread(SPREAD_1).
          build();

  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_NOTIONAL_1_DEFINITION =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(PAYER_1).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1)
          .index(USDLIBOR3M).
          accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
          .spread(SPREAD_1).
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
          build();

  @Test
  public void arithmeticAverage() {
    int nbOnAaCpn = TENOR_YEAR_1 * 4;
    assertEquals("FloatingAnnuityDefinitionBuilderTest: arithmetic average",
        ONAA_LEG_1_DEFINITION.getNumberOfPayments(), nbOnAaCpn);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: arithmetic average",
        ONAA_LEG_1_DEFINITION.getNthPayment(nbOnAaCpn - 1).getPaymentDate().toLocalDate(), MATURITY_DATE_1);
    ZonedDateTime effectiveDateTime = DateUtils.getUTCDate(EFFECTIVE_DATE_1.getYear(),
        EFFECTIVE_DATE_1.getMonthValue(),
        EFFECTIVE_DATE_1.getDayOfMonth());
    for (int loopcpn = 0; loopcpn < nbOnAaCpn; loopcpn++) {
      assertTrue("FloatingAnnuityDefinitionBuilderTest: arithmetic average",
          ONAA_LEG_1_DEFINITION.getNthPayment(loopcpn) instanceof CouponONArithmeticAverageSpreadDefinition);
      CouponONArithmeticAverageSpreadDefinition cpn =
          (CouponONArithmeticAverageSpreadDefinition) ONAA_LEG_1_DEFINITION.getNthPayment(loopcpn);
      ZonedDateTime expectedPaymentDate = ScheduleCalculator.getAdjustedDate(effectiveDateTime,
          PAYMENT_PERIOD.multipliedBy(loopcpn + 1), ADJUSTED_DATE_FEDFUND.getBusinessDayConvention(), NYC);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: arithmetic average", cpn.getPaymentDate().toLocalDate(),
          expectedPaymentDate.toLocalDate());
      assertEquals("FloatingAnnuityDefinitionBuilderTest: arithmetic average", cpn.getSpread(),
          SPREAD_1, TOLERANCE_RATE);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: arithmetic average", cpn.getNotional(),
          NOTIONAL_1, TOLERANCE_AMOUNT);
    }
  }

  @Test
  public void couponIbor() {
    int nbIborCpn = TENOR_YEAR_1 * 4;
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor", IBOR_LEG_1_DEFINITION.getNumberOfPayments(),
        nbIborCpn);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor",
        IBOR_LEG_1_DEFINITION.getNthPayment(nbIborCpn - 1).getPaymentDate().toLocalDate(), MATURITY_DATE_1);
    ZonedDateTime effectiveDateTime = DateUtils.getUTCDate(EFFECTIVE_DATE_1.getYear(),
        EFFECTIVE_DATE_1.getMonthValue(),
        EFFECTIVE_DATE_1.getDayOfMonth());
    for (int loopcpn = 0; loopcpn < nbIborCpn; loopcpn++) {
      assertTrue("FloatingAnnuityDefinitionBuilderTest: coupon ibor",
          IBOR_LEG_1_DEFINITION.getNthPayment(loopcpn) instanceof CouponIborSpreadDefinition);
      CouponIborSpreadDefinition cpn = (CouponIborSpreadDefinition) IBOR_LEG_1_DEFINITION.getNthPayment(loopcpn);
      ZonedDateTime expectedPaymentDate = ScheduleCalculator.getAdjustedDate(effectiveDateTime,
          PAYMENT_PERIOD.multipliedBy(loopcpn + 1), ADJUSTED_DATE_FEDFUND.getBusinessDayConvention(), NYC);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor", cpn.getPaymentDate().toLocalDate(),
          expectedPaymentDate.toLocalDate());
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor", cpn.getSpread(), SPREAD_1, TOLERANCE_RATE);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor", cpn.getNotional(), NOTIONAL_1, TOLERANCE_AMOUNT);
    }
  }

  @Test
  public void couponIborNotional() {
    int nbIborCpn = TENOR_YEAR_1 * 4;
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNumberOfPayments(), nbIborCpn + 2);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(nbIborCpn).getPaymentDate().toLocalDate(), MATURITY_DATE_1);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(nbIborCpn + 1).getPaymentDate().toLocalDate(), MATURITY_DATE_1);
    ZonedDateTime effectiveDateTime = DateUtils.getUTCDate(EFFECTIVE_DATE_1.getYear(),
        EFFECTIVE_DATE_1.getMonthValue(),
        EFFECTIVE_DATE_1.getDayOfMonth());
    assertTrue("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(0) instanceof CouponFixedDefinition);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(0).getNotional(), -NOTIONAL_1, TOLERANCE_AMOUNT);
    assertTrue("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(nbIborCpn + 1) instanceof CouponFixedDefinition);
    assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
        IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(nbIborCpn + 1).getNotional(), NOTIONAL_1, TOLERANCE_AMOUNT);
    for (int loopcpn = 0; loopcpn < nbIborCpn; loopcpn++) {
      assertTrue("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional",
          IBOR_LEG_NOTIONAL_1_DEFINITION.getNthPayment(loopcpn + 1) instanceof CouponIborSpreadDefinition);
      CouponIborSpreadDefinition cpn = (CouponIborSpreadDefinition) IBOR_LEG_NOTIONAL_1_DEFINITION
          .getNthPayment(loopcpn + 1);
      ZonedDateTime expectedPaymentDate = ScheduleCalculator.getAdjustedDate(effectiveDateTime,
          PAYMENT_PERIOD.multipliedBy(loopcpn + 1), ADJUSTED_DATE_FEDFUND.getBusinessDayConvention(), NYC);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional", cpn.getPaymentDate().toLocalDate(),
          expectedPaymentDate.toLocalDate());
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional", cpn.getSpread(),
          SPREAD_1, TOLERANCE_RATE);
      assertEquals("FloatingAnnuityDefinitionBuilderTest: coupon ibor - notional", cpn.getNotional(),
          NOTIONAL_1, TOLERANCE_AMOUNT);
    }
  }

  /**
   * variable notional test
   */
  @Test
  public void variableNotionalTest() {
    /*
     * Construct annuity by the builder
     */
    FloatingAnnuityDefinitionBuilder builder = new FloatingAnnuityDefinitionBuilder().payer(PAYER_1)
        .startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).index(USDLIBOR3M).
        accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
        dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
        .spread(SPREAD_1).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR)
        .endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR);
    ZonedDateTime[] accrualEndDates = builder.getAccrualEndDates();
    ZonedDateTime startDate = builder.getStartDate();
    ZonedDateTime[] accrualStartDates = ScheduleCalculator.getStartDates(startDate, accrualEndDates);
    int nDates = accrualStartDates.length; // assumes NO initial/final notional exchange
    LocalDate[] dates = new LocalDate[nDates];
    double[] notionals = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      dates[i] = accrualStartDates[i].toLocalDate(); // notional is specified by accrual start date in the builder
      notionals[i] = NOTIONAL_1 * (1.0 - 0.02 * i);
    }
    NotionalProvider provider = new VariableNotionalProvider(dates, notionals);
    AnnuityDefinition<? extends CouponDefinition> iborDefinition = (AnnuityDefinition<? extends CouponDefinition>) builder
        .notional(provider).build();

    /*
     * Construct annuity from individual coupon payments
     */
    ZonedDateTime startDateBare = EFFECTIVE_DATE_1.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault());
    ZonedDateTime[] accrualEndDatesBare = ScheduleCalculator.getAdjustedDateSchedule(startDateBare,
        MATURITY_DATE_1.atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()), PAYMENT_PERIOD, StubType.NONE,
        ADJUSTED_DATE_LIBOR.getBusinessDayConvention(), ADJUSTED_DATE_LIBOR.getCalendar(), null);
    ZonedDateTime[] accrualStartDatesBare = ScheduleCalculator.getStartDates(startDateBare, accrualEndDatesBare);
    int nCoupons = accrualEndDatesBare.length;
    CouponDefinition[] coupons = new CouponIborSpreadDefinition[nCoupons];
    for (int i = 0; i < nCoupons; ++i) {
      ZonedDateTime fixingPeriodStartDate = ADJUSTED_DATE_LIBOR.getBusinessDayConvention().adjustDate(
          OFFSET_ADJ_LIBOR.getCalendar(), accrualStartDatesBare[i]);
      ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate,
          OFFSET_ADJ_LIBOR.getBusinessDayConvention(), OFFSET_ADJ_LIBOR.getCalendar(), OFFSET_ADJ_LIBOR.getOffset());
      ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, PAYMENT_PERIOD,
          ADJUSTED_DATE_LIBOR.getBusinessDayConvention(), OFFSET_ADJ_LIBOR.getCalendar(), null);
      double paymentYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(PAYMENT_PERIOD,
          ADJUSTED_DATE_LIBOR.getCalendar(), USDLIBOR3M.getDayCount(), StubType.NONE, StubType.NONE,
          accrualStartDatesBare[i], accrualEndDatesBare[i], i == 0, i == accrualEndDates.length - 1);
      double fixingPeriodYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(PAYMENT_PERIOD,
          ADJUSTED_DATE_LIBOR.getCalendar(), USDLIBOR3M.getDayCount(), StubType.NONE, StubType.NONE,
          fixingPeriodStartDate, fixingPeriodEndDate, i == 0, i == accrualEndDates.length - 1);
      coupons[i] = new CouponIborSpreadDefinition(USD, accrualEndDatesBare[i], accrualStartDatesBare[i],
          accrualEndDatesBare[i], paymentYearFraction, notionals[i], fixingDate, fixingPeriodStartDate,
          fixingPeriodEndDate, fixingPeriodYearFraction, USDLIBOR3M, SPREAD_1, ADJUSTED_DATE_LIBOR.getCalendar());
    }
    AnnuityDefinition<?> iborDefinitionBare = new AnnuityDefinition<>(coupons, ADJUSTED_DATE_LIBOR.getCalendar());
    assertEquals(iborDefinitionBare, iborDefinition);

    /*
     * Construct annuity by the builder without dates
     */
    provider = new VariableNotionalProvider(notionals);
    AnnuityDefinition<? extends CouponDefinition> iborDefinition1 =
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(PAYER_1)
            .startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).index(USDLIBOR3M)
            .accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
            .resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR)
            .dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
            .spread(SPREAD_1).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR)
            .endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).notional(provider).build();
    assertEquals(iborDefinitionBare, iborDefinition1);
  }

  /**
   * Test consistency with constant notional
   */
  @Test
  public void variableNotionalConsistencyTest() {
    int nDates = 42;
    double[] notionals = new double[nDates];
    Arrays.fill(notionals, NOTIONAL_1);
    NotionalProvider provider = new VariableNotionalProvider(notionals);
    AnnuityDefinition<? extends CouponDefinition> iborDefinition =
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(PAYER_1)
            .startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).index(USDLIBOR3M)
            .accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
            .resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR)
            .dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
            .spread(SPREAD_1).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR)
            .endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).exchangeInitialNotional(true).exchangeFinalNotional(true)
            .notional(provider).build();

    AnnuityDefinition<? extends CouponDefinition> iborDefinitionConst =
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(PAYER_1)
            .startDate(EFFECTIVE_DATE_1).endDate(MATURITY_DATE_1).index(USDLIBOR3M)
            .accrualPeriodFrequency(PAYMENT_PERIOD).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
            .resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR)
            .dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USD)
            .spread(SPREAD_1).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR)
            .endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).exchangeInitialNotional(true).exchangeFinalNotional(true)
            .notional(NOTIONAL_PROV_1).build();

    assertEquals(iborDefinitionConst, iborDefinition);
  }

  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final IborIndex USDLIBOR6M = MASTER_IBOR.getIndex("USDLIBOR6M");
  private static final AdjustedDateParameters ADJUSTED_DATE_USDLIBOR =
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_FIXING_USDLIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final Period P1M = Period.ofMonths(1);
  private static final Period P3M = Period.ofMonths(3);
  private static final Period P6M = Period.ofMonths(6);
  private static final Period P9M = Period.ofMonths(9);
  private static final Period P1Y = Period.ofYears(1);

  /* Stub: Long Start with a unique index, different from the leg one */
  private static final LocalDate START_DATE_STUB1 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB1 = LocalDate.of(2015, 9, 10);
  private static final CouponStub CPN_IBOR_STUB1 = new CouponStub(StubType.LONG_START, USDLIBOR6M, USDLIBOR6M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB1 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR3M).
          accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).startStub(CPN_IBOR_STUB1).build();
  /* Stub: Short Start with two indexes, different from the leg one */
  private static final LocalDate START_DATE_STUB2 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB2 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB2 = new CouponStub(StubType.SHORT_START, USDLIBOR1M, USDLIBOR3M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB2 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB2).endDate(END_DATE_STUB2).index(USDLIBOR6M).
          accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).startStub(CPN_IBOR_STUB2).build();
  /* Stub: Short end with two indexes, different from the leg one */
  private static final LocalDate START_DATE_STUB3 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB3 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB3 = new CouponStub(StubType.SHORT_END, USDLIBOR1M, USDLIBOR3M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB3 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB3).endDate(END_DATE_STUB3).index(USDLIBOR6M)
          .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).endStub(CPN_IBOR_STUB3).build();
  /* Stub: Short start with one index, different from the leg one */
  private static final LocalDate START_DATE_STUB4 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB4 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB4 = new CouponStub(StubType.SHORT_START, USDLIBOR3M, USDLIBOR3M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB4 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB4).endDate(END_DATE_STUB4).index(USDLIBOR6M)
          .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).startStub(CPN_IBOR_STUB4).build();
  /* Stub: Long start with two indexes, different from the leg one */
  private static final LocalDate START_DATE_STUB5 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB5 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB5 = new CouponStub(StubType.LONG_START, USDLIBOR3M, USDLIBOR6M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB5 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB5).endDate(END_DATE_STUB5).index(USDLIBOR3M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).startStub(CPN_IBOR_STUB5).build();
  /* Stub: Short end with one index, different from the leg one */
  private static final LocalDate START_DATE_STUB6 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB6 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB6 = new CouponStub(StubType.SHORT_END, USDLIBOR3M, USDLIBOR3M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB6 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB6).endDate(END_DATE_STUB6).index(USDLIBOR6M)
          .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).endStub(CPN_IBOR_STUB6).build();
  /* Stub: Long end with one index, different from the leg one */
  private static final LocalDate START_DATE_STUB7 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB7 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB7 = new CouponStub(StubType.LONG_END, USDLIBOR6M, USDLIBOR6M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB7 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB7).endDate(END_DATE_STUB7).index(USDLIBOR3M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).endStub(CPN_IBOR_STUB7).build();
  /* Stub: Long end with two indexes, different from the leg one */
  private static final LocalDate START_DATE_STUB8 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB8 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB8 = new CouponStub(StubType.LONG_END, USDLIBOR3M, USDLIBOR6M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB8 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB8).endDate(END_DATE_STUB8).index(USDLIBOR3M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).spread(0.0).endStub(CPN_IBOR_STUB8).build();

  /**
   * start/end stub with one/two ibor indexes, StubType.BOTH not supported
   */
  @Test
  public void stubCouponIborIndexTest() {
    testStub("FloatingAnnuityDefinitionBuilder - Stub - long start one index", LEG_IBOR_STUB1, new IborIndex[] {
        USDLIBOR6M }, true, 5, START_DATE_STUB1, END_DATE_STUB1.minus(P1Y));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - short start two indexes", LEG_IBOR_STUB2, new IborIndex[] {
        USDLIBOR1M, USDLIBOR3M }, true, 3, START_DATE_STUB2, END_DATE_STUB2.minus(P1Y));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - short end two indexes", LEG_IBOR_STUB3, new IborIndex[] {
        USDLIBOR1M, USDLIBOR3M }, false, 3, START_DATE_STUB3.plus(P1Y), END_DATE_STUB3);
    testStub("FloatingAnnuityDefinitionBuilder - Stub - Short start one index", LEG_IBOR_STUB4,
        new IborIndex[] {USDLIBOR3M }, true, 3, START_DATE_STUB4, END_DATE_STUB4.minus(P1Y));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - long start two indexes", LEG_IBOR_STUB5,
        new IborIndex[] {USDLIBOR3M, USDLIBOR6M }, true, 4, START_DATE_STUB5, END_DATE_STUB5.minus(P9M));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - Short end one index", LEG_IBOR_STUB6,
        new IborIndex[] {USDLIBOR3M }, false, 3, START_DATE_STUB6.plus(P1Y), END_DATE_STUB6);
    testStub("FloatingAnnuityDefinitionBuilder - Stub - Long end one index", LEG_IBOR_STUB7,
        new IborIndex[] {USDLIBOR6M }, false, 4, START_DATE_STUB7.plus(P9M), END_DATE_STUB7);
    testStub("FloatingAnnuityDefinitionBuilder - Stub - Long end two indexes", LEG_IBOR_STUB8,
        new IborIndex[] {USDLIBOR3M, USDLIBOR6M }, false, 4, START_DATE_STUB8.plus(P9M), END_DATE_STUB8);
  }

  /* Stub: Long start with one index, different from the leg one, compounding */
  private static final LocalDate START_DATE_STUB9 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB9 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB9 = new CouponStub(StubType.LONG_START, USDLIBOR1M, USDLIBOR1M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB9 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB9).endDate(END_DATE_STUB9).index(USDLIBOR1M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).startStub(CPN_IBOR_STUB9).compoundingMethod(CompoundingMethod.NONE).build();
  /* Stub: short start with one index, different from the leg one, compounding */
  private static final LocalDate START_DATE_STUB10 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB10 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB10 = new CouponStub(StubType.SHORT_START, USDLIBOR1M, USDLIBOR1M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB10 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB10).endDate(END_DATE_STUB10).index(USDLIBOR1M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).startStub(CPN_IBOR_STUB10).compoundingMethod(CompoundingMethod.NONE).build();
  /* Stub: Long end with one index, different from the leg one, compounding */
  private static final LocalDate START_DATE_STUB11 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB11 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB11 = new CouponStub(StubType.LONG_END, USDLIBOR1M, USDLIBOR1M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB11 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB11).endDate(END_DATE_STUB11).index(USDLIBOR1M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).endStub(CPN_IBOR_STUB11).compoundingMethod(CompoundingMethod.NONE).build();
  /* Stub: short end with one index, different from the leg one, compounding */
  private static final LocalDate START_DATE_STUB12 = LocalDate.of(2014, 3, 12);
  private static final LocalDate END_DATE_STUB12 = LocalDate.of(2015, 5, 12);
  private static final CouponStub CPN_IBOR_STUB12 = new CouponStub(StubType.SHORT_END, USDLIBOR1M, USDLIBOR1M);
  private static final AnnuityDefinition<? extends CouponDefinition> LEG_IBOR_STUB12 =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
          .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB12).endDate(END_DATE_STUB12).index(USDLIBOR1M)
          .accrualPeriodFrequency(P3M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
          .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
          currency(USD).endStub(CPN_IBOR_STUB12).compoundingMethod(CompoundingMethod.NONE).build();

  /**
   * start/end stub with one/two ibor compounding indexes, StubType.BOTH not supported. 
   * Note that compounding accrual dates in each coupon are computed short start. 
   */
  @Test
  public void stubCouponIborIndexCompoundingTest() {
    testStub("FloatingAnnuityDefinitionBuilder - Stub - long start one index, compounding", LEG_IBOR_STUB9,
        new IborIndex[] {USDLIBOR1M }, true, 4, START_DATE_STUB9, END_DATE_STUB9.minus(P9M));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - short start one index, compounding", LEG_IBOR_STUB10,
        new IborIndex[] {USDLIBOR1M }, true, 5, START_DATE_STUB10, END_DATE_STUB10.minus(P1Y));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - long end one index, compounding", LEG_IBOR_STUB11,
        new IborIndex[] {USDLIBOR1M }, false, 4, START_DATE_STUB11.plus(P9M), END_DATE_STUB11);
    testStub("FloatingAnnuityDefinitionBuilder - Stub - short end one index, compounding", LEG_IBOR_STUB12,
        new IborIndex[] {USDLIBOR1M }, false, 5, START_DATE_STUB12.plus(P1Y), END_DATE_STUB12);
  }

  private void testStub(String message, AnnuityDefinition<? extends CouponDefinition> targetAnnuity, IborIndex[] index,
      boolean startStub, int expectedLength, LocalDate expectedStartDate, LocalDate expectedEndDate) {
    assertEquals(message, expectedLength, targetAnnuity.getNumberOfPayments());
    int refPos = startStub ? 0 : expectedLength - 1;
    if (index.length == 2) {
      assertTrue(message, targetAnnuity.getNthPayment(refPos) instanceof CouponIborAverageIndexDefinition);
      CouponIborAverageIndexDefinition cpnL = (CouponIborAverageIndexDefinition) targetAnnuity.getNthPayment(refPos);
      assertEquals(message, index[0], cpnL.getIndex1());
      assertEquals(message, index[1], cpnL.getIndex2());
    } else {
      assertTrue(message, targetAnnuity.getNthPayment(refPos).getClass() == targetAnnuity.getNthPayment(1).getClass());
      if (targetAnnuity.getNthPayment(refPos) instanceof CouponIborSpreadDefinition) {
        CouponIborSpreadDefinition cpnL = (CouponIborSpreadDefinition) targetAnnuity.getNthPayment(refPos);
        assertEquals(message, index[0], cpnL.getIndex());
      } else if (targetAnnuity.getNthPayment(refPos) instanceof CouponIborDefinition) {
        CouponIborDefinition cpnL = (CouponIborDefinition) targetAnnuity.getNthPayment(refPos);
        assertEquals(message, index[0], cpnL.getIndex());
      } else if (targetAnnuity.getNthPayment(refPos) instanceof CouponIborCompoundingDefinition) {
        CouponIborCompoundingDefinition cpnL = (CouponIborCompoundingDefinition) targetAnnuity.getNthPayment(refPos);
        assertEquals(message, index[0], cpnL.getIndex());
      } else {
        throw new IllegalArgumentException("check coupon type"); // further branches should be added if other types are tested
      }
    }
    assertEquals(message, targetAnnuity.getNthPayment(refPos).getAccrualStartDate().toLocalDate(), expectedStartDate);
    assertEquals(message, targetAnnuity.getNthPayment(refPos).getAccrualEndDate().toLocalDate(), expectedEndDate);
  }

  /**
   * Use the same index as other coupon payments
   */
  @Test
  public void sameIndexTest() {
    CouponStub shortStart = new CouponStub(StubType.SHORT_START, USDLIBOR6M, USDLIBOR6M);
    CouponStub longStart = new CouponStub(StubType.LONG_START, USDLIBOR6M, USDLIBOR6M);
    CouponStub shortEnd = new CouponStub(StubType.SHORT_END, USDLIBOR6M, USDLIBOR6M);
    CouponStub longEnd = new CouponStub(StubType.LONG_END, USDLIBOR6M, USDLIBOR6M);
    CouponStub shortStartNoIndex = new CouponStub(StubType.SHORT_START);
    CouponStub longStartNoIndex = new CouponStub(StubType.LONG_START);
    CouponStub shortEndNoIndex = new CouponStub(StubType.SHORT_END);
    CouponStub longEndNoIndex = new CouponStub(StubType.LONG_END);
    AnnuityDefinition<?> legShortStart = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(0.0).startStub(shortStart).build();
    AnnuityDefinition<?> legShortStartNoIndex = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(0.0).startStub(shortStartNoIndex).build();
    assertTrue(legShortStart.equals(legShortStartNoIndex));
    AnnuityDefinition<?> legLongStart = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(0.0).startStub(longStart).build();
    AnnuityDefinition<?> legLongStartNoIndex = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(0.0).startStub(longStartNoIndex).build();
    assertTrue(legLongStart.equals(legLongStartNoIndex));
    AnnuityDefinition<?> legShortEnd = new FloatingAnnuityDefinitionBuilder().payer(true).endStub(shortEnd)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).currency(USD).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).build();
    AnnuityDefinition<?> legShortEndNoIndex = new FloatingAnnuityDefinitionBuilder().endStub(shortEndNoIndex)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).payer(true).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).currency(USD).build();
    assertTrue(legShortEnd.equals(legShortEndNoIndex));
    AnnuityDefinition<?> legLongEnd = new FloatingAnnuityDefinitionBuilder().payer(true).endStub(longEnd)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M).
        accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).currency(USD).
        resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).build();
    AnnuityDefinition<?> legLongEndNoIndex = new FloatingAnnuityDefinitionBuilder().payer(true).endStub(longEndNoIndex)
        .notional(NOTIONAL_PROV_1).startDate(START_DATE_STUB1).endDate(END_DATE_STUB1).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).currency(USD)
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
        .dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).build();
    assertTrue(legLongEnd.equals(legLongEndNoIndex));
  }

  /**
   * Testing StubType.NONE does nothing
   * Note that default is StubType.SHORT_START
   */
  @Test
  public void noneStubTypeTest() {
    LocalDate startDate = LocalDate.of(2014, 5, 12);
    CouponStub noneStub = new CouponStub(StubType.NONE);
    CouponStub noneStubWithDate = new CouponStub(StubType.NONE, LocalDate.of(2014, 5, 18));
    double spread = 0.015;
    FloatingAnnuityDefinitionBuilder builder1 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread).startStub(noneStub);
    FloatingAnnuityDefinitionBuilder builder2 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread).endStub(noneStub);
    FloatingAnnuityDefinitionBuilder builder3 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread);
    FloatingAnnuityDefinitionBuilder builder4 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread).endStub(noneStubWithDate);
    FloatingAnnuityDefinitionBuilder builder5 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread).startStub(noneStub).endStub(noneStub);
    FloatingAnnuityDefinitionBuilder builder6 = new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(END_DATE_STUB3).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).spread(spread).endStub(noneStub).startStub(noneStub);
    AnnuityDefinition<?> dfn1 = builder1.build();
    AnnuityDefinition<?> dfn2 = builder2.build();
    AnnuityDefinition<?> dfn3 = builder3.build();
    AnnuityDefinition<?> dfn4 = builder4.build();
    AnnuityDefinition<?> dfn5 = builder5.build();
    AnnuityDefinition<?> dfn6 = builder6.build();
    assertTrue(dfn1.equals(dfn2));
    assertTrue(dfn1.equals(dfn3));
    assertTrue(dfn1.equals(dfn4));
    assertTrue(dfn1.equals(dfn5));
    assertTrue(dfn1.equals(dfn6));
  }

  /**
   * Test StubType.BOTH, where the underlying coupon is CouponIborDefinition
   */
  @Test
  public void bothStubTest() {
    LocalDate startDate = LocalDate.of(2014, 5, 12);
    LocalDate endDate = LocalDate.of(2015, 10, 12);
    /*
     * In order to construct expected annuity definition, both the stub flags should be 
     * set StubType.BOTH with two effective dates synthesized with accrualPeriodFrequency.
     */
    CouponStub stubStart = new CouponStub(StubType.BOTH, LocalDate.of(2014, 7, 12), USDLIBOR1M, USDLIBOR3M);
    CouponStub stubEnd = new CouponStub(StubType.BOTH, LocalDate.of(2015, 7, 12), USDLIBOR3M, USDLIBOR3M);
    AnnuityDefinition<? extends CouponDefinition> dfn1 =
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(true)
        .notional(NOTIONAL_PROV_1).startDate(startDate).endDate(endDate).index(USDLIBOR6M)
        .accrualPeriodFrequency(P6M).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).
        dayCount(USDLIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).
        currency(USD).startStub(stubStart).endStub(stubEnd).build();
    BusinessDayConvention bdc = ADJUSTED_DATE_USDLIBOR.getBusinessDayConvention();
    testStub("FloatingAnnuityDefinitionBuilder - Stub - both", dfn1, new IborIndex[] {USDLIBOR1M, USDLIBOR3M }, true,
        4, startDate, bdc.adjustDate(ADJUSTED_DATE_USDLIBOR.getCalendar(), stubStart.getEffectiveDate()));
    testStub("FloatingAnnuityDefinitionBuilder - Stub - both", dfn1, new IborIndex[] {USDLIBOR3M }, false,
        4, bdc.adjustDate(ADJUSTED_DATE_USDLIBOR.getCalendar(), stubEnd.getEffectiveDate()),
        bdc.adjustDate(ADJUSTED_DATE_USDLIBOR.getCalendar(), endDate));
  }

}

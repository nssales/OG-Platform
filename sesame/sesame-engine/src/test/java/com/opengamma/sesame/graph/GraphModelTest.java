/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.graph;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.config.EngineUtils;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.function.ConfigurationErrorFunction;
import com.opengamma.sesame.function.FunctionMetadata;
import com.opengamma.sesame.function.InvokableFunction;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class GraphModelTest {

  /** Tests that an invalid portfolio function build a placeholder with an error message. */
  @Test
  public void invalidPortfolioFunction() {
    FunctionMetadata metadata = EngineUtils.createMetadata(PortfolioFn.class, "foo");
    FunctionModel functionModel = FunctionModel.forFunction(metadata);
    Map<Class<?>, FunctionModel> fnMap = ImmutableMap.<Class<?>, FunctionModel>of(FXForwardSecurity.class, functionModel);
    String columnName = "col name";
    Map<String, Map<Class<?>, FunctionModel>> colMap = ImmutableMap.of(columnName, fnMap);
    GraphModel graphModel = new GraphModel(colMap, Collections.<String, FunctionModel>emptyMap());
    Graph graph = graphModel.build(ComponentMap.EMPTY, new FunctionBuilder());

    Map<Class<?>, InvokableFunction> functionsForColumn = graph.getFunctionsForColumn(columnName);
    InvokableFunction invokableFunction = functionsForColumn.get(FXForwardSecurity.class);
    assertNotNull(invokableFunction);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class));
    //SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), new ResettableLiveMarketDataSource(MarketData.live(), mock(LDClient.class)));
    Result<?> result = (Result<?>) invokableFunction.invoke(env, null, FunctionArguments.EMPTY);
    assertFalse(result.isSuccess());
    assertEquals(FailureStatus.ERROR, result.getStatus());
    assertEquals(ConfigurationErrorFunction.CONFIG_ERROR, result.getFailureMessage());
  }

  /** Tests that an invalid non-portfolio function build a placeholder with an error message. */
  @Test
  public void invalidNonPortfolioFunction() {
    FunctionMetadata metadata = EngineUtils.createMetadata(NonPortfolioFn.class, "foo");
    FunctionModel functionModel = FunctionModel.forFunction(metadata);
    Map<String, Map<Class<?>, FunctionModel>> portfolioFunctionModels = Collections.emptyMap();
    String outputName = "output name";
    GraphModel graphModel = new GraphModel(portfolioFunctionModels, ImmutableMap.of(outputName, functionModel));
    Graph graph = graphModel.build(ComponentMap.EMPTY, new FunctionBuilder());

    InvokableFunction invokableFunction = graph.getNonPortfolioFunction(outputName);
    assertNotNull(invokableFunction);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class));
    //SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), new ResettableLiveMarketDataSource(MarketData.live(), mock(LDClient.class)));
    Result<?> result = (Result<?>) invokableFunction.invoke(env, null, FunctionArguments.EMPTY);
    assertFalse(result.isSuccess());
    assertEquals(FailureStatus.ERROR, result.getStatus());
    assertEquals(ConfigurationErrorFunction.CONFIG_ERROR, result.getFailureMessage());
  }

  public static interface PortfolioFn {
    @Output("Foo")
    String foo(FXForwardSecurity security);
  }

  public static interface NonPortfolioFn {
    @Output("Foo")
    String foo();
  }
}

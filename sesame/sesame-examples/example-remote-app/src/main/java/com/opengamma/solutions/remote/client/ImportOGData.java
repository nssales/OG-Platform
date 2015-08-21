/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of
 * companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.remote.client;


import org.apache.commons.cli.CommandLine;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.regression.DatabaseRestore;
import com.opengamma.integration.regression.FudgeXMLFormat;
import com.opengamma.integration.regression.RegressionIO;
import com.opengamma.integration.regression.ZipFileRegressionIO;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.scripts.Scriptable;
import java.io.File;

/**
 * The entry point for running an example remote view.
 */
@Scriptable
public class ImportOGData extends AbstractTool<ToolContext> {

    private static ToolContext s_context;

    //-------------------------------------------------------------------------
    /**
     * Main method to run the tool.
     *
     * @param args the standard tool arguments, not null
     */
    public static void main(final String[] args) { // CSIGNORE
        new ImportOGData().invokeAndTerminate(args);
    }

    //-------------------------------------------------------------------------
    @Override
    protected void doRun() throws Exception {

        s_context = getToolContext();
        CommandLine commandLine = getCommandLine();
        ImmutableList.Builder<Object> inputs = ImmutableList.<Object>builder();

        /* Create a RemoteFunctionServer to executes view requests RESTfully.*/
        String url = commandLine.getOptionValue(CONFIG_RESOURCE_OPTION) + "/jax";

        File zip = new File("D:\\OG-master\\opengamma\\platform\\sesame\\sesame-examples\\example-server\\resources\\import-data.zip");
        RegressionIO io = ZipFileRegressionIO.createReader(zip, new FudgeXMLFormat());

        try (RemoteServer server = RemoteServer.create(url)) {
            DatabaseRestore databaseRestore = new DatabaseRestore(io, 
                    server.getSecurityMaster(), 
                    server.getPositionMaster(), 
                    server.getPortfolioMaster(), 
                    server.getConfigMaster(),
                    server.getHistoricalTimeSeriesMaster(), 
                    server.getHolidayMaster(), 
                    server.getExchangeMaster(), 
                    server.getMarketDataSnapshotMaster(), 
                    server.getLegalEntityMaster(),
                    server.getConventionMaster());
            databaseRestore.restoreDatabase();
        }
        System.exit(0);
    }
}

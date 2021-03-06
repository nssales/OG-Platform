/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.legacyfixedrecoverycdssecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            config.title = 'Legacy Fixed Recovery CDS';
            config.type = 'LegacyFixedRecoveryCDSSecurity';
            return new og.blotter.forms.cds(config);
        };
    }
});
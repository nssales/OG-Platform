/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.CurvePlot',
    dependencies: [],
    obj: function () {
        return function (config) {
            var curve = this, input = config.data, $selector = $(config.selector), $reset, $init_msg, $plot, $flot,
                data = {}, alive = og.common.id('gadget_curve_plot'), loading_template,
                tooltip_class = og.common.id('gadget_curve_tooltip'), css = {},
                color_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'],
                flot_options = {
                    zoom: {interactive: true}, selection: {mode: null},
                    grid: {
                        borderWidth: 0, color: '#999', minBorderMargin: 0, labelMargin: 4, hoverable: true,
                        markings: [
                            {xaxis: {from: -Number.MAX_VALUE, to: Number.MAX_VALUE}, yaxis: {from: 0, to: 0}},
                            {xaxis: {from: 0, to: 0}, yaxis: {from: -Number.MAX_VALUE, to: Number.MAX_VALUE}}
                        ],
                        markingsLineWidth: 1, markingsColor: '#aaa'
                    },
                    lines: {lineWidth: 1, fill: false, fillColor: '#f8fbfd'},
                    pan: {interactive: true, cursor: "move", frameRate: 30},
                    series: {shadowSize: 1, points: {radius: 2, lineWidth: 1, fill: true, fillColor: "#ffffff"}},
                    xaxis: {mode: 'years', tickLength: 'full', labelHeight: 14},
                    yaxis: {position: 'left', tickLength: 'full', labelWidth: 30}
                };
            tooltip = Handlebars.compile('<div class="og-curve-tooltip"><table><tr><td>X:&nbsp;&nbsp;</td><td>{{{x}}}</td></tr><tr><td>Y:&nbsp;&nbsp;</td><td>{{{y}}}</td></tr></table></div>');
            /**
            * Format data object and update flot option object
            * @param data {Array} array of objects each containing a curve array and an optional nodes array
            * @param options {Object} flot options object. colors and ranges need updating based on the data
            */
            var formatter = function (data, options) {
                var obj = {options: options, data: []};
                if ($.isArray(data)) data.forEach(function (val, i) {
                    if (val.curve) {
                        obj.data.push({data: val.curve, color: '#42669a'/*,
                            dashes: {show: true, dashLength: [5,5], lineWidth: 1}*/});
                    }
                    if (val.nodes) {
                        obj.data.push({data: val.nodes, color: '#42669a',
                            points: {show: true, radius: 3, lineWidth: 2},
                            dashes: {show: true, lineWidth: 1, dashLength: [2,2]}});
                    }
                    if (val.knots) {
                        obj.data.push({data: val.knots, color: '#42669a',
                            points: {show: true, lineWidth: 3}, lines: {show: true}});
                    }
                });
                return obj;
            };
            curve.resize = function () {
                var width = $selector.width(), height = $selector.height() - $options.height();
                $plot.css({height: height, width: width});
                $flot = $.plot($plot, data.data, data.options);
                $flot.zoom({amount:0.90});
            };
            curve.alive = function () {
                return !!$('.' + alive).length;
            };
            var load = function () {
                $selector.html(loading_template({text: 'loading...'}));
                og.api.text({module: 'og.views.gadgets.curve.plot_tash'}).pipe(function (template) {
                    var previous_hover = null, sel = '.' + tooltip_class;
                    $selector.html((Handlebars.compile(template))({alive: alive}));
                    data = formatter(input, flot_options);
                    $reset = $selector.find('span.og-curve-refresh').hide();
                    $plot = $selector.find('div.og-curve-plot');
                    $options = $selector.find('div.og-curve-options');
                    curve.resize();
                    /**
                     * Implement tooltip
                     */
                    $plot.bind('plothover', function (event, pos, item) {
                        var top, left;
                        if (item && previous_hover != item.dataIndex) {
                            $(sel).remove(), previous_hover = item.dataIndex;
                            top = item.pageY + 5;
                            left = item.pageX + 5;
                            $(tooltip({x: item.datapoint[0], y: item.datapoint[1]})).addClass(tooltip_class)
                                .css({top: top, left: left}).appendTo('body').show();
                        }
                        if (!item) $(sel).remove(), previous_hover = null;
                    });
                    $plot.bind('plotpan plotzoom', function () {
                        $reset.show();
                        curve.update(input);
                    });
                    $reset.on('click', function () {
                        curve.resize();
                    });
                });
            };
            /**
            * Updates data only
            */
            curve.update = function (input) {
                $flot.setData(formatter(input, flot_options).data);
                $flot.draw();
            };
            if (loading_template)
                load();
            else {
                og.api.text({module: 'og.views.gadgets.loading_tash'}).pipe(function (template) {
                    loading_template = Handlebars.compile(template);
                    load();
                });
             }
        };
    }
});
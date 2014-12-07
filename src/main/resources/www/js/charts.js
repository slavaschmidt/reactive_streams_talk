
var w           = 5,
    h           = 80,
    barCount    = 100,
    fontSize    = 16,
    duration    = 250,
    now         = new Date().getMilliseconds(),
    data        = d3.range(barCount-1).map(
        function() { return [
            {name:"RS-Alice", cpu: 0, time: now, swap: 0},
            {name:"RS-Borice", cpu: 0, time: now, swap: 0}
            ];
        })
    ;

var x = d3.scale.linear().domain([0, 1]).range([0, w]);

var y = d3.scale.linear().domain([0, 1000]).rangeRound([0, h]);

var source = new EventSource("/heartbeat");

createName("RS-Alice", "swap");
createName("RS-Alice", "cpu");
createName("RS-Borice", "swap");
createName("RS-Borice", "cpu");
createState("RS-Alice");
createState("RS-Borice");

source.onopen = function () {

};

source.onmessage = function (message) {
    var n = $.parseJSON(message.data);
    data.shift();
    data.push(n.data);
    redrawAll(n.data);
};

function redrawAll(dataSet) {
    var i = 0, count = data.length;
    if (data[count-1].length === 0) return;
    for (;i<dataSet.length;i++) {
        redrawName(i, "cpu");
        redrawName(i, "swap");
        redrawState(i);
    }
}

function redrawState(idx) {
    var chart = d3.select(".state_" + data[data.length-1][idx].name);

    var rect = chart.selectAll("rect").data(data, function(d) {
        if (d.length === 0) { return Date.now(); } else { return d[idx].time; }
    });

    rect.enter().insert("rect")
        .attr("class", function(d) { if (d[idx].state) return d[idx].state; else return "NONE"; })
        .attr("x", function(d, i) { return x(i+1); })
        .attr("y", 20)
        .attr("width", w)
        .attr("height", h-20)
        .transition()
        .duration(duration)
        .attr("x", function(d, i) { return x(i); });

    rect.transition()
        .duration(duration)
        .attr("x", function(d, i) { return x(i); });

    rect.exit().remove()
    ;
}


function redrawName(idx, name) {

    var chart = d3.select("." + name + "_" + data[data.length-1][idx].name);

    var rect = chart.selectAll("rect." + name).data(data, function(d) {
        if (d.length === 0 || ! d[idx][name]) { return Date.now(); } else { return d[idx].time; }
    });

    var fromData = function(data, idx) {
        if (data === 0) return y(0); else return y(data[idx][name]);
    };

    rect.enter().insert("rect").attr("class", name)
        .attr("x", function(d, i) { return x(i+1); })
        .attr("width", w)
        .attr("y", function(d) { return h; })
        .attr("height", function(d) { return 0; })
        .transition()
        .duration(duration)
        .attr("x", function(d, i) { return x(i); })
    ;

    rect.transition()
        .duration(duration)
        .attr("x", function(d, i) { return x(i); })
        .attr("y", function(d) { return h - fromData(d, idx); })
        .attr("height", function(d) { return fromData(d, idx); })
    ;

    rect.exit().remove()
    ;

    var top = chart.selectAll("line.top_"+name).data(data, function(d) {
        if (d.length === 0) { return Date.now(); } else { return d[idx].time; }
    });

    top.enter().insert("line").attr("class", "top_"+name)
        .attr("x1", function(d, i) { return x(i+1); })
        .attr("x2", function(d, i) { return x(i+1)+w; })
        .attr("y1", function(d) { return 0; })
        .attr("y2", function(d) { return 0; })
        .transition()
        .duration(duration)
        .attr("x1", function(d, i) { return x(i); })
        .attr("x2", function(d, i) { return x(i)+w; })
    ;

    top.transition()
        .duration(duration)
        .attr("x1", function(d, i) { return x(i); })
        .attr("x2", function(d, i) { return x(i)+w; })
        .attr("y1", function(d) { return h - fromData(d, idx) - 1; })
        .attr("y2", function(d) { return h - fromData(d, idx) - 1; })
    ;

    top.exit().remove()
    ;
}

function createName(i, name) {
    var chart   = d3.select("div#" + i).select("." + name).append("svg")
        .attr("class", name + "_" + i)
        .attr("width", w * barCount)
        .attr("height", h);

     chart.selectAll("rect")
        .data(data)
        .enter().append("rect").attr("class", name)
        .attr("x", function(d, i) { return x(i); })
        .attr("y", function(d) { return h - y(0); })
        .attr("width", w)
        .attr("height", function(d) { return y(0); });

    chart.append("line").attr("class", name)
        .attr("x1", 0)
        .attr("x2", w * barCount)
        .attr("y1", h)
        .attr("y2", h)
        .style("stroke", "#000");

    return chart;
}
function createState(i) {
    var state   = d3.select("div#" + i).select(".state").append("svg")
        .attr("class", "state_"+i)
        .attr("width", w * barCount-1)
        .attr("height", h);

    return state;
}
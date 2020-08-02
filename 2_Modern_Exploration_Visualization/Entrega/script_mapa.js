var map = L.map('div_mapa').setView([40.48, -3.70], 11);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

var svg = d3.select(map.getPanes().overlayPane).append('svg');

var g = svg.append("g").attr("class", "leaflet-zoom-hide");

function cargaMapaSecciones() {
    d3.json('tabla_airbnb.json')
        .then((seccionesCollection) => {

            var transform = d3.geoTransform({
                point: projectPoint
            });

            var pathProjection = d3.geoPath().projection(transform);

            var scaleColor = d3.scaleOrdinal(d3.schemePaired);
            var features = seccionesCollection.features;

            d3_features =
                g.selectAll("path")
                .data(features)
                .enter()
                .append("path")
                .attr("d", pathProjection)
                .attr("opacity", 0.6)
                .attr("fill", (d, i) => scaleColor(i))
                .on("click", handleClick);

            map.on("zoom", update)
                .on("click", handleClick);

            update();

            function update() {
                bounds = pathProjection.bounds(seccionesCollection);

                var topLeft = bounds[0],
                    bottomRight = bounds[1];

                svg.attr("width", bottomRight[0] - topLeft[0])
                    .attr("height", bottomRight[1] - topLeft[1])
                    .style("left", topLeft[0] + "px")
                    .style("top", topLeft[1] + "px");

                g.attr("transform", "translate(" + -topLeft[0] + "," +
                    -topLeft[1] + ")");

                d3_features.attr("d", pathProjection);
            }

            function projectPoint(x, y) {
                var point = map.latLngToLayerPoint(new L.LatLng(y, x));
                this.stream.point(point.x - 3, point.y + 2);
            }

        });
}

function handleClick(d, i) {
    console.log("Click!!");
}

function cargaTablaPropiedadesHabitaciones(barrio, colorBarrio) {
    d3.json('tabla_airbnb.json')
        .then((propiedadesCollection) => {
            //console.log(propiedadesCollection);
            //console.log(propiedadesCollection.features);
            //console.log(propiedadesCollection.features[0].properties);
            var datos = propiedadesCollection.features[barrio].properties.avgbedrooms;
            var barrioName = propiedadesCollection.features[barrio].properties.name;
            console.log(barrioName);

            var height = 170;
            var width = 300;
            var marginbottom = 130;
            var margintop = 200;

            var svgTabla = d3.select('div')
                .append('svg')
                .attr('width', width + 300)
                .attr('height', height + marginbottom + margintop)
                .append("g")
                .attr("transform", "translate(0," + marginbottom + ")");

            var xscale = d3.scaleBand()
                .domain(datos.map(function(d) {
                    return d.bedrooms;
                }))
                .range([0, width])
                .padding(0.1);

            var yscale = d3.scaleLinear()
                .domain([0, d3.max(datos, function(d) {
                    return d.total;
                })])
                .range([height, 0]);

            var xaxis = d3.axisBottom(xscale);

            var rect = svgTabla
                .selectAll('rect')
                .data(datos)
                .enter()
                .append('rect')
                .attr("fill", colorBarrio);

            rect.attr('class', (d) => {
                if (d.total > 10) {
                    return 'rectwarning';
                }
            });

            rect.attr("x", function(d) {
                    return xscale(d.bedrooms);
                })
                .attr('y', d => {
                    return yscale(d.total)
                })
                .attr("width", xscale.bandwidth())
                .attr("height", function(d) {
                    return height - yscale(d.total)
                });

            var text = svgTabla.selectAll('text')
                .data(datos)
                .enter()
                .append('text')
                .text(d => d.total)
                .attr("x", function(d) {
                    return xscale(d.bedrooms) + xscale.bandwidth() / 2;
                })
                .attr('y', d => {
                    return yscale(d.total)
                })

            svgTabla.append("g")
                .attr("transform", "translate(0," + height + ")")
                .call(xaxis);

            svgTabla.append("text")
                .attr("y", 0)
                .attr("dx", 300)
                .attr("dy", "0em")
                .style("font-weight", "bold")
                .style("font-size", "27")
                .text(barrioName);

            svgTabla.append("text")
                .attr("transform", "rotate(-90)")
                .attr("y", 0)
                .attr("dx", -35)
                .attr("dy", "1em")
                .style("text-anchor", "middle")
                .style("font-size", "11")
                .text("Total / Habitaciones");
        });
}

function cargaLeyenda() {
    var width = 600;
    var nblegend = 10;
    var widthRect = (width / nblegend) - 2;
    var heightRect = 10;

    var scaleLegend = d3.scaleLinear()
        .domain([0, nblegend])
        .range([0, width]);

    svg.append("g")
        .selectAll("rect")
        .data(d3.schemePaired)
        .enter()
        .append("rect")
        .attr("width", widthRect)
        .attr("height", heightRect + 8)
        .attr("x", (d, i) => scaleLegend(i))
        .attr("fill", (d) => d);

    svg.append("g")
        .selectAll("text")
        .data(d3.schemePaired)
        .enter()
        .append("text")
        .attr("x", (d, i) => scaleLegend(i) + 7)
        .attr("y", (heightRect * 2.5) - 12)
        .text((d) => d)
        .attr("font-size", 12);
}

window.onload = function leyendaFunction() {

    cargaMapaSecciones();

    cargaLeyenda();

    cargaTablaPropiedadesHabitaciones(13, "#A6CEE3");
}
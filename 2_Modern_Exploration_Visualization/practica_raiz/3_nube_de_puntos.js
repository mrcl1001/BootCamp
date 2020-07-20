//Dataset con precios de pisos/area
var data = [{
            area: 800, //area en square feets
            precio: 350 //K USD
        },
        {
            area: 900,
            precio: 450
        },
        {
            area: 850,
            precio: 450
        },
        {
            area: 1250,
            precio: 700
        },
        {
            area: 1100,
            precio: 650
        },
        {
            area: 1350,
            precio: 850
        },
        {
            area: 1200,
            precio: 900
        },
        {
            area: 1410,
            precio: 1250
        },
        {
            area: 1250,
            precio: 1100
        },
        {
            area: 1400,
            precio: 1150
        },
        {
            area: 1500,
            precio: 1050
        },
        {
            area: 1330,
            precio: 1120
        },
        {
            area: 1580,
            precio: 1220
        },
        {
            area: 1620,
            precio: 1400
        },
        {
            area: 1250,
            precio: 1450
        },
        {
            area: 1350,
            precio: 1600
        },
        {
            area: 1650,
            precio: 1300
        },
        {
            area: 1700,
            precio: 1620
        },
        {
            area: 1750,
            precio: 1700
        },
        {
            area: 1830,
            precio: 1800
        },
        {
            area: 1900,
            precio: 2000
        },
        {
            area: 2050,
            precio: 2200
        },
        {
            area: 2150,
            precio: 1960
        },
        {
            area: 2250,
            precio: 1990
        }
    ]
    //Formateando datos
var f = d3.format(".1f"); //nos devuelve un string
data.forEach((d) => {
    d.area = +f(d.area * 0.09290304);
    d.precio = +f(d.precio * 0.87);
})
console.log(data)

var width = 800;
var height = 600;

var svg = d3.select("div")
    .append("svg")
    .attr("width", width + 100)
    .attr("height", height + 100)
    .append("g")
    .attr("transform", "translate(50,0)");


var tooltip = d3.select("div")
    .append("div")
    .attr("class", "tooltip")
    .style("position", "absolute")
    .style()

/*console.log(d3.min(data, (d) => {
return d.area
}))*/

var xmax = d3.max(data, (d) => {
    return d.area
})
var xmin = d3.min(data, (d) => {
    return d.area
})

var ymax = d3.max(data, (d) => {
    return d.precio
})
var ymin = d3.min(data, (d) => {
    return d.precio
})
var scalex = d3.scaleLinear()
    .domain([xmin, xmax])
    .range([0, width]);

var scaley = d3.scaleLinear()
    .domain([ymin, ymax])
    .range([height / 2, 0]);

var Xaxis = d3.axisBottom(scalex);
var Yaxis = d3.axisLeft(scaley);

var circle = svg.selectAll("circle")
    .data(data)
    .enter()
    .append("circle")
    .attr("cx", (d) => {
        return scalex(d.area)
    })
    .attr("cy", (d) => {
        return scaley(d.precio)
    })
    .attr("r", 5)
    .attr("fill", "green");

var texto = svg.selectAll("text")
    .data(data)
    .enter()
    .append("text")
    .attr("id", function(d, i) {
        return "text_" + i
    })
    .attr("x", (d) => {
        return scalex(d.area)
    })
    .attr("y", (d) => {
        return scaley(d.precio) - 15
    })
    .text(function(d) {
        return "Area: " + d.area + " Precio: " + d.precio
    })
    .attr("visibility", "hidden");

circle.on("mouseover", function(d, i) {
        d3.select(this)
            .attr("visibility", "visible")
            .attr("fill", "orange");
        d3.select("#text_" + i).attr("visibility", "visible");
    })
    .on("mouseout", function(d, i) {
        d3.select(this)
            .attr("fill", "green");
        d3.select("#text_" + i).attr("visibility", "hidden");
    })

svg.append("g").attr("transform", "translate(0," + height / 2 + ")").call(Xaxis);
svg.append("g").call(Yaxis);





//console.log(scalex(74.3))

//Para el tooltip
/*.style("left", (d3.event.pageX + 20) + "px")
.style("top", (d3.event.pageY - 30) + "px")
.text(`Area: ${d.area}m2, Price: ${d.precio}k Eur`)*/
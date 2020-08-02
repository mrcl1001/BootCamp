var input = [{
        platform: 'Blackberry',
        color: 'green',
        value: 5
    },
    {
        platform: 'Windows',
        color: 'gray',
        value: 10
    },
    {
        platform: 'Ios',
        color: 'orange',
        value: 15
    },
    {
        platform: 'Android',
        color: 'blue',
        value: 20
    }
];

var height = 500;
var width = 500;
var marginbottom = 100;
var margintop = 50;

var svg = d3.select('div')
    .append('svg')
    .attr('width', width)
    .attr('height', height + marginbottom + margintop)
    .append("g")
    .attr("transform", "translate(0," + margintop + ")");

//Creacion de escalas
var xscale = d3.scaleBand()
    .domain(input.map(function(d) {
        return d.platform;
    }))
    .range([0, width])
    .padding(0.1);

var yscale = d3.scaleLinear()
    .domain([0, d3.max(input, function(d) {
        return d.value;
    })])
    .range([height, 0]);

//Creación de eje X
var xaxis = d3.axisBottom(xscale);

//Creacion de los rectangulos
var rect = svg
    .selectAll('rect')
    .data(input)
    .enter()
    .append('rect')
    .attr("fill", "#93CAAE");

rect.attr('class', (d) => {
    if (d.value > 10) {
        return 'rectwarning';
    }
});

rect
    .attr("x", function(d) {
        return xscale(d.platform);
    })
    .attr('y', d => {
        return yscale(d.value)
    })
    .attr("width", xscale.bandwidth())
    .attr("height", function(d) {
        return height - yscale(d.value); //Altura real de cada rectangulo.
    });


//Añadimos el texto correspondiente a cada rectangulo.
var text = svg.selectAll('text')
    .data(input)
    .enter()
    .append('text')
    .text(d => d.value)
    .attr("x", function(d) {
        return xscale(d.platform) + xscale.bandwidth() / 2;
    })
    .attr('y', d => {
        return yscale(d.value)
    })
    //.style("opacity", 1);

//Por si queremos aplicar el estilo creado al texto
/*text.attr('class', (d) => {
        if (d.value > 10) {
            return 'rectwarning';
        }
        return 'text';
    })*/


//Añadimos el eje X
svg.append("g")
    .attr("transform", "translate(0," + height + ")")
    .call(xaxis);
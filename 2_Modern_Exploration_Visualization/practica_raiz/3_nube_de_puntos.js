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
data.forEach((d) => {})
console.log(data)



//Para el tooltip
/*.style("left", (d3.event.pageX + 20) + "px")
.style("top", (d3.event.pageY - 30) + "px")
.text(`Area: ${d.area}m2, Price: ${d.precio}k Eur`)*/
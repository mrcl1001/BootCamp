#install.packages(c('repr','IRdisplay', 'evaluate', 'crayon', 'pbdZMQ', 'devtools', 'uuid', 'digest','ggplot2','reshape2','entropy','quantmod'), repos='https://cran.rstudio.com/')
#install.packages(c('caret','curl','dbsca','dplyr','dslabs','e1071','egg','euralet','GGally','ggdedro','ggpubr','glmet','jpeg','MASS','microbechmark','plotrix','plyr','pracma','reshape2','ROCR','tm','zoo','glmnetUtils'),repos='https://cran.rstudio.com/')

```python
library(ggplot2)
library(dplyr)
library(tidyverse)
library(vcov)
options(repr.plot.height=4,repr.plot.width=6)
```

Cargar los datos en un dataframe llamado: airbnb

```python
airbnb<-read.csv('data//airbnb.csv',sep = ',')
```

Mostrar las primeras 6 filas del dataframe

```python
head(airbnb, 6)
#primerasfilas <- airbnb[1:6,]
#print(primerasfilas)
```

Renombrar las columnas de la siguiente forma:

| Nombre original | Nuevo nombre |
| - | - |
| Zipcode | CodigoPostal |
| Neighbourhood.Cleansed | Barrio |
| Property.Type	| TipoPropiedad |
| Room.Type | TipoAlquiler |
| Accommodates | MaxOcupantes |
| Bathrooms	| NumBanyos |
| Bedrooms	| NumDormitorios |
| Beds	|  NumCamas |
| Bed.Type	| TipoCama |
| Amenities | Comodidades |
| Square.Feet | PiesCuadrados |
| Price	| Precio |
| Review.Scores.Rating | Puntuacion |


```python
newnames<-c("CodigoPostal","Barrio","TipoPropiedad","TipoAlquiler","MaxOcupantes","NumBanyos",
"NumDormitorios","NumCamas","TipoCama","Comodidades","PiesCuadrados","Precio","Puntuacion")

colnames(airbnb) <- newnames
```

Crea una nueva columna llamada MetrosCuadrados a partir de la columna PiesCuadrados.
Ayuda: 1 pie cuadrado son 0,092903 metros cuadrdados


```python
airbnb <- data.frame(airbnb, MetrosCuadrados = (airbnb$PiesCuadrados*0.092903))
```

Miremos el código postal. Es una variable con entradas erroneas.
Hay valores como '', '-'  y '28' que deberían ser considerados como NA.
Así mismo también debería ser NA todos los que no empiencen por 28, ya que estamos con códigos postales de Madrid

El código postal 28002, 28004 y 28051 tienen entradas repetidas. 
Por ejemplo las entradas 28002\n20882 deberían ir dnetro de 28002

El codigo 2804 debería ser 28004, 2805 deberia ser 28005 y 2815 juncto con 2815 debería ser 28015

Limpia los datos de la columna Codigo Postal


```python
airbnb$CodigoPostal[airbnb$CodigoPostal==''] <- NA
airbnb$CodigoPostal[airbnb$CodigoPostal=='-'] <- NA
airbnb$CodigoPostal[airbnb$CodigoPostal=='28'] <- NA
airbnb$CodigoPostal[substring(airbnb$CodigoPostal,1,2)!='28'] <- NA

airbnb$CodigoPostal[airbnb$CodigoPostal=='2804'] <- '28004'
airbnb$CodigoPostal[airbnb$CodigoPostal=='2805'] <- '28005'
airbnb$CodigoPostal[airbnb$CodigoPostal=='2815'] <- '28015'

airbnb$CodigoPostal <- ifelse(nchar(airbnb$CodigoPostal) > 5, substring(airbnb$CodigoPostal, 0,5), airbnb$CodigoPostal)

airbnb <- filter(airbnb, !is.na(airbnb$CodigoPostal))
```

Una vez limpios los datos ¿Cuales son los códigos postales que tenemos? 


```python
ccpp <- distinct(airbnb, CodigoPostal)
ccpp <- arrange(ccpp, CodigoPostal)
print(ccpp)
```

¿Cuales son los 5 códigos postales con más entradas? 


```python
df_ccpp <- table(airbnb$CodigoPostal)
ccpp2 <- data.frame(ccpp, CCPP = df_ccpp)
ccpp2 <- ccpp2$CCPP.Freq
ccpp <- data.frame(ccpp, Total = ccpp2)
ccpp_max <- arrange(ccpp, desc(Total))
head(ccpp_max, 5)
```

¿Y con menos?

```python
ccpp_min <- arrange(ccpp, Total)
print(ccpp_min[1:5,])
```

¿Cuantas entradas tienen?

```python
#summary(airbnb$CodigoPostal)
print(paste0("Tiene ", sum(df_ccpp), " entradas"))
```

¿Cuales son los barrios que hay en el código postal 28012?


```python
barrios <- filter(airbnb, airbnb$CodigoPostal == "28012")
barrios <- distinct(barrios, Barrio)
barrios
```

¿Cuantas entradas hay en cada uno de esos barrios para el codigo postal 28012?


```python
entradas <- filter(airbnb, airbnb$CodigoPostal == "28012")
table(entradas$Barrio)
```

¿Cuantos barrios hay en todo el dataset airbnb? 


```python
barrios_totales <- distinct(airbnb, Barrio)
nrow(barrios_totales)
```

¿Cuales son?

```python
barrios_totales
```


¿Cuales son los 5 barrios que tienen mayor número entradas?


```python
barrio2 <- distinct(airbnb, Barrio)
barrio2 <- arrange(barrio2, Barrio)

barrios_entradas <- table(airbnb$Barrio)
barrios_entradas2 <- data.frame(barrio2, Entradas = barrios_entradas)
barrios_entradas2 <- barrios_entradas2$Entradas.Freq

barrio2 <- data.frame(barrio2, Total = barrios_entradas2)
barrio2_max <- arrange(barrio2, desc(Total))
head(barrio2_max, 5)
```

¿Cuantos Tipos de Alquiler diferentes hay? 


```python
tipo_alquiler <- distinct(airbnb, TipoAlquiler)
nrow(tipo_alquiler)
```

¿Cuales son? 


```python
tipo_alquiler
```

¿Cuantas entradas en el dataframe hay por cada tipo?


```python
tipo_alquiler_entradas <- table(airbnb$TipoAlquiler)
tipo_alquiler_entradas
```

Muestra el diagrama de cajas del precio para cada uno de los diferentes Tipos de Alquiler


```python
ggplot(data=airbnb,aes(x=TipoAlquiler, y=Precio,color=TipoAlquiler))+geom_boxplot()+
 scale_color_discrete(name="Alquileres")+ylab("Precio")+xlab("Tipo de Alquiler")
```

Cual es el precio medio de alquiler de cada uno, la diferencia que hay ¿es estadísticamente significativa?
¿Con que test lo comprobarías?


```python
Entire_home_mean <- airbnb$Precio[airbnb$TipoAlquiler=='Entire home/apt']
Private_room_mean <- airbnb$Precio[airbnb$TipoAlquiler=='Private room']
Shared_room_mean <- airbnb$Precio[airbnb$TipoAlquiler=='Shared room'] 

print(paste("Media de Entire home/apt: ", mean(Entire_home_mean)))
print(paste("Media de Private room: ", mean(Private_room_mean)))
print(paste("Media de Shared room: ", mean(Shared_room_mean)))

print("Como Entire_home_mean y Private_room_mean me salen NA por temas de R tengo que sacarlos de otra forma")

myOwnMean<-function(coll){
    m<-0
    for (x in coll){
        m <- m + x
    }
    m/length(coll)
}

Entire_home_mean_2 <- airbnb$Precio[airbnb$TipoAlquiler=='Entire home/apt']
Entire_home_mean <- mean(myOwnMean(Entire_home_mean_2[1:1000]))

Private_room_mean_2 <- airbnb$Precio[airbnb$TipoAlquiler=='Private room']
Private_room_mean <- mean(myOwnMean(Private_room_mean_2[1:800]))

print(paste("Media de Entire home/apt: ", Entire_home_mean))
print(paste("Media de Private room: ", Private_room_mean))
print(paste("Media de Shared room: ", mean(Shared_room_mean)))

print("La diferencia que hay es considerable con el primer Tipo de alquiler (Entire home/apt), asi que como me parece estadísticamente significativa voy a aplicar el test de Shapiro para comprobarlo, ya que se considera uno de los tests más fiables")

shapiro.test(rnorm(100, mean=Entire_home_mean, sd=4))
shapiro.test(rnorm(100, mean=Private_room_mean, sd=4))
shapiro.test(rnorm(100, mean=Shared_room_mean, sd=4))

print("Como el p-value de 'Shared_room_mean' sale muy pequeño se rechaza la hipótesis nula, osea que los datos no están en una distribucióbn normal")
```

Filtra el dataframe cuyos tipo de alquiler sea  'Entire home/apt' y guardalo en un dataframe llamado 
*airbnb_entire*.
Estas serán las entradas que tienen un alquiler del piso completo.


```python
airbnb_entire <- filter(airbnb, airbnb$TipoAlquiler=='Entire home/apt')
print("Como en este punto hay una columna que es siempre la misma 'TipoAlquiler', la elimino para que ocupe menos el dataframe")
airbnb_entire <- select(airbnb_entire, -TipoAlquiler)
head(airbnb_entire)
```

¿Cuales son los 5 barrios que tienen un mayor número de apartamentos enteros en alquiler?
Nota: Mirar solo en airbnb_entire


```python
print("He considerado como 'apartamentos enteros' los que son del TipoPropiedad = 'Apartment'")
airbnb_entire_Apartments <- filter(airbnb_entire, airbnb_entire$TipoPropiedad=='Apartment')
airbnb_entire_Apartments <- select(airbnb_entire_Apartments, -TipoPropiedad)

apartment_1 <- distinct(airbnb_entire_Apartments, Barrio)
apartment_1 <- arrange(apartment_1, Barrio)

sort(desc(apartment_1$Total))
apartment_2 <- table(airbnb_entire_Apartments$Barrio)
apartment_3 <- data.frame(apartment_1, Entradas = apartment_2)
apartment_3 <- apartment_3$Entradas.Freq

apartment_1 <- data.frame(apartment_1, Total = apartment_3)
apartment_solucion <- arrange(apartment_1, desc(Total))
head(apartment_solucion ,5)
```

¿Cuales son los 5 barrios que tienen un mayor precio medio de alquiler para apartamentos enteros?
¿Cual es su precio medio?
Ayuda: Usa la función aggregate `aggregate(.~colname,df,mean,na.rm=TRUE)`


```python
barrios_precio_medio <- aggregate(Precio ~ Barrio, FUN = mean, data = airbnb_entire_Apartments)
head(barrios_precio_medio[order(desc(barrios_precio_medio$Precio)),],5)
```

¿Cuantos apartamentos hay en cada uno de esos barrios?
Mostrar una dataframe con el nombre del barrio, el precio y el número de entradas.
Ayuda: Podeis crear un nuevo dataframe con las columnas "Barrio" y "Freq" que contenga el número de entradas en cada barrio y hacer un merge con el dataframe del punto anterior.


```python
print("Los apartamentos que hay por Barrio son: ")
apartamentos_por_barrio <- airbnb_entire_Apartments %>% count(Barrio)
apartamentos_por_barrio <- merge(apartamentos_por_barrio,barrios_precio_medio,by="Barrio")
newColumnNames <- c("Barrio", "Frecuencia", "Precio_Medio")
colnames(apartamentos_por_barrio) <- newColumnNames
apartamentos_por_barrio <- apartamentos_por_barrio[order(desc(apartamentos_por_barrio$Frecuencia)),]
```

Partiendo del dataframe anterior, muestra los 5 barrios con mayor precio, pero que tengan más de 100 entradas de alquiler.


```python
barrios_mas_precio <- apartamentos_por_barrio[order(desc(apartamentos_por_barrio$Precio_Medio)),]
barrios_mas_precio <- filter(barrios_mas_precio, barrios_mas_precio$Frecuencia > 100)
barrios_mas_precio_5 <- head(barrios_mas_precio, 5)
barrios_mas_precio_5
```

Dibuja el diagrama de densidad de distribución de los diferentes precios. Serían 5 gráficas, una por cada barrio.


```python
#DiagramasDistribucion <- function(coll){
#    for (bar in coll){
#        barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio==bar)
#        grafica <- ggplot(data=barrio_df_2graf, aes(Precio)) +  geom_density(adjust = 3) + ylab(bar)
#        print(grafica)
#    }
#}
#DiagramasDistribucion(barrios_mas_precio_5$Barrio)

barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Recoletos')
ggplot(data=barrio_df_2graf, aes(Precio)) + geom_density(adjust = 3) + ylab('Recoletos')

barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Goya')
ggplot(data=barrio_df_2graf, aes(Precio)) + geom_density(adjust = 3) + ylab('Goya')

barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Sol')
ggplot(data=barrio_df_2graf, aes(Precio)) + geom_density(adjust = 3) + ylab('Sol')

barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Justicia')
ggplot(data=barrio_df_2graf, aes(Precio)) + geom_density(adjust = 3) + ylab('Justicia')

barrio_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Trafalgar')
ggplot(data=barrio_df_2graf, aes(Precio)) + geom_density(adjust = 3) + ylab('Trafalgar')
```

Calcula el tamaño medio, en metros cuadrados, para los 5 barrios anteriores y muestralo en el mismo dataframe junto con el precio y número de entradas


```python
barrios_tamanio_medio <- aggregate(MetrosCuadrados ~ Barrio, FUN = mean, data = airbnb_entire_Apartments, na.rm = TRUE)
barrios_tamanio_medio_merge <- merge(barrios_mas_precio_5, barrios_tamanio_medio, by="Barrio")
barrios_tamanio_medio_merge
```

Dibuja el diagrama de densidad de distribución de los diferentes tamaños de apartamentos. Serían 5 gráficas, una por cada barrio.


```python
tamanios_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Recoletos', !is.na(airbnb_entire_Apartments$MetrosCuadrados))
ggplot(data=tamanios_df_2graf, aes(MetrosCuadrados)) + geom_density(adjust = 3) + ylab('Recoletos')

tamanios_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Goya', !is.na(airbnb_entire_Apartments$MetrosCuadrados))
ggplot(data=tamanios_df_2graf, aes(MetrosCuadrados)) + geom_density(adjust = 3) + ylab('Goya')

tamanios_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Sol', !is.na(airbnb_entire_Apartments$MetrosCuadrados))
ggplot(data=tamanios_df_2graf, aes(MetrosCuadrados)) + geom_density(adjust = 3) + ylab('Sol')

tamanios_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Justicia', !is.na(airbnb_entire_Apartments$MetrosCuadrados))
ggplot(data=tamanios_df_2graf, aes(MetrosCuadrados)) + geom_density(adjust = 3) + ylab('Justicia')

tamanios_df_2graf <- filter(airbnb_entire_Apartments, airbnb_entire_Apartments$Barrio=='Trafalgar', !is.na(airbnb_entire_Apartments$MetrosCuadrados))
ggplot(data=tamanios_df_2graf, aes(MetrosCuadrados)) + geom_density(adjust = 3) + ylab('Trafalgar')
```

Esta claro que las medias de cada uno de estos 5 barrios parecen ser diferentes, pero ¿son estadísticamente diferentes?
¿Que test habría que usar para comprobarlo?


```python
print("Yo aplicaría el Test de Tukey, ya que lo que busca las medias que son significativamente diferentes")
TukeyHSD(aov(Precio_Medio ~ Barrio, data=barrios_tamanio_medio_merge))
```

Para únicamente los pisos de alquiler en el barrio de Sol:
``barrio_sol<-subset(airbnb_entire,Barrio=="Sol")``
Calcular un modelo lineal que combine alguna de estas variables:
* NumBanyos
* NumDormitorios
* MaxOcupantes
* MetrosCuadrados


```python
barrio_sol<-subset(airbnb_entire,Barrio=="Sol")

modelo_lineal <- lm(data=barrio_sol, formula=NumDormitorios~MaxOcupantes)
summary(modelo_lineal)
```

Primero calculamos la correlación para ver como se relacionan estas variables entre sí.


```python
cor(barrio_sol[,c("NumBanyos","NumDormitorios","MaxOcupantes", "MetrosCuadrados")],use = "complete.obs")
```

Se observa que la correlación entre el número de dormitorios y los metros cuadrados es sorprendentemente baja.
¿Son de fiar esos números?

Mediante un histograma o curvas de densidad podemos descartar números que no tienen sentido en el dataframe barrio_sol,
para tener una matriz de correlación que tenga mayor sentido.


```python
print("La lógica dicta que no parecen muy lógicos esos datos, pero también pude ser por unos datos erroneos o un mal cálculo de estos")

ggplot(barrio_sol,aes(x=MetrosCuadrados, y=NumDormitorios, color=MetrosCuadrados)) + geom_col(size=3)

barrio_sol_filter <- filter(barrio_sol, barrio_sol$MetrosCuadrados > 10, !is.na(barrio_sol$MetrosCuadrados), barrio_sol$NumDormitorios > 0)
ggplot(barrio_sol_filter,aes(x=MetrosCuadrados, y=NumDormitorios, color=MetrosCuadrados)) + geom_col(size=3)

correlacion <- cor(barrio_sol_filter[,c("NumBanyos","NumDormitorios","MaxOcupantes", "MetrosCuadrados")],use = "complete.obs")
correlacion
```

Una vez que hayamos filtrado los datos correspondientes calcular el valor o la combinación de valores que mejor nos permite obtener el precio de un inmueble.


```python
combinacion_valores <- cov(barrio_sol_filter[,c("NumBanyos","NumDormitorios","MaxOcupantes", "MetrosCuadrados")],use = "complete.obs")
combinacion_valores
```

¿Que variable es más fiable para conocer el precio de un inmueble, el número de habitaciones o los metros cuadrados?


```python
print("Pienso que es mas fiable el número de habitaciones ya que en la gráfica sale que cuantas mas habitaciones mas caro es el inmueble")
ggplot(barrio_sol_filter,aes(y=MetrosCuadrados, x=NumDormitorios, color=Precio)) + geom_col(size=3)
```

Responde con su correspondiente margen de error del 95%, ¿cuantos euros incrementa el precio del alquiler por cada metro cuadrado extra del piso?


```python
modelo1 <- lm(data = barrio_sol_filter, formula= MetrosCuadrados ~ Precio)
summary(modelo1)
modelo1

print(paste("En torno a los ", modelo1$coefficients[2]))

intervalo_confianza <- confint(object = modelo, parm = "Precio", level = 0.95)
intervalo_confianza
prediccion_confianza <- predict(modelo, barrio_sol_filter,interval = 'confidence')
head(prediccion_confianza)
```

Responde con su correspondiente margen de error del 95%, ¿cuantos euros incrementa el precio del alquiler por cada habitación?


```python
modelo2 <- lm(data = barrio_sol_filter, formula = NumDormitorios ~ Precio)
summary(modelo2)
modelo2

print(paste("En torno a los ", modelo2$coefficients[2]))
```

¿Cual es la probabilidad de encontrar, en el barrio de Sol, un apartamento en alquiler con 3 dormitorios?


```python
barrio_sol_3Dormitorios <- filter(barrio_sol_filter, barrio_sol_filter$NumDormitorios == 3)
probabilidad_sol_3Dormitorios = length(barrio_sol_3Dormitorios)/length(length(barrio_sol_filter))
print(paste("la probabilidad es del: ", probabilidad_sol_3Dormitorios, "%"))
```

¿Cual es el margen de error de esa probabilidad?

```python
margen_lo <- qnorm(probabilidad_sol_3Dormitorios/100, lower.tail = T)
margen_up <- qnorm(probabilidad_sol_3Dormitorios/100, lower.tail = F)
print(paste("El margen está entre ", margen_lo, " y ", margen_up))
```

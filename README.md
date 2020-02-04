# JWT sample
Progetto Spring Boot con autenticazione gestita da Spring Security tramite JWT. Questo template può essere utilizzato come base da cui partire per sviluppare webservice sicuri che necessitano di un meccanismo di autenticazione degli utenti.


## database
Il webservice utilizza un database H2 (con persistenza) incapsulato direttamente nell'applicazione; nel caso in cui si voglia utilizzare una base di dati differente sarà sufficiente modificare i parametri corrispondenti nel file *src/main/resources/application.properties* (ovviamente mantenendo lo stesso db schema):

```
spring.datasource.url=jdbc:h2:file:./data/demo
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true.
```
Lo schema del database è il seguente:

**tabella USERS:**
```
USERNAME VARCHAR(50) PRIMARY KEY
ENABLED BOOLEAN NOT NULL
PASSWORD VARCHAR(100) NOT NULL
```

**tabella AUTHORITIES:**
```
ID BIGINT PRIMARY KEY
NAME VARCHAR(50) NOT NULL
```

**tabella USERS_AUTHORITIES:**
```
USER_USERNAME VARCHAR(50) PRIMARY KEY
AUTHORITY_ID BIGINT PRIMARY KEY
```




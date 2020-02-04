# JWT sample
Progetto Spring Boot con autenticazione gestita da Spring Security tramite JWT. Questo template può essere utilizzato come base da cui partire per sviluppare webservice sicuri che necessitano di un meccanismo di autenticazione degli utenti.


## Database
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

## Login
Il processo di autenticazione di un utente inizia con la chiamata a **/public/login**. Il metodo handler di questo endpoint si aspetta di parsare un oggetto di tipo *JwtAuthenticationRequest* a partire dal body della richiesta, che quindi dovrà consistere in un oggetto JSON fatto in questo modo:

```
{
  "username" : *username utente*,
  "password" : *password utente*
}
```
Se i dati sono corretti  il webservice restituisce una response costruita in questo modo: l'header **X-Auth** viene valorizzato con un nuovo jwt e il body viene riempito con lo username dell'utente loggato e le sue authorities. Per modificare i parametri con cui vengono generati i jwt è sufficiente cambiare queste voci nel file *src/main/resources/application.properties*:
```
jwt.header
jwt.secret
jwt.expiration
```
A questo punto il client può chiamare anche gli endpoint del webservice che non sono pubblici, semplicemente settando l'header X-Auth della request con il jwt appena ricevuto. 

## Controllo autenticativo
La validazione dei token delle richieste è interamente gestita da Spring Security.


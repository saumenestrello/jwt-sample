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
Se i dati sono corretti  il webservice restituisce una response costruita in questo modo: l'header **X-Auth** viene valorizzato con un nuovo jwt e il body viene riempito con lo username dell'utente loggato e le sue authorities. A questo punto il client può chiamare anche gli endpoint del webservice che non sono pubblici, semplicemente settando l'header X-Auth della request con il jwt appena ricevuto. 

## Controllo autenticativo
La validazione dei token delle richieste è interamente gestita da Spring Security. Le configurazioni necessarie sono contenute nella classe *WebSecurityConfig*, in cui:
- viene impostata la funzione di hash che utilizzerà l'authenticationManager per verificare le credenziali al momento del login
```
  @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```
- vengono settati i parametri necessari ad evitare CORS issues
```
@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.setAllowedMethods(Arrays.asList("POST, PUT, GET, OPTIONS, DELETE"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
```
- vengono stabilite le regole di sicurezza per le richieste HTTP
```
@Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                //unauthorizedHandler è di tipo JwtAuthenticationEntryPoint, ha il metodo commence invocato ad autenticazione fallita   
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and() 
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // non abbiamo bisogno di una sessione
                .cors().and()
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll() //dichiarazione endpoint pubblici (non autenticati)
                .antMatchers("/public/**").permitAll()
                .anyRequest().authenticated();

        // viene settato il filtro custom JWT che verifica la validità dei token delle richieste
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        httpSecurity.headers().cacheControl();
    }
```

La classe *JwtAuthenticationTokenFilter* filtra le richieste HTTP e verifica la validità dei token attraverso il metodo *doFilterInternal*:
```
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authToken = request.getHeader(this.tokenHeader);

        UserDetails userDetails = null;

        if(authToken != null){
            userDetails = jwtTokenUtil.getUserDetails(authToken);
        }

        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Ricostruisco l userdetails con i dati contenuti nel token
            // controllo integrita' token
            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
 ```
 
 ## Jwt 
 Per modificare i parametri con cui vengono generati i jwt è sufficiente cambiare queste voci nel file *src/main/resources/application.properties*:
```
jwt.header
jwt.secret
jwt.expiration
```
I jwt vengono gestiti tramite la classe *JwtTokenUtil*, che contiene i metodi per generare i token, validarli ed estrarre le claim. 
Le claim previste dall'applicazione sono:
```
static final String CLAIM_KEY_USERNAME = "sub";
static final String CLAIM_KEY_AUDIENCE = "audience";
static final String CLAIM_KEY_CREATED = "iat";
static final String CLAIM_KEY_AUTHORITIES = "roles";
static final String CLAIM_KEY_IS_ENABLED = "isEnabled";
```

##Importare in Eclipse
L'intero progetto può essere importato velocemente in Eclipse. E' sufficiente cliccare su **File>Import...** e scegliere *Existing Maven Project*. Per eseguire l'applicazione è necessario cliccare col tasto destro sulla root del progetto, scegliere **Run As>Java Application** e poi scegliere la classe JwtApplication (se appare un dialog intitolato *Select Java Application*).

# springai-mcp-archetype

> Arquitectura de referencia para construir **servidores MCP (Model Context Protocol)** con **Java 25**, **Spring Boot 4** y **Spring AI**.

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot)
![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-6DB33F?logo=spring)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

Este repositorio proporciona un **arquetipo Maven multimódulo** diseñado como punto de partida para el desarrollo de aplicaciones basadas en Spring AI que expongan *Tools*, *Resources* y *Prompts* mediante el protocolo MCP (Model Context Protocol).

El arquetipo define una arquitectura estandarizada, modular y basada en capas, proporcionando una estructura común que facilita la estandarización, reutilización y evolución de los proyectos. Además de los componentes funcionales propios de cada aplicación, incorpora una serie de capacidades transversales reutilizables que, de otro modo, deberían implementarse y configurarse de forma independiente en cada proyecto.

Entre estas capacidades se incluyen:

- **Autenticación y seguridad**
- **Gestión de caché > mcp-session-id**
- **Integración con servicios HTTP**
- **Configuración de CORS**
- **Observabilidad y monitorización**

Úsalo con **"Use this template"** en GitHub, o clónalo y renombra los packages a tu propio namespace.

---

## Índice

- [Descripción del Arquetipo](#descripción-del-Arquetipo)
- [Arquitectura](#arquitectura)
- [Módulos_Java](#módulos_java)
- [Inicio rápido](#inicio-rápido)
- [Usar los módulos en tu proyecto](#usar-los-módulos-en-tu-proyecto)
- [Configuración](#configuración)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Build y desarrollo](#build-y-desarrollo)
- [Contribuir](#contribuir)
- [Licencia](#licencia)

---
## Descripción del Arquetipo
> [NOTE]
> Este proyecto proporciona un **Maven Archetype multimódulo** para generar proyectos de servidores **MCP basados en Spring AI** de forma estandarizada.
>
> El arquetipo define una **arquitectura modular y componible**, incorporando capacidades transversales como **seguridad, gestión de sesiones, transporte/streamable, caché, integración y observabilidad**.
>
> Estas capacidades se proporcionan mediante **módulos Maven reutilizables**, gestionados bajo un **Maven BOM**, permitiendo adaptar cada servidor MCP a sus necesidades funcionales sin duplicar soluciones técnicas comunes.

## 🧩 Arquitectura del Framework MCP


### 🌱 Spring AI MCP Ecosystem

El arquetipo generado se construye sobre el stack tecnológico Java/Spring Boot:

![Spring AI](/img/stack_spring_ai_v1.png)

### 🔧 Requisitos

| 🏷️ **Categoría** | 🔧 **Requisito**      | 📌 **Versión / Detalle** |
|---|-----------------------|--------------------------|
| ☕ **JDK** | Java Development Kit  | **JDK 25**               |
| 🌱 **Framework** | Spring Boot           | **4.0.6**                |
| 🔌 **MCP** | Spring AI MCP Server `spring-ai-mcp-server-webmvc` | **2.0.0-M6**             |
| 📦 **Build** | Maven                 | **Maven 3.9+**           |

### 🧱 Stack del Framework por Capas

La arquitectura se apoya en **JDK 25** como runtime base y en **Spring Boot 4.x / Spring Framework 4.x**, aprovechando el starter oficial `spring-ai-mcp-server`, que permite declarar capacidades MCP de forma nativa mediante anotaciones (`@Tool`, `@Resource`, `@Prompt`), sin necesidad de implementar el protocolo JSON-RPC a bajo nivel.

| **Capa** | **Tecnología / Componente**                      | **Responsabilidad** |
|---|--------------------------------------------------|---|
| ☕ **Runtime** | **JDK 25** · Virtual Threads                     | Runtime y modelo de concurrencia para operaciones ligeras y sesiones **Streamable HTTP**. |
| 🌱 **Framework** | **Spring Framework 7.x** · Spring Boot **4.0.6** | IoC/DI, autoconfiguración, configuración y ciclo de vida de la aplicación. |
| 🤖 **AI / MCP** | **Spring AI 2.0.0-M6**                           | Integración MCP y abstracciones para definir y registrar **Tools, Prompts y Resources**. |
| 🔌 **Protocol** | **Model Context Protocol (MCP)**                 | Estándar de interacción entre el cliente MCP y el servidor MCP. |
| 🌐 **Transport** | **Streamable HTTP**                              | Canal de comunicación HTTP entre clientes y servidores MCP. |
| 🔐 **Security** | **JWT** · IdP / JWKS                             | Autenticación y validación de identidad de las peticiones. |
| 🔄 **Session** | **Transport Session Management**                 | Gestión del contexto técnico asociado a las sesiones de transporte. |
| ⚡ **Caching** | **Caffeine**                                     | Caché local para reducir accesos repetitivos a sistemas externos. |
| 📊 **Observability** | **JSON Structured Logging**                      | Estandarización de eventos **TECHNICAL**, **FUNCTIONAL** y **SECURITY**. |
| 🏗️ **Scaffolding** | **Maven Archetype**                              | Generación de una estructura de proyecto MCP estandarizada y reutilizable. |
---

## 📦 Módulos Java

El siguiente diagrama representa los **módulos internos que componen el framework**. Estos módulos se organizan en **cuatro grupos funcionales**, cada uno de ellos con una responsabilidad técnica claramente delimitada y cohesiva.

![Spring AI](./img/mcp_modulos.png)

Los módulos forman parte del desarrollo propio del **stack** y constituyen los componentes que conforman su arquitectura base. El framework incorpora, además, dependencias de terceros como **Spring AI, Spring Boot y Caffeine**, que son gestionadas de forma transitiva mediante el **BOM** del framework.

Esta organización modular permite mantener una **clara separación de responsabilidades**, reducir el acoplamiento entre componentes y facilitar la evolución y reutilización de las capacidades proporcionadas por el framework.


Todos los módulos comparten el group id `io.github.ricardodlm.springai.mcp` y la versión del proyecto.

| Módulo | Artefacto | Propósito                                                                                                 |
|---|---|-----------------------------------------------------------------------------------------------------------|
| Padre / BOM | `mcp-architecture-framework` | Gestión de dependencias, configuración de plugins, propiedades compartidas.                               |
| Common · JWT | `common-jwt` | Decodificación de JWT, verificación de firma y extracción de claims.                                      |
| Adapter · Auth IdP | `adapter-auth-idp` | Integración OIDC con Red Hat SSO / Keycloak (JWKS, introspección de tokens).                              |
| Adapter · Cache Caffeine | `adapter-cache-caffeine` | Abstracción de caché basada en Caffeine con TTL y tamaño configurables.                                   |
| Adapter · REST Client | `adapter-rest-client` | Cliente HTTP preconfigurado para llamar a APIs externas desde tools MCP.                                  |
| Transport · Auth | `transport-auth-service` | Filtro servlet que exige autenticación en los endpoints MCP.                                              |
| Transport · Session | `transport-session-service` | Gestión de la sesión - cache de autenticación y del `Mcp-Session-Id` (creación, validación y expiración). |
| Transport · Streamable | `transport-streamable-service` | Transporte MCP *Streamable HTTP* (endpoint único con respuestas en streaming).                            |
| Transport · CORS | `transport-cors-service` | Configuración CORS para hosts MCP basados en navegador.                                                   |
| Observability | `observability` | logs estructurados.                                                                                       |

---

## 🚀 Developer Quickstart & Scaffolding

### Compilar

```bash
git clone https://github.com/ricardo07dlm/springai-mcp-archetype.git
cd springai-mcp-archetype/mcp-architecture-framework
mvn clean install
```

### Ejecutar el servidor de ejemplo <!-- TODO: ajustar si no hay módulo sample -->

```bash
cd samples/mcp-server-sample
mvn spring-boot:run
```

El endpoint MCP queda disponible en `http://localhost:8080/mcp`

### Conectar un host MCP

Ejemplo de configuración para un host que habla MCP sobre HTTP/SSE:

```json
{
  "mcpServers": {
    "springai-archetype": {
      "url": "http://localhost:8080/mcp",
      "headers": {
        "Authorization": "Bearer <tu-jwt>"
      }
    }
  }
}
```

---

## Usar los módulos en tu proyecto

Importa el BOM una vez y añade solo los módulos que necesites:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.ricardodlm.springai.mcp</groupId>
      <artifactId>mcp-architecture-framework</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.ricardodlm.springai.mcp</groupId>
    <artifactId>transport-auth-service</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.ricardodlm.springai.mcp</groupId>
    <artifactId>adapter-cache-caffeine</artifactId>
  </dependency>
</dependencies>
```

> Los módulos todavía no están publicados en Maven Central. Ejecuta `mvn install` desde `mcp-architecture-framework` para tenerlos disponibles en tu repositorio local.

Expón una tool desde cualquier bean de Spring:

```java
@Component
public class WeatherTools {

    @Tool(description = "Devuelve la temperatura actual de una ciudad")
    public String currentTemperature(String city) {
        return "Hacen 24 °C en " + city;
    }
}
```

---

## Configuración

Todos los módulos se configuran bajo el prefijo `mcp.*`. <!-- TODO: alinear con los nombres reales de las propiedades -->

```yaml
mcp:
  auth:
    enabled: true
    issuer-uri: https://sso.example.com/realms/mcp
    jwk-set-uri: https://sso.example.com/realms/mcp/protocol/openid-connect/certs
    required-scope: mcp:tools
  cors:
    allowed-origins:
      - https://mi-host-mcp.example.com
    allowed-methods: [GET, POST, OPTIONS]
  cache:
    caffeine:
      ttl: 5m
      maximum-size: 10000
  rest-client:
    connect-timeout: 2s
    read-timeout: 10s
```

---

## Estructura del Maven Archetype

```
springai-mcp-archetype/
└── mcp-architecture-framework/          # POM padre / BOM
    ├── common-architecture-fwk/
    │   └── common-jwt/
    │   ├── common-exceptions/
    ├── adapters-architecture-fwk/
    │   ├── adapter-auth-idp/
    │   ├── adapter-cache-caffeine/
    │   └── adapter-rest-client/
    ├── transport-architecture-fwk/
    │   ├── transport-auth-service/
    │   ├── transport-cors-service/
    │   ├── transport-session-service/
    │   └── transport-streamable-service/
    └── observability-architecture-fwk/
        └── observability-logging/
```

## Módulo de Generación de Scaffolding

```
springai-mcp-archetype/
└── mcp-architecture-framework/  
    ├── mcp-archetype-installer/  # Archetype Installer / Dependeicas
    ├── mcp-server-archetype/     # Archetype Maven para generar proyectos MCP Server  
```

Convención de packages: `io.github.ricardodlm.springai.mcp.<capa>.<módulo>` — p. ej. `io.github.ricardodlm.springai.mcp.adapters.auth.idp`.

---

## Build y desarrollo

| Comando | Qué hace |
|---|---|
| `mvn clean install` | Build completo con tests (ejecutar desde `mcp-architecture-framework`) |
| `mvn -N install` | Instala solo el POM padre / BOM |
| `mvn install -pl adapters-architecture-fwk/adapter-auth-idp -am` | Compila un módulo y sus dependencias |
| `mvn verify -s /dev/null` | Verifica que el build resuelve todo desde Maven Central sin settings personalizados |


## Contribuir

Los issues y pull requests son bienvenidos. Para cambios grandes, abre primero un issue para que podamos discutir el diseño.

---

## Licencia

Distribuido bajo la [Apache License 2.0](LICENSE). <!-- TODO: añadir archivo LICENSE -->

---

Creado por **Ricardo D. L. M.** · [LinkedIn](https://www.linkedin.com/in/<!-- TODO -->) · [GitHub](https://github.com/ricardo07dlm)

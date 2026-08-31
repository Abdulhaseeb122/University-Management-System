A robust, scalable RESTful backend service built with Spring Boot 3 and Java 21, designed to manage university administration workflows, user access controls, and student information management.

Key Features

Stateless JWT Authentication: Secure login and registration powered by Spring Security, custom JWT generation, and BCrypt password hashing.

Custom Security Filter: Integrated JwtAuthenticationFilter intercepting incoming HTTP requests to validate Bearer tokens and enforce route-level authorization.

Role-Based Access Control (RBAC): Dynamic role and permission mapping configured through Spring Data JPA and MySQL.

Global Exception Handling: Centralized exception handling ensuring structured, clean JSON error responses for duplicate records, bad requests, and unauthorized access.

Tech Stack

Core: Java 21, Spring Boot 3.2.5

Security: Spring Security, JJWT (Java JWT 0.11.5)

Persistence: Spring Data JPA, Hibernate, MySQL Connector/J

Utilities & Build: Lombok, Apache Maven

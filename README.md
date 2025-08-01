# spring-payroll
This project contains essential components of spring

Rest Controller,
Service,
DTO (Data Transfer object),
Model,
Assembler,
Exception,
Validation (Jakarta Bean Validation)

Use OrderDto in Your Controller + Assembler
You hide internal database fields (e.g., audit fields, lazy-loaded relations)

exceptions:
exception.specific: For domain-specific exceptions that represent business rules.
exception.general: For application-wide, reusable, or system-level exceptions.
exception.handler: For your @RestControllerAdvice classes that handle and format exceptions globally.



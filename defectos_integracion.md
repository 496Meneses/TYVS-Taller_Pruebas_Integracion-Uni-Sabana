# Registro de Defectos de Integración

**Proyecto:** Sistema de Gestión de Pedidos de Librería  
**Repositorio:** TYVS-Proyecto_Pruebas_Integracion  
**Curso:** Testing y Validación de Software — Universidad de La Sabana

---

## Plantilla de defecto

| Campo | Descripción |
|-------|-------------|
| **ID** | Identificador único |
| **Título** | Descripción breve del defecto |
| **Componente** | Capa afectada (dominio / aplicación / infraestructura / delivery) |
| **Tipo** | Funcional / Lógica de negocio / Integración / Rendimiento |
| **Severidad** | Alta / Media / Baja |
| **Estado** | Abierto / En progreso / Resuelto |
| **Pasos para reproducir** | Secuencia de acciones |
| **Resultado esperado** | Comportamiento correcto |
| **Resultado actual** | Comportamiento observado |
| **Corrección aplicada** | Cambio implementado para resolverlo |

---

## Defecto 001 — Stock no se reduce al confirmar pedido (RESUELTO)

| Campo | Valor |
|-------|-------|
| **ID** | DEF-001 |
| **Título** | El stock del libro no se actualiza al colocar un pedido exitoso |
| **Componente** | application/usecase — OrderService |
| **Tipo** | Lógica de negocio |
| **Severidad** | Alta |
| **Estado** | Resuelto |

**Pasos para reproducir:**
1. Agregar un libro con stock = 10.
2. Realizar un pedido de 3 unidades vía `POST /orders`.
3. Consultar el libro vía `GET /books/{id}`.

**Resultado esperado:** Stock = 7 (10 - 3).

**Resultado actual (pre-corrección):** Stock = 10 (no se actualizó).

**Corrección aplicada:**  
En `OrderService.placeOrder()`, después de persistir el pedido se invoca  
`bookRepo.updateStock(bookId, book.getStock() - quantity)` para reflejar la reducción.

---

## Defecto 002 — Stock en cero no marca el libro como no disponible (RESUELTO)

| Campo | Valor |
|-------|-------|
| **ID** | DEF-002 |
| **Título** | Un libro con stock=0 permanece marcado como `available=true` |
| **Componente** | infrastructure/persistence — H2BookRepository |
| **Tipo** | Integración |
| **Severidad** | Media |
| **Estado** | Resuelto |

**Pasos para reproducir:**
1. Agregar un libro con stock = 1.
2. Llamar a `updateStock(id, 0)`.
3. Consultar el libro; el campo `available` sigue siendo `true`.

**Resultado esperado:** `available = false` cuando `stock = 0`.

**Resultado actual (pre-corrección):** `available = true` aunque el stock sea 0.

**Corrección aplicada:**  
En `H2BookRepository.updateStock()` se actualiza simultáneamente `available = (newStock > 0)`:
```sql
UPDATE books SET stock = ?, available = ? WHERE id = ?
```

---

## Defecto 003 — Confirmar pedido cancelado no lanza error claro (ABIERTO)

| Campo | Valor |
|-------|-------|
| **ID** | DEF-003 |
| **Título** | Intentar confirmar un pedido CANCELLED retorna 400 genérico |
| **Componente** | delivery/rest — OrderController |
| **Tipo** | Funcional |
| **Severidad** | Baja |
| **Estado** | Abierto |

**Pasos para reproducir:**
1. Crear un pedido y cancelarlo (`PUT /orders/{id}/cancel`).
2. Intentar confirmarlo (`PUT /orders/{id}/confirm`).

**Resultado esperado:** HTTP 409 Conflict con mensaje descriptivo.

**Resultado actual:** HTTP 400 Bad Request con cuerpo `"INVALID_STATUS"`.

**Propuesta de corrección:**  
Agregar un nuevo `ResponseEntity` con `HttpStatus.CONFLICT` para el caso `INVALID_STATUS` en el controlador.  
Pendiente de priorización.

---

*Documento actualizado: 2026-06-08*

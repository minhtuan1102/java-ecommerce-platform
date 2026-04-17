# Multi-Vendor E-commerce Blueprint (Sprint 2 & 3 + Frontend)

## 1. Architectural Shift: Single-Vendor to Multi-Vendor
The system is transitioning from a basic B2C store to a B2B2C Marketplace (like Shopee/Amazon). 
This introduces the `ROLE_SELLER` and the concept of a `Shop`.

### Database Schema Updates (Requires Flyway Migrations)
*   **Users & Roles:** 
    *   Add `ROLE_SELLER` to the `roles` table.
*   **Shop Module:**
    *   `shops`: `id`, `name`, `description`, `owner_id` (FK to `users`), `status` (PENDING, ACTIVE, SUSPENDED).
    *   A User with `ROLE_SELLER` manages one `Shop`.
*   **Product Module:**
    *   `products`: `id`, `name`, `description`, `price`, `stock`, `category_id` (FK), `shop_id` (FK).
    *   Products now belong to a specific shop.
*   **Cart Module:**
    *   `cart_items`: `id`, `user_id`, `product_id`, `quantity`. (Grouped by shop in the UI/Service layer).
*   **Order Module:**
    *   `orders`: `id`, `user_id` (Buyer), `shop_id` (Seller), `total_amount`, `status`.
    *   *Crucial Change:* If a buyer checks out items from 3 different shops, 3 separate `orders` are created.

## 2. Backend Implementation Strategy (Java Spring Boot)

We must execute these phases sequentially. We cannot build Products or Carts securely without JWT.

### Phase 1: Security Foundation (Sprint 2 Recovery)
1.  **Spring Security Setup:** Configure `SecurityFilterChain`, CORS, and stateless session management.
2.  **Authentication Filter:** Implement `JwtAuthenticationFilter` to intercept requests, validate the Access Token, and set the Security Context.
3.  **Login Update:** Modify `AuthService.login` to generate and return a short-lived JWT Access Token.
4.  **Role Seed:** Update `AuthDataInitializer` to include `ROLE_SELLER`.

### Phase 2: Advanced Auth & Core Modules (Sprint 3)
1.  **Refresh Token Flow:** Implement `/api/v1/auth/refresh` to issue new Access Tokens, and `/logout` to revoke Refresh Tokens in the DB.
2.  **Shop Module:** Create CRUD APIs for Shops. Only `ROLE_SELLER` can create/manage their shop. `ROLE_ADMIN` can approve/suspend shops.
3.  **Product Module:** Create CRUD APIs. Sellers can only manage products within their own `shop_id`. Buyers can list all active products.
4.  **Cart & Checkout:** Implement cart management.
5.  **Redis (Optional for now):** Add caching for public product/category listings to improve performance.

## 3. Frontend Architecture (Vue 3 + Vite)

A single Vue SPA (Single Page Application) will handle all three roles using distinct layouts and route guards.

### Technology Stack
*   **Framework:** Vue 3 (Composition API, `<script setup>`)
*   **Build Tool:** Vite
*   **Routing:** Vue Router
*   **State Management:** Pinia (for Cart, User Session)
*   **HTTP Client:** Axios (with Interceptors for appending JWT and handling 401 token refreshes)
*   **Styling:** Tailwind CSS (or Vanilla CSS if preferred, but Tailwind is faster for prototyping dashboards).

### Routing Structure & Layouts
```text
src/
├── layouts/
│   ├── MainLayout.vue    (Header, Footer for Buyers/Guests)
│   ├── SellerLayout.vue  (Sidebar, Header for Sellers)
│   └── AdminLayout.vue   (Sidebar, Header for Admins)
├── views/
│   ├── public/           (Home, Product Details, Login, Register)
│   ├── buyer/            (Cart, Checkout, Profile - Requires Auth)
│   ├── seller/           (Dashboard, Manage Products, Orders - Requires ROLE_SELLER)
│   └── admin/            (Dashboard, Manage Users, Shops, Categories - Requires ROLE_ADMIN)
├── router/
│   └── index.js          (Defines routes and Navigation Guards)
```

### Authentication Flow (Frontend)
1.  **Login:** User logs in. Frontend receives `accessToken` (saves in memory/memory-store) and `refreshToken` (saves in HttpOnly Cookie or LocalStorage).
2.  **Axios Interceptor:** Every request automatically attaches `Authorization: Bearer <accessToken>`.
3.  **Auto-Refresh:** If an API returns `401 Unauthorized`, the interceptor pauses the request, calls the `/refresh` endpoint, updates the `accessToken`, and retries the original request seamlessly.
4.  **Route Guards:** Before navigating to `/seller/*`, the router checks if the user has `ROLE_SELLER`. If not, redirects to `/403` or `/`.

## 4. Immediate Next Steps (Execution Plan)

To move forward, we should tackle **Phase 1 (Security Foundation)** immediately. Without it, the backend cannot differentiate between a Buyer, Seller, or Admin.

**Action Item 1:** Add Spring Security and JWT dependencies to `pom.xml`.
**Action Item 2:** Create the `JwtService` (token generation/validation) and `SecurityConfig`.
**Action Item 3:** Update `AuthDataInitializer` to include `ROLE_SELLER` and perhaps seed a dummy seller account.

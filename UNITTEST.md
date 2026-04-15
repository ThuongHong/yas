# Unit Test Documentation — YAS (Yet Another Shop)

This document outlines unit test cases for every backend service in the YAS microservice platform.

**Stack**: Java 25 · Spring Boot 4.0.2 · JUnit 5 · Mockito · Instancio · Testcontainers  
**Coverage tool**: JaCoCo · SonarCloud  
**Test naming convention**: `methodName_stateUnderTest_expectedBehavior`

---

## Table of Contents

1. [Order Service](#1-order-service)
2. [Product Service](#2-product-service)
3. [Inventory Service](#3-inventory-service)
4. [Payment Service](#4-payment-service)
5. [Payment-PayPal Service](#5-payment-paypal-service)
6. [Rating Service](#6-rating-service)
7. [Promotion Service](#7-promotion-service)
8. [Cart Service](#8-cart-service)
9. [Customer Service](#9-customer-service)
10. [Location Service](#10-location-service)
11. [Tax Service](#11-tax-service)
12. [Media Service](#12-media-service)
13. [Search Service](#13-search-service)
14. [Webhook Service](#14-webhook-service)
15. [Common Library](#15-common-library)

---

## 1. Order Service

**Class**: `com.yas.order.service.OrderService`  
**Dependencies to mock**: `OrderRepository`, `OrderItemRepository`, `OrderAddressRepository`, `CheckoutRepository`, `TaxService`, `PromotionService`, `CustomerService`, `ProductService`, `CartService`, `AuthenticationUtils`

### 1.1 `createOrder(OrderPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 1 | `createOrder_validRequest_returnsOrderVm` | Mock `ProductService.getProductCheckoutList` returns valid products; `TaxService` returns rates; `PromotionService.verifyPromotion` returns discount | `OrderRepository.save` called once; returned `OrderVm` has correct total |
| 2 | `createOrder_productNotFound_throwsNotFoundException` | Mock `ProductService` throws `NotFoundException` | `NotFoundException` propagated |
| 3 | `createOrder_promotionInvalid_throwsBadRequestException` | Mock `PromotionService.verifyPromotion` returns invalid coupon | `BadRequestException` thrown |
| 4 | `createOrder_emptyItemList_throwsBadRequestException` | `OrderPostVm` with empty item list | `BadRequestException` thrown |
| 5 | `createOrder_success_clearsCart` | Happy-path mock setup | `CartService.deleteCartItems` invoked after order persisted |

### 1.2 `getOrderWithItemsById(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 6 | `getOrderWithItemsById_existingId_returnsOrder` | `OrderRepository.findById` returns `Optional<Order>` | Returns mapped `OrderVm` |
| 7 | `getOrderWithItemsById_nonExistingId_throwsNotFoundException` | `OrderRepository.findById` returns `Optional.empty()` | `NotFoundException` thrown |

### 1.3 `getAllOrder(...)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 8 | `getAllOrder_noFilters_returnsPagedOrders` | Repository returns page of orders | Correct `PagedOrders` structure returned |
| 9 | `getAllOrder_statusFilter_appliesSpec` | Filter by `OrderStatus.PENDING` | JPA Specification with status predicate applied |
| 10 | `getAllOrder_dateRangeFilter_appliesSpec` | Filter by date range | Specification includes date-range predicates |

### 1.4 `updateOrderPaymentStatus(PaymentOrderStatusVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 11 | `updateOrderPaymentStatus_paidStatus_updatesOrderStatus` | Order exists; payment status `PAID` | `Order.setOrderStatus(PAID)` and `Order.setPaymentStatus(PAID)` |
| 12 | `updateOrderPaymentStatus_orderNotFound_throwsNotFoundException` | `OrderRepository.findById` empty | `NotFoundException` thrown |

### 1.5 `rejectOrder(Long, String)` / `acceptOrder(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 13 | `rejectOrder_existingOrder_setsStatusReject` | Order in `PENDING` state | `Order.setOrderStatus(REJECT)` and rejection reason stored |
| 14 | `acceptOrder_existingOrder_setsStatusAccepted` | Order in `PENDING` state | `Order.setOrderStatus(ACCEPTED)` |
| 15 | `rejectOrder_alreadyRejected_throwsBadRequestException` | Order already in `REJECT` state | `BadRequestException` thrown |

### 1.6 `exportCsv(OrderRequest)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 16 | `exportCsv_withOrders_returnsNonEmptyByteArray` | Orders present matching criteria | Byte array contains CSV header row and data rows |
| 17 | `exportCsv_noOrders_returnsHeaderOnlyByteArray` | No orders match filter | Byte array contains only header row |

### 1.7 `isOrderCompletedWithUserIdAndProductId(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 18 | `isOrderCompleted_completedOrder_returnsTrue` | Repository finds completed order for user+product | Returns `true` |
| 19 | `isOrderCompleted_noCompletedOrder_returnsFalse` | Repository returns empty | Returns `false` |

---

## 2. Product Service

**Class**: `com.yas.product.service.ProductService`  
**Dependencies to mock**: `ProductRepository`, `CategoryRepository`, `BrandRepository`, `ProductImageRepository`, `ProductOptionRepository`, `MediaService`, `AuthenticationUtils`

### 2.1 `createProduct(ProductPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 20 | `createProduct_validVm_savesAndReturnsProductVm` | Brand and categories exist; `MediaService` returns image URL | `ProductRepository.save` called; returned `ProductVm` has correct slug |
| 21 | `createProduct_duplicateSlug_throwsDuplicatedException` | `ProductRepository.existsBySlug` returns `true` | `DuplicatedException` thrown |
| 22 | `createProduct_brandNotFound_throwsNotFoundException` | `BrandRepository.findById` empty | `NotFoundException` thrown |
| 23 | `createProduct_categoryNotFound_throwsNotFoundException` | `CategoryRepository.findById` empty | `NotFoundException` thrown |
| 24 | `createProduct_withVariations_createsOptionCombinations` | Product has options configured | `ProductOptionCombinationRepository.saveAll` invoked |

### 2.2 `updateProduct(ProductPutVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 25 | `updateProduct_validVm_updatesAllFields` | Product exists; all fields provided | Product entity updated; repository saved |
| 26 | `updateProduct_productNotFound_throwsNotFoundException` | `ProductRepository.findById` empty | `NotFoundException` thrown |
| 27 | `updateProduct_newSlugAlreadyTaken_throwsDuplicatedException` | `ProductRepository.existsBySlug` returns `true` for new slug | `DuplicatedException` thrown |

### 2.3 `subtractProductStockQuantity(OrderVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 28 | `subtractProductStockQuantity_validOrder_reducesStock` | Products exist with sufficient stock | Stock quantity decremented; `ProductRepository.saveAll` called |
| 29 | `subtractProductStockQuantity_insufficientStock_throwsBadRequestException` | Product stock `< ordered quantity` | `BadRequestException` thrown |

### 2.4 `getProductsByCategory(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 30 | `getProductsByCategory_existingCategory_returnsProducts` | Category exists; products linked | List of `ProductVm` returned |
| 31 | `getProductsByCategory_emptyCategory_returnsEmptyList` | Category exists but no products | Empty list returned |

### 2.5 `CategoryService.create / update / delete`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 32 | `createCategory_validVm_savesCategory` | No duplicate name | Category persisted |
| 33 | `createCategory_duplicateName_throwsDuplicatedException` | `CategoryRepository.existsByName` true | `DuplicatedException` thrown |
| 34 | `deleteCategory_withChildren_throwsBadRequestException` | Category has child categories | `BadRequestException` thrown |
| 35 | `deleteCategory_withProducts_throwsBadRequestException` | Category has associated products | `BadRequestException` thrown |

### 2.6 `BrandService.create / delete`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 36 | `createBrand_validVm_returnsBrandVm` | Unique brand name | Saved and returned |
| 37 | `createBrand_duplicateName_throwsDuplicatedException` | Name already exists | `DuplicatedException` thrown |
| 38 | `deleteBrand_brandWithProducts_throwsBadRequestException` | Products reference this brand | `BadRequestException` thrown |

---

## 3. Inventory Service

**Class**: `com.yas.inventory.service.StockService`  
**Dependencies to mock**: `StockRepository`, `StockHistoryRepository`, `WarehouseRepository`, `ProductService`

### 3.1 `addProductIntoWarehouse(List<StockPostVm>)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 39 | `addProductIntoWarehouse_validList_savesAllStocks` | Warehouse exists; products valid | `StockRepository.saveAll` called with correct quantities |
| 40 | `addProductIntoWarehouse_warehouseNotFound_throwsNotFoundException` | `WarehouseRepository.findById` empty | `NotFoundException` thrown |
| 41 | `addProductIntoWarehouse_duplicateProductInWarehouse_throwsStockExistingException` | Product already in warehouse | `StockExistingException` thrown |
| 42 | `addProductIntoWarehouse_emptyList_noRepositoryCall` | Empty input list | `StockRepository.saveAll` never called |

### 3.2 `updateProductQuantityInStock(StockQuantityUpdateVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 43 | `updateProductQuantityInStock_increaseQty_updatesStockAndCreatesHistory` | Stock exists; new quantity > current | Stock updated; `StockHistoryRepository.save` called with INCREASE action |
| 44 | `updateProductQuantityInStock_decreaseQty_updatesStockAndCreatesHistory` | Stock exists; new quantity < current | Stock updated; history recorded with DECREASE action |
| 45 | `updateProductQuantityInStock_stockNotFound_throwsNotFoundException` | `StockRepository.findById` empty | `NotFoundException` thrown |
| 46 | `updateProductQuantityInStock_negativeQuantity_throwsBadRequestException` | Negative quantity in VM | `BadRequestException` thrown |

### 3.3 `WarehouseService` CRUD

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 47 | `createWarehouse_validVm_savesWarehouse` | Unique warehouse name | Persisted and returned |
| 48 | `createWarehouse_duplicateName_throwsDuplicatedException` | Name already exists | `DuplicatedException` thrown |
| 49 | `deleteWarehouse_warehouseWithStock_throwsBadRequestException` | Stocks linked to warehouse | `BadRequestException` thrown |

---

## 4. Payment Service

**Class**: `com.yas.payment.service.PaymentService`  
**Dependencies to mock**: `PaymentRepository`, `PaymentProviderRepository`, `OrderService`, `PaymentHandler` (mock implementation)

### 4.1 `initPayment(InitPaymentRequestVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 50 | `initPayment_validRequest_returnsPaymentUrl` | Provider exists; `PaymentHandler.initPayment` returns URL | URL returned; `PaymentRepository.save` called |
| 51 | `initPayment_providerNotFound_throwsNotFoundException` | `PaymentProviderRepository.findByName` empty | `NotFoundException` thrown |
| 52 | `initPayment_handlerThrowsException_propagatesException` | `PaymentHandler.initPayment` throws | Exception propagated without saving |

### 4.2 `capturePayment(CapturePaymentRequestVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 53 | `capturePayment_approvedPayment_updatesOrderStatus` | Handler returns `CAPTURED`; payment record exists | `OrderService.updateOrderPaymentStatus` called with PAID status |
| 54 | `capturePayment_declinedPayment_updatesOrderStatusFailed` | Handler returns `DECLINED` | Order status updated to payment-failed |
| 55 | `capturePayment_paymentNotFound_throwsNotFoundException` | `PaymentRepository.findByToken` empty | `NotFoundException` thrown |

### 4.3 `PaymentProviderService` CRUD

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 56 | `createProvider_uniqueName_savesProvider` | No existing provider with same name | Saved and returned |
| 57 | `createProvider_duplicateName_throwsDuplicatedException` | Name already used | `DuplicatedException` thrown |

---

## 5. Payment-PayPal Service

**Class**: `com.yas.paymentpaypal.service.PaypalService`  
**Dependencies to mock**: PayPal SDK client, `PaypalConfig`

### 5.1 `initPayment`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 58 | `initPayment_validRequest_returnsApprovalUrl` | PayPal API returns order with approval link | Approval URL extracted and returned |
| 59 | `initPayment_paypalApiError_throwsInternalServerErrorException` | SDK throws API exception | `InternalServerErrorException` thrown |

### 5.2 `capturePayment`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 60 | `capturePayment_approvedOrder_returnsCapturedStatus` | PayPal `COMPLETED` response | Returns `CapturePaymentResponseVm` with CAPTURED status |
| 61 | `capturePayment_declinedOrder_returnsDeclinedStatus` | PayPal `DECLINED` response | Returns response with DECLINED status |
| 62 | `capturePayment_networkFailure_throwsInternalServerErrorException` | SDK throws connectivity exception | Exception wrapped and rethrown |

---

## 6. Rating Service

**Class**: `com.yas.rating.service.RatingService`  
**Dependencies to mock**: `RatingRepository`, `CustomerService`, `OrderService`, `AuthenticationUtils`

### 6.1 `createRating(RatingPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 63 | `createRating_verifiedPurchase_savesRating` | `OrderService.isOrderCompleted` returns `true`; customer exists | `RatingRepository.save` called; rating persisted |
| 64 | `createRating_unverifiedPurchase_throwsForbiddenException` | `OrderService.isOrderCompleted` returns `false` | `ForbiddenException` thrown |
| 65 | `createRating_invalidStarValue_throwsBadRequestException` | Star rating outside 1-5 | `BadRequestException` thrown |
| 66 | `createRating_duplicateRating_throwsDuplicatedException` | Customer already rated this product | `DuplicatedException` thrown |

### 6.2 `getRatingListByProductId(Long, int, int)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 67 | `getRatingListByProductId_existingProduct_returnsPagedRatings` | Ratings exist for product | Correct page of `RatingVm` returned |
| 68 | `getRatingListByProductId_noRatings_returnsEmptyPage` | No ratings for product | Empty page returned |
| 69 | `getRatingListByProductId_pageNumberOutOfBounds_returnsEmptyPage` | Page number exceeds total pages | Empty page returned |

### 6.3 `deleteRating(long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 70 | `deleteRating_existingRating_deletesSuccessfully` | Rating exists | `RatingRepository.deleteById` called |
| 71 | `deleteRating_notOwner_throwsForbiddenException` | Rating belongs to different customer | `ForbiddenException` thrown |
| 72 | `deleteRating_nonExistingRating_throwsNotFoundException` | `RatingRepository.findById` empty | `NotFoundException` thrown |

### 6.4 `calculateAverageStar(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 73 | `calculateAverageStar_multipleRatings_returnsCorrectAverage` | Ratings: 3, 4, 5 | Returns `4.0` |
| 74 | `calculateAverageStar_noRatings_returnsZero` | No ratings | Returns `0.0` |

---

## 7. Promotion Service

**Class**: `com.yas.promotion.service.PromotionService`  
**Dependencies to mock**: `PromotionRepository`, `PromotionUsageRepository`, `ProductService`, `AuthenticationUtils`

### 7.1 `createPromotion(PromotionPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 75 | `createPromotion_validVm_savesPromotion` | Unique coupon code; valid date range | Promotion persisted and returned |
| 76 | `createPromotion_duplicateCouponCode_throwsDuplicatedException` | `PromotionRepository.existsByCode` true | `DuplicatedException` thrown |
| 77 | `createPromotion_endDateBeforeStartDate_throwsBadRequestException` | `endDate` before `startDate` | `BadRequestException` thrown |
| 78 | `createPromotion_percentageOver100_throwsBadRequestException` | Discount type `PERCENTAGE`; value > 100 | `BadRequestException` thrown |

### 7.2 `verifyPromotion(PromotionVerifyVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 79 | `verifyPromotion_validCoupon_returnsDiscount` | Active promotion; customer usage under limit | Returns computed discount amount |
| 80 | `verifyPromotion_expiredCoupon_throwsBadRequestException` | Promotion past `endDate` | `BadRequestException` thrown |
| 81 | `verifyPromotion_usageLimitReached_throwsBadRequestException` | Customer reached `PER_CUSTOMER` limit | `BadRequestException` thrown |
| 82 | `verifyPromotion_couponNotFound_throwsNotFoundException` | `PromotionRepository.findByCode` empty | `NotFoundException` thrown |
| 83 | `verifyPromotion_inactivePromotion_throwsBadRequestException` | Promotion not yet started | `BadRequestException` thrown |

### 7.3 `updateUsagePromotion(List<PromotionUsageVm>)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 84 | `updateUsagePromotion_validList_savesUsageRecords` | Promotions exist | `PromotionUsageRepository.saveAll` called |
| 85 | `updateUsagePromotion_emptyList_noRepositoryCall` | Empty input | Repository `saveAll` not called |

---

## 8. Cart Service

**Class**: `com.yas.cart.service.CartItemService`  
**Dependencies to mock**: `CartItemRepository`, `ProductService`, `AuthenticationUtils`

### 8.1 `addCartItem(CartItemPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 86 | `addCartItem_newItem_createsCartItem` | Product exists; cart empty | `CartItemRepository.save` called with quantity 1 |
| 87 | `addCartItem_existingItem_incrementsQuantity` | Product already in cart with quantity 2 | Quantity updated to 3 |
| 88 | `addCartItem_productNotFound_throwsNotFoundException` | `ProductService.existsById` false | `NotFoundException` thrown |
| 89 | `addCartItem_zeroQuantity_throwsBadRequestException` | Quantity 0 in request | `BadRequestException` thrown |

### 8.2 `updateCartItem(Long, CartItemPutVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 90 | `updateCartItem_validRequest_updatesQuantity` | Cart item exists | Quantity updated; repository saved |
| 91 | `updateCartItem_itemNotFound_throwsNotFoundException` | `CartItemRepository.findByCustomerIdAndProductId` empty | `NotFoundException` thrown |
| 92 | `updateCartItem_negativeQuantity_throwsBadRequestException` | Quantity < 0 | `BadRequestException` thrown |

### 8.3 `deleteOrAdjustCartItem(List<CartItemDeleteVm>)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 93 | `deleteOrAdjustCartItem_singleQuantityItem_deletesItem` | Item with quantity 1 | Item deleted from repository |
| 94 | `deleteOrAdjustCartItem_multiQuantityItem_decrementsQuantity` | Item with quantity 3; decrement by 1 | Quantity becomes 2 |
| 95 | `deleteOrAdjustCartItem_itemNotFound_throwsNotFoundException` | Item does not exist for user | `NotFoundException` thrown |

### 8.4 `getCartItems()`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 96 | `getCartItems_authenticatedUser_returnsUserItems` | Current user has 3 items | Returns list of 3 `CartItemVm` |
| 97 | `getCartItems_emptyCart_returnsEmptyList` | No items for user | Returns empty list |

---

## 9. Customer Service

**Class**: `com.yas.customer.service.CustomerService`  
**Dependencies to mock**: `KeycloakAdmin`, `UserAddressRepository`, `LocationService`, `AuthenticationUtils`

### 9.1 `getCustomers(int)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 98 | `getCustomers_validPage_returnsPagedCustomers` | Keycloak returns user list | Returns `CustomerVm` list with pagination metadata |
| 99 | `getCustomers_emptyPage_returnsEmptyList` | Keycloak returns empty | Empty list returned |

### 9.2 `updateCustomer(String, CustomerProfileRequestVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 100 | `updateCustomer_validRequest_updatesKeycloakUser` | User exists in Keycloak | Keycloak `UserResource.update` called |
| 101 | `updateCustomer_invalidEmail_throwsWrongEmailFormatException` | Malformed email in request | `WrongEmailFormatException` thrown |
| 102 | `updateCustomer_userNotFound_throwsNotFoundException` | Keycloak returns 404 | `NotFoundException` thrown |

### 9.3 `deleteCustomer(String)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 103 | `deleteCustomer_existingUser_disablesUser` | User found in Keycloak | `enabled` set to `false`; Keycloak update called |
| 104 | `deleteCustomer_nonExistingUser_throwsNotFoundException` | Keycloak user not found | `NotFoundException` thrown |

### 9.4 `createGuestUser()`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 105 | `createGuestUser_success_returnsGuestToken` | Keycloak accepts user creation | Guest user created; credentials returned |
| 106 | `createGuestUser_keycloakError_throwsInternalServerErrorException` | Keycloak returns error response | `InternalServerErrorException` thrown |

### 9.5 `UserAddressService` CRUD

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 107 | `createAddress_validVm_savesAddress` | Location exists; user authenticated | Address persisted |
| 108 | `getAddresses_authenticatedUser_returnsAddresses` | User has 2 addresses | Returns list of 2 `UserAddressVm` |
| 109 | `deleteAddress_existingAddress_deletesSuccessfully` | Address belongs to user | `UserAddressRepository.deleteById` called |
| 110 | `deleteAddress_foreignAddress_throwsForbiddenException` | Address belongs to different user | `ForbiddenException` thrown |

---

## 10. Location Service

**Class**: `com.yas.location.service.CountryService`, `StateOrProvinceService`, `DistrictService`, `AddressService`  
**Dependencies to mock**: `CountryRepository`, `StateOrProvinceRepository`, `DistrictRepository`, `AddressRepository`

### 10.1 `CountryService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 111 | `findAllCountries_always_returnsAlphabeticallySortedList` | 3 countries in random order | Returns list sorted by name ascending |
| 112 | `create_validVm_savesCountry` | Unique country code and name | Persisted and returned |
| 113 | `create_duplicateCode_throwsDuplicatedException` | Country code already exists | `DuplicatedException` thrown |
| 114 | `delete_countryWithStates_throwsBadRequestException` | Country has linked states | `BadRequestException` thrown |
| 115 | `update_nonExistingCountry_throwsNotFoundException` | `CountryRepository.findById` empty | `NotFoundException` thrown |
| 116 | `getPageableCountries_validPageRequest_returnsPaginatedResult` | 10 countries; page size 5 | Returns page 1 with 5 items; `totalPages = 2` |

### 10.2 `StateOrProvinceService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 117 | `createStateOrProvince_validVm_savesState` | Country exists; unique state name | Persisted and returned |
| 118 | `createStateOrProvince_countryNotFound_throwsNotFoundException` | `CountryRepository.findById` empty | `NotFoundException` thrown |
| 119 | `deleteStateOrProvince_withDistricts_throwsBadRequestException` | State has linked districts | `BadRequestException` thrown |

### 10.3 `DistrictService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 120 | `createDistrict_validVm_savesDistrict` | State exists | Persisted and returned |
| 121 | `createDistrict_stateNotFound_throwsNotFoundException` | `StateOrProvinceRepository.findById` empty | `NotFoundException` thrown |
| 122 | `deleteDistrict_existingDistrict_deletesSuccessfully` | District has no child dependencies | `DistrictRepository.deleteById` called |

### 10.4 `AddressService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 123 | `createAddress_validVm_savesAddress` | District, state, country all exist | Address persisted |
| 124 | `getAddressById_existingId_returnsAddress` | `AddressRepository.findById` returns address | `AddressVm` with location names returned |
| 125 | `getAddressById_nonExistingId_throwsNotFoundException` | Repository returns empty | `NotFoundException` thrown |

---

## 11. Tax Service

**Class**: `com.yas.tax.service.TaxRateService`, `TaxClassService`  
**Dependencies to mock**: `TaxRateRepository`, `TaxClassRepository`, `LocationService`

### 11.1 `TaxRateService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 126 | `createTaxRate_validVm_savesTaxRate` | `TaxClassRepository.existsById` returns `true` | `TaxRateRepository.save` called; entity returned |
| 127 | `createTaxRate_taxClassNotFound_throwsNotFoundException` | `TaxClassRepository.existsById` returns `false` | `NotFoundException` with `TAX_CLASS_NOT_FOUND` |
| 128 | `updateTaxRate_validVm_updatesAllFields` | Rate found; tax class exists | Rate, zipCode, taxClass, stateOrProvinceId, countryId all updated; `save` called |
| 129 | `updateTaxRate_rateNotFound_throwsNotFoundException` | `TaxRateRepository.findById` empty | `NotFoundException` with `TAX_RATE_NOT_FOUND` |
| 130 | `updateTaxRate_taxClassNotFound_throwsNotFoundException` | Rate found; `TaxClassRepository.existsById` false | `NotFoundException` with `TAX_CLASS_NOT_FOUND` |
| 131 | `delete_existingRate_deletesSuccessfully` | `TaxRateRepository.existsById` true | `TaxRateRepository.deleteById` called |
| 132 | `delete_rateNotFound_throwsNotFoundException` | `TaxRateRepository.existsById` false | `NotFoundException` with `TAX_RATE_NOT_FOUND` |
| 133 | `findById_existingId_returnsTaxRateVm` | `TaxRateRepository.findById` returns entity | `TaxRateVm.fromModel` result returned |
| 134 | `findById_nonExistingId_throwsNotFoundException` | `TaxRateRepository.findById` empty | `NotFoundException` thrown |
| 135 | `findAll_withRates_returnsMappedList` | `TaxRateRepository.findAll` returns 3 rates | List of 3 `TaxRateVm` returned |
| 136 | `findAll_noRates_returnsEmptyList` | `TaxRateRepository.findAll` returns empty | Empty list returned |
| 137 | `getPageableTaxRates_withStateOrProvinces_enrichesWithLocationNames` | Page has rates with stateOrProvinceIds; `LocationService` returns names | Each `TaxRateGetDetailVm` has `stateName` and `countryName` populated |
| 138 | `getPageableTaxRates_emptyStateIds_skipsLocationServiceCall` | Page has rates with `null` stateOrProvinceId | `LocationService.getStateOrProvinceAndCountryNames` never called |
| 139 | `getTaxPercent_rateFound_returnsPercent` | `TaxRateRepository.getTaxPercent` returns `0.1` | Returns `0.1` |
| 140 | `getTaxPercent_noRateFound_returnsZero` | `TaxRateRepository.getTaxPercent` returns `null` | Returns `0.0` |
| 141 | `getBulkTaxRate_validParams_returnsMappedList` | `TaxRateRepository.getBatchTaxRates` returns 2 rates | Returns list of 2 `TaxRateVm` |
| 142 | `getBulkTaxRate_emptyTaxClassIds_returnsEmptyList` | Repository returns empty collection | Returns empty list |

### 11.2 `TaxClassService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 143 | `findAllTaxClasses_always_returnsSortedByNameAsc` | 3 classes in unsorted order | Returns list sorted alphabetically by `name` |
| 144 | `findAllTaxClasses_noClasses_returnsEmptyList` | Repository returns empty | Returns empty list |
| 145 | `findById_existingId_returnsTaxClassVm` | `TaxClassRepository.findById` returns entity | `TaxClassVm.fromModel` result returned |
| 146 | `findById_nonExistingId_throwsNotFoundException` | `TaxClassRepository.findById` empty | `NotFoundException` with `TAX_CLASS_NOT_FOUND` |
| 147 | `create_validVm_savesTaxClass` | `TaxClassRepository.existsByName` returns `false` | `TaxClassRepository.save` called; entity returned |
| 148 | `create_duplicateName_throwsDuplicatedException` | `TaxClassRepository.existsByName` returns `true` | `DuplicatedException` with `NAME_ALREADY_EXITED` |
| 149 | `update_validVm_updatesName` | Class found; `existsByNameNotUpdatingTaxClass` false | `TaxClass.setName` called; `save` called |
| 150 | `update_classNotFound_throwsNotFoundException` | `TaxClassRepository.findById` empty | `NotFoundException` with `TAX_CLASS_NOT_FOUND` |
| 151 | `update_newNameTakenByOtherClass_throwsDuplicatedException` | `existsByNameNotUpdatingTaxClass` returns `true` | `DuplicatedException` thrown |
| 152 | `update_sameNameAsCurrentClass_updatesSuccessfully` | `existsByNameNotUpdatingTaxClass` false (own ID excluded) | No exception; `save` called |
| 153 | `delete_existingClass_deletesSuccessfully` | `TaxClassRepository.existsById` true | `TaxClassRepository.deleteById` called |
| 154 | `delete_classNotFound_throwsNotFoundException` | `TaxClassRepository.existsById` false | `NotFoundException` with `TAX_CLASS_NOT_FOUND` |
| 155 | `getPageableTaxClasses_validPage_returnsPaginatedResult` | 10 classes; page size 5 | Returns `TaxClassListGetVm` with correct `totalPages`, `totalElements`, `isLast` |

---

## 12. Media Service

**Class**: `com.yas.media.service.MediaServiceImpl`  
**Dependencies to mock**: `MediaRepository`, `FileSystemRepository`

### 12.1 `saveMedia(MediaPostVm)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 136 | `saveMedia_validFile_persistsFileAndRecord` | Valid multipart file; storage write succeeds | `FileSystemRepository.writeFile` called; `MediaRepository.save` called; returned `MediaVm` has valid URL |
| 137 | `saveMedia_storageFailure_throwsInternalServerErrorException` | `FileSystemRepository.writeFile` throws `IOException` | `InternalServerErrorException` thrown |
| 138 | `saveMedia_emptyFileName_throwsBadRequestException` | `MediaPostVm` has blank filename | `BadRequestException` thrown |

### 12.2 `removeMedia(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 139 | `removeMedia_existingMedia_deletesFileAndRecord` | `MediaRepository.findById` returns entity | `FileSystemRepository.delete` called; `MediaRepository.deleteById` called |
| 140 | `removeMedia_mediaNotFound_throwsNotFoundException` | `MediaRepository.findById` empty | `NotFoundException` thrown |
| 141 | `removeMedia_fileDeletionFailure_stillDeletesDatabaseRecord` | File delete throws `IOException` | DB record deleted; `IOException` logged but not rethrown |

### 12.3 `getMediaById(Long)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 142 | `getMediaById_existingId_returnsMediaVm` | Media entity found | `MediaVm` with URL constructed from file path |
| 143 | `getMediaById_nonExistingId_throwsNotFoundException` | Repository returns empty | `NotFoundException` thrown |

### 12.4 `getMediaByIds(List<Long>)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 144 | `getMediaByIds_validIds_returnsListOfMediaVm` | All IDs resolve to entities | Returns list matching size of input |
| 145 | `getMediaByIds_someIdsNotFound_returnsOnlyFoundEntities` | 2 of 3 IDs exist | Returns list of 2 |
| 146 | `getMediaByIds_emptyList_returnsEmptyList` | Empty input list | Returns empty list |

---

## 13. Search Service

**Class**: `com.yas.search.service.ProductService`  
**Dependencies to mock**: `ElasticsearchOperations`, `ProductRepository` (Elasticsearch)

### 13.1 `findProductAdvance(ProductCriteriaDto)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 147 | `findProductAdvance_keywordQuery_returnsFuzzyMatchedProducts` | Elasticsearch returns matching docs | Products with fuzzy-matched names returned |
| 148 | `findProductAdvance_brandFilter_appliesBrandTermQuery` | Filter by brand ID | Query includes term filter on `brandId` |
| 149 | `findProductAdvance_categoryFilter_appliesCategoryTermQuery` | Filter by category ID | Query includes nested term filter on `categoryIds` |
| 150 | `findProductAdvance_priceRangeFilter_appliesRangeQuery` | Min `10.0`, max `50.0` | Query includes range filter on `price` |
| 151 | `findProductAdvance_sortByPriceAsc_returnsSortedResults` | Sort mode `PRICE_ASC` | Results sorted by `price` ascending |
| 152 | `findProductAdvance_sortByPriceDesc_returnsSortedResults` | Sort mode `PRICE_DESC` | Results sorted by `price` descending |
| 153 | `findProductAdvance_withAggregations_returnsFacets` | Aggregation enabled | Response contains category, brand, and attribute buckets |
| 154 | `findProductAdvance_noResults_returnsEmptyPage` | Elasticsearch returns empty hits | Empty page returned with zero total |
| 155 | `findProductAdvance_elasticsearchDown_throwsInternalServerErrorException` | `ElasticsearchOperations` throws | `InternalServerErrorException` thrown |

---

## 14. Webhook Service

**Class**: `com.yas.webhook.service.WebhookService`, `EventService`  
**Dependencies to mock**: `WebhookRepository`, `EventRepository`, `WebhookEventRepository`, `WebhookEventNotificationRepository`, `RestClient`

### 14.1 `WebhookService` CRUD

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 156 | `create_validVm_savesWebhook` | Valid URL and events | `WebhookRepository.save` called |
| 157 | `create_invalidUrl_throwsBadRequestException` | Malformed URL | `BadRequestException` thrown |
| 158 | `update_existingWebhook_updatesFields` | Webhook exists | Updated entity saved |
| 159 | `update_webhookNotFound_throwsNotFoundException` | `WebhookRepository.findById` empty | `NotFoundException` thrown |
| 160 | `delete_existingWebhook_deletesSuccessfully` | Webhook exists | `WebhookRepository.deleteById` called |
| 161 | `getPageableWebhooks_validPage_returnsPaginatedResult` | 6 webhooks; page size 3 | Returns page with 3 items |

### 14.2 `notifyToWebhook(WebhookEventNotificationDto)`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 162 | `notifyToWebhook_successfulDelivery_setsStatusNotified` | HTTP call returns 200 | `NotificationStatus.NOTIFIED` persisted |
| 163 | `notifyToWebhook_httpFailure_setsStatusFailed` | HTTP call returns 500 | `NotificationStatus.FAILED` persisted; no exception thrown |
| 164 | `notifyToWebhook_networkTimeout_setsStatusFailed` | `RestClient` throws `ConnectTimeoutException` | `NotificationStatus.FAILED` persisted |
| 165 | `notifyToWebhook_isAsync_doesNotBlockCaller` | Verify method is `@Async` annotated | Method annotation present on invocation path |

### 14.3 `EventService`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 166 | `createEvent_validVm_savesEvent` | Unique event name | Event persisted |
| 167 | `createEvent_duplicateName_throwsDuplicatedException` | Name already exists | `DuplicatedException` thrown |
| 168 | `getPageableEvents_validRequest_returnsPaginatedEvents` | Multiple events | Paginated result returned |

---

## 15. Common Library

**Class**: `com.yas.commonlibrary.utils.AuthenticationUtils`, `CsvExporter`

### 15.1 `AuthenticationUtils`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 169 | `getCurrentUserId_validJwt_returnsSubjectClaim` | JWT principal with `sub` claim | Returns user ID string |
| 170 | `getCurrentUserId_noAuthentication_throwsAccessDeniedException` | `SecurityContextHolder` has no auth | `AccessDeniedException` thrown |
| 171 | `getCurrentUserId_anonymousUser_throwsAccessDeniedException` | Principal is anonymous token | `AccessDeniedException` thrown |

### 15.2 `CsvExporter`

| # | Test Case | Setup | Expected |
|---|-----------|-------|----------|
| 172 | `export_listOfObjects_producesValidCsv` | List of `Order` objects | CSV rows match order count; header present |
| 173 | `export_emptyList_producesHeaderOnly` | Empty list | CSV has only the header row |
| 174 | `export_specialCharactersInFields_escapedProperly` | Field contains comma and quote | CSV field properly quoted and escaped |

---

## Test Infrastructure Notes

### Test Setup Pattern (per service)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductService productService;
    // ... other mocks

    @InjectMocks OrderService orderService;

    @BeforeEach
    void setUp() {
        // Instancio-based test data generation
        order = Instancio.create(Order.class);
    }
}
```

### Instancio Usage

Use Instancio for generating test data to reduce boilerplate:

```java
// Random valid Order
Order order = Instancio.create(Order.class);

// Controlled fields
OrderPostVm vm = Instancio.of(OrderPostVm.class)
    .set(field(OrderPostVm::status), OrderStatus.PENDING)
    .create();
```

### Integration Test Pattern

Integration tests (annotated `*IT`) use Testcontainers:

```java
@SpringBootTest
@Testcontainers
class OrderServiceIT extends AbstractControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
}
```

### Exception Verification

```java
assertThatThrownBy(() -> service.createOrder(invalidVm))
    .isInstanceOf(NotFoundException.class)
    .hasMessageContaining("Product not found");
```

### Coverage Targets

| Layer | Minimum Coverage |
|-------|-----------------|
| Service | 80% line, 75% branch |
| Repository (custom queries) | 70% |
| Mapper | 90% |
| Controller | 70% (via integration tests) |

> Classes excluded from coverage: `*Application.java`, `*Config.java`, `*Exception.java`, `*Constant.java`

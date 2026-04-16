package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.product.model.ProductOption;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.product.ProductCheckoutListVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    // Helper to build a minimal valid ProductPostVm (no variations, no options)
    private ProductPostVm buildProductPostVm(String name, String slug, Long brandId, List<Long> categoryIds) {
        return new ProductPostVm(
            name, slug, brandId, categoryIds,
            "short desc", "desc", "spec",
            "SKU-001", "", 1.0, null, 2.0, 1.0, 0.5,
            10.0, true, true, false, true, false,
            "title", "keyword", "meta",
            null, List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
    }

    private ProductPutVm buildProductPutVm(String name, String slug, Long brandId, List<Long> categoryIds) {
        return new ProductPutVm(
            name, slug, 10.0, true, true, false, true, false,
            brandId, categoryIds,
            "short desc", "desc", "spec",
            "SKU-001", "", 1.0, null, 2.0, 1.0, 0.5,
            "title", "keyword", "meta",
            null, List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
    }

    @Nested
    class CreateProductTest {

        @Test
        void createProduct_validVm_savesAndReturnsProductVm() {
            ProductPostVm vm = buildProductPostVm("Laptop", "laptop", 1L, List.of(2L));

            Brand brand = new Brand();
            brand.setId(1L);
            brand.setName("BrandX");

            Category category = new Category();
            category.setId(2L);

            Product savedProduct = Product.builder()
                .id(10L)
                .name("Laptop")
                .slug("laptop")
                .productCategories(new ArrayList<>())
                .build();

            when(productRepository.findBySlugAndIsPublishedTrue("laptop")).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue("SKU-001")).thenReturn(Optional.empty());
            when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());
            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(categoryRepository.findAllById(List.of(2L))).thenReturn(List.of(category));
            when(productImageRepository.saveAll(anyList())).thenReturn(List.of());
            when(productCategoryRepository.saveAll(anyList())).thenReturn(List.of());

            ProductGetDetailVm result = productService.createProduct(vm);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        void createProduct_duplicateSlug_throwsDuplicatedException() {
            ProductPostVm vm = buildProductPostVm("Laptop", "laptop", null, List.of());

            Product existing = Product.builder().id(99L).slug("laptop").build();
            when(productRepository.findBySlugAndIsPublishedTrue("laptop")).thenReturn(Optional.of(existing));
            when(productRepository.findAllById(anyList())).thenReturn(List.of());

            assertThrows(DuplicatedException.class, () -> productService.createProduct(vm));
            verify(productRepository, never()).save(any());
        }

        @Test
        void createProduct_brandNotFound_throwsNotFoundException() {
            ProductPostVm vm = buildProductPostVm("Laptop", "laptop", 99L, List.of());

            when(productRepository.findBySlugAndIsPublishedTrue("laptop")).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue("SKU-001")).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());

            Product savedProduct = Product.builder()
                .id(1L).name("Laptop").slug("laptop").productCategories(new ArrayList<>()).build();
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(productImageRepository.saveAll(anyList())).thenReturn(List.of());
            when(productCategoryRepository.saveAll(anyList())).thenReturn(List.of());
            when(brandRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.createProduct(vm));
        }

        @Test
        void createProduct_categoryNotFound_throwsBadRequestException() {
            ProductPostVm vm = buildProductPostVm("Laptop", "laptop", null, List.of(77L));

            when(productRepository.findBySlugAndIsPublishedTrue("laptop")).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue("SKU-001")).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());

            Product savedProduct = Product.builder()
                .id(1L).name("Laptop").slug("laptop").productCategories(new ArrayList<>()).build();
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(productImageRepository.saveAll(anyList())).thenReturn(List.of());
            when(categoryRepository.findAllById(List.of(77L))).thenReturn(List.of()); // none found

            assertThrows(BadRequestException.class, () -> productService.createProduct(vm));
        }
    }

    @Nested
    class UpdateProductTest {

        @Test
        void updateProduct_validVm_updatesProduct() {
            ProductPutVm vm = buildProductPutVm("Updated", "updated", 1L, List.of());

            Product existing = Product.builder()
                .id(5L).name("Old").slug("old")
                .productCategories(new ArrayList<>())
                .productImages(new ArrayList<>())
                .products(new ArrayList<>())
                .relatedProducts(new ArrayList<>())
                .build();

            Brand brand = new Brand();
            brand.setId(1L);

            ProductOption productOption = new ProductOption();
            productOption.setId(1L);

            when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(productRepository.findBySlugAndIsPublishedTrue("updated")).thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue("SKU-001")).thenReturn(Optional.empty());
            when(productRepository.findAllById(anyList())).thenReturn(List.of());
            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
            when(productCategoryRepository.findAllByProductId(5L)).thenReturn(List.of());
            when(productImageRepository.saveAll(anyList())).thenReturn(List.of());
            when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(productOption));

            productService.updateProduct(5L, vm);

            verify(productRepository).findById(5L);
        }

        @Test
        void updateProduct_notFound_throwsNotFoundException() {
            ProductPutVm vm = buildProductPutVm("Updated", "updated", null, List.of());
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.updateProduct(99L, vm));
        }

        @Test
        void updateProduct_slugTakenByOtherProduct_throwsDuplicatedException() {
            ProductPutVm vm = buildProductPutVm("Updated", "taken-slug", null, List.of());

            Product existing = Product.builder().id(5L).slug("old-slug").build();
            Product other = Product.builder().id(20L).slug("taken-slug").build();

            when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(productRepository.findBySlugAndIsPublishedTrue("taken-slug")).thenReturn(Optional.of(other));
            when(productRepository.findAllById(anyList())).thenReturn(List.of());

            assertThrows(DuplicatedException.class, () -> productService.updateProduct(5L, vm));
        }
    }

    @Nested
    class GetProductsByCategoryTest {

        @Test
        void getProductsFromCategory_found_returnsVm() {
            Category category = new Category();
            category.setId(1L);
            category.setSlug("electronics");

            Product product = Product.builder()
                .id(10L).name("Phone").slug("phone").thumbnailMediaId(1L).build();
            ProductCategory pc = ProductCategory.builder().product(product).category(category).build();

            Page<ProductCategory> page = new PageImpl<>(List.of(pc));

            when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category));
            when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class)))
                .thenReturn(page);
            when(mediaService.getMedia(anyLong())).thenReturn(new com.yas.product.viewmodel.NoFileMediaVm(
                1L, "", "", "", "http://img.jpg"));

            var result = productService.getProductsFromCategory(0, 10, "electronics");

            assertThat(result.products()).hasSize(1);
        }

        @Test
        void getProductsFromCategory_categoryNotFound_throwsNotFoundException() {
            when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                () -> productService.getProductsFromCategory(0, 10, "unknown"));
        }
    }

    @Nested
    class SubtractStockQuantityTest {

        @Test
        void subtractStockQuantity_valid_reducesStock() {
            Product product = Product.builder()
                .id(1L).stockTrackingEnabled(true).stockQuantity(10L).build();

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 3L)));

            assertThat(product.getStockQuantity()).isEqualTo(7L);
            verify(productRepository).saveAll(anyList());
        }

        @Test
        void subtractStockQuantity_quantityExceedsStock_setsZero() {
            Product product = Product.builder()
                .id(1L).stockTrackingEnabled(true).stockQuantity(2L).build();

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 10L)));

            assertThat(product.getStockQuantity()).isEqualTo(0L);
        }
    }

    @Nested
    class GetProductCheckoutListTest {

        @Test
        void getProductCheckoutList_validIds_returnsVm() {
            Brand brand = new Brand();
            brand.setId(1L);

            Product product = Product.builder()
                .id(1L).name("Phone").slug("phone")
                .thumbnailMediaId(1L).price(99.0).brand(brand)
                .build();

            Page<Product> page = new PageImpl<>(List.of(product));

            when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
            when(mediaService.getMedia(anyLong())).thenReturn(new com.yas.product.viewmodel.NoFileMediaVm(
                1L, "", "", "", "http://img.jpg"));

            ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

            assertThat(result.productCheckoutListVms()).hasSize(1);
            verify(productRepository).findAllPublishedProductsByIds(anyList(), any(Pageable.class));
        }
    }
}

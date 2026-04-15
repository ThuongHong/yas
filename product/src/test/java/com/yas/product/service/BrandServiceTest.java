package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Product;
import com.yas.product.repository.BrandRepository;
import com.yas.product.viewmodel.brand.BrandPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Nested
    class CreateBrandTest {

        @Test
        void create_validVm_savesAndReturnsBrand() {
            BrandPostVm vm = new BrandPostVm("BrandX", "brandx", true);
            Brand saved = vm.toModel();
            saved.setId(1L);

            when(brandRepository.findExistedName("BrandX", null)).thenReturn(null);
            when(brandRepository.save(any(Brand.class))).thenReturn(saved);

            Brand result = brandService.create(vm);

            assertNotNull(result);
            assertEquals("BrandX", result.getName());
            assertEquals("brandx", result.getSlug());
            verify(brandRepository).save(any(Brand.class));
        }

        @Test
        void create_duplicateName_throwsDuplicatedException() {
            BrandPostVm vm = new BrandPostVm("ExistingBrand", "existing-brand", true);
            when(brandRepository.findExistedName("ExistingBrand", null)).thenReturn(new Brand());

            assertThrows(DuplicatedException.class, () -> brandService.create(vm));
            verify(brandRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteBrandTest {

        @Test
        void delete_brandWithProducts_throwsBadRequestException() {
            Brand brand = new Brand();
            brand.setId(1L);
            brand.setProducts(List.of(new Product()));

            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

            assertThrows(BadRequestException.class, () -> brandService.delete(1L));
            verify(brandRepository, never()).deleteById(any());
        }

        @Test
        void delete_cleanBrand_deletesSuccessfully() {
            Brand brand = new Brand();
            brand.setId(1L);
            brand.setProducts(List.of());

            when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

            brandService.delete(1L);

            verify(brandRepository).deleteById(1L);
        }

        @Test
        void delete_brandNotFound_throwsNotFoundException() {
            when(brandRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> brandService.delete(99L));
        }
    }
}

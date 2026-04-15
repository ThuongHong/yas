package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.category.CategoryPostVm;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryPostVm buildVm(String name, Long parentId) {
        return new CategoryPostVm(name, "slug-" + name.toLowerCase().replace(" ", "-"),
            "desc", parentId, "keywords", "meta", (short) 1, true, null);
    }

    @Nested
    class CreateCategoryTest {

        @Test
        void create_validVm_savesAndReturnsCategory() {
            CategoryPostVm vm = buildVm("Electronics", null);
            Category saved = new Category();
            saved.setId(1L);
            saved.setName("Electronics");

            when(categoryRepository.findExistedName("Electronics", null)).thenReturn(null);
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            Category result = categoryService.create(vm);

            assertNotNull(result);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void create_duplicateName_throwsDuplicatedException() {
            CategoryPostVm vm = buildVm("Electronics", null);
            Category existing = new Category();
            existing.setId(99L);

            when(categoryRepository.findExistedName("Electronics", null)).thenReturn(existing);

            assertThrows(DuplicatedException.class, () -> categoryService.create(vm));
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateCategoryTest {

        @Test
        void update_notFound_throwsNotFoundException() {
            CategoryPostVm vm = buildVm("Updated", null);

            when(categoryRepository.findExistedName("Updated", 99L)).thenReturn(null);
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> categoryService.update(vm, 99L));
        }

        @Test
        void update_validVm_updatesCategory() {
            CategoryPostVm vm = buildVm("Updated", null);
            Category existing = new Category();
            existing.setId(1L);
            existing.setName("Old");

            when(categoryRepository.findExistedName("Updated", 1L)).thenReturn(null);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

            categoryService.update(vm, 1L);

            verify(categoryRepository).findById(1L);
        }
    }
}

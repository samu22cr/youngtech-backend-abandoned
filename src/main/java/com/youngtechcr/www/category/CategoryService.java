package com.youngtechcr.www.category;

import com.youngtechcr.www.product.Product;
import com.youngtechcr.www.category.subcategory.Subcategory;
import com.youngtechcr.www.exceptions.custom.AlreadyExistsException;
import com.youngtechcr.www.exceptions.custom.NoDataFoundException;
import com.youngtechcr.www.exceptions.custom.ValueMismatchException;
import com.youngtechcr.www.category.subcategory.SubcategoryService;
import com.youngtechcr.www.exceptions.HttpErrorMessages;
import com.youngtechcr.www.domain.TimestampedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryService subcategoryService;

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryRepository categoryRepository, SubcategoryService subcategoryService) {
        this.categoryRepository = categoryRepository;
        this.subcategoryService = subcategoryService;
    }

    @Transactional(readOnly = true)
    public Category findCategoryById(Integer categoryId) {
        return this
                .categoryRepository
                    .findById(categoryId)
                        .orElseThrow( () ->  new NoDataFoundException(HttpErrorMessages.NO_ELEMENT_WITH_THE_REQUESTED_ID_WAS_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Category>  findAllCategories() {
       List<Category> allCategories = this.categoryRepository.findAll();
        if(!allCategories.isEmpty())  return allCategories;
	throw new NoDataFoundException(HttpErrorMessages.NO_ELEMENTS_FOUND_IN_SERVER);
	
    }
    @Transactional
    public Category createCategory(Category categoryToBeCreated) {
        if (!this.categoryRepository.existsByName(categoryToBeCreated.getName())) {
            TimestampedUtils.setTimestampsToNow(categoryToBeCreated);
            Category createdCategory = this.categoryRepository.save(categoryToBeCreated);
            log.info("Created new category: " + createdCategory);
            return createdCategory;
        }
        throw new AlreadyExistsException(HttpErrorMessages.CANT_CREATE_DUPLICATE_NAME);
    }

    @Transactional
    public Category updateCategoryById(Integer categoryId, Category categoryToBeUpdated) {
        if (categoryId.equals(categoryToBeUpdated.getId())) {
                LocalDateTime storedCreatedAtTimestamp = this.findCategoryById(categoryId).getCreatedAt();
            TimestampedUtils.updateTimeStamps(categoryToBeUpdated, storedCreatedAtTimestamp);
            Category updatedCategory = this.categoryRepository.save(categoryToBeUpdated);
            log.info("Updated category: " + updatedCategory);
            return updatedCategory;
        }
        throw new ValueMismatchException(HttpErrorMessages.PROVIDED_IDS_DONT_MATCH);
    }

    @Transactional
    public void deleteCategoryById(Integer categoryId) {
        if(this.categoryRepository.existsById(categoryId)) {
            this.categoryRepository.deleteById(categoryId);
            log.info("Deleted category with id: " + categoryId);
            return;
        }
        throw new NoDataFoundException(HttpErrorMessages.NO_ELEMENT_WITH_THE_REQUESTED_ID_WAS_FOUND);
    }

    @Transactional(readOnly = true)
    public List<Subcategory> findSubcategories(Integer categoryId) {
        Category requestedCategory = this
                .categoryRepository
                .findById(categoryId)
                .orElseThrow( () -> new NoDataFoundException(HttpErrorMessages.NO_ELEMENT_WITH_THE_REQUESTED_ID_WAS_FOUND));
        return requestedCategory.getSubcategories();
    }


    @Transactional
    public Subcategory createSubcategoryByCategoryId(Integer categoryId, Subcategory subcategoryToBeCreated) {
        Category categoryToBeAddedNewSubcategory = this.findCategoryById(categoryId);
        subcategoryToBeCreated.setCategory(categoryToBeAddedNewSubcategory);
        Subcategory createdSubcategory = this.subcategoryService.create(subcategoryToBeCreated);
        log.info("Created subcategory: " + createdSubcategory + " in category: " + categoryToBeAddedNewSubcategory);
        return createdSubcategory;
    }

    @Transactional(readOnly = true)
    public Subcategory findSubcategoryByCategoryId(Integer categoryId, Integer subcategoryId) {
        Category categoryToBeConsulted = this.findCategoryById(categoryId);
        Subcategory requetedSubcategory = this.subcategoryService.find(subcategoryId);
        if(requetedSubcategory.getCategory() != null && requetedSubcategory.getCategory().equals(categoryToBeConsulted)) {
            return requetedSubcategory;
        }
        throw new ValueMismatchException(HttpErrorMessages.REQUESTED_CHILD_ELEMENT_DOESNT_EXIST);
    }

    @Transactional
    public Subcategory updateSubcategoryByCategoryId(Integer categoryId, Integer subcategoryId, Subcategory subcategoryToBeUpdated) {
        Category categoryWhosSubcategoryWillBeModified = this.findCategoryById(categoryId);
        if(subcategoryToBeUpdated.getCategory().getId().equals(categoryWhosSubcategoryWillBeModified.getId())) {
            var updatedSubcategory = subcategoryService.update(subcategoryId, subcategoryToBeUpdated);
            log.info("Updated subcategory: " + updatedSubcategory + "in category: " + categoryWhosSubcategoryWillBeModified);
            return updatedSubcategory;
        }
        throw new ValueMismatchException(HttpErrorMessages.REQUESTED_CHILD_ELEMENT_DOESNT_EXIST);
    }

    @Transactional
    public void deleteSubcategoryByCategoryId(Integer categoryId, Integer subcategoryId) {
        Category categoryWhosSubcategotyWillBeDeleted = this.findCategoryById(categoryId);
        Subcategory subcategoryToBeDeleted = this.subcategoryService.find(subcategoryId);
        if(subcategoryToBeDeleted.getCategory().getId().equals(categoryWhosSubcategotyWillBeDeleted.getId())) {
            this.subcategoryService.delete(subcategoryId);
            log.info("Deleted subcategory: " + subcategoryToBeDeleted + " from category " + categoryWhosSubcategotyWillBeDeleted);
            return;
        }
        throw new ValueMismatchException(HttpErrorMessages.REQUESTED_CHILD_ELEMENT_DOESNT_EXIST);
    }

   @Transactional(readOnly = true)
   public List<Product> findProductsByCategory(Integer categoryId) {
   	var categoryWhoseProductsWillBeObtained = this.findCategoryById(categoryId);
   	List<Product> productsFound = categoryWhoseProductsWillBeObtained.getProducts();
	if(!productsFound.isEmpty()) return productsFound;
	throw new NoDataFoundException(HttpErrorMessages.NO_ELEMENTS_FOUND_IN_SERVER);
   }

   @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return categoryRepository.existsById(id);
   }
}

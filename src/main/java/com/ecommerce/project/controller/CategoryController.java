package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategory(@RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                           @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                           @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_CATEGORY_BY)String sortBy,
                                                           @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        CategoryResponse catList = categoryService.getAllCategory(pageNumber,pageSize,sortBy,sortOrder);
        return ResponseEntity.status(HttpStatus.OK).body(catList);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){

         CategoryDTO savedCat = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCat,HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){

            CategoryDTO deleteCategory = categoryService.deleteCategory(categoryId);
            return ResponseEntity.status(HttpStatus.OK).body(deleteCategory);

    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO,@PathVariable Long categoryId){

           CategoryDTO updatedCat =  categoryService.updateCategory(categoryDTO,categoryId);
            return ResponseEntity.status(HttpStatus.OK).body(updatedCat);

    }

}

package com.ecommerce.project.service;

import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.dto.CategoryResponse;
import com.ecommerce.project.exception.ApiException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategory(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> catList = categoryRepository.findAll(pageable);
        if(catList.isEmpty()){
            throw  new ApiException("Category is yet to be created!!");
        }
        List<CategoryDTO> catDto = catList.stream().map(category -> modelMapper.map(category,CategoryDTO.class)).toList();
        CategoryResponse res = new CategoryResponse();
        res.setContent(catDto);
        res.setPageNumber(catList.getNumber());
        res.setPageSize(catList.getSize());
        res.setTotalPages(catList.getTotalPages());
        res.setTotalElements(catList.getTotalElements());
        res.setLastPage(catList.isLast());
        return res;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDto) {
        Category cat = modelMapper.map(categoryDto,Category.class);
        Category existingCategory = categoryRepository.findByCategoryName(cat.getCategoryName());
        if(existingCategory != null){
            throw new ApiException("Category with categoryName : "+ cat.getCategoryName() + " already exits!!");
        }
        Category savedCat = categoryRepository.save(cat);
        return modelMapper.map(savedCat,CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        categoryRepository.delete(cat);
        return modelMapper.map(cat,CategoryDTO.class);

    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));
        Category cat = modelMapper.map(categoryDTO,Category.class);
        existingCategory.setCategoryName(cat.getCategoryName());
        Category updatedCat = categoryRepository.save(existingCategory);
        return modelMapper.map(updatedCat,CategoryDTO.class);
    }
}

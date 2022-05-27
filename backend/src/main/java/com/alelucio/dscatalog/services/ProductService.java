package com.alelucio.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alelucio.dscatalog.dto.CategoryDTO;
import com.alelucio.dscatalog.dto.ProductDTO;
import com.alelucio.dscatalog.entities.Category;
import com.alelucio.dscatalog.entities.Product;
import com.alelucio.dscatalog.repositories.CategoryRepository;
import com.alelucio.dscatalog.repositories.ProductRepository;
import com.alelucio.dscatalog.services.exceptions.DataBaseException;
import com.alelucio.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest){
		Page<Product> list = repository.findAll(pageRequest);		
		return list.map(x -> new ProductDTO(x));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id){
		Optional<Product> obj = repository.findById(id);//Optional é para evitar trabalhar com valor nulo
		Product entity = obj.orElseThrow(() -> new EntityNotFoundException("Id não encontrado"));
		return new ProductDTO(entity, entity.getCategories());
	}
	
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}
	
	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id não encontrado");
		}
	}
	
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		}catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id não encontrado");
		}catch(DataIntegrityViolationException e) {
			throw new DataBaseException("Violação de integridade");
		}
	}

	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for(CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getById(catDto.getId());
			entity.getCategories().add(category);
		}
	}
}

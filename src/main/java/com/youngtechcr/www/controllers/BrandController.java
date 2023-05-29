package com.youngtechcr.www.controllers;


import com.youngtechcr.www.domain.Brand;
import com.youngtechcr.www.services.domain.BrandService;
import com.youngtechcr.www.utils.ResponseEntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/brands")
public class BrandController implements BasicCrudController<Brand>{

    @Autowired
    private BrandService brandService;

    @Override
    @GetMapping(path = "/{id}")
    public ResponseEntity<Brand> findById(@PathVariable("id") Integer brandId) {
        Brand fetchedBrand = this.brandService.findById(brandId);
        return ResponseEntity.ok().body(fetchedBrand);
    }
    @Override
    @PostMapping
    public ResponseEntity<Brand> create(@RequestBody Brand brandToBeCreated){
        Brand createdBrand = this.brandService.create(brandToBeCreated);
        return ResponseEntityUtils.created(createdBrand);
    }

    @Override
    @PutMapping(path = "/{id}")
    public ResponseEntity<Brand> updateById(
            @Validated @PathVariable("id") Integer brandId,
            @RequestBody Brand brandToBeUpdated
    ) {
        Brand updatedBrand = this.brandService.updateById(brandId, brandToBeUpdated);
        return ResponseEntity.ok(updatedBrand);
    }

    @Override
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Brand> deleteById(@Validated @PathVariable("id") Integer brandId){
        this.brandService.deleteById(brandId);
        return ResponseEntity.noContent().build();
    }



}

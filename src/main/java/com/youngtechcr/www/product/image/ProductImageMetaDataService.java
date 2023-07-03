package com.youngtechcr.www.product.image;

import com.youngtechcr.www.exceptions.custom.NoDataFoundException;
import com.youngtechcr.www.exceptions.HttpErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageMetaDataService {

    @Autowired
    private ProductImageMetaDataRepository imageMetaDataRepository;

    @Transactional(readOnly = true)
    public ProductImageMetaData findImageMetaDataById(Integer imageMetaDataId) {
        return this
                .imageMetaDataRepository
                .findById(imageMetaDataId)
                .orElseThrow( () -> new NoDataFoundException(HttpErrorMessages.NO_ELEMENT_WITH_THE_REQUESTED_ID_WAS_FOUND));
    }

}
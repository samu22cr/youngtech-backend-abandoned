package com.youngtechcr.www.services.storage;

import com.youngtechcr.www.domain.Product;
import com.youngtechcr.www.domain.storage.ProductImageMetaData;
import com.youngtechcr.www.exceptions.custom.AlreadyExistsException;
import com.youngtechcr.www.exceptions.custom.FileOperationException;
import com.youngtechcr.www.exceptions.custom.NoDataFoundException;
import com.youngtechcr.www.repositories.ProductImageMetaDataRepository;
import com.youngtechcr.www.services.ProductService;
import com.youngtechcr.www.utils.ErrorMessages;
import com.youngtechcr.www.utils.TimestampUtils;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.youngtechcr.www.utils.StorageUtils;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProductImageStorageService implements FileSystemStorageService<ProductImageMetaData> {

    private static final Logger log = LoggerFactory.getLogger(ProductImageStorageService.class);

    @Autowired
    private ProductImageMetaDataRepository productImageFileDataRepository;
    @Lazy
    @Autowired
    private ProductService productService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProductImageMetaData storeProductImage(
            Integer productId,
            MultipartFile imageToBeUploaded,
            @Nullable ProductImageMetaData imageMetaData
    ) {
        Product productToAddNewImage = this.productService.findProductById(productId);
        if(!this.productService.hasMainImage(productToAddNewImage)){
            Path posibleProductDirectory = Paths
                    .get(StorageUtils.PRODUCT_STORAGE_DIRECTORY)
                    .resolve(productId.toString())
                    .resolve(StorageUtils.IMAGE_DIRECTORY); // equals to ../youngtech-storage/products/{id}/images
            String serverFileName = StorageUtils.generateServerFileName(productId, imageToBeUploaded, FileType.IMAGE);
            var savedWithRelativePathAndServerNameAndOriginalName = this.saveToFileSystem(serverFileName, posibleProductDirectory, imageToBeUploaded);
            StorageUtils.setFileMetaData(
                    savedWithRelativePathAndServerNameAndOriginalName,
                    imageMetaData != null ? imageMetaData.isMain() : false, // isMainImage ????
                    imageToBeUploaded.getSize(),imageToBeUploaded.getContentType());
            savedWithRelativePathAndServerNameAndOriginalName.setProduct(productToAddNewImage);
            ProductImageMetaData savedProductImageRepresentation = this.saveToDataBase(savedWithRelativePathAndServerNameAndOriginalName);
            log.info("Stored (in file system and in database) product image in server successfully: " + savedProductImageRepresentation);
            return savedProductImageRepresentation;
        }
        throw new AlreadyExistsException(ErrorMessages.CANT_CREATE_DUPLICATE_MAIN_IMAGE);
    }

    public Resource obtainProductImage(ProductImageMetaData imageFileMetaData) {
        Path absoluteImagePath = Path.of(imageFileMetaData.getRelativePath()).toAbsolutePath().normalize();
        Resource retrievedImageFromFileSystem = this.retrieveFromFileSystem(absoluteImagePath);
        return retrievedImageFromFileSystem;
    }
    @Override
    public ProductImageMetaData saveToFileSystem(String serverFileName, Path posibleProductDirectoryPath, MultipartFile imageToBeUploaded) {
        StorageUtils.createDirectoriesIfNotAlreadyExists(posibleProductDirectoryPath);
        Path relativeImagePath = posibleProductDirectoryPath.resolve(serverFileName);
        StorageUtils.saveFileOrReplaceIfExisting(imageToBeUploaded, relativeImagePath);
        var savedProductImageInFileSystem = new ProductImageMetaData(
            serverFileName,
            imageToBeUploaded.getOriginalFilename(),
            relativeImagePath.toString()
        );
        return savedProductImageInFileSystem;
    }

    @Override
    public Resource retrieveFromFileSystem(Path absoluteImagePath) {
        if(Files.exists(absoluteImagePath) && !Files.isDirectory(absoluteImagePath)) {
            try {
                UrlResource imageResource = new UrlResource(absoluteImagePath.toUri());
                return imageResource;
            } catch (MalformedURLException malformedURLException) {
                log.warn("Couldn't download file due to malformed url/uri exception created from file path");
                malformedURLException.printStackTrace();
                throw new FileOperationException(ErrorMessages.UNABLE_TO_DOWNLOAD_REQUESTED_FILE);
            }
        }
        throw new NoDataFoundException(ErrorMessages.UNABLE_TO_LOCATE_REQUESTES_FILE);
    }

    @Override
    @Transactional
    public ProductImageMetaData saveToDataBase(ProductImageMetaData productImageToBeSaved) {
        TimestampUtils.setTimestampsToNow(productImageToBeSaved);
        var savedProductImageRepresentationInDataBase = this.productImageFileDataRepository.save(productImageToBeSaved);
        return savedProductImageRepresentationInDataBase;
    }


}

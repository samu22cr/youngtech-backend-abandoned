package com.youngtechcr.www.storage;

public enum FileType {

    IMAGE("img"),
    VIDEO("video"),
    PDF("pdf"),
    XLS_OR_XLSX("xlsOrXslx");
    // ^^^^^^ no "_" or "-" are used becasue "_" can cause confusion and
    // the "-" is a delimiter of server file name structire

    public final String identifier;

    FileType(String identifier) {
        this.identifier = identifier;
    }

}

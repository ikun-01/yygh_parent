package com.jing.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**文件上传到阿里云
     * @param file
     * @return
     */

    String upload(MultipartFile file);
}
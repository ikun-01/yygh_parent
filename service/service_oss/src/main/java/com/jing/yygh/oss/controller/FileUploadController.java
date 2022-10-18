package com.jing.yygh.oss.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/oss/file")
public class FileUploadController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file){
        String url = fileService.upload(file);
        return R.ok().message("文件上传成功").data("url",url);
    }
}

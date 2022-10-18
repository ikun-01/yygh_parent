package com.jing.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.jing.yygh.oss.service.FileService;
import com.jing.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String upload(MultipartFile file) {

        String bucketName = ConstantOssPropertiesUtils.BUCKET_NAME;
        String endPoint = ConstantOssPropertiesUtils.END_POINT;
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.ACCESS_KEY_SECRET;

        try {

            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            // 获取文件名称
            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            fileName = uuid + fileName;
            // 根据当前日期生成对应的文件夹名称
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            fileName = timeUrl + "/" + fileName;
            // 创建PutObject请求。
            ossClient.putObject(bucketName, fileName, inputStream);
            ossClient.shutdown();
            // 上传之后返回文件路径
            return "https://" + bucketName + "." + endPoint + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}

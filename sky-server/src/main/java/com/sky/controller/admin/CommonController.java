package com.sky.controller.admin;

import com.aliyuncs.exceptions.ClientException;
import com.sky.annotation.AutoFill;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file 文件
     * @return 文件上传后的访问地址
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) throws IOException, ClientException {
        log.info("文件上传开始: {}", file.getOriginalFilename());
        return Result.success(aliOssUtil.upload(file.getBytes(), file.getOriginalFilename()));
    }
}



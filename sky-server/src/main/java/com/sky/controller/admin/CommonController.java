package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Api(tags = "文件上传")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/admin/common/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传");
        String fileOriginalFilename = file.getOriginalFilename();
        String png = fileOriginalFilename.substring(fileOriginalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + png;
        try {
            String filePath = aliOssUtil.upload(file.getBytes(), fileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.info("上传失败");
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}

package com.reggie.controller;

import com.baomidou.mybatisplus.annotation.Version;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

/**
 * @create: 2022/11/17 9:27
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value(value = "${reggie.filePath}")
    private String filePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String > upload(MultipartFile file){//file是临时文件，需要转存保存

        //确保根路径可用
        File dir = new File(filePath);
        if (!dir.exists()){
            dir.mkdirs();
        }

        //使用uuid重新生成文件名，防止重名导致文件覆盖
        UUID fileName = UUID.randomUUID();

        //获取文件类型
        String originalFilename = file.getOriginalFilename();
        String fileType = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));

        try {
            file.transferTo(new File(filePath + fileName + fileType));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName + fileType);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，获取图片
            FileInputStream fileInputStream = new FileInputStream(filePath + name);

            //输出流，将图片写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            int length;
            byte[] bytes = new byte[1024];

            while ((length = (fileInputStream.read(bytes))) != -1){
                outputStream.write(bytes);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.tjfintech.common.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.minio.*;
import io.minio.errors.MinioException;
import jdk.internal.org.xml.sax.InputSource;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.tjfintech.common.utils.UtilsClassGD.*;


@Slf4j
public class MinIOOperation {
    public void uploadFileToMinIO(String endPoint,String bucket,String uploadFile) throws Exception{
        // 参数为：图床，账号，密码
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endPoint)
                .credentials(minIOUser, minIOPwd)
                .build();

        // 检查文件夹是否已经存在
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if(isExist) {
            System.out.println("文件夹已经存在了");
        }
        else {
            // 创建一个名为managertest的文件夹
            System.out.println("文件夹还没存在");
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        // 使用putObject上传一个文件到文件夹中。
        //参数为：文件夹，要存成的名字，要存的文件
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(uploadFile.substring(uploadFile.lastIndexOf("\\")))
                        .filename(uploadFile)
                        .build());
        System.out.println(uploadFile + " is successfully uploaded as object '" +
                uploadFile.substring(uploadFile.lastIndexOf("\\")) + "' to bucket '" + bucket + "'.");
        System.out.println("成功了");

    }

    public String getFileFromMinIO(String endPoint,String bucket,String targetFile,String saveFile) throws Exception{
        InputStream isGet = null;
        String strGet = "";
        try {
            // 参数为：图床，账号，密码
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endPoint)
                    .credentials(minIOUser, minIOPwd)
                    .build();

            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if(!isExist) System.out.println(bucket + " 不存在");
            //使用getObject获取一个文件
            // 调用statObject()来判断对象是否存在。
            String temp = minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucket).object(targetFile).build()).toString();
            System.out.println("存在状态" + temp);
            // 获取1.png的流并保存到photo.png文件中。
            //参数为：文件夹，要获得的文件，要写入的文件
            isGet = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(targetFile).build());

            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader br = new BufferedReader(new InputStreamReader(isGet));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            strGet = sb.toString();

//            //存在保存文件名时 则存储 否则只返回读取到的文件
//            if(!saveFile.isEmpty()) {
//                FileOutputStream fos = new FileOutputStream(saveFile);
//                byte[] b = new byte[1024];
//                while ((isGet.read(b)) != -1) {
//                    fos.write(b);// 写入数据
//                }
//                isGet.close();
//                fos.close();// 保存数据
//            }

        }catch(MinioException e) {
            System.out.println("错误: " + e);
        }
        return strGet;
    }
}

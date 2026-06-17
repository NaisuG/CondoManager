package com.condominio.documentos.Config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(minioUrl, "us-east-1"))
                .withPathStyleAccessEnabled(true) 
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(bucketName);
        }

        return s3Client;
    }
}
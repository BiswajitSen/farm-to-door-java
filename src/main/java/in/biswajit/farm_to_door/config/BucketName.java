package in.biswajit.farm_to_door.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class BucketName {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Bean(name = "awsBucketName")
    public String bucketName() {
        return bucketName;
    }
}
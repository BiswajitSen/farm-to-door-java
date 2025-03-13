package in.biswajit.farm_to_door.service;

import in.biswajit.farm_to_door.entity.FoodEntity;
import in.biswajit.farm_to_door.request.FoodRequest;
import in.biswajit.farm_to_door.response.FoodResponse;
import in.biswajit.farm_to_door.respository.FoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final S3Client s3Client;
    private final FoodRepository foodRepository;
    private final String awsBucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID().toString()+"."+ext;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            if(response.sdkHttpResponse().isSuccessful()) {
                return "https://"+awsBucketName+".s3.amazonaws.com/"+key;
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while uploading file");
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while uploading file");
        }
    }

    @Override
    public FoodResponse addFood(FoodRequest foodRequest, MultipartFile file) {
        FoodEntity foodEntity = mapToEntity(foodRequest, uploadFile(file));
        FoodEntity foodEntityWithId = foodRepository.save(foodEntity);
        return mapToResponse(foodEntityWithId);
    }

    @Override
    public List<FoodResponse> getFoods() {
        return foodRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FoodResponse getFoodById(String id) {
        return foodRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));
    }

    @Override
    public void deleteFood(String id) {
        FoodResponse foodResponse = getFoodById(id);
        System.out.println("Deleting food with id: " + id);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(awsBucketName)
                .key(foodResponse.getImageUrl().substring(foodResponse.getImageUrl().lastIndexOf("/") + 1))
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        foodRepository.deleteById(id);
    }

    private FoodEntity mapToEntity(FoodRequest foodRequest, String imageUrl) {
        return FoodEntity.builder()
                .name(foodRequest.getName())
                .description(foodRequest.getDescription())
                .price(foodRequest.getPrice())
                .category(foodRequest.getCategory())
                .imageUrl(imageUrl)
                .build();
    }

    private FoodResponse mapToResponse(FoodEntity foodEntity) {
        return FoodResponse.builder()
                .id(foodEntity.getId())
                .name(foodEntity.getName())
                .description(foodEntity.getDescription())
                .price(foodEntity.getPrice())
                .category(foodEntity.getCategory())
                .imageUrl(foodEntity.getImageUrl())
                .build();
    }
}

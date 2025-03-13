package in.biswajit.farm_to_door.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import in.biswajit.farm_to_door.entity.FoodEntity;
import in.biswajit.farm_to_door.request.FoodRequest;
import in.biswajit.farm_to_door.response.FoodResponse;
import in.biswajit.farm_to_door.respository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class FoodServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private FoodServiceImpl foodService;

    private final String awsBucketName = "your-bucket-name";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        foodService = new FoodServiceImpl(s3Client, foodRepository, awsBucketName);
    }

    @Test
    void testAddFood() throws IOException {
        FoodRequest foodRequest = new FoodRequest("Apple", "Fresh red apple", 1.99, "Fruits");
        FoodEntity foodEntity = new FoodEntity("1", "Apple", "Fresh red apple", 1.99, "Fruits", "https://bucket-name.s3.amazonaws.com/image.jpg");

        SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);

        PutObjectResponse putObjectResponse = mock(PutObjectResponse.class);
        when(putObjectResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putObjectResponse);
        when(file.getOriginalFilename()).thenReturn("image.jpg");
        when(file.getBytes()).thenReturn(new byte[0]);
        when(foodRepository.save(any(FoodEntity.class))).thenReturn(foodEntity);

        FoodResponse foodResponse = foodService.addFood(foodRequest, file);

        assertEquals("1", foodResponse.getId());
        assertEquals("Apple", foodResponse.getName());
        assertEquals("Fresh red apple", foodResponse.getDescription());
        assertEquals(1.99, foodResponse.getPrice());
        assertEquals("Fruits", foodResponse.getCategory());
        assertEquals("https://bucket-name.s3.amazonaws.com/image.jpg", foodResponse.getImageUrl());

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(foodRepository).save(any(FoodEntity.class));
    }
    
    @Test
    void testUploadFile() throws IOException {
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getBytes()).thenReturn(new byte[0]);

        SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);

        PutObjectResponse putObjectResponse = mock(PutObjectResponse.class);
        when(putObjectResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(putObjectResponse);

        foodService.uploadFile(file);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testGetFoods() {
        FoodEntity foodEntity1 = new FoodEntity("1", "Apple", "Fresh red apple", 1.99, "Fruits", "https://bucket-name.s3.amazonaws.com/image1.jpg");
        FoodEntity foodEntity2 = new FoodEntity("2", "Banana", "Fresh yellow banana", 0.99, "Fruits", "https://bucket-name.s3.amazonaws.com/image2.jpg");

        when(foodRepository.findAll()).thenReturn(Arrays.asList(foodEntity1, foodEntity2));

        List<FoodResponse> foods = foodService.getFoods();

        assertEquals(2, foods.size());
        assertEquals("Apple", foods.get(0).getName());
        assertEquals("Banana", foods.get(1).getName());
    }

    @Test
    void testGetFoodById() {
        FoodEntity foodEntity = new FoodEntity("1", "Apple", "Fresh red apple", 1.99, "Fruits", "https://bucket-name.s3.amazonaws.com/image.jpg");

        when(foodRepository.findById("1")).thenReturn(Optional.of(foodEntity));

        FoodResponse foodResponse = foodService.getFoodById("1");

        assertEquals("1", foodResponse.getId());
        assertEquals("Apple", foodResponse.getName());
        assertEquals("Fresh red apple", foodResponse.getDescription());
        assertEquals(1.99, foodResponse.getPrice());
        assertEquals("Fruits", foodResponse.getCategory());
        assertEquals("https://bucket-name.s3.amazonaws.com/image.jpg", foodResponse.getImageUrl());
    }

    @Test
    void testGetFoodByIdNotFound() {
        when(foodRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> foodService.getFoodById("1"));
    }

    @Test
    void testDeleteFood() {
        FoodEntity foodEntity = new FoodEntity();
        foodEntity.setId("1");
        foodEntity.setName("Apple");
        foodEntity.setDescription("Fresh red apple");
        foodEntity.setPrice(1.99);
        foodEntity.setCategory("Fruits");
        foodEntity.setImageUrl("https://bucket-name.s3.amazonaws.com/image.jpg");

        when(foodRepository.findById("1")).thenReturn(Optional.of(foodEntity));

        foodService.deleteFood("1");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(foodRepository).deleteById("1");
    }
}
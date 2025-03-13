package in.biswajit.farm_to_door.service;

import in.biswajit.farm_to_door.request.FoodRequest;
import in.biswajit.farm_to_door.response.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {
    String uploadFile(MultipartFile file);
    FoodResponse addFood(FoodRequest foodRequest, MultipartFile file);
    List<FoodResponse> getFoods();
    FoodResponse getFoodById(String id);
    void deleteFood(String id);
}

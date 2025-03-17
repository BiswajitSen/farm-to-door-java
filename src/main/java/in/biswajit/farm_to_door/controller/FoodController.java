package in.biswajit.farm_to_door.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.biswajit.farm_to_door.request.FoodRequest;
import in.biswajit.farm_to_door.response.FoodResponse;
import in.biswajit.farm_to_door.service.FoodService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@AllArgsConstructor
@CrossOrigin("*")
public class FoodController {
    private final FoodService foodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse addFood(@RequestPart("food") String food,
                                @RequestPart("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        FoodRequest foodRequest = null;
        try {
            foodRequest = objectMapper.readValue(food, FoodRequest.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid food request");
        }

        return foodService.addFood(foodRequest, file);
    }

    @GetMapping
    public List<FoodResponse> getAllFoods() {
        return foodService.getFoods();
    }

    @GetMapping("/{id}")
    public FoodResponse getFoodById(@PathVariable String id) {
        return foodService.getFoodById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFoodById(@PathVariable String id) {
        foodService.deleteFood(id);
    }
}

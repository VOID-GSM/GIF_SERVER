package com.example.gifserverv2.domain.item.controller;

import com.example.gifserverv2.domain.item.dto.request.BuyItemRequest;
import com.example.gifserverv2.domain.item.dto.request.EquipItemsRequest;
import com.example.gifserverv2.domain.item.dto.response.ItemResponse;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.service.ItemService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "아이템", description = "코인으로 캐릭터 꾸미기 아이템을 사고 장착한다. 코인은 GitHub 커밋 동기화와 역량검사로 얻는다. 외부 사용자(`role=EXTERNAL`)도 호출할 수 있으며, 외부 사용자는 GitHub 연동을 할 수 없어 역량검사로만 코인을 얻는다.")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "전체 아이템 목록", description = "모든 아이템을 내 보유·장착 여부와 함께 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음")
    })
    @GetMapping
    public List<ItemResponse> getAllItems(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        validateUser(currentUser);
        return itemService.getAllItems(currentUser.userId());
    }

    @Operation(summary = "카테고리별 아이템 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음")
    })
    @GetMapping("/category/{category}")
    public List<ItemResponse> getItemsByCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "카테고리. EXPRESSION(표정) / HAIR(머리카락) / CLOTH(옷) / OBJECT(소품) / DECORATION(머리장식) / GLASSES(안경)", example = "HAIR") @PathVariable ItemCategory category
    ) {
        validateUser(currentUser);
        return itemService.getItemsByCategory(currentUser.userId(), category);
    }

    @Operation(summary = "아이템 구매", description = "코인을 차감하고 아이템을 보유 목록에 추가한다. 구매한 아이템 정보를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구매 성공"),
            @ApiResponse(responseCode = "400", description = "이미 보유, 코인 부족"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 아이템 없음")
    })
    @PostMapping("/buy")
    public ItemResponse buyItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody BuyItemRequest request
    ) {
        validateUser(currentUser);
        return itemService.buyItem(currentUser.userId(), request);
    }

    @Operation(summary = "아이템 장착", description = "장착할 아이템 목록 전체를 보낸다. 목록에 없는 보유 아이템은 장착 해제된다(빈 배열이면 모두 해제). 카테고리당 하나만 장착할 수 있다. 전체 아이템 목록을 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장착 성공"),
            @ApiResponse(responseCode = "400", description = "보유하지 않은 아이템, 같은 카테고리 중복"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/equip")
    public List<ItemResponse> equipItems(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody EquipItemsRequest request
    ) {
        validateUser(currentUser);
        return itemService.equipItems(currentUser.userId(), request);
    }

    private void validateUser(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
    }
}
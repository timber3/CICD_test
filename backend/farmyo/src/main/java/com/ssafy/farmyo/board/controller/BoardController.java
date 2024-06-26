package com.ssafy.farmyo.board.controller;

import com.ssafy.farmyo.board.dto.*;
import com.ssafy.farmyo.board.service.BoardService;
import com.ssafy.farmyo.common.auth.CustomUserDetails;
import com.ssafy.farmyo.common.exception.CustomException;
import com.ssafy.farmyo.common.exception.ExceptionType;
import com.ssafy.farmyo.common.response.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "4.Board", description = "BOARD API")
public class BoardController {

    private final BoardService boardService;

    //삼요게시물 작성
    @Operation(summary = "삼요게시물작성", description = "/boards/buy\n\n 삼요게시물 작성")
    @PostMapping("/buy")
    @ApiResponse(responseCode = "201", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> createBuyBoard(@RequestBody @Validated AddBuyBoardReqDto addBuyBoardReqDto, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 농부인지 구매자인지 확인
        if (userDetails.getJob() == 0) {
            throw new CustomException(ExceptionType.FARMER_CANNOT_POST_BUY_BOARD);
        }


        int boardId = boardService.addBuyerBoard(addBuyBoardReqDto, userDetails.getId());
        log.info("삼요 게시판 작성, boardId = {}", boardId);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0, boardId));
    }

    //    팜요게시물 작성

    @Operation(summary = "팜요게시물작성", description = "/boards/sell\n\n 팜요게시글 작성")
    @PostMapping(value = "/sell", consumes = "multipart/form-data")
    @ApiResponse(responseCode = "201", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> createFarmerBoard(@ModelAttribute @Valid AddFarmerBoardReqDto addFarmerBoardReqDto, @RequestPart(name = "images", required = false) List<MultipartFile> images, Authentication authentication) {
        // DTO 객체를 수동으로 생성

        System.out.println("images = " + images);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        //농부인지 구매자인지 확인 농부만 가능
        if (userDetails.getJob() == 1) {
            throw new CustomException(ExceptionType.USER_FARMER_REQUIRED);
        }

        int boardId = boardService.addFarmerBoard(addFarmerBoardReqDto, images, userDetails.getId());
        log.info("팜요 게시판 작성 boardId = {}", boardId);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseBody.of(0, boardId));
    }


    //게시물 상세조회
    @Operation(summary = "게시물상세조회", description = "/boards/{boardId}\n\n 게시물 상세내용 조회")
    @GetMapping("/{boardId}")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> getBoardDetail(@PathVariable int boardId) {
        BoardDetailResDto boardDetailResDto = boardService.getBoardDetail(boardId);
        log.info("게시물 상세조회 API 호출 - 게시물 ID: {}", boardId);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, boardDetailResDto));
    }

    //게시물 목록조회 params 0 은 팜요 1 은 삼요
    @Operation(summary = "게시물목록조회", description = "/boards \n\n 게시물 목록 조회")
    @GetMapping("")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> getBoardList(@RequestParam("type") int boardType, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        List<BoardListResDto> boardListResDtoList = boardService.findBoardListByType(boardType, page, size);
        if (boardType == 0) {
            log.info("팜요 게시물 목록조회 API 호출");
        } else {
            log.info("삼요 게시물 목록조회 API 호출");
        }
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, boardListResDtoList));
    }

    //게시물수정
    @Operation(summary = "게시물수정", description = "/boards/{boardId} \n\n 게시물 수정")
    @PatchMapping(value = "/{boardId}", consumes = "multipart/form-data")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> updateBoard(@PathVariable int boardId, @ModelAttribute @Validated PatchBoardReqDto patchBoardReqDto, @RequestPart(name = "images", required = false) List<MultipartFile> images, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int patchBoardId = boardService.patchBoard(boardId, patchBoardReqDto, images,userDetails.getId());
        log.info("{}번 게시물 업데이트", boardId);

        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, patchBoardId));

    }


    //유저로그인아이디로 게시물 목록 조회
    @Operation(summary = "로그인ID로 게시물 조회", description = "/boards/list/{loginId} \n\n 유저 게시물 목록 조회")
    @GetMapping("/list/{loginId}")
    @ApiResponse(responseCode = "200", description = "성공 \n\n Success 반환")
    public ResponseEntity<? extends BaseResponseBody> getBoardListbyLoginId(@PathVariable String loginId, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size,Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<BoardListFindByUserResDto> boardListByLoginId= boardService.findBoardListByLoginId(loginId,page, size, userDetails.getId());
        log.info("{}의 게시물 목록 조회",loginId);

        return ResponseEntity.status(HttpStatus.OK).body(BaseResponseBody.of(0, boardListByLoginId));
    }

}

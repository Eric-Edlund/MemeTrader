package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/user")
public class UserStockController {

    @Autowired
    public UserStockController(MemeStockService memeStockService, MemeStockRepository memeStockRepository) {
        this.memeStockService = memeStockService;
        this.memeStockRepository = memeStockRepository;
    }

    private final MemeStockService memeStockService;
    private final MemeStockRepository memeStockRepository;

    @GetMapping
    public ResponseEntity<UserMetadata> userMetadata(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(memeStockRepository.getUserMetadata(1));
    }

    @PostMapping(value = "/order", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Object> placeOrder(@RequestBody OrderRequestForm form) {
        // assert userId > 0 &&  stockId > 0 && numShares > 0 && pricePerShare > 0 && totalPrice > 0;
        // assert totalPrice == numShares * pricePerShare;

        try {
            memeStockService.placeOrder(form.userId(), form.stockId(), form.order(), form.numShares(), form.pricePerShare());
            System.out.println(form);
        } catch (PlaceOrderFailed e) {
            return ResponseEntity.internalServerError().body("Failed to add transaction to ledger. " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/holdings")
    public ResponseEntity<HoldingsResponse> getUserHoldings(@AuthenticationPrincipal UserDetails user) {
        List<Holding> holdings = memeStockRepository.getHoldings(1);
        return ResponseEntity.ok(new HoldingsResponse(holdings));
    }

    private record HoldingsResponse(List<Holding> holdings) {}

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getUserBalance(@AuthenticationPrincipal UserDetails user) {
        int balance = memeStockRepository.getAcctBalance(1);
        return ResponseEntity.ok(new BalanceResponse(balance));
    }
    private record BalanceResponse(int balance) {}

//    @GetMapping("accountHistory")
//    public ResponseEntity<AccountHistoryResponse> getAccountHistory(
//            @RequestParam(value = "startDate", required = false) String startDate,
//            @RequestParam(value = "endDate", required = false) String endDate,
//            @AuthenticationPrincipal UserDetails user
//    ) {
//        return ResponseEntity.ok(memeStockRepository.getAccountHistory(startDate, endDate, 1));
//    }

    private record AccountHistoryResponse(Map<OffsetDateTime, BalanceHoldingsPair> history) {};
    private record BalanceHoldingsPair(int balance, List<Holding> holdings) {};

    @PostMapping("/description")
    public ResponseEntity<UpdateBioResponse> setDescription(@RequestBody UpdateBioRequest request, @AuthenticationPrincipal UserDetails user) {
        memeStockRepository.setUserBio(1, request.newBio());
        return ResponseEntity.ok(new UpdateBioResponse(request.newBio()));
    }

    private record UpdateBioResponse(String newBio) {};
    private record UpdateBioRequest(String newBio) {};


//    @PostMapping("/articles/{articleId}/comments")
//    public ResponseEntity<Comment> postArticleComment(@PathVariable Long articleId, @RequestBody Comment comment) {
//        // TODO: Implement logic to save the comment to the database
//    }


}
